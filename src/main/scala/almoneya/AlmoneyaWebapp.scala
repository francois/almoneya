package almoneya

import java.sql.DriverManager

import org.joda.time.LocalDate
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

object AlmoneyaWebapp {
    def main(args: Array[String]): Unit = {
        Class.forName("org.postgresql.Driver")
        val connection = DriverManager.getConnection("jdbc:postgresql://10.9.1.21:5432/vagrant", "vagrant", null)
        val executor: QueryExecutor = new SqlQueryExecutor(connection)
        val usersRepo = new UsersRepository(executor)
        val signInsRepo = new SignInsRepository(executor)
        val bankAccountTransactionsRepo = new BankAccountTransactionsRepository(executor)
        val transactionsRepo = new TransactionsRepository(executor)
        usersRepo.findCredentialsByUsername(Username(args.head)).map(maybeCredentials => maybeCredentials.map(_.authenticatesWith(Password(args.last)))) match {
            case Success(Some(true)) =>
                signInsRepo.create(SignIn(username = Username(args.head), sourceIp = IpAddress("127.0.0.1"), userAgent = UserAgent("Chrome"), method = UserpassSignIn, successful = true)) match {
                    case Failure(ex) => log.warn("Failed to insert sign_in row", ex)
                    case _ => ()
                }
                log.info("Authenticated!")

                val checkingAccount = BankAccount(accountNum = AccountHash("aa00"), last4 = AccountLast4("8888"))
                val bankAccounts = Set(checkingAccount)
                val transactions = Set(BankAccountTransaction(
                    bankAccount = checkingAccount,
                    postedOn = new LocalDate("2016-05-05"),
                    description1 = Some(Description("VIR INTER")),
                    amount = Amount(BigDecimal("13.29"))))
                bankAccountTransactionsRepo.importBankTransactionsTransactions(TenantId(1), bankAccounts, transactions) match {
                    case Success(txns) => log.info("Successfully imported {} transactions", txns.size)
                    case Failure(ex) => log.error("Failed to import transactions", ex)
                }

                val txn = Transaction(payee = Payee("VISA Desjardins"), postedOn = new LocalDate(),
                    entries = Set(TransactionEntry(account = Account(name = AccountName("VISA Desjardins"), kind = Liability), amount = Amount(BigDecimal("99.39"))),
                        TransactionEntry(account = Account(name = AccountName("Desjardins Compte Maison"), kind = Asset), amount = Amount(BigDecimal("-99.39")))))
                transactionsRepo.create(TenantId(1), txn) match {
                    case Success(Transaction(Some(TransactionId(id)), _, _, _, _, _, _, _)) => log.info("Created transaction with ID {}", id)
                    case Success(newTransaction) => log.warn("Hmmm, added transaction but ID wasn't assigned... {}", newTransaction)
                    case Failure(ex) => log.warn("Failed to insert transaction", ex);
                }

            case Success(Some(false)) =>
                signInsRepo.create(SignIn(username = Username(args.head), sourceIp = IpAddress("127.0.0.1"), userAgent = UserAgent("Chrome"), method = UserpassSignIn, successful = false)) match {
                    case Failure(ex) => log.warn("Failed to insert sign_in row", ex)
                    case _ => ()
                }
                log.warn("Bad password")

            case Success(None) =>
                signInsRepo.create(SignIn(username = Username(args.head), sourceIp = IpAddress("127.0.0.1"), userAgent = UserAgent("Chrome"), method = UserpassSignIn, successful = false)) match {
                    case Failure(ex) => log.warn("Failed to insert sign_in row", ex)
                    case _ => ()
                }
                log.warn("Bad username")

            case Failure(ex) =>
                log.warn("Failed to query database", ex)
        }
    }

    val log = LoggerFactory.getLogger("almoneya.AlmoneyaWebapp")
}
