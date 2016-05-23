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
    override def numberOfPayoutsBefore(date: LocalDate): Int = payoutEventsStream.takeWhile(_.isBefore(date)).size

    override def startOn: LocalDate = dueOn
}
