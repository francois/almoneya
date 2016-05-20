package almoneya.automation

import almoneya.Amount
import org.joda.time.LocalDate

case class RevenueShare(obligations: Set[RecurringObligation], goals: Set[FixedDateObligation], revenues: Set[Revenue], autoFulfillThreshold: Amount = Amount(BigDecimal(100))) {
    def generatePayments(paidOn: LocalDate, amountReceived: Amount): Seq[Payment] = {
        val plan = obligations.toSeq.map(fundingGoal => Payment(fundingGoal, planToTake = fundingGoal.amountMissing))
        val runningBalances = amountReceived +: plan.indices.tail.map(idx => amountReceived - plan.slice(0, idx).map(_.planToTake).reduce(_ add _))
        plan.zip(runningBalances).map {
            case (payment, balance) if balance.isPositive && balance >= payment.planToTake => payment.copy(realTake = payment.planToTake)
            case (payment, balance) if balance.isPositive => payment.copy(realTake = balance)
            case (payment, _) => payment
        }
    }
}
