package almoneya

import org.joda.time.DateTime

case class TransactionEntry(transactionEntryId: Option[TransactionEntryId] = None,
                            account: Account,
                            amount: Amount,
                            createdAt: DateTime = new DateTime,
                            updatedAt: DateTime = new DateTime) {
    def accountName: AccountName = account.name
}
