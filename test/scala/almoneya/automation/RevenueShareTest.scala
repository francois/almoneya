package almoneya.automation

import almoneya.{Amount, ObligationName}
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class RevenueShareTest extends FunSuite {
    test("with a single recurring obligation of 100$ and a revenue of 200$, the payment is fulfilled") {
        val obligations = Set(newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22)))
        val goals = Set.empty[FixedDateObligation]
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val sharer = RevenueShare(obligations, goals, revenues)
        val payments = sharer.generatePayments(new LocalDate(2016, 5, 20), Amount(BigDecimal(200)))
        assert(payments.size == 1, "one obligation == one payment")
        assert(payments.forall(_.fulfilled), payments)
    }

    def newWeeklyRevenue(name: String, dueOn: LocalDate): Revenue = {
        Revenue(RevenueName(name), dueOn, Weekly, Frequency(1))
    }

    def newGoal(name: String, targetAmount: Int, dueOn: LocalDate): FixedDateObligation = {
        FixedDateObligation(priority = Priority(1), name = ObligationName(name), target = Amount(BigDecimal(targetAmount)), balance = Amount(BigDecimal(0)), dueOn = dueOn)
    }

    def newWeeklyObligation(name: String, targetAmount: Int, dueOn: LocalDate): RecurringObligation = {
        RecurringObligation(priority = Priority(1), name = ObligationName(name), target = Amount(BigDecimal(targetAmount)), balance = Amount(BigDecimal(0)), dueOn = dueOn, period = Weekly, frequency = Frequency(1))
    }

    def newMonthlyObligation(name: String, targetAmount: Int, dueOn: LocalDate): RecurringObligation = {
        RecurringObligation(priority = Priority(1), name = ObligationName(name), target = Amount(BigDecimal(targetAmount)), balance = Amount(BigDecimal(0)), dueOn = dueOn, period = Monthly, frequency = Frequency(1))
    }
}
