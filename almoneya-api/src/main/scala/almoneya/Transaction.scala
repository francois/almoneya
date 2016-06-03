package almoneya

import org.joda.time.{DateTime, LocalDate}

case class Transaction(transactionId: Option[TransactionId] = None,
                       payee: Payee,
                       description: Option[Description] = None,
                       postedOn: LocalDate,
                       bookedAt: DateTime = new DateTime,
                       entries: Set[TransactionEntry] = Set.empty[TransactionEntry])
