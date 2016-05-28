module Account exposing (Account, decodeAccountJson, decodeAccountsJson)

import Json.Decode exposing (Decoder, list, int, string, bool, maybe, at, (:=), object5)

type alias Account =  {   accountId : Int
                        , name      : String
                        , kind      : String
                        , virtual   : Bool
                        , balance   : Maybe String
                      }

decodeAccountJson : Decoder Account
decodeAccountJson = object5 Account
                      ("id"      := int)
                      ("name"    := string)
                      ("kind"    := string)
                      ("virtual" := bool)
                      ("balance" := maybe string)

decodeAccountsJson : Decoder (List Account)
decodeAccountsJson = at ["data"] (list decodeAccountJson)
