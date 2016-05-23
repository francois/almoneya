package almoneya.automation

import almoneya.{Amount, ObligationName}
import org.joda.time.LocalDate

case class RevenueAllocator(obligations: Set[FundingGoal], revenues: Set[Revenue], autoFulfillThreshold: Amount = Amount(BigDecimal(100))) {
    def generatePlan(paidOn: LocalDate, amountReceived: Amount): Seq[Allocation] = {
        val plan = obligations.toSeq.filterNot(_.fulfilled).map {
            case fundingGoal if numberOfRevenueEventsBetween(fundingGoal.name, paidOn, fundingGoal.dueOn) <= 1 =>
                Allocation(fundingGoal, planToTake = fundingGoal.amountMissing)
            case fundingGoal =>
                Allocation(fundingGoal, planToTake = fundingGoal.amountMissing / numberOfRevenueEventsBetween(fundingGoal.name, paidOn, fundingGoal.dueOn))
        }.sortWith((a, b) => a.dueOn.compareTo(b.dueOn) < 0 || a.priority.compareTo(b.priority) < 0 || a.amountMissing.compareTo(b.amountMissing) < 0 || a.name.compareTo(b.name) < 0)

        if (plan.isEmpty) {
            Seq.empty[Allocation]
        } else {
            val runningBalances = amountReceived +: plan.indices.tail.map(idx => amountReceived - plan.slice(0, idx).map(_.planToTake).reduce(_ add _))
            plan.zip(runningBalances).map {
                case (payment, balance) if balance >= payment.planToTake && payment.goal.amountMissing <= autoFulfillThreshold =>
                    payment.copy(planToTake = payment.goal.amountMissing, realTake = payment.goal.amountMissing)

                case (payment, balance) if balance >= payment.planToTake =>
                    payment.copy(realTake = payment.planToTake)

                case (payment, balance) if balance.isPositive =>
                    payment.copy(realTake = balance)

                case (payment, balance) =>
                    payment
            }
        }
    }

    private[this] def numberOfRevenueEventsBetween(goalName: ObligationName, paidOn: LocalDate, cutoffOn: LocalDate): Int = {
        assert(paidOn.isBefore(cutoffOn))
        val nextDatesByRevenue = for (revenue <- revenues) yield {
            val x1 = revenue.revenueEventsStream.takeWhile(_.isBefore(cutoffOn))
            val x2 = x1.dropWhile(_.isBefore(paidOn))
            val x3 = x2.toList
            x3
        }
        val nextRevenueDates = nextDatesByRevenue.toSeq
        nextRevenueDates.flatten.size
    }
}
