package almoneya.http

import almoneya.{AccountName, Amount}

case class TransactionEntryForm(accountName: AccountName,
                                amount: Amount)
