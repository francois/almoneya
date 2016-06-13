module Domain exposing (..)


type alias Account =
    { id : Maybe AccountId
    , code : Maybe AccountCode
    , name : AccountName
    , kind : AccountKind
    , balance : Maybe Amount
    , virtual : Bool
    }


type alias AccountId =
    Int


type alias AccountCode =
    String


type alias AccountName =
    String


type alias AccountKind =
    String


type alias Amount =
    String


type alias Transaction =
    { id : Int
    , payee : String
    , description : Maybe String
    , postedOn : String
    , bookedAt : String
    , balance : Maybe String
    , entries : List TransactionEntry
    }


type alias TransactionEntry =
    { id : Int
    , amount : String
    , account : Account
    }
