package almoneya

import org.joda.time.{DateTime, LocalDate}

import scala.util.Try

class TransactionsRepository(executor: QueryExecutor) {

    import TransactionsRepository.{insertTransactionEntriesSql, insertTransactionSql}

    def create(tenantId: TenantId, transaction: Transaction): Try[Transaction] = {
        def createTransactionRow(): Try[Transaction] = {
            executor.insertOne(insertTransactionSql, tenantId, transaction.postedOn, transaction.payee, transaction.description) { rs =>
                transaction.copy(
                    transactionId = Some(TransactionId(rs.getInt("transaction_id"))),
                    payee = Payee(rs.getString("payee")),
                    description = Option(rs.getString("description")).map(Description.apply),
                    postedOn = new LocalDate(rs.getDate("posted_on")),
                    bookedAt = new DateTime(rs.getTimestamp("booked_at")))
            }
        }

        def createTransactionEntriesRows(transactionId: TransactionId): Try[Seq[TransactionEntry]] = {
            val entries = transaction.entries.map(entry => Seq[SqlValue](tenantId, transactionId, entry.accountName, entry.amount)).toSeq
            executor.insertMany(insertTransactionEntriesSql, entries) { rs =>
                TransactionEntry(
                    transactionEntryId = Some(TransactionEntryId(rs.getInt("transaction_entry_id"))),
                    account = Account(name = AccountName(rs.getString("account_name")), kind = Asset),
                    amount = Amount(rs.getBigDecimal("amount")))
            }
        }

        for (newTransaction <- createTransactionRow();
             newEntries <- createTransactionEntriesRows(newTransaction.transactionId.get)) yield
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
            "SELECT transaction_entry_id, account_name, account_kind, amount " +
            "FROM new_rows " +
            "JOIN accounts USING (account_name)")
}
