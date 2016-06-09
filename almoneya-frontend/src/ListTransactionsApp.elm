module ListTransactionsApp exposing (Model, Msg, init, view, update)

import Domain exposing (..)
import DomainRest exposing (..)
import Html.Attributes exposing (class, classList, disabled)
import Html.Events exposing (onSubmit, onInput)
import Html exposing (..)
import HtmlHelpers exposing (viewErrors)
import HttpBuilder as Http
import String
import Task exposing (..)


type alias Model =
    { loading : Bool
    , transactions : List Transaction
    , errors : List String
    }


type Msg
    = LoadTransactionsFailed (Http.Error (List String))
    | LoadTransactionsOk (Http.Response (List Transaction))


init : ( Model, Cmd Msg )
init =
    ( { loading = True, transactions = [], errors = [] }, Task.perform LoadTransactionsFailed LoadTransactionsOk getTransactions )


update : Msg -> Model -> ( Model, Cmd Msg )
update ev model =
    case ev of
        LoadTransactionsFailed error ->
            case error of
                Http.UnexpectedPayload str ->
                    ( { model | loading = False, errors = [ "Unexpected response from server: " ++ str ] }, Cmd.none )

                Http.NetworkError ->
                    ( { model | loading = False, errors = [ "Network error contacting server" ] }, Cmd.none )

                Http.Timeout ->
                    ( { model | loading = False, errors = [ "Timeout fetching accounts from server" ] }, Cmd.none )

                Http.BadResponse errors ->
                    ( { model | loading = False, errors = errors.data }, Cmd.none )

        LoadTransactionsOk response ->
            ( { model | loading = False, transactions = response.data }, Cmd.none )


viewPayee : Transaction -> List (Html Msg)
viewPayee txn =
    case txn.description of
        Just desc ->
            [ text txn.payee, div [ class "muted" ] [ text desc ] ]

        Nothing ->
            [ text txn.payee ]


viewTransaction : Transaction -> Html Msg
viewTransaction txn =
    tr []
        [ td [ class "date" ] [ text txn.postedOn ]
        , td [] (viewPayee txn)
        , td [ class "amount" ] [ text txn.balance ]
        , td [] [ List.map (\x -> x.account.name) txn.entries |> List.sortBy String.toLower |> String.join ", " |> text ]
        ]


sortedTransactions : List Transaction -> List Transaction
sortedTransactions txns =
    let
        compValue txn =
            (txn.postedOn, txn.bookedAt)
    in
        List.sortBy compValue txns


view : Model -> Html Msg
view model =
    div []
        [ h1 [] [ text "List Transactions" ]
        , viewErrors model.errors
        , table []
            [ thead []
                [ tr []
                    [ th [] [ text "Date" ]
                    , th [] [ text "Payee" ]
                    , th [] [ text "Amount" ]
                    , th [] [ text "Accounts" ]
                    ]
                ]
            , tbody [] (sortedTransactions model.transactions |> List.map viewTransaction)
            ]
        ]
