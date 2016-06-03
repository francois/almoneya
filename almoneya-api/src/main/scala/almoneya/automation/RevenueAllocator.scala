package almoneya.automation

import almoneya.{Amount, _}
import org.joda.time.LocalDate

case class RevenueAllocator(obligations: Set[FundingGoal], revenues: Set[Revenue], autoFulfillThreshold: Amount = Amount(BigDecimal(100))) {
    if (obligations.isEmpty) throw new IllegalArgumentException("Expected to have at least one obligation, else the calculations are meaningless")
    if (revenues.isEmpty) throw new IllegalArgumentException("Expected to have at least one revenue source, else the calculations are incorrect")

    def generatePlan(paidOn: LocalDate, amountReceived: Amount): Seq[Allocation] = {
        val nextRevenueOn = revenues.flatMap(_.dueOnAfter(paidOn)).toSeq.sorted.headOption
        val plan = obligations.toSeq.filterNot(_.fulfilled).map {
            case fundingGoal if nextRevenueOn.exists(_.isBefore(fundingGoal.dueOn)) =>
                // We have more than one revenue event before the payout is due:
                // plan to take an amount that is equally divided between this revenue event
                // all future events
                val numRevenueEvents = numberOfRevenueEventsBetween(paidOn, fundingGoal.dueOn)
                Allocation(fundingGoal, planToTake = fundingGoal.amountMissing / numRevenueEvents)

            case fundingGoal if nextRevenueOn.isDefined =>
                // We may have to fund more than one payout before the next revenue event,
                // hence, take what we need to cover multiple payouts of this expense
                val numPayoutEvents = fundingGoal.numberOfPayoutsOnOrBefore(nextRevenueOn.get)
                Allocation(fundingGoal, planToTake = fundingGoal.amountMissing * numPayoutEvents)

            case fundingGoal =>
                // We don't know when we will have other revenue, thus we can only take
                // what's missing and hope for the best
                Allocation(fundingGoal, planToTake = fundingGoal.amountMissing)
        }.sorted

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

    private[this] def numberOfRevenueEventsBetween(paidOn: LocalDate, cutoffOn: LocalDate): Int = {
        assert(paidOn.isBefore(cutoffOn))
        val nextDatesByRevenue = for (revenue <- revenues) yield {
            val x1 = revenue.revenueEventsStream.takeWhile(_.compareTo(cutoffOn) <= 0)
            val x2 = x1.dropWhile(_.isBefore(paidOn))
            val x3 = x2.toList
            x3
        }
        val nextRevenueDates = nextDatesByRevenue.toSeq
        nextRevenueDates.flatten.size
    }
}
