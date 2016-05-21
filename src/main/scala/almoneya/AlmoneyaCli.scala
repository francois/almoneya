package almoneya

import java.io.{BufferedReader, FileInputStream, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.sql.DriverManager

import org.apache.commons.csv.CSVFormat
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}

object AlmoneyaCli {

    private[this] def chooseParser(rows: Seq[Seq[String]]): Either[Exception, Seq[BankAccountTransaction]] = {
        val sizes = rows.map(_.size).toSet
        if (sizes.contains(14)) {
            Right(new DesjardinsParser().parse(rows))
        } else if (sizes.contains(8) || sizes.contains(9)) {
            Right(new RbcParser().parse(rows))
        } else {
            Left(new IllegalArgumentException("Unknown parser class to use for files with " + sizes + " columns"))
        }
    }

    def main(args: Array[String]): Unit = {
        Class.forName("org.postgresql.Driver")
        log.info("Connecting to database server")
        val connection = DriverManager.getConnection("jdbc:postgresql://10.9.1.21:5432/vagrant", "vagrant", null)
        val executor: QueryExecutor = new SqlQueryExecutor(connection)
        val usersRepo = new UsersRepository(executor)
        val signInsRepo = new SignInsRepository(executor)
        val bankAccountTransactionsRepo = new BankAccountTransactionsRepository(executor)
        val transactionsRepo = new TransactionsRepository(executor)
        usersRepo.findCredentialsByUsername(Username(args(0))).map(maybeCredentials => maybeCredentials.map(_.authenticatesWith(Password(args(1))))) match {
            case Success(Some(true)) =>
                signInsRepo.create(SignIn(username = Username(args.head), sourceIp = IpAddress("127.0.0.1"), userAgent = UserAgent("Chrome"), method = UserpassSignIn, successful = true)) match {
                    case Failure(ex) => log.warn("Failed to insert sign_in row", ex)
                    case _ => ()
                }
                log.info("Authenticated!")

                val maybeParsed = args.slice(2, Int.MaxValue).map(fn => new BufferedReader(new InputStreamReader(new FileInputStream(fn), StandardCharsets.ISO_8859_1)))
                        .map(in => CSVFormat.EXCEL.parse(in))
                        .map(_.getRecords)
                        .map(rows => rows.map(row => (0 until row.size()).map(idx => row.get(idx))))
                        .map(chooseParser)
                maybeParsed.zip(args.slice(2, Int.MaxValue)).filter(_._1.isLeft).foreach(pair => log.warn("Could not parse {}: {}", pair._2, pair._1.left.get.getMessage, ""))
                val parsed = maybeParsed.filter(_.isRight).map(_.right.get).map { txns =>
                    bankAccountTransactionsRepo.importBankTransactionsTransactions(TenantId(1), txns.map(_.bankAccount).toSet, txns)
                }
                log.info("{}", parsed)

            /*
                val txn = Transaction(payee = Payee("VISA Desjardins"), postedOn = new LocalDate(),
                    entries = Set(TransactionEntry(account = Account(name = AccountName("VISA Desjardins"), kind = Liability), amount = Amount(BigDecimal("99.39"))),
                        TransactionEntry(account = Account(name = AccountName("Desjardins Compte Maison"), kind = Asset), amount = Amount(BigDecimal("-99.39")))))
                transactionsRepo.create(TenantId(1), txn) match {
                    case Success(Transaction(Some(TransactionId(id)), _, _, _, _, _, _, _)) => log.info("Created transaction with ID {}", id)
                    case Success(newTransaction) => log.warn("Hmmm, added transaction but ID wasn't assigned... {}", newTransaction)
                    case Failure(ex) => log.warn("Failed to insert transaction", ex);
                }
            */

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
