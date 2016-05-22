package almoneya

import org.joda.time.LocalDate

import scala.util.Try

class BankAccountTransactionsRepository(executor: QueryExecutor) {

    import BankAccountTransactionsRepository.importBankTransactionSql

    def importBankTransactionsTransactions(tenantId: TenantId, transactions: Seq[BankAccountTransaction]): Try[Seq[BankAccountTransaction]] = {
        val bankAccounts = transactions.map(_.bankAccount).toSet
        executor.findAll(Query("SELECT bank_account_hash FROM bank_accounts")) { rs =>
            AccountHash(rs.getString("bank_account_hash"))
        }.map(_.toSet).map { existingAccountNums =>
            transactions.filterNot(txn => existingAccountNums.contains(txn.bankAccount.accountNum)).map(_.bankAccount.accountNum)
        }.map { missingAccountNums =>
            bankAccounts.filter(account => missingAccountNums.contains(account.accountNum))
        }.flatMap { missingAccounts =>
            executor.insertMany(Query("INSERT INTO bank_accounts(tenant_id, bank_account_hash, bank_account_last4) VALUES"), missingAccounts.map(account => Seq(tenantId, account.accountNum, account.last4)).toSeq) { rs =>
                BankAccount(id = Some(BankAccountId(rs.getInt("bank_account_id"))),
                    accountNum = AccountHash(rs.getString("bank_account_hash")),
                    last4 = AccountLast4(rs.getString("bank_account_last4")))
            }
        }.flatMap { newAccounts =>
            val accounts = (newAccounts ++ bankAccounts).map(account => (account.accountNum, account)).toMap
            val values: Seq[Seq[SqlValue]] = transactions.map(txn => Seq[SqlValue](tenantId, txn.bankAccount.accountNum, txn.postedOn, txn.description1, txn.description2, txn.checkNum, txn.amount))

            executor.insertMany(importBankTransactionSql, values) { rs =>
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
}

object BankAccountTransactionsRepository {
    val importBankTransactionSql = Query("INSERT INTO bank_account_transactions(tenant_id, bank_account_hash, posted_on, description1, description2, check_number, amount) VALUES",
        Seq(Column("bank_account_transaction_id"), Column("bank_account_hash"), Column("posted_on"), Column("description1"), Column("description2"), Column("check_number"), Column("amount")))
}
