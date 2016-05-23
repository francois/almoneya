package almoneya

import scala.util.Try

class EnvelopesRepository(executor: QueryExecutor) {
    def findAll(tenantId: TenantId): Try[Set[Envelope]] =
        executor.findAll(Query("SELECT envelope_id, envelope_name FROM envelopes WHERE tenant_id = ?"), tenantId) { rs =>
            Envelope(id = Some(EnvelopeId(rs.getInt("envelope_id"))),
                name = EnvelopeName(rs.getString("envelope_name")))
        }.map(_.toSet)

    def findAllWithBalance(tenantId: TenantId): Try[Set[Envelope]] =
        executor.findAll(Query("SELECT envelope_id, envelope_name, 0 AS balance FROM envelopes WHERE tenant_id = ?"), tenantId) { rs =>
            Envelope(id = Some(EnvelopeId(rs.getInt("envelope_id"))),
                name = EnvelopeName(rs.getString("envelope_name")),
                balance = Option(rs.getBigDecimal("balance")).map(Amount.apply(_)))
        }.map(_.toSet)
}
