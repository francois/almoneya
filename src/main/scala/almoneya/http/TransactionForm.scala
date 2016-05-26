package almoneya.http

import almoneya.{Description, Payee}
import org.joda.time.LocalDate

case class TransactionForm(payee:Payee,
                           description:Option[Description],
                           postedOn:LocalDate,
                           entries:Set[TransactionEntryForm])
