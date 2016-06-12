module ImportBankAccountTransactionsApp exposing (Model, Msg, init, update, view)

-- import Html.Events exposing ()

import Html exposing (..)
import Html.Attributes exposing (class, type', name, value, action, enctype, method)
import HtmlHelpers exposing (..)


type alias Model =
    { errors : List String }


type Msg
    = Submit


init : ( Model, Cmd Msg )
init =
    ( { errors = [] }, Cmd.none )


update : Msg -> Model -> ( Model, Cmd Msg )
update ev model =
    case ev of
        _ ->
            ( model, Cmd.none )


view : Model -> Html Msg
view model =
    div []
        [ h1 [] [ text "Import Bank Account Transactions" ]
        , viewErrors model.errors
        , form [ method "post", action "/api/bank-account-transactions/import", enctype "multipart/form-data" ]
            [ input [ type' "hidden", name "redirect_to", value "/" ] []
            , label [] [ text "Import file", input [ type' "file", name "file" ] [] ]
            , button [ class "button primary", type' "submit" ] [ text "Upload" ]
            ]
        ]
