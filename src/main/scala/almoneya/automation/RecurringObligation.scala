package almoneya.automation

import almoneya.{Amount, ObligationName}
import org.joda.time.LocalDate

case class RecurringObligation(priority: Priority,
                               name: ObligationName,
                               target: Amount,
                               balance: Amount,
                               dueOn: LocalDate,
                               period: Period,
                               frequency: Frequency,
                               endOn: Option[LocalDate] = None) extends FundingGoal {
}
