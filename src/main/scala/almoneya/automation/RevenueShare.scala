package almoneya.automation

import almoneya.Amount
import org.joda.time.{Interval, LocalDate, PeriodType}

case class RevenueShare(obligations: Set[RecurringObligation], goals: Set[FixedDateObligation], revenues: Set[Revenue], autoFulfillThreshold: Amount = Amount(BigDecimal(100))) {
    def generatePayments(paidOn: LocalDate, amountReceived: Amount): Seq[Payment] = {
        val fundingGoals = (obligations.asInstanceOf[Set[FundingGoal]] ++ goals.asInstanceOf[Set[FundingGoal]]).toSeq.sortWith { case (a, b) =>
            val aDaysInInterval = new Interval(paidOn.toDateTimeAtStartOfDay, a.dueOn.toDateTimeAtStartOfDay).toPeriod(PeriodType.days()).getDays
            val bDaysInInterval = new Interval(paidOn.toDateTimeAtStartOfDay, b.dueOn.toDateTimeAtStartOfDay).toPeriod(PeriodType.days()).getDays
            if (aDaysInInterval.compare(bDaysInInterval) < 0) {
                true
            } else {
                val aPriority = a.priority
                val bPriority = b.priority
                if (aPriority.compare(bPriority) < 0) {
                    true
                } else {
                    val aAmountMissing = a.amountMissing
                    val bAmountMissing = b.amountMissing
                    if (aAmountMissing.compare(bAmountMissing) < 0) {
                        true
                    } else {
                        val aName = a.name.toLowerCase
                        val bName = b.name.toLowerCase
                        aName.compare(bName) < 0
                    }
                }
            }
        }

        val unfulfilledFundingGoals = fundingGoals.filterNot(_.fulfilled)
        System.err.println(unfulfilledFundingGoals.map(_.toString).mkString("\n"))
        val plan = unfulfilledFundingGoals.map { case target =>
            val revenueEvents = for (revenue <- revenues;
                                     event <- revenue.revenueEventsStream if event.isBefore(target.dueOn) && event.isAfter(paidOn))
                yield event
            revenueEvents.size match {
                case 0 => Payment(target, target.amountMissing)
                case _ if target.amountMissing < autoFulfillThreshold => Payment(target, target.amountMissing)
                case n => Payment(target, target.amountMissing / n)
            }
        }

        System.err.println(plan.map(_.toString).mkString("\n"))
        val amountsPlannedToTake = plan.map(_.planToTake)
        System.err.println(amountsPlannedToTake.map(_.toString).mkString("\n"))
        val runningSums = plan.indices.map(maxIndex => amountsPlannedToTake.slice(0, maxIndex + 1)).map(_.reduce((a, b) => a add b))
        System.err.println(runningSums.map(_.toString).mkString("\n"))
        plan.zip(runningSums).map {
            case (payment, runningSum) if payment.planToTake < runningSum => payment.copy(realTake = payment.planToTake)
            case (payment, _) => payment.copy(realTake = Amount(0))
        }
    }
}
