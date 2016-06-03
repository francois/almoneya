package almoneya.automation

import almoneya._
import org.joda.time.LocalDate

case class RecurringObligation(priority: Priority,
                               name: ObligationName,
                               target: Amount,
                               balance: Amount,
                               dueOn: LocalDate,
                               period: Period,
                               every: Every,
                               endOn: Option[LocalDate] = None) extends FundingGoal with DueOnOrAfter {
    override def payoutsOnOrBefore(cutoffOn: LocalDate): Seq[LocalDate] =
        payoutEventsStream.takeWhile(_.compareTo(cutoffOn) <= 0)

    override def startOn: LocalDate = dueOn
}
