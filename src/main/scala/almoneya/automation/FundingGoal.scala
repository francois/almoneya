package almoneya.automation

import almoneya.{Amount, ObligationName}
import org.joda.time.LocalDate

trait FundingGoal {
    def numberOfPayoutsBefore(date: LocalDate): Int

    def name: ObligationName

    def priority: Priority

    def dueOn: LocalDate

    def balance: Amount

    def target: Amount

    def fulfilled = balance > target

    def amountMissing = target - balance
}
