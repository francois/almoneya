package almoneya.automation

import org.joda.time.LocalDate

case class Revenue(name: RevenueName, dueOn: LocalDate, period: Period, frequency: Frequency) {
    def revenueEventsStream: Stream[LocalDate] = Stream.from(0).map(n => nextDateAfter(n))

    def nextDateAfter(scalar: Int): LocalDate = dueOn.withPeriodAdded(frequency.toPeriod(period), scalar)
}
