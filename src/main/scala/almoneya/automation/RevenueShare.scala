package almoneya.automation

import almoneya.Amount
import org.joda.time.LocalDate

case class RevenueShare(obligations: Set[RecurringObligation], goals: Set[FixedDateObligation], revenues: Set[Revenue], autoFulfillThreshold: Amount = Amount(BigDecimal(100))) {
    def generatePayments(paidOn: LocalDate, amountReceived: Amount): Seq[Payment] = {
        obligations.toSeq.map(fundingGoal => Payment(fundingGoal, planToTake = fundingGoal.amountMissing, realTake = fundingGoal.amountMissing))
    }
}
