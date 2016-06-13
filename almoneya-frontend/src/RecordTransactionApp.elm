module RecordTransactionApp exposing (Model, Msg, init, update, view)

import Domain exposing (Account, AccountName, Amount, Transaction)
import DomainRest exposing (getAccounts, transactionDecoder, errorsDecoder)
import Html.Attributes exposing (type', placeholder, name, value, selected, class)
import Html.Events exposing (onInput, onSubmit, onClick)
import Html exposing (..)
import HtmlHelpers exposing (viewErrors)
import HttpBuilder as Http
import Json.Decode as Decode
import Json.Encode as Encode
import List
import Maybe
import String
import Task exposing (..)
import Time


type alias TransactionForm =
    { payee : String
    , description : String
    , postedOn : String
    }


type alias TransactionEntryForm =
    { account : AccountName
    , amount : Amount
    }


type alias Model =
    { transaction : TransactionForm
    , entries : List TransactionEntryForm
    , accounts : List Account
    , errors : List String
    , saving : Bool
    }


type Msg
    = AccountsOk (Http.Response (List Account))
    | AccountsFailed (Http.Error (List String))
    | ChangePayee String
    | ChangeDescription String
    | ChangePostedOn String
    | Submit
    | SaveOk (Http.Response Transaction)
    | SaveFailed (Http.Error (List String))
    | ChangeEntryAccount Int String
    | ChangeEntryAmount Int String
    | AddEntryRows Int


initEntry : TransactionEntryForm
initEntry =
    { account = "", amount = "" }


init : ( Model, Cmd Msg )
init =
    ( { saving = False, transaction = { payee = "", description = "", postedOn = "" }, entries = List.repeat 5 initEntry, accounts = [], errors = [] }
    , Task.perform AccountsFailed AccountsOk getAccounts
    )


updateEntry : Int -> (TransactionEntryForm -> TransactionEntryForm) -> ( Int, TransactionEntryForm ) -> TransactionEntryForm
updateEntry targetIndex callback ( index, form ) =
    if index == targetIndex then
        callback form
    else
        form


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ChangePayee payee ->
            let
                transaction =
                    model.transaction

                newTransaction =
                    { transaction | payee = payee }
            in
                ( { model | transaction = newTransaction }, Cmd.none )

        ChangeDescription description ->
            let
                transaction =
                    model.transaction

                newTransaction =
                    { transaction | description = description }
            in
                ( { model | transaction = newTransaction }, Cmd.none )

        ChangePostedOn postedOn ->
            let
                transaction =
                    model.transaction

                newTransaction =
                    { transaction | postedOn = postedOn }
            in
                ( { model | transaction = newTransaction }, Cmd.none )

        ChangeEntryAccount index account ->
            let
                candidates =
                    List.indexedMap (,) model.entries

                newEntries =
                    List.map (updateEntry index (\x -> { x | account = account })) candidates
            in
                ( { model | entries = newEntries }, Cmd.none )

        ChangeEntryAmount index amount ->
            let
                candidates =
                    List.indexedMap (,) model.entries

                newEntries =
                    List.map (updateEntry index (\x -> { x | amount = amount })) candidates
            in
                ( { model | entries = newEntries }, Cmd.none )

        AddEntryRows n ->
            let
                newEntries =
                    List.repeat n initEntry
            in
                ( { model | entries = model.entries ++ newEntries }, Cmd.none )

        AccountsOk response ->
            ( { model | accounts = response.data }, Cmd.none )

        AccountsFailed error ->
            case error of
                Http.UnexpectedPayload str ->
                    ( { model | errors = [ "Unexpected response from server: " ++ str ] }, Cmd.none )

                Http.NetworkError ->
                    ( { model | errors = [ "Network error contacting server" ] }, Cmd.none )

                Http.Timeout ->
                    ( { model | errors = [ "Timeout fetching accounts from server" ] }, Cmd.none )

                Http.BadResponse errors ->
                    ( { model | errors = errors.data }, Cmd.none )

        Submit ->
            ( { model | saving = True }, Task.perform SaveFailed SaveOk (submit model) )

        SaveOk response ->
            ( { model | saving = False, transaction = { payee = "", description = "", postedOn = "" }, entries = List.repeat 5 initEntry }, Cmd.none )

        SaveFailed error ->
            case error of
                Http.UnexpectedPayload str ->
                    ( { model | errors = [ "Unexpected response from server: " ++ str ] }, Cmd.none )

                Http.NetworkError ->
                    ( { model | errors = [ "Network error contacting server" ] }, Cmd.none )

                Http.Timeout ->
                    ( { model | errors = [ "Timeout saving transaction" ] }, Cmd.none )

                Http.BadResponse errors ->
                    ( { model | errors = errors.data }, Cmd.none )


viewEmptyOption : Html Msg
viewEmptyOption =
    option [] [ text "Please choose an entry" ]


viewAccountOption : AccountName -> AccountName -> Html Msg
viewAccountOption selectedAccount name =
    option [ selected <| selectedAccount == name ] [ text name ]


viewAccountsOptions : AccountName -> List Account -> List (Html Msg)
viewAccountsOptions selectedName accounts =
    let
        names =
            List.map (\x -> x.name) accounts

        options =
            List.map (viewAccountOption selectedName) names
    in
        [ viewEmptyOption ] ++ options


viewEntry : List Account -> Int -> TransactionEntryForm -> Html Msg
viewEntry accounts index form =
    tr []
        [ td [] [ select [ onInput (ChangeEntryAccount index), name "transaction[entries][][account]" ] (viewAccountsOptions form.account accounts) ]
        , td [] [ input [ onInput (ChangeEntryAmount index), name "transaction[entries][][amount]", type' "text", value form.amount ] [] ]
        ]


view : Model -> Html Msg
view model =
    div []
        [ h1 [] [ text "Record Transaction" ]
        , viewErrors model.errors
        , form [ onSubmit Submit ]
            [ label []
                [ text "Payee"
                , input [ onInput ChangePayee, type' "text", placeholder "ACME Corp Inc.", name "transaction[payee]", value model.transaction.payee ] []
                ]
            , label []
                [ text "Description"
                , input [ onInput ChangeDescription, type' "text", placeholder "New shoes for the little one", name "transaction[description]", value model.transaction.description ] []
                ]
            , label []
                [ text "Posted On"
                , input [ onInput ChangePostedOn, type' "date", name "transaction[posted_on]", placeholder "2016-06-09", value model.transaction.postedOn ] []
                ]
            , h3 [] [ text "Entries" ]
            , table []
                [ thead []
                    [ tr []
                        [ th [] [ text "Account" ]
                        , th [] [ text "Amount" ]
                        ]
                    ]
                , tbody [] (List.indexedMap (viewEntry model.accounts) model.entries)
                , tfoot [] [ a [ onClick (AddEntryRows 5) ] [ text "Add 5 entries" ] ]
                ]
            , button [ type' "submit", class "button primary" ] [ text "Create" ]
            ]
        ]


entryToValue : TransactionEntryForm -> Maybe Encode.Value
entryToValue entry =
    if String.isEmpty entry.amount then
        Nothing
    else
        Just
            <| Encode.object
                [ ( "account", Encode.string entry.account )
                , ( "amount", Encode.string entry.amount )
                ]


entriesToValue : List TransactionEntryForm -> Encode.Value
entriesToValue entries =
    Encode.list <| List.filterMap entryToValue entries


modelToValue : Model -> Encode.Value
modelToValue model =
    let
        transaction =
            model.transaction

        entries =
            model.entries
    in
        Encode.object
            [ ( "payee", Encode.string transaction.payee )
            , ( "description", if String.isEmpty transaction.description then Encode.null else Encode.string transaction.description )
            , ( "posted_on", Encode.string transaction.postedOn )
            , ( "entries", entriesToValue entries )
            ]


submit : Model -> Task (Http.Error (List String)) (Http.Response Transaction)
submit model =
    let
        body =
            modelToValue model

        url =
            "/api/transactions/create"
    in
        Http.post url
            |> Http.withHeader "Content-Type" "application/json"
            |> Http.withHeader "Accept" "application/json"
            |> Http.withCredentials
            |> Http.withJsonBody body
            |> Http.withTimeout (5 * Time.second)
            |> Http.send (Http.jsonReader (Decode.at [ "data" ] transactionDecoder)) (Http.jsonReader errorsDecoder)
