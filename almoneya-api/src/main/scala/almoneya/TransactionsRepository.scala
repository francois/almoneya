package almoneya

import java.sql.Connection

import org.joda.time.{DateTime, LocalDate}

class TransactionsRepository(val executor: QueryExecutor) extends Repository {

    import TransactionsRepository.{FIND_ALL_QUERY, FIND_ENTRIES_WITH_ROUND_AMOUNTS_QUERY, INSERT_TRANSACTION_ENTRIES_QUERY, INSERT_TRANSACTION_QUERY}

    def findAllWithBalance(tenantId: TenantId)(implicit connection: Connection): Set[Transaction] = {
        val transactions = executor.findAll(FIND_ALL_QUERY, tenantId) { rs =>
            Transaction(transactionId = Some(TransactionId(rs.getInt("transaction_id"))),
                payee = Payee(rs.getString("payee")),
                description = Option(rs.getString("description")).map(Description.apply),
                postedOn = new LocalDate(rs.getDate("posted_on")),
                bookedAt = new DateTime(rs.getTimestamp("booked_at")),
                entries = Set.empty)
        }

        val entries = executor.findAll(FIND_ENTRIES_WITH_ROUND_AMOUNTS_QUERY, tenantId) { rs =>
            (TransactionId(rs.getInt("transaction_id")), TransactionEntry(transactionEntryId = Some(TransactionEntryId(rs.getInt("transaction_entry_id"))),
                amount = Amount(rs.getBigDecimal("amount")),
                account = Account(id = Some(AccountId(rs.getInt("account_id"))),
                    code = Option(rs.getString("account_code")).map(AccountCode.apply),
                    name = AccountName(rs.getString("account_name")),
                    kind = AccountKind.fromString(rs.getString("account_kind")),
                    virtual = rs.getBoolean("virtual"))))
        }

        transactions.map { txn =>
            val entriesOfThisTransaction = entries.filter(_._1 == txn.transactionId.get)
            val transactionEntries = entriesOfThisTransaction.map(_._2)
            val balance = if (transactionEntries.isEmpty) {
                Amount(0)
            } else {
                transactionEntries.filterNot(_.account.virtual).map(_.amount).filter(_.isPositive).reduce(_ + _)
            }

            txn.copy(entries = transactionEntries, balance = Some(balance))
        }.toSet
    }

    def findAllIds(tenantId: TenantId)(implicit connection: Connection): Set[TransactionId] =
        executor.findAll(Query("SELECT transaction_id FROM public.transactions WHERE tenant_id = ?"), tenantId) { rs =>
            TransactionId(rs.getInt("transaction_id"))
        }.toSet

    def create(tenantId: TenantId, transaction: Transaction)(implicit connection: Connection): Transaction = {
        def createTransactionRow(): Transaction = {
            executor.insertOne(INSERT_TRANSACTION_QUERY, tenantId, transaction.postedOn, transaction.payee, transaction.description) { rs =>
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
            executor.insertMany(INSERT_TRANSACTION_ENTRIES_QUERY, entries) { rs =>
                TransactionEntry(
                    transactionEntryId = Some(TransactionEntryId(rs.getInt("transaction_entry_id"))),
                    account = Account(id = Some(AccountId(rs.getInt("account_id"))), name = AccountName(rs.getString("account_name")), kind = Asset, virtual = rs.getBoolean("virtual")),
                    amount = Amount(rs.getBigDecimal("amount")))
            }
        }

        val newTransaction = createTransactionRow()
        val newEntries = createTransactionEntriesRows(newTransaction.transactionId.get)
        newTransaction.copy(entries = newEntries.toSet, balance = Some(newEntries.filterNot(_.account.virtual).map(_.amount).reduce(_ + _)))
    }
}

object TransactionsRepository {
    val INSERT_TRANSACTION_QUERY = Query("INSERT INTO transactions(tenant_id, posted_on, payee, description) VALUES(?, ?, ?, ?)",
        Seq(Column("transaction_id"), Column("posted_on"), Column("payee"), Column("description"), Column("booked_at")))

    val INSERT_TRANSACTION_ENTRIES_QUERY = Query("" +
            "WITH new_rows AS (" +
            "    INSERT INTO transaction_entries(tenant_id, transaction_id, account_name, amount) " +
            "    VALUES ... " +
            "    RETURNING transaction_entry_id, account_name, amount) " +
            "SELECT transaction_entry_id, account_name, virtual, account_id, account_kind, amount " +
            "FROM new_rows " +
            "JOIN accounts USING (account_name)")

    val FIND_ALL_QUERY = Query("SELECT transaction_id, payee, description, posted_on, booked_at FROM public.transactions WHERE tenant_id = ?")
    val FIND_ENTRIES_WITH_ROUND_AMOUNTS_QUERY = Query("" +
            "SELECT transaction_id, transaction_entry_id, round(amount, 2) AS amount, account_id, account_code, account_name, account_kind, virtual " +
            "FROM public.transaction_entries " +
            "  JOIN accounts USING (tenant_id, account_name) " +
            "WHERE tenant_id = ? " +
            "ORDER BY transaction_id, LOWER(account_name)")
}
