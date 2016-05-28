import Account exposing (Account, decodeAccountsJson)
import AccountsTable exposing (AccountsResponse, accountsTable)
import Debug exposing (crash)
import Either exposing (..)
import Html.App as Html
import Html.Attributes exposing (class, classList, colspan)
import Html.Events exposing (onClick)
import Html exposing (Html, div, text, table, thead, tr, th, tbody, td, h1, p)
import Http exposing (get)
import List exposing (length)
import Task exposing (Task)

main = Html.program { init = init, view = view, subscriptions = subscriptions, update = update }

type alias Model = AccountsResponse

type Msg = FetchFail Http.Error
         | FetchSucceed (List Account)

init : (Model, Cmd Msg)
init = (Right [], getAccounts)

subscriptions : Model -> Sub Msg
subscriptions model = Sub.none

update : Msg -> Model -> (Model, Cmd Msg)
update msg model = case msg of
    FetchFail error       -> (Left (toString error), Cmd.none)
    FetchSucceed accounts -> (Right accounts, Cmd.none)

view : Model -> Html Msg
view model =
  div [class "row"] [
    div [class "small-12 medium-6 columns"] [
        h1 [] [text "Accounts"]
      , accountsTable model
    ]
  ]

getAccounts : Cmd Msg
getAccounts = Task.perform FetchFail FetchSucceed (Http.get decodeAccountsJson "/api/accounts/")
