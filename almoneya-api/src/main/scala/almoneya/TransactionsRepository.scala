package almoneya

import java.sql.Connection

import org.joda.time.{DateTime, LocalDate}

class TransactionsRepository(val executor: QueryExecutor) extends Repository {

    import TransactionsRepository.{insertTransactionEntriesSql, insertTransactionSql}

    def findAllIds(tenantId: TenantId)(implicit connection: Connection): Set[TransactionId] =
        executor.findAll(Query("SELECT transaction_id FROM public.transactions WHERE tenant_id = ?"), tenantId) { rs =>
            TransactionId(rs.getInt("transaction_id"))
        }.toSet

    def create(tenantId: TenantId, transaction: Transaction)(implicit connection: Connection): Transaction = {
        def createTransactionRow(): Transaction = {
            executor.insertOne(insertTransactionSql, tenantId, transaction.postedOn, transaction.payee, transaction.description) { rs =>
                transaction.copy(
                    transactionId = Some(TransactionId(rs.getInt("transaction_id"))),
                    payee = Payee(rs.getString("payee")),
                    description = Option(rs.getString("description")).map(Description.apply),
                    postedOn = new LocalDate(rs.getDate("posted_on")),
                    bookedAt = new DateTime(rs.getTimestamp("booked_at")))
            }
        }

        def createTransactionEntriesRows(transactionId: TransactionId)(implicit connection: Connection): Seq[TransactionEntry] = {
            val entries = transaction.entries.map(entry => Seq[SqlValue](tenantId, transactionId, entry.accountName, entry.amount)).toSeq
            executor.insertMany(insertTransactionEntriesSql, entries) { rs =>
                TransactionEntry(
                    transactionEntryId = Some(TransactionEntryId(rs.getInt("transaction_entry_id"))),
                    account = Account(id = Some(AccountId(rs.getInt("account_id"))), name = AccountName(rs.getString("account_name")), kind = Asset, virtual = rs.getBoolean("virtual")),
                    amount = Amount(rs.getBigDecimal("amount")))
            }
        }

        val newTransaction = createTransactionRow()
        val newEntries = createTransactionEntriesRows(newTransaction.transactionId.get)
        newTransaction.copy(entries = newEntries.toSet)
    }
}

object TransactionsRepository {
    val insertTransactionSql = Query("INSERT INTO transactions(tenant_id, posted_on, payee, description) VALUES(?, ?, ?, ?)",
        Seq(Column("transaction_id"), Column("posted_on"), Column("payee"), Column("description"), Column("booked_at")))

    val insertTransactionEntriesSql = Query("" +
            "WITH new_rows AS (" +
            "    INSERT INTO transaction_entries(tenant_id, transaction_id, account_name, amount) " +
            "    VALUES ... " +
            "    RETURNING transaction_entry_id, account_name, amount) " +
            "SELECT transaction_entry_id, account_name, virtual, account_id, account_kind, amount " +
            "FROM new_rows " +
            "JOIN accounts USING (account_name)")
}
