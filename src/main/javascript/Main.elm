import Html exposing (..)
import Html.App as App
import Html.Attributes exposing (..)

type alias Model = {}

type Msg = None

main = App.program { init = init, update = update, view = view, subscriptions = subscriptions }

init : (Model, Cmd Msg)
init = ({}, Cmd.none)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model = (model, Cmd.none)

view : Model -> (Html Msg)
view model = div [class "expanded row"] [
  div [class "large-3 medium-4 columns show-for-medium menubar"] [
    h1 [] [ text "Almoneya" ]
    , h2 [] [ text "Operations" ]
    , ul [class "menu vertical"] [
        li [class "active"] [ a [href "#"] [ i [ class "fi-page-csv" ] [], text " Import ", span [class "hide"] [text "Bank Transactions"]] ]
      , li [] [ a [href "#"] [i [class "fi-calendar"] [], text " Reconcile", span [class "hide"] [text "Bank Transactions"]] ]
      , li [] [ a [href "#"] [i [class "fi-ticket"] [], text " Record Check"] ]
      , li [] [ a [href "#"] [i [class "fi-clipboard-pencil"] [], text " Record Expense"] ]
      , li [] [ a [href "#"] [i [class "fi-clipboard-notes"] [], text " Record Revenue"] ]
      ]
      , h2 [] [text "Reports"]
      , ul [class "menu vertical"] [
          li [] [ a [href "#"] [i [class "fi-graph-trend"] [], text " Next Obligations"] ]
        , li [] [ a [href "#"] [i [class "fi-calendar"] [], text " Obligations"] ]
        , li [] [ a [href "#"] [i [class "fi-calendar"] [], text " Goals"] ]
        , li [] [ a [href "#"] [i [class "fi-book"] [], text " Transactions"] ]
        , li [] [ a [href "#"] [i [class "fi-map"] [], text " Accounts"] ]
        , li [] [ a [href "#"] [i [class "fi-clipboard"] [], text " Bank Transactions"] ]
        ]
      , h2 [] [text "Utilities"]
      , ul [class "menu vertical"] [
          li [] [ a [href "#"] [i [class "fi-telephone"] [], text " Need help?"] ]
        , li [] [ a [href "#"] [i [class "fi-widget"] [], text " Settings"] ]
        ]
    ]
    , div [class "large-9 medium-8 columns"] [
      h1 [] [text "Content"]
    ]
  ]

subscriptions : Model -> Sub Msg
subscriptions model = Sub.none
