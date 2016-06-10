module RecordRevenueApp exposing (Model, Msg, init, update, view)

import Domain exposing (..)
import DomainRest exposing (..)
import Html.Attributes exposing (type', name, placeholder, value, class, classList, disabled, selected)
import Html.Events exposing (onSubmit, onInput, onClick)
import Html exposing (..)
import HtmlHelpers exposing (viewErrors)
import HttpBuilder as Http
import Json.Decode as Decode
import Json.Encode as Encode
import String
import Task exposing (..)
import Time


type alias Model =
    { revenueAccountName : AccountName
    , bankAccountAccountName : AccountName
    , receivedOn : String
    , payee : String
    , amount : String
    , saving : Bool
    , saved : Bool
    , errors : List String
    , accounts : List Account
    }


type Msg
    = ChangeRevenueName String
    | ChangeBankAccountAccountName String
    | ChangeReceivedOn String
    | ChangeAmount String
    | ChangePayee String
    | Submit
    | SaveOk (Http.Response Transaction)
    | SaveFailed (Http.Error (List String))
    | AccountsOk (Http.Response (List Account))
    | AccountsFailed (Http.Error (List String))


init : ( Model, Cmd Msg )
init =
    ( { revenueAccountName = ""
      , bankAccountAccountName = ""
      , receivedOn = ""
      , payee = ""
      , amount = ""
      , saving = False
      , saved = False
      , errors = []
      , accounts = []
      }
    , Task.perform AccountsFailed AccountsOk getAccounts
    )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ChangeRevenueName newValue ->
            ( { model | revenueAccountName = newValue }, Cmd.none )

        ChangeBankAccountAccountName newValue ->
            ( { model | bankAccountAccountName = newValue }, Cmd.none )

        ChangeReceivedOn newValue ->
            ( { model | receivedOn = newValue }, Cmd.none )

        ChangePayee newValue ->
            ( { model | payee = newValue }, Cmd.none )

        ChangeAmount newValue ->
            ( { model | amount = newValue }, Cmd.none )

        Submit ->
            ( { model | saving = True }, Task.perform SaveFailed SaveOk (submit model) )

        AccountsOk response ->
            ( { model | accounts = response.data }, Cmd.none )

        AccountsFailed error ->
            case error of
                Http.UnexpectedPayload str ->
                    ( { model | saving = False, errors = [ "Unexpected response from server: " ++ str ] }, Cmd.none )

                Http.NetworkError ->
                    ( { model | saving = False, errors = [ "Network error contacting server" ] }, Cmd.none )

                Http.Timeout ->
                    ( { model | saving = False, errors = [ "Timeout fetching accounts from server" ] }, Cmd.none )

                Http.BadResponse errors ->
                    ( { model | saving = False, errors = errors.data }, Cmd.none )

        SaveOk response ->
            ( { model | saving = False, saved = True }, Cmd.none )

        SaveFailed error ->
            case error of
                Http.UnexpectedPayload str ->
                    ( { model | saving = False, errors = [ "Unexpected response from server: " ++ str ] }, Cmd.none )

                Http.NetworkError ->
                    ( { model | saving = False, errors = [ "Network error while saving this form" ] }, Cmd.none )

                Http.Timeout ->
                    ( { model | saving = False, errors = [ "Timeout saving... please try again" ] }, Cmd.none )

                Http.BadResponse errors ->
                    ( { model | saving = False, errors = errors.data }, Cmd.none )


accountOption : AccountName -> AccountName -> Html Msg
accountOption selectedAccount name =
    option [ selected (selectedAccount == name) ] [ text name ]


emptyOption : Html Msg
emptyOption =
    option [] [ text "Please choose an entry" ]


accountOptions : AccountKind -> AccountName -> List Account -> List (Html Msg)
accountOptions kind selectedName accounts =
    let
        candidateAccounts =
            List.filter (\x -> (x.virtual == False) && (x.kind == kind)) accounts

        names =
            List.map (\x -> x.name) candidateAccounts

        options =
            List.map (accountOption selectedName) names
    in
        [ emptyOption ] ++ options


revenueAccountOptions : AccountName -> List Account -> List (Html Msg)
revenueAccountOptions =
    accountOptions "asset"


bankAccountOptions : AccountName -> List Account -> List (Html Msg)
bankAccountOptions =
    accountOptions "revenue"


submitButtonOrSavedLabel : Model -> Html Msg
submitButtonOrSavedLabel model =
    case model.saved of
        True ->
            div [ class "callout success" ] [ p [] [ text "Saved" ] ]

        False ->
            button [ type' "submit", class "button primary", disabled model.saving ] [ i [ class "icon" ] [], text "Record" ]


view : Model -> Html Msg
view model =
    div []
        [ h1 [] [ text "Record Revenue" ]
        , form [ classList [ ( "saving", model.saving ) ], onSubmit Submit ]
            [ viewErrors model.errors
            , label [] [ text "Payee", input [ type' "text", name "revenue[payee]", placeholder "ACME Corp.", value model.payee, onInput ChangePayee ] [] ]
            , label [] [ text "Received on", input [ type' "text", name "revenue[received_on]", placeholder "2016-06-09", value model.receivedOn, onInput ChangeReceivedOn ] [] ]
            , label [] [ text "Amount", input [ type' "text", name "revenue[amount]", placeholder "1031.78", value model.amount, onInput ChangeAmount ] [] ]
            , label [] [ text "Bank Account Account Name", select [ name "revenue[bank_account_account_name]", onInput ChangeBankAccountAccountName ] (revenueAccountOptions model.bankAccountAccountName model.accounts) ]
            , label [] [ text "Revenue Account Name", select [ name "revenue[revenue_account_name]", onInput ChangeRevenueName ] (bankAccountOptions model.revenueAccountName model.accounts) ]
            , submitButtonOrSavedLabel model
            ]
        ]


modelToValue : Model -> Encode.Value
modelToValue model =
    Encode.object
        [ ( "revenue_account_name", Encode.string model.revenueAccountName )
        , ( "bank_account_account_name", Encode.string model.bankAccountAccountName )
        , ( "received_on", Encode.string model.receivedOn )
        , ( "payee", Encode.string model.payee )
        , ( "amount", Encode.string model.amount )
        ]


submit : Model -> Task (Http.Error (List String)) (Http.Response Transaction)
submit model =
    let
        body =
            modelToValue model

        url =
            "/api/revenues/create"

        decoder =
            Decode.string
    in
        Http.post url
            |> Http.withHeader "Content-Type" "application/json"
            |> Http.withHeader "Accept" "application/json"
            |> Http.withCredentials
            |> Http.withJsonBody body
            |> Http.withTimeout (5 * Time.second)
            |> Http.send (Http.jsonReader (Decode.at [ "data" ] transactionDecoder)) (Http.jsonReader errorsDecoder)
