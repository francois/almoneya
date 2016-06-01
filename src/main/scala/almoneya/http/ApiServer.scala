package almoneya.http

import java.sql.DriverManager
import java.util.Collections

import almoneya._
import org.eclipse.jetty.security._
import org.eclipse.jetty.security.authentication.BasicAuthenticator
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.handler.{ContextHandler, ContextHandlerCollection, ResourceHandler}
import org.eclipse.jetty.util.security.Constraint
import org.slf4j.LoggerFactory

import scala.collection.SortedSet
import scala.language.higherKinds

object ApiServer {

    def main(args: Array[String]): Unit = {
        Class.forName("org.postgresql.Driver")

        log.info("Booting")
        log.info("Connecting to database server")
        val connection = DriverManager.getConnection("jdbc:postgresql://10.9.1.21:5432/vagrant", "vagrant", null)
        val executor: QueryExecutor = new SqlQueryExecutor(connection)

        val usersRepository = new UsersRepository(executor)
        val signInsRepo = new SignInsRepository(executor)
        val accountsRepository = new AccountsRepository(executor)
        val bankAccountTransactionsRepository = new BankAccountTransactionsRepository(executor)
        val transactionsRepository = new TransactionsRepository(executor)
        val goalsRepository = new GoalsRepository(executor)
        val obligationsRepository = new ObligationsRepository(executor)
        val revenuesRepository = new RevenuesRepository(executor)
        val reconciliationsRepository = new ReconciliationsRepository(executor)

        val loginService = new RepoLoginService(usersRepository, signInsRepo)

        val server = new Server(8080)
        server.addBean(loginService)

        val security = new ConstraintSecurityHandler()
        val gzipHandler = new GzipHandler()
        gzipHandler.setHandler(security)
        server.setHandler(gzipHandler)

        val constraint = new Constraint()
        constraint.setName("auth")
        constraint.setAuthenticate(true)
        constraint.setRoles(Array[String]("user", "admin"))

        val mapping = new ConstraintMapping()
        mapping.setPathSpec("/*")
        mapping.setConstraint(constraint)

        security.setConstraintMappings(Collections.singletonList(mapping))
        security.setAuthenticator(new BasicAuthenticator())
        security.setLoginService(loginService)

        val fileServer = new ContextHandler("/")
        val fileServerHandler = new ResourceHandler()
        fileServerHandler.setDirectoriesListed(false)
        fileServerHandler.setResourceBase("public/")
        fileServer.setHandler(fileServerHandler)

        val router = Router(SortedSet(
            Route("""/accounts""".r, methods = Set(Route.GET), controller = new ListAccountsController(accountsRepository)),
            Route("""/accounts""".r, methods = Set(Route.POST), controller = new CreateAccountController(accountsRepository)),
            Route("""/accounts/search""".r, methods = Set(Route.GET), controller = new SearchAccountsController(accountsRepository)),
            Route("""/allocations/build""".r, methods = Set(Route.GET), controller = new BuildAllocationController(accountsRepository, goalsRepository, obligationsRepository, revenuesRepository)),
            Route("""/bank-account-transactions/import""".r, methods = Set(Route.POST), controller = new ImportBankAccountTransactionsController(bankAccountTransactionsRepository)),
            Route("""/transactions""".r, methods = Set(Route.POST), controller = new CreateTransactionController(JSON.mapper, accountsRepository, transactionsRepository, bankAccountTransactionsRepository)),
            Route("""/reconciliations""".r, methods = Set(Route.POST), controller = new CreateReconciliationController(reconciliationsRepository)),
            Route("""/transactions/reconcile""".r, methods = Set(Route.POST), controller = new ReconcileTransactionController(reconciliationsRepository))
        ))

        val frontController = new ContextHandler("/api")
        frontController.setHandler(new FrontController(router, JSON.mapper))

        val contexts = new ContextHandlerCollection()
        contexts.setHandlers(Array(fileServer, frontController))

        security.setHandler(contexts)

        server.start()
        server.join()
    }

    val log = LoggerFactory.getLogger("almoneya.http.ApiServer")

    val RequestIdAttribute = "almoneya.RequestId"
    val TenantIdAttribute = "almoneya.TenantId"
    val UserIdAttribute = "almoneya.UserId"
}
