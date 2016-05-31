package almoneya

import org.joda.time.LocalDate

class BankAccountTransactionsRepository(val executor: QueryExecutor) extends Repository {

    import BankAccountTransactionsRepository.{IMPORT_BANK_ACCOUNT_TRANSACTIONS_QUERY, LINK_BANK_ACCOUNT_TRANSACTION_TO_TRANSACTION_ENTRY_QUERY}

    def linkBankAccountTransactionToTransactionEntry(tenantId: TenantId,
                                                     bankAccountTransactionId: BankAccountTransactionId,
                                                     transactionId: TransactionId): Option[BankAccountTransaction] = {
        executor.findOne(LINK_BANK_ACCOUNT_TRANSACTION_TO_TRANSACTION_ENTRY_QUERY, transactionId, tenantId, tenantId, bankAccountTransactionId) { rs =>
            BankAccountTransaction(id = Some(BankAccountTransactionId(rs.getInt("bank_account_transaction_id"))),
                bankAccount = BankAccount(id = Option(rs.getInt("bank_account_id")).map(BankAccountId.apply),
                    accountHash = AccountHash(rs.getString("bank_account_hash")),
                    last4 = AccountLast4(rs.getString("bank_account_last4")),
                    account = Some(Account(id = Option(rs.getInt("account_id")).map(AccountId.apply),
                        code = Option(rs.getString("account_code")).map(AccountCode.apply),
                        name = AccountName(rs.getString("account_name")),
                        kind = AccountKind.fromString(rs.getString("account_kind")),
                        virtual = rs.getBoolean("virtual"),
                        balance = None))),
                checkNum = Option(rs.getString("check_num")).map(CheckNum.apply),
                postedOn = new LocalDate(rs.getDate("posted_on")),
                description1 = Option(rs.getString("description1")).map(Description.apply),
                description2 = Option(rs.getString("description2")).map(Description.apply),
                amount = Amount(rs.getBigDecimal("amount"))
            )
        }
    }

    def importBankTransactionsTransactions(tenantId: TenantId, transactions: Seq[BankAccountTransaction]): Seq[BankAccountTransaction] = {
        val bankAccounts = transactions.map(_.bankAccount).toSet
        val existingAccountNums = executor.findAll(Query("SELECT bank_account_hash FROM bank_accounts")) { rs =>
            AccountHash(rs.getString("bank_account_hash"))
        }
        val missingAccountNums = transactions.filterNot(txn => existingAccountNums.contains(txn.bankAccount.accountHash)).map(_.bankAccount.accountHash)
        val missingAccounts = bankAccounts.filter(account => missingAccountNums.contains(account.accountHash))
        val newAccounts = executor.insertMany(Query("INSERT INTO bank_accounts(tenant_id, bank_account_hash, bank_account_last4) VALUES"), missingAccounts.map(account => Seq(tenantId, account.accountHash, account.last4)).toSeq) { rs =>
            BankAccount(id = Some(BankAccountId(rs.getInt("bank_account_id"))),
                accountHash = AccountHash(rs.getString("bank_account_hash")),
                last4 = AccountLast4(rs.getString("bank_account_last4")))
        }
        val accounts = (newAccounts ++ bankAccounts).map(account => (account.accountHash, account)).toMap
        val values: Seq[Seq[SqlValue]] = transactions.map(txn => Seq[SqlValue](tenantId, txn.bankAccount.accountHash, txn.postedOn, txn.description1, txn.description2, txn.checkNum, txn.amount))

        executor.insertMany(IMPORT_BANK_ACCOUNT_TRANSACTIONS_QUERY, values) { rs =>
            BankAccountTransaction(id = Some(BankAccountTransactionId(rs.getInt("bank_account_transaction_id"))),
                bankAccount = accounts(AccountHash(rs.getString("bank_account_hash"))),
                checkNum = Option(rs.getString("check_number")).map(CheckNum.apply),
                postedOn = new LocalDate(rs.getDate("posted_on")),
                description1 = Option(rs.getString("description1")).map(Description.apply),
                description2 = Option(rs.getString("description2")).map(Description.apply),
                amount = Amount(rs.getBigDecimal("amount")))
        }
    }
}

object BankAccountTransactionsRepository {
    val IMPORT_BANK_ACCOUNT_TRANSACTIONS_QUERY = Query("INSERT INTO bank_account_transactions(tenant_id, bank_account_hash, posted_on, description1, description2, check_number, amount) VALUES",
        Seq(Column("bank_account_transaction_id"), Column("bank_account_hash"), Column("posted_on"), Column("description1"), Column("description2"), Column("check_number"), Column("amount")))

    val LINK_BANK_ACCOUNT_TRANSACTION_TO_TRANSACTION_ENTRY_QUERY = Query("" +
            "WITH new_values AS (" +
            "    UPDATE public.bank_account_transactions " +
            "    SET account_name = bank_accounts.account_name, transaction_id = ? " +
            "    FROM public.bank_accounts " +
            "    WHERE bank_account_transactions.tenant_id = ? " +
            "      AND bank_accounts.tenant_id = ? " +
            "      AND bank_account_transaction_id = ? " +
            "      AND bank_accounts.bank_account_hash = bank_account_transactions.bank_account_hash " +
            "    RETURNING bank_account_transactions.tenant_id, bank_account_transactions.bank_account_hash, bank_account_transactions.posted_on, bank_account_transactions.description1, bank_account_transactions.description2, bank_account_transactions.check_number, bank_account_transactions.amount, bank_account_transactions.transaction_id, bank_account_transactions.account_name, bank_account_transactions.bank_account_transaction_id)" +
            "" +
            "SELECT new_values.*, bank_accounts.bank_account_id, bank_accounts.bank_account_last4, accounts.account_id, accounts.account_code, accounts.account_name, accounts.account_kind, accounts.virtual " +
            "FROM new_values " +
            "JOIN public.bank_accounts USING (tenant_id, bank_account_hash, account_name) " +
            "JOIN public.accounts USING (tenant_id, account_name)")
}
