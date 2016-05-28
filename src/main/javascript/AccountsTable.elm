module AccountsTable exposing (AccountsResponse, accountsTable)

import Account exposing (Account)
import Either exposing (..)
import Html.App as Html
import Html.Attributes exposing (class, classList, colspan)
import Html exposing (Html, div, text, table, thead, tr, th, tbody, td, h1, p)
import String exposing (toLower)

type alias AccountsResponse = Either.Either String (List Account)

accountsTable : AccountsResponse -> Html a
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

accountRows : Either.Either String (List Account) -> List (Html a)
accountRows model = case model of
  Either.Left err       -> [ tr [] [ td [colspan 3] [ text err ] ] ]
  Either.Right accounts -> List.map accountRow (List.sortBy (\account -> (toLower account.name)) accounts)

accountRow : Account -> Html a
accountRow account = tr [] [
    td [] [ text account.name ]
  , td [] [ text account.kind ]
  , td [class "amount"] [ balanceToString account.balance ]
  ]

balanceToString : Maybe String -> Html a
balanceToString str = case str of
  Just str -> text str
  Nothing  -> text ""
