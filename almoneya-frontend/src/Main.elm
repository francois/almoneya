module Main exposing (..)

import Html.App as App
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import RecordRevenueApp
import ListTransactionsApp
import ImportBankAccountTransactionsApp
import RecordTransactionApp
import BankAccountTransactionsApp


main =
    App.program { init = init, update = update, view = view, subscriptions = subscriptions }


type alias Model =
    { location : Location
    , recordRevenueApp : RecordRevenueApp.Model
    , listTransactionsApp : ListTransactionsApp.Model
    , importBankAccountTransactionsApp : ImportBankAccountTransactionsApp.Model
    , recordTransactionApp : RecordTransactionApp.Model
    , bankAccountTransactionsApp : BankAccountTransactionsApp.Model
    }


type Msg
    = NavigateTo Location
    | RecordRevenueEvent RecordRevenueApp.Msg
    | ListTransactionsEvent ListTransactionsApp.Msg
    | ImportBankAccountTransactionsEvent ImportBankAccountTransactionsApp.Msg
    | RecordTransactionEvent RecordTransactionApp.Msg
    | BankAccountTransactionsEvent BankAccountTransactionsApp.Msg


type Location
    = ImportBankTransactions
    | ReconcileBankTransactions
    | RecordCheck
    | RecordExpense
    | RecordRevenue
    | RecordTransaction
    | NextObligations
    | Obligations
    | Goals
    | Transactions
    | Accounts
    | BankTransactions
    | NeedHelp
    | Settings


init : ( Model, Cmd Msg )
init =
    let
        ( recordRevenueAppModel, recordRevenueAppCmd ) =
            RecordRevenueApp.init

        ( listTransactionsAppModel, listTransactionsAppCmd ) =
            ListTransactionsApp.init

        ( importBankAccountTransactionsAppModel, importBankAccountTransactionsAppCmd ) =
            ImportBankAccountTransactionsApp.init

        ( recordTransactionAppModel, recordTransactionAppCmd ) =
            RecordTransactionApp.init

        ( bankAccountTransactionsAppModel, bankAccountTransactionsAppCmd ) =
            BankAccountTransactionsApp.init
    in
        ( { location = RecordRevenue
          , recordRevenueApp = recordRevenueAppModel
          , listTransactionsApp = listTransactionsAppModel
          , importBankAccountTransactionsApp = importBankAccountTransactionsAppModel
          , recordTransactionApp = recordTransactionAppModel
          , bankAccountTransactionsApp = bankAccountTransactionsAppModel
          }
        , Cmd.batch
            [ Cmd.map RecordRevenueEvent recordRevenueAppCmd
            , Cmd.map ListTransactionsEvent listTransactionsAppCmd
            , Cmd.map ImportBankAccountTransactionsEvent importBankAccountTransactionsAppCmd
            , Cmd.map RecordTransactionEvent recordTransactionAppCmd
            , Cmd.map BankAccountTransactionsEvent bankAccountTransactionsAppCmd
            ]
        )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NavigateTo RecordRevenue ->
            let
                ( recordRevenueAppModel, recordRevenueAppCmd ) =
                    RecordRevenueApp.init
            in
                ( { model
                    | location = RecordRevenue
                    , recordRevenueApp = recordRevenueAppModel
                  }
                , Cmd.batch [ Cmd.map RecordRevenueEvent recordRevenueAppCmd ]
                )

        NavigateTo Transactions ->
            let
                ( transactionsModel, transactionsCmd ) =
                    ListTransactionsApp.init
            in
                ( { model | location = Transactions, listTransactionsApp = transactionsModel }, Cmd.map ListTransactionsEvent transactionsCmd )

        NavigateTo RecordTransaction ->
            let
                ( recordTransactionModel, recordTransactionCmd ) =
                    RecordTransactionApp.init
            in
                ( { model | location = RecordTransaction, recordTransactionApp = recordTransactionModel }, Cmd.map RecordTransactionEvent recordTransactionCmd )

        NavigateTo ImportBankTransactions ->
            let
                ( bankTransactionsModel, bankTransactionsCmd ) =
                    ImportBankAccountTransactionsApp.init
            in
                ( { model | location = ImportBankTransactions, importBankAccountTransactionsApp = bankTransactionsModel }, Cmd.map ImportBankAccountTransactionsEvent bankTransactionsCmd )

        NavigateTo BankTransactions ->
            let
                ( bankAccountTransactionsAppModel, bankAccountTransactionsAppCmd ) =
                    BankAccountTransactionsApp.init
            in
                ( { model | location = BankTransactions, bankAccountTransactionsApp = bankAccountTransactionsAppModel }, Cmd.map BankAccountTransactionsEvent bankAccountTransactionsAppCmd )

        RecordRevenueEvent rreMsg ->
            let
                ( rreModel, rreCmd ) =
                    RecordRevenueApp.update rreMsg model.recordRevenueApp
            in
                ( { model | recordRevenueApp = rreModel }, Cmd.map RecordRevenueEvent rreCmd )

        ListTransactionsEvent ltMsg ->
            let
                ( ltModel, ltCmd ) =
                    ListTransactionsApp.update ltMsg model.listTransactionsApp
            in
                ( { model | listTransactionsApp = ltModel }, Cmd.map ListTransactionsEvent ltCmd )

        ImportBankAccountTransactionsEvent ibatMsg ->
            let
                ( ibatModel, ibatCmd ) =
                    ImportBankAccountTransactionsApp.update ibatMsg model.importBankAccountTransactionsApp
            in
                ( { model | importBankAccountTransactionsApp = ibatModel }, Cmd.map ImportBankAccountTransactionsEvent ibatCmd )

        RecordTransactionEvent rtMsg ->
            let
                ( rtModel, rtCmd ) =
                    RecordTransactionApp.update rtMsg model.recordTransactionApp
            in
                ( { model | recordTransactionApp = rtModel }, Cmd.map RecordTransactionEvent rtCmd )

        BankAccountTransactionsEvent batMsg ->
            let
                ( batModel, batCmd ) =
                    BankAccountTransactionsApp.update batMsg model.bankAccountTransactionsApp
            in
                ( { model | bankAccountTransactionsApp = batModel }, Cmd.map BankAccountTransactionsEvent batCmd )

        NavigateTo newLocation ->
            ( { model | location = newLocation }, Cmd.none )


menuItem : Model -> String -> String -> String -> Location -> Html Msg
menuItem model icon content anchorTitle location =
    li [ classList [ ( "active", model.location == location ) ] ] [ a [ title anchorTitle, onClick (NavigateTo location) ] [ i [ class icon ] [], text content ] ]


drawView : Model -> Html Msg
drawView model =
    case model.location of
        RecordRevenue ->
            App.map RecordRevenueEvent (RecordRevenueApp.view model.recordRevenueApp)

        Transactions ->
            App.map ListTransactionsEvent (ListTransactionsApp.view model.listTransactionsApp)

        ImportBankTransactions ->
            App.map ImportBankAccountTransactionsEvent (ImportBankAccountTransactionsApp.view model.importBankAccountTransactionsApp)

        RecordTransaction ->
            App.map RecordTransactionEvent (RecordTransactionApp.view model.recordTransactionApp)

        BankTransactions ->
            App.map BankAccountTransactionsEvent (BankAccountTransactionsApp.view model.bankAccountTransactionsApp)

        _ ->
            h1 [] [ text "Content" ]


view : Model -> Html Msg
view model =
    div [ class "expanded row" ]
        [ div [ class "large-3 medium-4 columns show-for-medium menubar" ]
            [ h1 [] [ text "Almoneya" ]
            , h2 [] [ text "Operations" ]
            , ul [ class "menu vertical" ]
                [ menuItem model "fi-page-csv" "Import" "Import Bank Transactions" ImportBankTransactions
                , menuItem model "fi-calendar" "Reconcile" "Reconcile Bank Transactions" ReconcileBankTransactions
                , menuItem model "fi-ticket" "Record Check" "Record a new check" RecordCheck
                , menuItem model "fi-clipboard-pencil" "Record Expense" "Record a new expense" RecordExpense
                , menuItem model "fi-clipboard-notes" "Record Revenue" "Record a new revenue event" RecordRevenue
                , menuItem model "fi-pencil" "Record Transaction" "Manually record a transaction" RecordTransaction
                ]
            , h2 [] [ text "Reports" ]
            , ul [ class "menu vertical" ]
                [ menuItem model "fi-graph-trend" "Next Obligations" "View the list of upcoming obligations and goals" NextObligations
                , menuItem model "fi-calendar" "Obligations" "Manage the list of obligations" Obligations
                , menuItem model "fi-calendar" "Goals" "Manage the list of goals" Goals
                , menuItem model "fi-book" "Transactions" "View and manually add transactions" Transactions
                , menuItem model "fi-map" "Accounts" "Manage the list of accounts" Accounts
                , menuItem model "fi-clipboard" "Bank Transactions" "Manage the bank transactions that were imported" BankTransactions
                ]
            , h2 [] [ text "Utilities" ]
            , ul [ class "menu vertical" ]
                [ menuItem model "fi-telephone" "Need Help?" "Do you need help to understand something? Contact us!" NeedHelp
                , menuItem model "fi-widget" "Settings" "Change your email and password and your subscription" Settings
                ]
            ]
        , div [ class "large-9 medium-8 columns" ]
            [ drawView model
            ]
        ]


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none
