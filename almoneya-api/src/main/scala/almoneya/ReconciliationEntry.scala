package almoneya

import org.joda.time.LocalDate

case class ReconciliationEntry(id: Option[ReconciliationEntryId] = None,
                               transactionId: TransactionId,
                               postedOn: LocalDate,
                               accountName: AccountName)
