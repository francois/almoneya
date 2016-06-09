module HtmlHelpers exposing (..)

import Html.Attributes exposing (class)
import Html exposing (Html, ul, li, div, h5, text)
import List exposing (isEmpty, map)


viewError : String -> Html a
viewError msg =
    li [] [ text msg ]


viewErrors : List String -> Html a
viewErrors errors =
    if isEmpty errors then
        div [] []
    else
        div [ class "callout warning" ]
            [ h5 [] [ text "Validation failures prevented this form from saving" ]
            , map viewError errors |> ul []
            ]
