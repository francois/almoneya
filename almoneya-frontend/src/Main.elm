import Html.App as App
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)

main = App.program { init = init, update = update, view = view, subscriptions = subscriptions }

type alias Model = {
    location : Location
  }

type Location = ImportBankTransactions
              | ReconcileBankTransactions
              | RecordCheck
              | RecordExpense
              | RecordRevenue
              | NextObligations
              | Obligations
              | Goals
              | Transactions
              | Accounts
              | BankTransactions
              | NeedHelp
              | Settings

type Msg = NavigateTo Location

init : (Model, Cmd Msg)
init = ({ location = RecordRevenue }, Cmd.none)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model = case msg of
  NavigateTo newLocation -> ({ model | location = newLocation }, Cmd.none)

menuItem : Model -> String -> String -> String -> Location -> Html Msg
menuItem model icon content anchorTitle location =
  li [classList [("active", model.location == location)]] [ a [ title anchorTitle, onClick (NavigateTo location) ] [ i [ class icon ] [], text content ] ]

view : Model -> (Html Msg)
view model = div [class "expanded row"] [
    div [class "large-3 medium-4 columns show-for-medium menubar"] [
        h1 [] [ text "Almoneya" ]
      , h2 [] [ text "Operations" ]
      , ul [class "menu vertical"] [
          menuItem model "fi-page-csv"        "Import"          "Import Bank Transactions"    ImportBankTransactions
        , menuItem model "fi-calendar"         "Reconcile"      "Reconcile Bank Transactions" ReconcileBankTransactions
        , menuItem model "fi-ticket"           "Record Check"   "Record a new check"          RecordCheck
        , menuItem model "fi-clipboard-pencil" "Record Expense" "Record a new expense"        RecordExpense
        , menuItem model "fi-clipboard-notes"  "Record Revenue" "Record a new revenue event"  RecordRevenue
      ]
      , h2 [] [text "Reports"]
      , ul [class "menu vertical"] [
          menuItem model "fi-graph-trend" "Next Obligations"  "View the list of upcoming obligations and goals" NextObligations
        , menuItem model "fi-calendar"    "Obligations"       "Manage the list of obligations"                  Obligations
        , menuItem model "fi-calendar"    "Goals"             "Manage the list of goals"                        Goals
        , menuItem model "fi-book"        "Transactions"      "View and manually add transactions"              Transactions
        , menuItem model "fi-map"         "Accounts"          "Manage the list of accounts"                     Accounts
        , menuItem model "fi-clipboard"   "Bank Transactions" "Manage the bank transactions that were imported" BankTransactions
      ]
      , h2 [] [text "Utilities"]
      , ul [class "menu vertical"] [
          menuItem model "fi-telephone" "Need Help?" "Do you need help to understand something? Contact us!" NeedHelp
        , menuItem model "fi-widget"    "Settings"   "Change your email and password and your subscription"  Settings
      ]
    ]
    , div [class "large-9 medium-8 columns"] [
      h1 [] [text "Content"]
    ]
  ]

subscriptions : Model -> Sub Msg
subscriptions model = Sub.none
