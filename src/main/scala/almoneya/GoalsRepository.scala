package almoneya

import org.joda.time.LocalDate

import scala.util.Try

class GoalsRepository(executor: QueryExecutor) {

    import GoalsRepository.FIND_ALL_QUERY

    def findAll(tenantId: TenantId): Try[Set[Goal]] = {
        executor.findAll(FIND_ALL_QUERY, tenantId) { rs =>
            val envelope = Envelope(id = Some(EnvelopeId(rs.getInt("envelope_id"))), name = EnvelopeName(rs.getString("envelope_name")))
            Goal(id = Some(GoalId(rs.getInt("goal_id"))),
                envelope = envelope,
                description = Option(rs.getString("description")).map(Description.apply),
                dueOn = new LocalDate(rs.getDate("due_on")),
                amount = Amount(rs.getBigDecimal("amount")))
        }.map(_.toSet)
    }
}

object GoalsRepository {
    val FIND_ALL_QUERY = Query("SELECT goal_id, envelope_name, envelope_id, description, due_on, amount FROM goals JOIN envelopes USING (tenant_id, envelope_name) WHERE tenant_id = ?")
}
