package almoneya.automation

import almoneya.{Amount, ObligationName}
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class RevenueShareTest extends FunSuite {
    test("when enough revenue, all payments are fulfilled") {
        val obligations = Set(newMonthlyObligation("rent", 300, new LocalDate(2016, 6, 1)), newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22)))
        val goals = Set(newGoal("save for car", 5000, new LocalDate(2018, 5, 29)))
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val sharer = RevenueShare(obligations, goals, revenues)
        val payments = sharer.generatePayments(new LocalDate(2016, 5, 20), Amount(BigDecimal(700)))
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
