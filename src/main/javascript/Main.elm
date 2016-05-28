import Debug exposing (crash)
import Html.App as Html
import Html.Attributes exposing (class, classList, colspan)
import Html.Events exposing (onClick)
import Html exposing (Html, div, text, table, thead, tr, th, tbody, td, h1, p)
import Http exposing (get)
import Json.Decode exposing (Decoder, list, int, string, bool, maybe, at, (:=), object5)
import List exposing (length)
import String exposing (toLower)
import Task exposing (Task)

main = Html.program { init = init, view = view, subscriptions = subscriptions, update = update }

type alias Account =  {   accountId : Int
                        , name      : String
                        , kind      : String
                        , virtual   : Bool
                        , balance   : Maybe String
                      }

type Either a b = Left a
                | Right b

type alias Model = Either String (List Account)

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

balanceToString : Maybe String -> Html Msg
balanceToString str = case str of
  Just str -> text str
  Nothing  -> text ""

accountRow : Account -> Html Msg
accountRow account = tr [] [
    td [] [ text account.name ]
  , td [] [ text account.kind ]
  , td [class "amount"] [ balanceToString account.balance ]
  ]

view : Model -> Html Msg
view model =
  div [class "row"] [
    div [class "small-12 medium-6 columns"] [
        h1 [] [text "Accounts"]
      , accountsTable model
    ]
  ]

accountsTable : Model -> Html Msg
accountsTable model =
  table [] [
    thead [] [
      tr [] [
          th [] [ text "Account" ]
        , th [] [ text "Kind" ]
        , th [class "amount"] [ text "Balance" ]
      ]
    ]
    , tbody [] (accountRows model)
  ]

accountRows : Model -> List (Html Msg)
accountRows model = case model of
  Left err       -> [ tr [] [ td [colspan 3] [ text err ] ] ]
  Right accounts -> List.map accountRow (List.sortBy (\account -> (toLower account.name)) accounts)

decodeAccountJson : Decoder Account
decodeAccountJson = object5 Account
                      ("id"      := int)
                      ("name"    := string)
                      ("kind"    := string)
                      ("virtual" := bool)
                      ("balance" := maybe string)

decodeAccountsJson : Decoder (List Account)
decodeAccountsJson = at ["data"] (list decodeAccountJson)

getAccounts : Cmd Msg
getAccounts = Task.perform FetchFail FetchSucceed (Http.get decodeAccountsJson "/api/accounts/")
