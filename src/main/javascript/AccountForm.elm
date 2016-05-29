module AccountForm exposing (Model, Msg, initNew, initExisting, update, view)

import Debug
import Account exposing (Account)
import Html.Attributes exposing (class, action, enctype, method, type', name, id, value, for)
import Html exposing (Html, div, text, label, form, input, button, select, option, h1)
import Html.Events exposing (onInput, onCheck)

type alias Model =
  {   name    : String
    , kind    : Maybe AccountKind
    , virtual : Bool
    , isNew   : Bool
  }

type Msg = Name String
        | Kind String
        | Virtual Bool

type AccountKind  = Asset
                  | Liability
                  | Equity
                  | Revenue
                  | Expense
                  | Contra

initNew : Model
initNew = { name = "", kind = Nothing, virtual = False, isNew = True }

initExisting : String -> AccountKind -> Bool -> Model
initExisting name kind virtual = { name = name, kind = Just kind, virtual = virtual, isNew = False }

update : Msg -> Model -> Model
update msg model = case msg of
  Name newName     -> { model | name = Debug.log "newName: " newName }
  Kind newKind     -> { model | kind = stringToKind newKind |> Debug.log "newKind: " }
  Virtual newState -> { model | virtual = newState |> Debug.log "newVirtual: " }

stringToKind : String -> Maybe AccountKind
stringToKind str = case str of
  "asset"     -> Just Asset
  "liability" -> Just Liability
  "equity"    -> Just Equity
  "revenue"   -> Just Revenue
  "expense"   -> Just Expense
  "contra"    -> Just Contra
  _           -> Nothing

accountFormTitle : Model -> String
accountFormTitle model = case model.isNew of
  True -> "New Account"
  False -> "Edit Account"

view : Model -> (Html Msg)
view account = form [ class "small-12 medium-6 columns" ] [
      h1 [] [ accountFormTitle account |> text ]
    , label [] [
          text "Name"
        , input [type' "text", value account.name, onInput Name] []
      ]
    , label [] [
        text "Kind"
      , select [ onInput Kind ] [
            option [ value "asset"     ] [ text "asset" ]
          , option [ value "liability" ] [ text "liability" ]
          , option [ value "equity"    ] [ text "equity" ]
          , option [ value "revenue"   ] [ text "revenue" ]
          , option [ value "expense"   ] [ text "expense" ]
          , option [ value "contra"    ] [ text "contra" ]
        ]
      ]
    , input [type' "checkbox", id "account_virtual", onCheck Virtual] []
    , label [ for "account_virtual"] [ text "Virtual" ]
  ]
