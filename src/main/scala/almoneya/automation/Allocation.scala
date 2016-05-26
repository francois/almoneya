package almoneya.automation

import almoneya.{Amount, Priority}
import org.joda.time.LocalDate

case class Allocation(goal: FundingGoal, planToTake: Amount, realTake: Amount = Amount(0)) extends Comparable[Allocation] {
    override def compareTo(other: Allocation): Int = {
        if (dueOn.compareTo(other.dueOn) == 0) {
            if (priority.compareTo(other.priority) == 0) {
                if (amountMissing.compareTo(other.amountMissing) == 0) {
                    name.toLowerCase.compareTo(other.name.toLowerCase)
                } else {
                    amountMissing.compareTo(other.amountMissing)
                }
            } else {
                priority.compareTo(other.priority)
            }
        } else {
            dueOn.compareTo(other.dueOn)
        }
    }

    def name = goal.name

    def fulfilled = realTake >= planToTake

    def dueOn: LocalDate = goal.dueOn

    def priority: Priority = goal.priority

    def amountMissing: Amount = goal.amountMissing
}
