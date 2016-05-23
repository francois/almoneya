package almoneya

import org.joda.time.LocalDate

case class Revenue(id: Option[RevenueId] = None,
                   name: RevenueName,
                   startOn: LocalDate,
                   endOn: Option[LocalDate] = None,
                   every: Every,
                   period: Period,
                   amount: Amount) extends DueOnOrAfter {
    assert(endOn.isEmpty || endOn.get.isAfter(startOn))
}
