package almoneya.automation

import almoneya.{Amount, ObligationName}
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class RevenueAllocatorTest extends FunSuite {
    test("with a single recurring obligation of 100$ and a revenue of 200$, the payment is fulfilled") {
        val obligations = Set(newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22)))
        val goals = Set.empty[FixedDateObligation]
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val sharer = RevenueAllocator(obligations, goals, revenues)
        val payments = sharer.generatePlan(new LocalDate(2016, 5, 20), Amount(BigDecimal(200)))
        assert(payments.size == 1, "one obligation == one payment")
        assert(payments.forall(_.fulfilled), payments)
    }

    test("with two recurring obligations of 100 and a revenue of 200") {
        val obligations = Set(newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22)), newWeeklyObligation("alimony", 100, new LocalDate(2016, 5, 23)))
        val goals = Set.empty[FixedDateObligation]
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val sharer = RevenueAllocator(obligations, goals, revenues)
        val payments = sharer.generatePlan(new LocalDate(2016, 5, 20), Amount(BigDecimal(200)))
        assert(payments.size == obligations.size, "N obligations == N payments")
        assert(payments.forall(_.fulfilled), payments)
    }

    test("with two recurring obligations of 100 but a revenue of 150") {
        val groceries = newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 23))
        val alimony = newWeeklyObligation("alimony", 100, new LocalDate(2016, 5, 23))
        val obligations = Set(groceries, alimony)
        val goals = Set.empty[FixedDateObligation]
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val sharer = RevenueAllocator(obligations, goals, revenues)
        val payments = sharer.generatePlan(new LocalDate(2016, 5, 20), Amount(BigDecimal(150)))

        assert(payments.find(_.goal == groceries).exists(_.fulfilled), "groceries must be fulfilled")
        assert(!payments.find(_.goal == alimony).forall(_.fulfilled), "alimoney must be unfulfilled")
    }

    test("prioritizes obligations that will be due sooner rather than later") {
        val groceries = newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22))
        val alimony = newWeeklyObligation("alimony", 200, new LocalDate(2016, 5, 25))
        // this is a BAD test: reversing the order of obligations in the Set declaration fails the test
        // At the moment, this is the only way I have of checking the prioritisation of obligations
        val goals = Set.empty[FixedDateObligation]
        val obligations = Set(alimony, groceries)
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val sharer = RevenueAllocator(obligations, goals, revenues)
        val payments = sharer.generatePlan(new LocalDate(2016, 5, 20), Amount(BigDecimal(50)))

        assert(payments.contains(Payment(groceries, planToTake = Amount(BigDecimal(100)), realTake = Amount(BigDecimal(50)))), "groceries received 100% of the money allocation")
        assert(!payments.find(_.goal == alimony).forall(_.fulfilled), "alimoney must be unfulfilled")
    }

    test("prioritizes obligations with lower priority") {
        val groceries = newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22))
        val alimony = newWeeklyObligation("alimony", 200, new LocalDate(2016, 5, 22), priority = 1)
        val goals = Set.empty[FixedDateObligation]
        val obligations = Set(groceries, alimony)
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val sharer = RevenueAllocator(obligations, goals, revenues)
        val payments = sharer.generatePlan(new LocalDate(2016, 5, 20), Amount(BigDecimal(201)))

        assert(payments.contains(Payment(alimony, planToTake = Amount(BigDecimal(200)), realTake = Amount(BigDecimal(200)))), "alimony received 99% of the allocation")
        assert(payments.contains(Payment(groceries, planToTake = Amount(BigDecimal(100)), realTake = Amount(BigDecimal(1)))), "groceries received 1% of the allocation")
    }

    def newWeeklyRevenue(name: String, dueOn: LocalDate): Revenue = {
        Revenue(RevenueName(name), dueOn, Weekly, Frequency(1))
    }

    def newGoal(name: String, targetAmount: Int, dueOn: LocalDate): FixedDateObligation = {
        FixedDateObligation(priority = Priority(1), name = ObligationName(name), target = Amount(BigDecimal(targetAmount)), balance = Amount(BigDecimal(0)), dueOn = dueOn)
    }

    def newWeeklyObligation(name: String, targetAmount: Int, dueOn: LocalDate, priority: Int = 100): RecurringObligation = {
        RecurringObligation(priority = Priority(priority), name = ObligationName(name), target = Amount(BigDecimal(targetAmount)), balance = Amount(BigDecimal(0)), dueOn = dueOn, period = Weekly, frequency = Frequency(1))
    }

    def newMonthlyObligation(name: String, targetAmount: Int, dueOn: LocalDate): RecurringObligation = {
        RecurringObligation(priority = Priority(1), name = ObligationName(name), target = Amount(BigDecimal(targetAmount)), balance = Amount(BigDecimal(0)), dueOn = dueOn, period = Monthly, frequency = Frequency(1))
    }
}
