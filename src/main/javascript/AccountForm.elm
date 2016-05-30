module AccountForm exposing (Model, Msg, initNew, initExisting, update, view)

import Account exposing (Account, decodeAccountJson, decodeAccountsJson)
import Debug
import Html.Attributes exposing (class, classList, action, enctype, method, type', name, id, value, for, disabled, selected)
import Html.Events exposing (onInput, onCheck, onSubmit)
import Html exposing (Html, div, text, label, form, input, button, select, option, h1, span)
import Http
import Json.Encode
import String
import Task

type alias Model =
  {   name    : String
    , kind    : Maybe AccountKind
    , virtual : Bool
    , isNew   : Bool
    , saving  : Bool
  }

type Msg  = Name String
          | Kind String
          | Virtual Bool
          | Save
          | SaveFailed Http.Error
          | SaveSucceeded Account

type AccountKind  = Asset
                  | Liability
                  | Equity
                  | Revenue
                  | Expense
                  | Contra

initNew : Model
initNew = { name = "", kind = Just Asset, virtual = False, isNew = True, saving = False }

initExisting : String -> AccountKind -> Bool -> Model
initExisting name kind virtual = { name = name, kind = Just kind, virtual = virtual, isNew = False, saving = False }

update : Msg -> Model -> (Model, Cmd Msg)
update msg model = case msg of
  Name newName     -> ({ model | name = newName }, Cmd.none)
  Kind newKind     -> ({ model | kind = stringToKind newKind }, Cmd.none)
  Virtual newState -> ({ model | virtual = newState }, Cmd.none)
  Save             -> ({ model | saving = True }, putAccount model)
  SaveFailed err   ->
    Debug.crash (toString err)
    (model, Cmd.none)
  SaveSucceeded _ ->
    (initNew, Cmd.none)

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
  True -> "Create Account"
  False -> "Edit Account"

formClasses : Bool -> List (String, Bool)
formClasses saving = [
      ("small-12", True)
    , ("medium-6", True)
    , ("large-12", True)
    , ("columns", True)
    , ("saving", saving)
  ]

view : Model -> (Html Msg)
view model = form [ formClasses model.saving |> classList, onSubmit Save ] [
      h1 [] [ accountFormTitle model |> text ]
    , div [] [
        label [] [
            text "Name"
          , input [type' "text", value model.name, onInput Name, model.saving |> disabled] []
        ]
      , label [] [
          text "Kind"
        , select [ onInput Kind, model.saving |> disabled ] [
              option [ selected (model.kind == Just Asset),     value "asset"     ] [ text "asset" ]
            , option [ selected (model.kind == Just Liability), value "liability" ] [ text "liability" ]
            , option [ selected (model.kind == Just Equity),    value "equity"    ] [ text "equity" ]
            , option [ selected (model.kind == Just Revenue),   value "revenue"   ] [ text "revenue" ]
            , option [ selected (model.kind == Just Expense),   value "expense"   ] [ text "expense" ]
            , option [ selected (model.kind == Just Contra),    value "contra"    ] [ text "contra" ]
          ]
        ]
      , input [type' "checkbox", model.saving |> disabled, id "account_virtual", onCheck Virtual] []
      , label [ for "account_virtual"] [ text "Virtual" ]
    ]
    , button [ class "button button-primary", model.saving |> disabled ] [
      span [class "icon"] [
        saveButtonLabel model |> text
      ]
    ]
  ]

saveButtonLabel : Model -> String
saveButtonLabel model = case (model.isNew, model.saving) of
  (True, False)  -> "Create"
  (True, True)   -> "Creating"
  (False, False) -> "Save"
  (False, True)  -> "Saving"

putAccount : Model -> Cmd Msg
putAccount model = Task.perform SaveFailed SaveSucceeded (Http.post decodeAccountJson "/api/accounts/create" (postAccountBody model))

postAccountBody : Model -> Http.Body
postAccountBody model = Http.multipart [
    Http.stringData "name" model.name
  , Http.stringData "kind" (Debug.log "kindToString = " kindToString (Debug.log "model.kind = " model.kind))
  , Http.stringData "virtual" (model.virtual |> toString |> String.toLower)
  ]

kindToString : Maybe AccountKind -> String
kindToString kind = case kind of
  Just Asset     -> "asset"
  Just Liability -> "liability"
  Just Equity    -> "equity"
  Just Revenue   -> "revenue"
  Just Expense   -> "expense"
  Just Contra    -> "contra"
  Nothing        -> ""

-- postJSON : Json.Encode.Value -> String -> Task.Task Http.RawError Http.Response
-- postJSON body url = Task.perform Http.send Http.defaultSettings {}
