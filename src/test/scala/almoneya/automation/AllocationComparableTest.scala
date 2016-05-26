package almoneya.automation

import almoneya.{Amount, ObligationName, Priority}
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class AllocationComparableTest extends FunSuite {

    import AllocationComparableTest.{amount, zero}

    test("compareTo() uses dueOn to order allocations") {
        val earlyMay = Allocation(FixedDateObligation(Priority(100), ObligationName("earlyMay"), amount(100), zero, new LocalDate("2016-05-09")), planToTake = zero)
        val midMay = Allocation(FixedDateObligation(Priority(100), ObligationName("midMay"), amount(100), zero, new LocalDate("2016-05-18")), planToTake = zero)
        val lateMay = Allocation(FixedDateObligation(Priority(100), ObligationName("lateMay"), amount(100), zero, new LocalDate("2016-05-27")), planToTake = zero)

        assert(earlyMay.compareTo(midMay) < 0)
        assert(midMay.compareTo(lateMay) < 0)
        assert(earlyMay.compareTo(lateMay) < 0)
        assert(earlyMay.compareTo(earlyMay) == 0)
        assert(midMay.compareTo(midMay) == 0)
        assert(lateMay.compareTo(lateMay) == 0)
        assert(lateMay.compareTo(midMay) > 0)
        assert(midMay.compareTo(earlyMay) > 0)
        assert(lateMay.compareTo(earlyMay) > 0)
    }

    test("compareTo() uses priority to order allocations when dueOn are equal") {
        val lowPrio = Allocation(FixedDateObligation(Priority(25), ObligationName("whatever"), amount(100), zero, new LocalDate("2016-05-09")), planToTake = zero)
        val midPrio = Allocation(FixedDateObligation(Priority(50), ObligationName("whatever"), amount(100), zero, new LocalDate("2016-05-09")), planToTake = zero)
        val highPrio = Allocation(FixedDateObligation(Priority(100), ObligationName("whatever"), amount(100), zero, new LocalDate("2016-05-09")), planToTake = zero)

        assert(lowPrio.compareTo(midPrio) < 0)
        assert(midPrio.compareTo(highPrio) < 0)
        assert(lowPrio.compareTo(highPrio) < 0)
        assert(lowPrio.compareTo(lowPrio) == 0)
        assert(midPrio.compareTo(midPrio) == 0)
        assert(highPrio.compareTo(highPrio) == 0)
        assert(highPrio.compareTo(midPrio) > 0)
        assert(midPrio.compareTo(lowPrio) > 0)
        assert(highPrio.compareTo(lowPrio) > 0)
    }

    test("compareTo() uses amountMissing to order allocations when dueOn, priority are equal") {
        val lowAmount = Allocation(FixedDateObligation(Priority(50), ObligationName("whatever"), amount(25), zero, new LocalDate("2016-05-09")), planToTake = zero)
        val medAmount = Allocation(FixedDateObligation(Priority(50), ObligationName("whatever"), amount(100), zero, new LocalDate("2016-05-09")), planToTake = zero)
        val highAmount = Allocation(FixedDateObligation(Priority(50), ObligationName("whatever"), amount(500), zero, new LocalDate("2016-05-09")), planToTake = zero)

        assert(lowAmount.compareTo(medAmount) < 0)
        assert(medAmount.compareTo(highAmount) < 0)
        assert(lowAmount.compareTo(highAmount) < 0)
        assert(lowAmount.compareTo(lowAmount) == 0)
        assert(medAmount.compareTo(medAmount) == 0)
        assert(highAmount.compareTo(highAmount) == 0)
        assert(highAmount.compareTo(medAmount) > 0)
        assert(medAmount.compareTo(lowAmount) > 0)
        assert(highAmount.compareTo(lowAmount) > 0)
    }

    test("compareTo() uses case-insensitive name to order allocations when dueOn, priority and amountMissing are equal") {
        val a = Allocation(FixedDateObligation(Priority(50), ObligationName("a"), amount(100), zero, new LocalDate("2016-05-09")), planToTake = zero)
        val b = Allocation(FixedDateObligation(Priority(50), ObligationName("B"), amount(100), zero, new LocalDate("2016-05-09")), planToTake = zero)
        val c = Allocation(FixedDateObligation(Priority(50), ObligationName("c"), amount(100), zero, new LocalDate("2016-05-09")), planToTake = zero)

        assert(a.compareTo(b) < 0)
        assert(b.compareTo(c) < 0)
        assert(a.compareTo(c) < 0)
        assert(a.compareTo(a) == 0)
        assert(b.compareTo(b) == 0)
        assert(c.compareTo(c) == 0)
        assert(c.compareTo(b) > 0)
        assert(b.compareTo(a) > 0)
        assert(c.compareTo(a) > 0)
    }
}

object AllocationComparableTest {
    val zero = amount(0)

    def amount(dollars: Int): Amount = Amount(dollars)
}
