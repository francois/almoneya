package almoneya

import org.joda.time.LocalDate

import scala.util.Try

class AccountsRepository(executor: QueryExecutor) {

    import AccountsRepository.FIND_ALL_WITH_BALANCE_QUERY

    def findAllWithBalance(tenantId: TenantId, balanceAsOf: LocalDate): Try[Set[Account]] = {
        executor.findAll(FIND_ALL_WITH_BALANCE_QUERY, tenantId, balanceAsOf) { rs =>
            Account(id = Some(AccountId(rs.getInt("account_id"))),
                code = Option(rs.getString("account_code")).map(AccountCode.apply),
                name = AccountName(rs.getString("account_name")),
                kind = kindStringToKind(rs.getString("account_kind")),
                balance = Some(Amount(0)))
        }
    }.map(_.toSet)

    def findAll(tenantId: TenantId): Try[Set[Account]] = {
        executor.findAll(Query("SELECT account_code, account_name, account_kind, account_id FROM public.accounts WHERE tenant_id = ?"), tenantId) { rs =>
            Account(id = Some(AccountId(rs.getInt("account_id"))),
                code = Option(rs.getString("account_code")).map(AccountCode.apply),
                name = AccountName(rs.getString("account_name")),
                kind = kindStringToKind(rs.getString("account_kind")))
        }
    }.map(_.toSet)

    private[this] def kindStringToKind(str: String): AccountKind = str match {
        case "asset" => Asset
        case "liability" => Liability
        case "equity" => Equity
        case "expense" => Expense
        case "revenue" => Income
        case "contra" => Contra
    }
}

object AccountsRepository {
    val FIND_ALL_WITH_BALANCE_QUERY = Query("SELECT account_code, account_name, account_kind, account_id, sum(amount) AS balance " +
            "FROM public.accounts " +
            "JOIN transaction_entries USING (tenant_id, account_name) " +
            "JOIN transactions USING (tenant_id, transaction_id) " +
            "WHERE tenant_id = ? " +
            "  AND posted_on < ?" +
            "GROUP BY account_code, account_name, account_kind, account_id")
}
