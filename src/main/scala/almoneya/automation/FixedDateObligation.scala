package almoneya.automation

import almoneya.{Priority, Amount, ObligationName}
import org.joda.time.LocalDate

case class FixedDateObligation(priority: Priority,
                               name: ObligationName,
                               target: Amount,
                               balance: Amount,
                               dueOn: LocalDate) extends FundingGoal {
    override def payoutsOnOrBefore(cutoffOn: LocalDate): Seq[LocalDate] =
        if (dueOn == cutoffOn || dueOn.isBefore(cutoffOn)) Seq(dueOn) else Seq.empty
}
