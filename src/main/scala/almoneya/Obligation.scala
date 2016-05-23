package almoneya

import org.joda.time.LocalDate

case class Obligation(id: Option[ObligationId] = None,
                      account: Account,
                      description: Option[Description] = None,
                      startOn: LocalDate,
                      endOn: Option[LocalDate] = None,
                      amount: Amount,
                      every: Every,
                      period: Period) extends DueOnOrAfter {
    assert(endOn.isEmpty || endOn.get.isAfter(startOn))
}
