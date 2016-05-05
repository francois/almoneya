package almoneya

import java.sql.DriverManager

import scala.util.{Failure, Success}

object AlmoneyaWebapp {
    def main(args: Array[String]): Unit = {
        Class.forName("org.postgresql.Driver")
        val connection = DriverManager.getConnection("jdbc:postgresql://10.9.1.21:5432/vagrant", "vagrant", null)
        val executor: QueryExecutor = new SqlQueryExecutor(connection)
        val usersRepo = new UsersRepository(executor)
        val signInsRepo = new SignInsRepository(executor)
        val bankAccountTransactionsRepo=new BankAccountTransactionsRepository(executor)
        usersRepo.findCredentialsByUsername(Username(args.head)).map(maybeCredentials => maybeCredentials.map(_.authenticatesWith(Password(args.last)))) match {
            case Success(Some(true)) =>
                signInsRepo.create(SignIn(username = Username(args.head), sourceIp = IpAddress("127.0.0.1"), userAgent = UserAgent("Chrome"), method = UserpassSignIn, successful = true)) match {
                    case Failure(ex) => println(ex.getLocalizedMessage); ex.printStackTrace(System.err)
                    case _ => ()
                }
                println("Authenticated!")
            case Success(Some(false)) =>
                signInsRepo.create(SignIn(username = Username(args.head), sourceIp = IpAddress("127.0.0.1"), userAgent = UserAgent("Chrome"), method = UserpassSignIn, successful = false)) match {
                    case Failure(ex) => println(ex.getLocalizedMessage); ex.printStackTrace(System.err)
                    case _ => ()
                }
                println("Bad password")
            case Success(None) =>
                signInsRepo.create(SignIn(username = Username(args.head), sourceIp = IpAddress("127.0.0.1"), userAgent = UserAgent("Chrome"), method = UserpassSignIn, successful = false)) match {
                    case Failure(ex) => println(ex.getLocalizedMessage); ex.printStackTrace(System.err)
                    case _ => ()
                }
                println("Bad username")
            case Failure(ex) =>
                System.err.println("Failed to query for user: " + ex.getMessage)
                ex.printStackTrace(System.err)
        }
    }
}
