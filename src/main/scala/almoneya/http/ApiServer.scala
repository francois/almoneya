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

        val accountsController = new ContextHandler("/api/accounts")
        accountsController.setHandler(new AccountsController(JSON.mapper, accountsRepository))

        val importBankAccountsController = new ContextHandler("/api/bank-account-transactions/import")
        importBankAccountsController.setHandler(new ImportBankAccountTransactionsController(JSON.mapper, bankAccountTransactionsRepository))

        val allocatorController = new ContextHandler("/api/allocator/run")
        allocatorController.setHandler(new AllocatorController(JSON.mapper, accountsRepository, goalsRepository, obligationsRepository, revenuesRepository))

        val createTransactionController = new ContextHandler("/api/transactions")
        createTransactionController.setHandler(new CreateTransactionController(JSON.mapper, accountsRepository, transactionsRepository, bankAccountTransactionsRepository))

        val reconcileTransactionController = new ContextHandler("/api/reconcile")
        reconcileTransactionController.setHandler(new ReconcileTransactionController(JSON.mapper, reconciliationsRepository))

        val createReconciliationController = new ContextHandler("/api/reconciliations")
        createReconciliationController.setHandler(new CreateReconciliationController(JSON.mapper, reconciliationsRepository))

        val fileServer = new ContextHandler("/")
        val fileServerHandler = new ResourceHandler()
        fileServerHandler.setDirectoriesListed(false)
        fileServerHandler.setResourceBase("public/")
        fileServer.setHandler(fileServerHandler)

        val contexts = new ContextHandlerCollection()
        contexts.setHandlers(Array(fileServer, accountsController, importBankAccountsController, allocatorController, createTransactionController, reconcileTransactionController, createReconciliationController))

        security.setHandler(contexts)


        val router = Router(Seq(
            Route("""^/accounts""".r,controller=new ListAccountsController(accountsRepository))
        ))
        new FrontController(router,JSON.mapper)

        server.start()
        server.join()
    }

    val log = LoggerFactory.getLogger("almoneya.http.ApiServer")

    val RequestIdAttribute = "almoneya.RequestId"
    val TenantIdAttribute = "almoneya.TenantId"
    val UserIdAttribute = "almoneya.UserId"
}
