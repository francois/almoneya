package almoneya

import org.joda.time.LocalDate

import scala.util.Try

class ObligationsRepository(executor: QueryExecutor) {

    import ObligationsRepository.FIND_ALL_QUERY

    def findAll(tenantId: TenantId): Try[Set[Obligation]] = {
        executor.findAll(FIND_ALL_QUERY, tenantId) { rs =>
            val envelope = Envelope(id = Some(EnvelopeId(rs.getInt("envelope_id"))), name = EnvelopeName(rs.getString("envelope_name")))
            Obligation(id = Some(ObligationId(rs.getInt("obligation_id"))),
                envelope = envelope,
                description = Option(rs.getString("description")).map(Description.apply),
                startOn = new LocalDate(rs.getDate("start_on")),
                endOn = Option(rs.getDate("end_on")).map(new LocalDate(_)),
                amount = Amount(rs.getBigDecimal("amount")),
                every = Every(rs.getInt("every")),
                period = rs.getString("period") match {
                    case "day" => Daily
                    case "week" => Weekly
                    case "month" => Monthly
                    case "year" => Yearly
                }
            )
        }.map(_.toSet)
    }
}

object ObligationsRepository {
    val FIND_ALL_QUERY = Query("SELECT obligation_id, envelope_name, envelope_id, description, start_on, end_on, amount, every, period FROM obligations JOIN envelopes USING (tenant_id, envelope_name) WHERE tenant_id = ?")
}
