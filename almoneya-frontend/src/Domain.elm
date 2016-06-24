module Domain exposing (..)


type alias BankTransactionId =
    Int


type alias Date =
    String


type alias CheckNum =
    String


type alias Description =
    String


type alias BankAccountTransaction =
    { id : Maybe BankTransactionId
    , postedOn : Date
    , desc1 : Description
    , desc2 : Maybe Description
    , amount : Amount
    , checkNum : Maybe CheckNum
    , bankAccount : BankAccount
    , reconciled : Maybe Bool
    }


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
    , postedOn : Date
    , bookedAt : String
    , balance : Maybe String
    , entries : List TransactionEntry
    }


type alias BankAccountId =
    Int


type alias BankAccountHash =
    String


type alias BankAccountLast4 =
    String


type alias BankAccount =
    { id : Maybe BankAccountId
    , hash : BankAccountHash
    , last4 : BankAccountLast4
    }


type alias TransactionEntry =
    { id : Int
    , amount : String
    , account : Account
    }
