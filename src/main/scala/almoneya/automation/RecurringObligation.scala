package almoneya.automation

import almoneya.{Period, Every, Amount, ObligationName}
import org.joda.time.LocalDate

case class RecurringObligation(priority: Priority,
                               name: ObligationName,
                               target: Amount,
                               balance: Amount,
                               dueOn: LocalDate,
                               period: Period,
                               every: Every,
                               endOn: Option[LocalDate] = None) extends FundingGoal
