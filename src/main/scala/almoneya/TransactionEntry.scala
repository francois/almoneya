package almoneya

case class TransactionEntry(transactionEntryId: Option[TransactionEntryId] = None,
                            account: Account,
                            amount: Amount) {
    def accountName: AccountName = account.name
}
