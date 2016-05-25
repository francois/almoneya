package almoneya.automation

import almoneya.{Priority, Amount, ObligationName}
import org.joda.time.LocalDate

case class FixedDateObligation(priority: Priority,
                               name: ObligationName,
                               target: Amount,
                               balance: Amount,
                               dueOn: LocalDate) extends FundingGoal {
    override def numberOfPayoutsBefore(date: LocalDate): Int = if (date.isBefore(dueOn)) 0 else 1
}
