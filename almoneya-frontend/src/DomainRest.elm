module DomainRest exposing (..)

import Domain exposing (..)
import HttpBuilder as Http
import Json.Decode as Decode exposing ((:=))
import Task exposing (..)
import Time


errorsDecoder : Decode.Decoder (List String)
errorsDecoder =
    Decode.at [ "errors" ] (Decode.list Decode.string)


transactionDecoder : Decode.Decoder Transaction
transactionDecoder =
    (Decode.object7 Transaction
        ("id" := Decode.int)
        ("payee" := Decode.string)
        ("description" := Decode.maybe Decode.string)
        ("posted_on" := Decode.string)
        ("booked_at" := Decode.string)
        ("balance" := Decode.string)
        ("entries" := Decode.list transactionEntryDecoder)
    )


transactionsDecoder : Decode.Decoder (List Transaction)
transactionsDecoder =
    Decode.list transactionDecoder


transactionEntryDecoder : Decode.Decoder TransactionEntry
transactionEntryDecoder =
    Decode.object3 TransactionEntry
        ("id" := Decode.int)
        ("amount" := Decode.string)
        ("account" := decodeAccount)


decodeAccount : Decode.Decoder Account
decodeAccount =
    Decode.object6 Account
        ("id" := Decode.maybe Decode.int)
        ("code" := Decode.maybe Decode.string)
        ("name" := Decode.string)
        ("kind" := Decode.string)
        ("balance" := Decode.maybe Decode.string)
        ("virtual" := Decode.bool)


decodeAccounts : Decode.Decoder (List Account)
decodeAccounts =
    (Decode.list decodeAccount)


getAccounts : Task (Http.Error (List String)) (Http.Response (List Account))
getAccounts =
    Http.get "/api/accounts"
        |> Http.withCredentials
        |> Http.withHeader "Accept" "application/json"
        |> Http.withHeader "Content-Type" "application/json"
        |> Http.withTimeout (2 * Time.second)
        |> Http.send (Http.jsonReader (Decode.at [ "data" ] decodeAccounts)) (Http.jsonReader errorsDecoder)


getTransactions : Task (Http.Error (List String)) (Http.Response (List Transaction))
getTransactions =
    Http.get "/api/transactions"
        |> Http.withCredentials
        |> Http.withHeader "Accept" "application/json"
        |> Http.withHeader "Content-Type" "application/json"
        |> Http.withTimeout (2 * Time.second)
        |> Http.send (Http.jsonReader (Decode.at [ "data" ] transactionsDecoder)) (Http.jsonReader errorsDecoder)
