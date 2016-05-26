package almoneya.automation

import almoneya._
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class RevenueAllocatorTest extends FunSuite {
    test("with a single recurring goal of 100$ and a revenue of 200$, the payment is fulfilled") {
        val goals = Set(newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22)))
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val allocator = RevenueAllocator(goals, revenues)
        val allocations = allocator.generatePlan(new LocalDate(2016, 5, 20), amount(200))
        assert(allocations.size == 1, "one goal == one allocation")
        assert(allocations.forall(_.fulfilled), allocations)
    }

    test("with two recurring goals of 100 and a revenue of 200") {
        val goals = Set(newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22)), newWeeklyObligation("alimony", 100, new LocalDate(2016, 5, 23)))
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val allocator = RevenueAllocator(goals, revenues)
        val allocations = allocator.generatePlan(new LocalDate(2016, 5, 20), amount(200))
        assert(allocations.size == goals.size, "N goals == N allocations")
        assert(allocations.forall(_.fulfilled), allocations)
    }

    test("prioritizes goals that will be due sooner rather than later") {
        val groceries = newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22))
        val alimony = newWeeklyObligation("alimony", 200, new LocalDate(2016, 5, 25))
        // this is a BAD test: reversing the order of goals in the Set declaration fails the test
        // At the moment, this is the only way I have of checking the prioritisation of goals
        val goals = Set(alimony, groceries)
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val allocator = RevenueAllocator(goals, revenues)
        val allocations = allocator.generatePlan(new LocalDate(2016, 5, 20), amount(50))

        assert(allocations.contains(Allocation(groceries, planToTake = amount(100), realTake = amount(50))), "groceries received 100% of the money allocation")
        assert(!allocations.find(_.goal == alimony).forall(_.fulfilled), "alimoney must be unfulfilled")
    }

    test("prioritizes goals which have a smaller missing amount") {
        val groceries = newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 25))
        val alimony = newWeeklyObligation("alimony", 200, new LocalDate(2016, 5, 25))
        val cell = newWeeklyObligation("cell phone service", 30, new LocalDate(2016, 5, 25))
        val goals = Set(alimony, groceries, cell)
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val allocator = RevenueAllocator(goals, revenues)
        val allocations = allocator.generatePlan(new LocalDate(2016, 5, 20), amount(50))

        assert(allocations.contains(Allocation(cell, planToTake = amount(30), realTake = amount(30))))
        assert(allocations.contains(Allocation(groceries, planToTake = amount(100), realTake = amount(20))))
        assert(allocations.contains(Allocation(alimony, planToTake = amount(200), realTake = amount(0))))
    }

    test("prioritizes goals with lower priority") {
        val groceries = newWeeklyObligation("groceries", 100, new LocalDate(2016, 5, 22))
        val alimony = newWeeklyObligation("alimony", 200, new LocalDate(2016, 5, 22), priority = 1)
        val goals = Set(groceries, alimony)
        val revenues = Set(newWeeklyRevenue("salary", new LocalDate(2016, 5, 20)))
        val allocator = RevenueAllocator(goals, revenues)
        val allocations = allocator.generatePlan(new LocalDate(2016, 5, 20), amount(201))

        assert(allocations.contains(Allocation(alimony, planToTake = amount(200), realTake = amount(200))), "alimony received 99% of the allocation")
        assert(allocations.contains(Allocation(groceries, planToTake = amount(100), realTake = amount(1))), "groceries received 1% of the allocation")
    }

    test("plans to take only 1/3 of the missing amount if 3 revenue events are due before the payment") {
        val carPayment = newMonthlyObligation("car payment", 302, new LocalDate(2016, 6, 10))
        val salary = newWeeklyRevenue("salary", new LocalDate(2016, 5, 19))
        val allocator = RevenueAllocator(Set(carPayment), Set(salary))
        val plan = allocator.generatePlan(new LocalDate(2016, 5, 20), amount(300))
        assert(plan.contains(Allocation(carPayment, planToTake = amount((302.0 / 3).ceil), realTake = amount(101))))
    }

    test("takes 100% of the missing amount if the missing amount is <= autoFulfillThreshold") {
        val carPayment = newMonthlyObligation("car payment", 302, new LocalDate(2016, 6, 10))
        val salary = newWeeklyRevenue("salary", new LocalDate(2016, 5, 19))
        val allocator = RevenueAllocator(Set(carPayment), Set(salary), autoFulfillThreshold = amount(500))
        val plan = allocator.generatePlan(new LocalDate(2016, 5, 19), amount(300))
        assert(plan.contains(Allocation(carPayment, planToTake = amount(302), realTake = amount(302))))
    }

    test("ignores goals that are already fulfilled") {
        val carPayment = newMonthlyObligation("car payment", 302, new LocalDate(2016, 6, 10), balance = 305)
        val salary = newWeeklyRevenue("salary", new LocalDate(2016, 5, 19))
        val allocator = RevenueAllocator(Set(carPayment), Set(salary), autoFulfillThreshold = amount(500))
        val plan = allocator.generatePlan(new LocalDate(2016, 5, 19), amount(300))
        assert(plan.isEmpty)
    }

    test("long-term goals are not fulfilled immediately if the missing amount is greater than the autoFulfillThreshold") {
        val newTires = newGoal("winter tires", 1200, new LocalDate("2017-09-01"))
        val carPayment = newMonthlyObligation("car", 100, new LocalDate("2016-06-18"))
        val salary = newWeeklyRevenue("salary", new LocalDate("2016-05-26"))
        val allocator = RevenueAllocator(Set(carPayment, newTires), Set(salary), autoFulfillThreshold = amount(200))
        val plan = allocator.generatePlan(new LocalDate("2016-05-27"), amount(300))
        val amountPerEvent = amount((1200.0 / 66 /* weeks */).ceil)
        assert(plan.contains(Allocation(newTires, planToTake = amountPerEvent, realTake = amountPerEvent)))
    }

    test("takes enough money to cover many payouts when no other revenue events are planned before the next payouts") {
        val groceries = newWeeklyObligation("groceries", 100, new LocalDate("2016-05-21"))
        val salary1 = newMonthlyRevenue("salary1", new LocalDate("2016-06-01"))
        val salary15 = newMonthlyRevenue("salary15", new LocalDate("2016-06-15"))
        val allocator = RevenueAllocator(Set(groceries), Set(salary1, salary15), autoFulfillThreshold = amount(10))
        val plan = allocator.generatePlan(new LocalDate("2016-05-15"), amount(1000))
        assert(plan.contains(Allocation(groceries, planToTake = amount(2 * 100), realTake = amount(2 * 100))))
    }

    test("respects balance in calculations") {
        val groceries = newWeeklyObligation("groceries", 100, new LocalDate("2016-05-21"), balance = amount(90))
        val salary1 = newMonthlyRevenue("salary1", new LocalDate("2016-06-01"))
        val allocator = RevenueAllocator(Set(groceries), Set(salary1))
        val plan = allocator.generatePlan(new LocalDate("2016-05-15"), amount(1000))
        assert(plan.contains(Allocation(groceries, planToTake = amount(10), realTake = amount(10))))
    }

    def amount(dollars: Double): Amount = amount(dollars.toInt)

    def amount(dollars: Int): Amount = Amount(BigDecimal(dollars))

    def newMonthlyRevenue(name: String, dueOn: LocalDate): automation.Revenue =
        automation.Revenue(name = RevenueName(name), dueOn = dueOn, every = Every(1), period = Monthly)

    def newWeeklyRevenue(name: String, dueOn: LocalDate): automation.Revenue = {
        automation.Revenue(name = RevenueName(name), dueOn = dueOn, every = Every(1), period = Weekly)
    }

    def newGoal(name: String, targetAmount: Int, dueOn: LocalDate, priority: Int = 200): FundingGoal = {
        FixedDateObligation(priority = Priority(priority), name = ObligationName(name), target = amount(targetAmount), balance = amount(0), dueOn = dueOn)
    }

    def newWeeklyObligation(name: String, targetAmount: Int, dueOn: LocalDate, priority: Int = 100, balance: Amount = amount(0)): FundingGoal = {
        RecurringObligation(priority = Priority(priority), name = ObligationName(name), target = amount(targetAmount), balance = balance, dueOn = dueOn, period = Weekly, every = Every(1))
    }

    def newMonthlyObligation(name: String, targetAmount: Int, dueOn: LocalDate, balance: Int = 0): FundingGoal = {
        RecurringObligation(priority = Priority(1), name = ObligationName(name), target = amount(targetAmount), balance = amount(balance), dueOn = dueOn, period = Monthly, every = Every(1))
    }
}
