package almoneya

import org.joda.time.LocalDate

import scala.util.Try

class ObligationsRepository(executor: QueryExecutor) {

    import ObligationsRepository.FIND_ALL_QUERY

    def findAll(tenantId: TenantId): Try[Set[Obligation]] = {
        executor.findAll(FIND_ALL_QUERY, tenantId) { rs =>
            val account = Account(id = Some(AccountId(rs.getInt("account_id"))), name = AccountName(rs.getString("account_name")), kind = AccountKind.fromString(rs.getString("account_kind")), virtual = rs.getBoolean("virtual"))
            Obligation(id = Some(ObligationId(rs.getInt("obligation_id"))),
                account = account,
                description = Option(rs.getString("description")).map(Description.apply),
                startOn = new LocalDate(rs.getDate("start_on")),
                endOn = Option(rs.getDate("end_on")).map(new LocalDate(_)),
                amount = Amount(rs.getBigDecimal("amount")),
                priority = Priority(rs.getInt("priority")),
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
    val FIND_ALL_QUERY = Query("SELECT obligation_id, account_name, virtual, account_id, account_kind, description, start_on, end_on, amount, every, period, priority FROM obligations JOIN accounts USING (tenant_id, account_name) WHERE tenant_id = ?")
}
