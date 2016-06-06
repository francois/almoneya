module RecordRevenueApp exposing (Model, Msg, init, update, view)

import Html.Attributes exposing (type', name, placeholder, value, class, classList, disabled, selected)
import Html.Events exposing (onSubmit, onInput)
import Html exposing (..)
import Http
import Json.Decode as Decode exposing ((:=))
import Json.Encode as Encode
import String
import Task exposing (..)

type alias Model = { revenueAccountName : AccountName
                   , bankAccountAccountName : AccountName
                   , receivedOn : String
                   , payee : String
                   , amount : String
                   , saving : Bool
                   , errors : List String
                   , accounts : List Account
                 }

type Msg  = ChangeRevenueName String
          | ChangeBankAccountAccountName String
          | ChangeReceivedOn String
          | ChangeAmount String
          | ChangePayee String
          | Submit
          | SaveOk String
          | SaveFailed Http.Error
          | AccountsOk (List Account)
          | AccountsFailed Http.Error

init : (Model, Cmd Msg)
init = ({ revenueAccountName = ""
        , bankAccountAccountName = ""
        , receivedOn = ""
        , payee = ""
        , amount = ""
        , saving = False
        , errors = []
        , accounts = []
      }, Task.perform AccountsFailed AccountsOk getAccounts)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model = case msg of
  ChangeRevenueName newValue            -> ({model | revenueAccountName = newValue}, Cmd.none)
  ChangeBankAccountAccountName newValue -> ({model | bankAccountAccountName = newValue}, Cmd.none)
  ChangeReceivedOn newValue             -> ({model | receivedOn = newValue}, Cmd.none)
  ChangePayee newValue                  -> ({model | payee = newValue}, Cmd.none)
  ChangeAmount newValue                 -> ({model | amount = newValue}, Cmd.none)
  Submit                                -> ({model | saving = True}, Task.perform SaveFailed SaveOk (submit model))
  SaveOk transaction                    -> ({model | saving = False}, Cmd.none)
  SaveFailed errors                     -> ({model | saving = False}, Cmd.none)
  AccountsOk accounts                   -> ({model | accounts = accounts}, Cmd.none)
  AccountsFailed errors                 ->
    Debug.log (toString errors)
    Debug.log "AccountsFailed" (model, Cmd.none)

viewError : String -> Html Msg
viewError msg = li [] [ text msg ]

viewErrors : Model -> Html Msg
viewErrors model =  if List.isEmpty model.errors then
                       div [] []
                    else
                      div [class "callout warning"]
                      [ h5 [] [ text "Validation failures prevented this form from saving" ]
                      , List.map viewError model.errors |> ul []
                      ]

accountOption : AccountName -> AccountName -> Html Msg
accountOption selectedAccount name = option [ selected (selectedAccount == name) ] [ text name ]

accountOptions : AccountKind -> AccountName -> List Account -> List (Html Msg)
accountOptions kind selectedName accounts =
  let candidateAccounts = List.filter (\x -> (x.virtual == False) && (x.kind == kind)) accounts
      names       = List.map (\x -> x.name) candidateAccounts
      sortedNames = List.sortBy String.toLower names
      options     = List.map (accountOption selectedName) sortedNames
  in options

revenueAccountOptions : AccountName -> List Account -> List (Html Msg)
revenueAccountOptions = accountOptions "asset"

bankAccountOptions : AccountName -> List Account -> List (Html Msg)
bankAccountOptions = accountOptions "revenue"

view : Model -> Html Msg
view model = div []
    [ h1 [] [ text "Record Revenue" ]
    , form [ classList [("saving", model.saving)], onSubmit Submit ]
      [ viewErrors model
      , label [] [ text "Payee", input [ type' "text", name "revenue[payee]", placeholder "ACME Corp.", value model.payee, onInput ChangePayee ] [] ]
      , label [] [ text "Received on", input [ type' "text", name "revenue[received_on]", placeholder "2016-06-09", value model.receivedOn, onInput ChangeReceivedOn ] [] ]
      , label [] [ text "Amount", input [ type' "text", name "revenue[amount]", placeholder "1031.78", value model.amount, onInput ChangeAmount ] [] ]
      , label [] [ text "Bank Account Account Name", select [ name "revenue[bank_account_account_name]", onInput ChangeBankAccountAccountName ] (revenueAccountOptions model.bankAccountAccountName model.accounts) ]
      , label [] [ text "Revenue Account Name", select [ name "revenue[revenue_account_name]", onInput ChangeRevenueName ] (bankAccountOptions model.revenueAccountName model.accounts) ]
      , button [ type' "submit", class "button primary", disabled model.saving ] [ i [class "icon"] [], text "Record" ]
    ]
  ]

modelToValue : Model -> Encode.Value
modelToValue model =
  Encode.object
    [ ("revenue_account_name", Encode.string model.revenueAccountName)
    , ("bank_account_account_name", Encode.string model.bankAccountAccountName)
    , ("received_on", Encode.string model.receivedOn)
    , ("payee", Encode.string model.payee)
    , ("amount", Encode.string model.amount)
    ]

type Either a b = Left a
                | Right b

submit : Model -> Task Http.Error String
submit model =
  let body     = modelToValue model |> Encode.encode 0 |> Http.string
      url      = "/api/revenues/create"
      decoder  = Decode.string
      defaultSettings = Http.defaultSettings
      settings = { defaultSettings | desiredResponseType = Just "application/json", withCredentials = True }
      request  = { verb = "POST", headers = [ ("Content-Type", "application/json;charset=utf-8") ], url = url, body = body }
  in Http.send settings request |> Http.fromJson decoder

type alias AccountId   = Int
type alias AccountCode = String
type alias AccountName = String
type alias AccountKind = String
type alias Amount      = String

type alias Account =  { id      : AccountId
                      , code    : Maybe AccountCode
                      , name    : AccountName
                      , kind    : AccountKind
                      , balance : Maybe Amount
                      , virtual : Bool }

decodeAccount : Decode.Decoder Account
decodeAccount = Decode.object6 Account
                  ("id"      := Decode.int)
                  ("code"    := Decode.maybe Decode.string)
                  ("name"    := Decode.string)
                  ("kind"    := Decode.string)
                  ("balance" := Decode.maybe Decode.string)
                  ("virtual" := Decode.bool)

decodeAccounts : Decode.Decoder (List Account)
decodeAccounts = Decode.at ["data"] (Decode.list decodeAccount)

getAccounts : Task Http.Error (List Account)
getAccounts = Http.get decodeAccounts "/api/accounts"
