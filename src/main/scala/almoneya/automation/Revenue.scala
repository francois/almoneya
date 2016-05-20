package almoneya.automation

import org.joda.time.LocalDate

case class Revenue(name: RevenueName, dueOn: LocalDate, period: Period, frequency: Frequency) {
    def revenueEventsStream: Stream[LocalDate] = Stream.Empty
}
