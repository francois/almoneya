package almoneya.automation

import almoneya.Amount
import org.joda.time.{Interval, LocalDate}

case class RevenueAllocator(obligations: Set[RecurringObligation], goals: Set[FixedDateObligation], revenues: Set[Revenue], autoFulfillThreshold: Amount = Amount(BigDecimal(100))) {
    def generatePlan(paidOn: LocalDate, amountReceived: Amount): Seq[Payment] = {
        val plan = obligations.toSeq.map {
            case fundingGoal if numberOfRevenueEventsBetween(paidOn, fundingGoal.dueOn) <= 1 => Payment(fundingGoal, planToTake = fundingGoal.amountMissing)
            case fundingGoal => Payment(fundingGoal, planToTake = fundingGoal.amountMissing / numberOfRevenueEventsBetween(paidOn, fundingGoal.dueOn))
        }.sortWith((a, b) => a.goal.dueOn.compareTo(b.goal.dueOn) < 0 || a.goal.priority.compareTo(b.goal.priority) < 0)
        val runningBalances = amountReceived +: plan.indices.tail.map(idx => amountReceived - plan.slice(0, idx).map(_.planToTake).reduce(_ add _))
        plan.zip(runningBalances).map {
            case (payment, balance) if balance.isPositive && balance >= payment.planToTake => payment.copy(realTake = payment.planToTake)
            case (payment, balance) if balance.isPositive => payment.copy(realTake = balance)
            case (payment, _) => payment
        }
    }

    def numberOfRevenueEventsBetween(paidOn: LocalDate, cutoffOn: LocalDate): Int = {
        // We CANNOT replace filter+size with count: revenueEventsStream returns a Stream, which may be infinite in size!
        revenues.map(_.revenueEventsStream.map(_.toDateTimeAtStartOfDay).takeWhile(_.isBefore(cutoffOn.toDateTimeAtStartOfDay)).filter(_.isAfter(paidOn.toDateTimeAtStartOfDay)).size).sum
    }
}
