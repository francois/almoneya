package almoneya.automation

import almoneya.{DueOnOrAfter, Every, Period, RevenueName}
import org.joda.time.LocalDate

case class Revenue(name: RevenueName, dueOn: LocalDate, period: Period, every: Every) extends DueOnOrAfter {
    override def startOn: LocalDate = dueOn

    override def endOn: Option[LocalDate] = None

    def revenueEventsStream: Stream[LocalDate] = Stream.from(0).map(n => nextDateAfter(n))

    def nextDateAfter(scalar: Int): LocalDate = dueOn.withPeriodAdded(every.toPeriod(period), scalar)
}
