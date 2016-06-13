module ListTransactionsApp exposing (Model, Msg, init, view, update)

import Domain exposing (..)
import DomainRest exposing (..)
import Html.Attributes exposing (class, classList, disabled, placeholder, type', selected)
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
    , accounts : List Account
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
    | ChangePostedOnOrAfter String
    | ChangePostedOnOrBefore String
    | ChangeBalanceGtOrEq String
    | ChangeBalanceLtOrEq String
    | AccountsOk (Http.Response (List Account))
    | AccountsFailed (Http.Error (List String))
    | ChangeAccount String


initFilters : ( Filters, Cmd Msg )
initFilters =
    ( { query = Nothing, postedOnOrAfter = Nothing, postedOnOrBefore = Nothing, balanceGtOrEq = Nothing, balanceLtOrEq = Nothing, account = Nothing, accounts = [] }, Task.perform AccountsFailed AccountsOk getAccounts )


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

        ChangePostedOnOrAfter str ->
            case str of
                "" ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | postedOnOrAfter = Nothing }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

                nonEmptyString ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | postedOnOrAfter = Just nonEmptyString }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

        ChangePostedOnOrBefore str ->
            case str of
                "" ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | postedOnOrBefore = Nothing }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

                nonEmptyString ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | postedOnOrBefore = Just nonEmptyString }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

        ChangeBalanceGtOrEq str ->
            case str of
                "" ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | balanceGtOrEq = Nothing }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

                nonEmptyString ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | balanceGtOrEq = Just nonEmptyString }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

        ChangeBalanceLtOrEq str ->
            case str of
                "" ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | balanceLtOrEq = Nothing }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

                nonEmptyString ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | balanceLtOrEq = Just nonEmptyString }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

        ChangeAccount str ->
            case str of
                "" ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | account = Nothing }
                    in
                        ( { model | filters = newFilters }, Cmd.none )

                nonEmptyString ->
                    let
                        oldFilters =
                            model.filters

                        newFilters =
                            { oldFilters | account = Just nonEmptyString }
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

        AccountsOk response ->
            let
                oldFilters =
                    model.filters

                newFilters =
                    { oldFilters | accounts = response.data }
            in
                ( { model | filters = newFilters }, Cmd.none )

        AccountsFailed error ->
            case error of
                Http.UnexpectedPayload str ->
                    ( { model | errors = [ "Unexpected response from server: " ++ str ] }, Cmd.none )

                Http.NetworkError ->
                    ( { model | errors = [ "Network error while loading accounts" ] }, Cmd.none )

                Http.Timeout ->
                    ( { model | errors = [ "Timeout loading accounts... please try again" ] }, Cmd.none )

                Http.BadResponse errors ->
                    ( { model | errors = errors.data }, Cmd.none )


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
        , td [ class "amount" ] [ text <| Maybe.withDefault "" txn.balance ]
        , td [] [ List.map (\x -> x.account.name) txn.entries |> List.sortBy String.toLower |> String.join ", " |> text ]
        ]


sortedTransactions : List Transaction -> List Transaction
sortedTransactions txns =
    let
        compValue txn =
            ( txn.postedOn, txn.bookedAt )
    in
        List.sortBy compValue txns


viewEmptyOption : Html Msg
viewEmptyOption =
    option [] [ text "Please choose an entry" ]


viewAccountOption : Maybe AccountName -> AccountName -> Html Msg
viewAccountOption selectedAccount name =
    let
        sel1 =
            Maybe.map (\x -> x == name) selectedAccount

        sel2 =
            Maybe.withDefault False sel1
    in
        option [ selected sel2 ] [ text name ]


viewAccountsOptions : Maybe AccountName -> List Account -> List (Html Msg)
viewAccountsOptions selectedName accounts =
    let
        names =
            List.map (\x -> x.name) accounts

        options =
            List.map (viewAccountOption selectedName) names
    in
        [ viewEmptyOption ] ++ options


viewFilterBar : Model -> Html Msg
viewFilterBar model =
    div [ class "callout" ]
        [ form []
            [ div [ class "row" ]
                [ div [ class "large-3 small-12 columns" ] [ label [] [ text "Search:", input [ type' "text", placeholder "Type to search...", onInput ChangeSearch ] [] ] ]
                , div [ class "large-3 small-12 columns" ] [ label [] [ text "Posted between:", input [ type' "date", placeholder "From YYYY-MM-DD", onInput ChangePostedOnOrAfter ] [], input [ type' "date", placeholder "To YYYY-MM-DD", onInput ChangePostedOnOrBefore ] [] ] ]
                , div [ class "large-2 small-12 columns" ] [ label [] [ text "Amount between:", input [ class "amount", type' "text", placeholder "xx.xx", onInput ChangeBalanceGtOrEq ] [], input [ class "amount", type' "text", placeholder "xx.xx", onInput ChangeBalanceLtOrEq ] [] ] ]
                , div [ class "large-4 small-12 columns" ] [ label [] [ text "Account:", select [ onInput ChangeAccount ] (viewAccountsOptions model.filters.account model.filters.accounts) ] ]
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


filterByDate : Maybe String -> Maybe String -> Transaction -> Bool
filterByDate postedOnOrAfter postedOnOrBefore txn =
    case ( postedOnOrAfter, postedOnOrBefore ) of
        ( Nothing, Nothing ) ->
            True && True

        ( Nothing, Just before ) ->
            True && txn.postedOn <= before

        ( Just after, Nothing ) ->
            after <= txn.postedOn && True

        ( Just after, Just before ) ->
            after <= txn.postedOn && txn.postedOn <= before


filterByAmount : Maybe String -> Maybe String -> Transaction -> Bool
filterByAmount balanceGtOrEq balanceLtOrEq txn =
    case ( balanceGtOrEq, balanceLtOrEq ) of
        ( Nothing, Nothing ) ->
            True && True

        ( Nothing, Just under ) ->
            True && (Result.withDefault 0 (String.toFloat (Maybe.withDefault "0" txn.balance))) <= (Result.withDefault maxFloat (String.toFloat under))

        ( Just over, Nothing ) ->
            (Result.withDefault minFloat (String.toFloat over)) <= (Result.withDefault 0 (String.toFloat (Maybe.withDefault "0" txn.balance))) && True

        ( Just over, Just under ) ->
            (Result.withDefault minFloat (String.toFloat over)) <= (Result.withDefault 0 (String.toFloat (Maybe.withDefault "0" txn.balance))) && (Result.withDefault 0 (String.toFloat (Maybe.withDefault "0" txn.balance))) <= (Result.withDefault maxFloat (String.toFloat under))


minFloat : Float
minFloat =
    -1.0e20


maxFloat : Float
maxFloat =
    1.0e20


filterByAccount : Maybe String -> Transaction -> Bool
filterByAccount str txn =
    case str of
        Nothing ->
            True

        Just name ->
            List.any (\x -> x.account.name == name) txn.entries


filterTransactions : Filters -> List Transaction -> List Transaction
filterTransactions filters txns =
    txns
        |> List.filter (filterByQuery filters.query)
        |> List.filter (filterByDate filters.postedOnOrAfter filters.postedOnOrBefore)
        |> List.filter (filterByAmount filters.balanceGtOrEq filters.balanceLtOrEq)
        |> List.filter (filterByAccount filters.account)


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
