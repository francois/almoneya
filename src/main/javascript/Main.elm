import Account exposing (Account, decodeAccountsJson)
import AccountForm
import AccountsTable exposing (AccountsResponse, accountsTable)
import Debug exposing (crash)
import Either exposing (Either(Left, Right))
import Html.App as Html
import Html.Attributes exposing (class, classList, colspan, action, enctype, method, type', name)
import Html.Events exposing (onClick)
import Html exposing (Html, div, text, table, thead, tr, th, tbody, td, h1, label, form, input, button)
import Http
import Task exposing (Task)

main = Html.program { init = init, view = view, subscriptions = subscriptions, update = update }

type alias Model =  { accounts : AccountsResponse
                    , newAccount : AccountForm.Model
                    }

type Msg = FetchFail Http.Error
         | FetchSucceed (List Account)
         | NewAccountEdit AccountForm.Msg

init : (Model, Cmd Msg)
init = ({ accounts = Right [], newAccount = AccountForm.initNew}, getAccounts)

subscriptions : Model -> Sub Msg
subscriptions model = Sub.none

update : Msg -> Model -> (Model, Cmd Msg)
update msg model = case msg of
    FetchFail error                  -> ({ model | accounts = Left (toString error) }, Cmd.none)
    FetchSucceed accounts            -> ({ model | accounts = Right accounts }, Cmd.none)
    NewAccountEdit newAccountEditMsg ->
      let (newAccountModel, saveAccountMsg) = AccountForm.update newAccountEditMsg model.newAccount 
      in ({ model | newAccount = newAccountModel }, Cmd.map NewAccountEdit saveAccountMsg)

view : Model -> Html Msg
view model =
  div [class "row"] [
      div [class "medium-12 large-6 columns"] [
        accountsTable model.accounts
    ]
    , div [class "medium-12 large-6 columns"] [ Html.map NewAccountEdit (AccountForm.view model.newAccount) ]
    , div [class "medium-12 large-6 columns show-for-medium"] [
        h1 [] [text "Import Transactions"]
      , form [method "post", enctype "multipart/form-data", action "/api/bank-account-transactions/import/"] [
          label [] [
            input [type' "file", name "file"] []]
        , button [type' "submit", class "button button-primary"] [ text "Import" ]
        ]
    ]
  ]

getAccounts : Cmd Msg
getAccounts = Task.perform FetchFail FetchSucceed (Http.get decodeAccountsJson "/api/accounts/")
