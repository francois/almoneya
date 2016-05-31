package almoneya

import java.sql.ResultSet

import org.joda.time.LocalDate

import scala.util.Try

class AccountsRepository(val executor: QueryExecutor) extends Repository {

    import AccountsRepository.{FIND_ALL_QUERY, FIND_ALL_WITH_BALANCE_QUERY, INSERT_ONE_QUERY}

    def findAllWithBalance(tenantId: TenantId, balance_on: LocalDate): Try[Set[Account]] =
        executor.findAll(FIND_ALL_WITH_BALANCE_QUERY, tenantId, balance_on)(resultSetRowToAccountWithBalance)
                .map(_.toSet)

    def findAll(tenantId: TenantId): Try[Set[Account]] =
        executor.findAll(FIND_ALL_QUERY, tenantId)(resultSetRowToAccountWithoutBalance)
                .map(_.toSet)

    def create(tenantId: TenantId, account: Account): Try[Account] =
        executor.findOne(INSERT_ONE_QUERY, tenantId, account.code, account.name, account.kind, account.virtual)(resultSetRowToAccountWithoutBalance).map(_.get)


    def search(tenantId: TenantId, query: String): Try[Set[Account]] =
        executor.findAll(FIND_ALL_QUERY.append("AND account_name ilike ?"), tenantId, "%" + query + "%")(resultSetRowToAccountWithoutBalance)
                .map(_.toSet)

    private[this] def resultSetRowToAccountWithBalance(rs: ResultSet): Account =
        Account(id = Some(AccountId(rs.getInt("account_id"))),
            code = Option(rs.getString("account_code")).map(AccountCode.apply),
            name = AccountName(rs.getString("account_name")),
            kind = AccountKind.fromString(rs.getString("account_kind")),
            balance = Option(rs.getBigDecimal("balance")).map(BigDecimal.apply).map(Amount.apply),
            virtual = rs.getBoolean("virtual"))

    private[this] def resultSetRowToAccountWithoutBalance(rs: ResultSet): Account =
        Account(id = Some(AccountId(rs.getInt("account_id"))),
            code = Option(rs.getString("account_code")).map(AccountCode.apply),
            name = AccountName(rs.getString("account_name")),
            kind = AccountKind.fromString(rs.getString("account_kind")),
            virtual = rs.getBoolean("virtual"))

}

object AccountsRepository {
    val FIND_ALL_WITH_BALANCE_QUERY = Query("" +
            "SELECT account_code, account_name, account_kind, virtual, account_id, sum(amount) AS balance " +
            "FROM        public.accounts " +
            "  LEFT JOIN public.transaction_entries USING (tenant_id, account_name) " +
            "  LEFT JOIN public.transactions USING (tenant_id, transaction_id) " +
            "WHERE tenant_id  = ? " +
            "  AND (posted_on IS NULL OR posted_on <= ?) " +
            "GROUP BY account_code, account_name, account_kind, virtual, account_id")

    val FIND_ALL_QUERY = Query("SELECT account_code, account_name, account_kind, virtual, account_id FROM public.accounts WHERE tenant_id = ?")

    val INSERT_ONE_QUERY = Query("INSERT INTO public.accounts(tenant_id, account_code, account_name, account_kind, virtual) VALUES (?, ?, ?, ?, ?)",
        Seq(Column("account_id"), Column("account_code"), Column("account_name"), Column("account_kind"), Column("virtual")))
}
