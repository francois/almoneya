package almoneya.http

import java.util.Collections

import almoneya._
import com.mchange.v2.c3p0.ComboPooledDataSource
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
        log.info("Booting")
        log.info("Connecting to database server")

        /* Configuration of the dataSource occurs using c3p0.properties, at the root of the classpath.
         * The defaults are suitable for connecting to the Vagrant-powered PostgreSQL database server.
         * Extra configuration can be set using System properties.
         * See http://www.mchange.com/projects/c3p0/#configuration_precedence for the order of precedence and
         * http://www.mchange.com/projects/c3p0/#configuration_properties for the configuration properties that can be set
         */
        val dataSource = new ComboPooledDataSource()
        physicallyConnectToDatabase(dataSource)

        val executor: QueryExecutor = new SqlQueryExecutor()

        val usersRepository = new UsersRepository(executor)
        val signInsRepo = new SignInsRepository(executor)
        val accountsRepository = new AccountsRepository(executor)
        val bankAccountTransactionsRepository = new BankAccountTransactionsRepository(executor)
        val transactionsRepository = new TransactionsRepository(executor)
        val goalsRepository = new GoalsRepository(executor)
        val obligationsRepository = new ObligationsRepository(executor)
        val revenuesRepository = new RevenuesRepository(executor)
        val reconciliationsRepository = new ReconciliationsRepository(executor)

        val loginService = new RepoLoginService(usersRepository, signInsRepo, dataSource)

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
        mapping.setPathSpec("/api/*")
        mapping.setConstraint(constraint)

        security.setConstraintMappings(Collections.singletonList(mapping))
        security.setAuthenticator(new BasicAuthenticator())
        security.setLoginService(loginService)

        val fileServer = new ContextHandler("/")
        val fileServerHandler = new ResourceHandler()
        fileServerHandler.setDirectoriesListed(false)
        fileServerHandler.setResourceBase("../almoneya-frontend/public/")
        fileServer.setHandler(fileServerHandler)

        val router = Router(SortedSet(
            Route("""/accounts""".r, methods = Set(Route.GET), controller = new ListAccountsController(accountsRepository), transactionalBehaviour = NoTransactionNeeded),
            Route("""/accounts""".r, methods = Set(Route.POST), controller = new CreateAccountController(accountsRepository)),
            Route("""/accounts/search""".r, methods = Set(Route.GET), controller = new SearchAccountsController(accountsRepository), transactionalBehaviour = NoTransactionNeeded),
            Route("""/allocations/build""".r, methods = Set(Route.GET), controller = new BuildAllocationController(accountsRepository, goalsRepository, obligationsRepository, revenuesRepository), transactionalBehaviour = NoTransactionNeeded),
            Route("""/bank-account-transactions""".r, methods = Set(Route.GET), controller = new ListBankAccountTransactionsController(bankAccountTransactionsRepository), transactionalBehaviour = NoTransactionNeeded),
            Route("""/bank-account-transactions/import""".r, methods = Set(Route.POST), controller = new ImportBankAccountTransactionsController(bankAccountTransactionsRepository)),
            Route("""/transactions""".r, methods = Set(Route.GET), controller = new ListTransactionsController(transactionsRepository), transactionalBehaviour = NoTransactionNeeded),
            Route("""/transactions""".r, methods = Set(Route.POST), controller = new CreateTransactionController(JSON.mapper, accountsRepository, transactionsRepository, bankAccountTransactionsRepository)),
            Route("""/reconciliations""".r, methods = Set(Route.POST), controller = new CreateReconciliationController(reconciliationsRepository)),
            Route("""/transactions/reconcile""".r, methods = Set(Route.POST), controller = new ReconcileTransactionController(reconciliationsRepository, accountsRepository, transactionsRepository)),
            Route("""/revenues/create""".r, methods = Set(Route.POST), controller = new CreateRevenueTransactionController(accountsRepository, transactionsRepository, JSON.mapper))
        ))

        val frontController = new ContextHandler("/api")
        frontController.setHandler(new FrontController(router, dataSource, JSON.mapper))

        val contexts = new ContextHandlerCollection()
        contexts.setHandlers(Array(fileServer, frontController))

        security.setHandler(contexts)

        server.start()
        server.join()
    }

    /**
      * Connects to the database and reports the version number we're working against.
      *
      * This method serves one purpose only: to make the c3p0 connection pool actually
      * connect to the database, instead of waiting for the first connection. During load
      * testing, waiting for the connection to the database server to be made will simply
      * not work.
      *
      * @param dataSource The data source which should be physically connected.
      */
    def physicallyConnectToDatabase(dataSource: ComboPooledDataSource): Unit = {
        val conn = dataSource.getConnection
        val statement = conn.createStatement()
        statement.execute("SELECT version()")
        val rs = statement.getResultSet
        while (rs.next()) {
            log.info("PostgreSQL version: {}", rs.getString("version"))
        }
        rs.close()
        statement.close()
        conn.close()
    }

    val log = LoggerFactory.getLogger("almoneya.http.ApiServer")

    val RequestIdAttribute = "almoneya.RequestId"
    val TenantIdAttribute = "almoneya.TenantId"
    val UserIdAttribute = "almoneya.UserId"
}
