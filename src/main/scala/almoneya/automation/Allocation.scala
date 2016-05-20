package almoneya.automation

import almoneya.Amount
import org.joda.time.LocalDate

case class Allocation(goal: FundingGoal, planToTake: Amount, realTake: Amount = Amount(0)) {
    def fulfilled = realTake >= planToTake

    def dueOn: LocalDate = goal.dueOn

    def priority: Priority = goal.priority

    def amountMissing: Amount = goal.amountMissing
}
