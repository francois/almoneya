package almoneya.http

import almoneya.{BankAccountTransactionId, Description, Payee}
import org.joda.time.LocalDate

case class TransactionForm(payee: Payee,
                           description: Option[Description],
                           postedOn: LocalDate,
                           entries: Set[TransactionEntryForm],
                           bankAccountTransactionId: Option[BankAccountTransactionId])
