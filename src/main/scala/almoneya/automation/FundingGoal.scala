package almoneya.automation

import almoneya.{Amount, ObligationName, Priority}
import org.joda.time.LocalDate

trait FundingGoal {
    def payoutsOnOrBefore(cutoffOn: LocalDate): Seq[LocalDate]

    def numberOfPayoutsOnOrBefore(cutoffOn: LocalDate): Int = payoutsOnOrBefore(cutoffOn).size

    def name: ObligationName

    def priority: Priority

    def dueOn: LocalDate

    def balance: Amount

    def target: Amount

    def fulfilled = balance > target

    def amountMissing = target - balance
}
