package almoneya

import org.joda.time.LocalDate

trait DueOnOrAfter {
    def startOn: LocalDate

    def endOn: Option[LocalDate]

    def every: Every

    def period: Period

    /**
      * Returns the next date the obligation is due strictly AFTER cutoffOn.
      *
      * If startOn is 2016-05-22 and the obligation is weekly, then asking for dueOnAfter(2016-05-22)
      * will return 2016-05-29. If cutoffOn were instead 2016-06-05, dueOnOrAfter() will return 2016-05-12.
      *
      * @param cutoffOn Returns a date that is strictly greater than cutoffOn.
      * @return If endOn is None, guaranteed to return a date. If endOn has a date, asking for a date after endOn will return None.
      */
    def dueOnAfter(cutoffOn: LocalDate): Option[LocalDate] =
        endOn match {
            case Some(stopOn) if stopOn.isBefore(cutoffOn) => None
            case Some(stopOn) => payoutEventsStream.dropWhile(isStrictlyAfterCutoff(cutoffOn)).takeWhile(_.isBefore(stopOn)).headOption
            case None => payoutEventsStream.dropWhile(isStrictlyAfterCutoff(cutoffOn)).headOption
        }

    private[this] def isStrictlyAfterCutoff(cutoffOn: LocalDate)(date: LocalDate): Boolean = {
        date.isBefore(cutoffOn) || date == cutoffOn
    }

    protected def payoutEventsStream: Stream[LocalDate] = Stream.from(0).map(n => nextDateAfter(n))

    private[this] def nextDateAfter(scalar: Int): LocalDate = startOn.withPeriodAdded(every.toPeriod(period), scalar)
}
