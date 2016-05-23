package almoneya

import scala.util.Try

class AccountsRepository(executor: QueryExecutor) {
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
