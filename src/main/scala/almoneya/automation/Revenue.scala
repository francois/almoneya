package almoneya.automation

import almoneya.{RevenueName, Period, Every}
import org.joda.time.LocalDate

case class Revenue(name: RevenueName, dueOn: LocalDate, period: Period, every: Every) {
    def revenueEventsStream: Stream[LocalDate] = Stream.from(0).map(n => nextDateAfter(n))

    def nextDateAfter(scalar: Int): LocalDate = dueOn.withPeriodAdded(every.toPeriod(period), scalar)
}
