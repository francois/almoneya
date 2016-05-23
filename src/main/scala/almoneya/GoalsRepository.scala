package almoneya

import org.joda.time.LocalDate

import scala.util.Try

class GoalsRepository(executor: QueryExecutor) {

    import GoalsRepository.FIND_ALL_QUERY

    def findAll(tenantId: TenantId): Try[Set[Goal]] = {
        executor.findAll(FIND_ALL_QUERY, tenantId) { rs =>
            val account = Account(id = Some(AccountId(rs.getInt("account_id"))), name = AccountName(rs.getString("account_name")), kind = AccountKind.fromString(rs.getString("account_kind")))
            Goal(id = Some(GoalId(rs.getInt("goal_id"))),
                account = account,
                description = Option(rs.getString("description")).map(Description.apply),
                dueOn = new LocalDate(rs.getDate("due_on")),
                amount = Amount(rs.getBigDecimal("amount")))
        }.map(_.toSet)
    }
}

object GoalsRepository {
    val FIND_ALL_QUERY = Query("SELECT goal_id, account_name, account_id, account_kind, description, due_on, amount FROM goals JOIN accounts USING (tenant_id, account_name) WHERE tenant_id = ?")
}
