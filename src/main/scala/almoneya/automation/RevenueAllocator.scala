package almoneya.automation

import almoneya.Amount
import org.joda.time.LocalDate

case class RevenueAllocator(obligations: Set[FundingGoal], revenues: Set[Revenue], autoFulfillThreshold: Amount = Amount(BigDecimal(100))) {
    def generatePlan(paidOn: LocalDate, amountReceived: Amount): Seq[Allocation] = {
        val plan = obligations.toSeq.filterNot(_.fulfilled).map {
            case fundingGoal if numberOfRevenueEventsBetween(paidOn, fundingGoal.dueOn) <= 1 => Allocation(fundingGoal, planToTake = fundingGoal.amountMissing)
            case fundingGoal => Allocation(fundingGoal, planToTake = fundingGoal.amountMissing / numberOfRevenueEventsBetween(paidOn, fundingGoal.dueOn))
        }.sortWith((a, b) => a.dueOn.compareTo(b.dueOn) < 0 || a.priority.compareTo(b.priority) < 0 || a.amountMissing.compareTo(b.amountMissing) < 0 || a.name.compareTo(b.name) < 0)

        if (plan.isEmpty) {
            Seq.empty[Allocation]
        } else {
            val runningBalances = amountReceived +: plan.indices.tail.map(idx => amountReceived - plan.slice(0, idx).map(_.planToTake).reduce(_ add _))
            plan.zip(runningBalances).map {
                case (payment, balance) if balance.isPositive && balance >= payment.planToTake && payment.goal.amountMissing <= autoFulfillThreshold =>
                    payment.copy(planToTake = payment.goal.amountMissing, realTake = payment.goal.amountMissing)

                case (payment, balance) if balance.isPositive && balance >= payment.planToTake => payment.copy(realTake = payment.planToTake)
                case (payment, balance) if balance.isPositive => payment.copy(realTake = balance)
                case (payment, _) => payment
            }
        }
    }

    def numberOfRevenueEventsBetween(paidOn: LocalDate, cutoffOn: LocalDate): Int = {
        // We CANNOT replace filter+size with count: revenueEventsStream returns a Stream, which may be infinite in size!
        revenues.map(_.revenueEventsStream.map(_.toDateTimeAtStartOfDay).takeWhile(_.isBefore(cutoffOn.toDateTimeAtStartOfDay)).filter(_.isAfter(paidOn.toDateTimeAtStartOfDay)).size).sum
    }
}
