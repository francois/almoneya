module RecordRevenueApp exposing (Model, Msg, init, update, view)

import Http
import Html.Attributes exposing (type', name, placeholder, value, class, classList, disabled)
import Html.Events exposing (onSubmit, onInput)
import Html exposing (..)
import Json.Encode as Encode
import Json.Decode as Decode
import Task exposing (..)

type alias Model = { revenueAccountName : String
                   , bankAccountAccountName : String
                   , receivedOn : String
                   , payee : String
                   , amount : String
                   , saving : Bool
                   , errors : List String
                 }

type Msg  = ChangeRevenueName String
          | ChangeBankAccountAccountName String
          | ChangeReceivedOn String
          | ChangeAmount String
          | ChangePayee String
          | Submit
          | SaveOk String
          | SaveFailed Http.Error

init : (Model, Cmd Msg)
init = ({ revenueAccountName = ""
        , bankAccountAccountName = ""
        , receivedOn = ""
        , payee = ""
        , amount = ""
        , saving = False
        , errors = ["Unknown revenue account name"]
      }, Cmd.none)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model = case msg of
  ChangeRevenueName newValue            -> ({model | revenueAccountName = newValue}, Cmd.none)
  ChangeBankAccountAccountName newValue -> ({model | bankAccountAccountName = newValue}, Cmd.none)
  ChangeReceivedOn newValue             -> ({model | receivedOn = newValue}, Cmd.none)
  ChangePayee newValue                  -> ({model | payee = newValue}, Cmd.none)
  ChangeAmount newValue                 -> ({model | amount = newValue}, Cmd.none)
  Submit                                -> Debug.log "submit" ({model | saving = True}, Task.perform SaveFailed SaveOk (submit model))
  SaveOk transaction                    -> ({model | saving = False}, Cmd.none)
  SaveFailed errors                     -> ({model | saving = False}, Cmd.none)

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

view : Model -> Html Msg
view model = div []
    [ h1 [] [ text "Record Revenue" ]
    , form [ classList [("saving", model.saving)], onSubmit Submit ]
      [ viewErrors model
      , label [] [ text "Payee", input [ type' "text", name "revenue[payee]", placeholder "ACME Corp.", value model.payee, onInput ChangePayee ] [] ]
      , label [] [ text "Received on", input [ type' "text", name "revenue[received_on]", placeholder "2016-06-09", value model.receivedOn, onInput ChangeReceivedOn ] [] ]
      , label [] [ text "Amount", input [ type' "text", name "revenue[amount]", placeholder "1031.78", value model.amount, onInput ChangeAmount ] [] ]
      , label [] [ text "Bank Account Account Name", input [ type' "text", name "revenue[bank_account_account_name]", placeholder "Checking", value model.bankAccountAccountName, onInput ChangeBankAccountAccountName ] [] ]
      , label [] [ text "Revenue Account Name", input [ type' "text", name "revenue[revenue_account_name]", placeholder "Partner Salary", value model.revenueAccountName, onInput ChangeRevenueName ] [] ]
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
