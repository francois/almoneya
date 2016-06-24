module BankAccountTransactionsApp exposing (Model, Msg, init, update, view)

import Domain exposing (BankAccountTransaction)
import DomainRest exposing (getBankAccountTransactions)
import Html.Attributes exposing (class)
import Html exposing (..)
import HtmlHelpers exposing (..)
import HttpBuilder as Http
import Task


type alias Model =
    { transactions : List BankAccountTransaction
    , errors : List String
    , loading : Bool
    }


type Msg
    = BankAccountTransactionsFailed (Http.Error (List String))
    | BankAccountTransactionsOk (Http.Response (List BankAccountTransaction))


init : ( Model, Cmd Msg )
init =
    ( { transactions = [], errors = [], loading = True }
    , Task.perform BankAccountTransactionsFailed BankAccountTransactionsOk getBankAccountTransactions
    )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BankAccountTransactionsFailed error ->
            case error of
                Http.UnexpectedPayload str ->
                    ( { model | loading = False, errors = [ "Unexpected response from server: " ++ str ] }, Cmd.none )

                Http.NetworkError ->
                    ( { model | loading = False, errors = [ "Network error contacting server" ] }, Cmd.none )

                Http.Timeout ->
                    ( { model | loading = False, errors = [ "Timeout fetching accounts from server" ] }, Cmd.none )

                Http.BadResponse errors ->
                    ( { model | loading = False, errors = errors.data }, Cmd.none )

        BankAccountTransactionsOk response ->
            ( { model | loading = False, transactions = response.data }, Cmd.none )


loadingOrCount : Model -> Html Msg
loadingOrCount model =
    case model.loading of
        True ->
            p [] [ text "Loading..." ]

        False ->
            p [] [ text "Found ", List.length model.transactions |> toString |> text, text " bank account transactions" ]


viewFilters : Model -> Html Msg
viewFilters model =
    div [] []


viewTransaction : BankAccountTransaction -> Html Msg
viewTransaction txn =
    tr []
        [ td [] [ text txn.postedOn ]
        , td [] [ text (Maybe.withDefault "" (Debug.log "checknum" txn.checkNum)) ]
        , td [] [ text txn.desc1, text (Maybe.withDefault "" txn.desc2) ]
        , td [ class "amount" ] [ text txn.amount ]
        , td [] [ text txn.bankAccount.last4 ]
        ]


viewTransactions : List BankAccountTransaction -> List (Html Msg)
viewTransactions =
    List.map viewTransaction


viewTransactionsTable : List BankAccountTransaction -> Html Msg
viewTransactionsTable txns =
    table []
        [ thead []
            [ tr []
                [ th [] [ text "Posted On" ]
                , th [] [ text "Check Num" ]
                , th [] [ text "Description" ]
                , th [] [ text "Amount" ]
                , th [] [ text "Account" ]
                ]
            ]
        , tbody []
            (viewTransactions txns)
        ]


view : Model -> Html Msg
view model =
    div []
        [ h1 [] [ text "Bank Account Transactions" ]
        , viewErrors model.errors
        , viewFilters model
        , viewTransactionsTable model.transactions
        ]
