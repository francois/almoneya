package almoneya

import org.joda.time.LocalDate
import org.scalatest.FunSuite

class ObligationTest extends FunSuite {
    test("nextDueOnAfter(2016-05-22) with no endOn returns the next date") {
        val obligation = newWeeklyObligation("2016-05-22")
        assert(obligation.dueOnAfter(new LocalDate("2016-05-22")).contains(new LocalDate("2016-05-29")))
    }

    test("nextDueOnAfter(2016-05-22) with an endOn of 2016-05-27 returns None") {
        val obligation = newWeeklyObligation("2016-05-22", Some("2016-05-27"))
        assert(obligation.dueOnAfter(new LocalDate("2016-05-22")).isEmpty)
    }

    test("nextDueOnAfter(2016-05-30) with no endOn returns the next date") {
        val obligation = newWeeklyObligation("2016-05-22")
        assert(obligation.dueOnAfter(new LocalDate("2016-05-30")).contains(new LocalDate("2016-06-05")))
    }

    test("nextDueOnAfter(2016-06-04) with no endOn returns the next date") {
        val obligation = newWeeklyObligation("2016-05-22")
        assert(obligation.dueOnAfter(new LocalDate("2016-06-04")).contains(new LocalDate("2016-06-05")))
    }

    test("nextDueOnAfter(2016-06-05) with no endOn returns the next date") {
        val obligation = newWeeklyObligation("2016-05-22")
        assert(obligation.dueOnAfter(new LocalDate("2016-06-05")).contains(new LocalDate("2016-06-12")))
    }

    private[this] def newWeeklyObligation(startOn: String, endOn: Option[String] = None) =
        Obligation(account = account, startOn = new LocalDate(startOn), endOn = endOn.map(new LocalDate(_)), amount = zero, every = Every(1), period = Weekly)

    private[this] val account = Account(name = AccountName("account"), kind = Asset)
    private[this] val zero = Amount(0)
}
