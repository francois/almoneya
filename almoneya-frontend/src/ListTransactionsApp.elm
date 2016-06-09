module ListTransactionsApp exposing (Model, Msg, init, view, update)

import Domain exposing (..)
import DomainRest exposing (..)
import Html.Attributes exposing (class, classList, disabled, placeholder, type')
import Html.Events exposing (onSubmit, onInput)
import Html exposing (..)
import HtmlHelpers exposing (viewErrors)
import HttpBuilder as Http
import String
import Task exposing (..)


type alias Filters =
    { query : Maybe String
    , postedOnOrAfter : Maybe String
    , postedOnOrBefore : Maybe String
    , balanceGtOrEq : Maybe String
    , balanceLtOrEq : Maybe String
    , account : Maybe AccountName
    }


type alias Model =
    { loading : Bool
    , transactions : List Transaction
    , errors : List String
    , filters : Filters
    }


type Msg
    = LoadTransactionsFailed (Http.Error (List String))
    | LoadTransactionsOk (Http.Response (List Transaction))
    | ChangeSearch String


initFilters : ( Filters, Cmd Msg )
initFilters =
    ( { query = Nothing, postedOnOrAfter = Nothing, postedOnOrBefore = Nothing, balanceGtOrEq = Nothing, balanceLtOrEq = Nothing, account = Nothing }, Cmd.none )


init : ( Model, Cmd Msg )
init =
    let
        ( filtersModel, filtersCmd ) =
            initFilters
    in
        ( { loading = True, transactions = [], errors = [], filters = filtersModel }, Cmd.batch [ filtersCmd, Task.perform LoadTransactionsFailed LoadTransactionsOk getTransactions ] )


update : Msg -> Model -> ( Model, Cmd Msg )
update ev model =
    case ev of
        ChangeSearch str ->
            case str of
                "" ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | query = Nothing }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

                nonEmptyString ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | query = Just nonEmptyString }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

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
            ( txn.postedOn, txn.bookedAt )
    in
        List.sortBy compValue txns


viewFilterBar : Model -> Html Msg
viewFilterBar model =
    div [ class "callout" ]
        [ form []
            [ div [ class "row" ]
                [ div [ class "large-3 small-12 columns" ] [ label [] [ text "Search:", input [ type' "text", placeholder "Type to search...", onInput ChangeSearch ] [] ] ]
                , div [ class "large-3 small-12 columns" ] [ label [] [ text "Posted between:", input [ type' "date", placeholder "From YYYY-MM-DD" ] [], input [ type' "date", placeholder "To YYYY-MM-DD" ] [] ] ]
                , div [ class "large-2 small-12 columns" ] [ label [] [ text "Amount between:", input [ class "amount", type' "text", placeholder "xx.xx" ] [], input [ class "amount", type' "text", placeholder "xx.xx" ] [] ] ]
                , div [ class "large-4 small-12 columns" ] [ label [] [ text "Account:", select [] [ option [] [ text "Choose an account" ] ] ] ]
                ]
            ]
        ]


filterByQuery : Maybe String -> Transaction -> Bool
filterByQuery str txn =
    let
        desc =
            case txn.description of
                Nothing ->
                    ""

                Just d ->
                    d

        haystack =
            String.join "  " [ txn.payee, desc ]

        needle =
            case str of
                Nothing ->
                    True

                Just s ->
                    String.contains (String.toLower s) (String.toLower haystack)
    in
        needle


filterTransactions : Filters -> List Transaction -> List Transaction
filterTransactions filters txns =
    txns
        |> List.filter (filterByQuery filters.query)


view : Model -> Html Msg
view model =
    div []
        [ h1 [] [ text "List Transactions" ]
        , viewFilterBar model
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
            , tbody [] (model.transactions |> filterTransactions model.filters |> sortedTransactions |> List.map viewTransaction)
            ]
        ]
