package almoneya.automation

import almoneya.{Amount, ObligationName}
import org.joda.time.LocalDate

case class FixedDateObligation(priority: Priority,
                               name: ObligationName,
                               target: Amount,
                               balance: Amount,
                               dueOn: LocalDate) extends FundingGoal
