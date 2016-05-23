package almoneya.http

import java.sql.DriverManager
import java.util.Collections

import almoneya._
import almoneya.automation.{Allocation, FundingGoal}
import com.fasterxml.jackson.core.{JsonFactory, JsonGenerator}
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.eclipse.jetty.security._
import org.eclipse.jetty.security.authentication.BasicAuthenticator
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{ContextHandler, ContextHandlerCollection}
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
        val envelopesRepository = new EnvelopesRepository(executor)
        val goalsRepository = new GoalsRepository(executor)
        val obligationsRepository = new ObligationsRepository(executor)
        val revenuesRepository = new RevenuesRepository(executor)

        val loginService = new RepoLoginService(usersRepository, signInsRepo)

        val server = new Server(8080)
        server.addBean(loginService)

        val security = new ConstraintSecurityHandler()
        server.setHandler(security)

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

        val mapper = prepareJacksonObjectMapper

        val listAccountsController = new ContextHandler("/api/accounts")
        listAccountsController.setHandler(new ListAccountsController(mapper, accountsRepository))

        val importBankAccountsController = new ContextHandler("/api/bank-account-transactions/import")
        importBankAccountsController.setHandler(new ImportBankAccountTransactionsController(mapper, bankAccountTransactionsRepository))

        val allocatorController = new ContextHandler("/api/allocator/run")
        allocatorController.setHandler(new AllocatorController(mapper, envelopesRepository, goalsRepository, obligationsRepository, revenuesRepository))

        val contexts = new ContextHandlerCollection()
        contexts.setHandlers(Array(listAccountsController, importBankAccountsController, allocatorController))

        security.setHandler(contexts)

        server.start()
        server.join()
    }

    private[this] def prepareJacksonObjectMapper: ObjectMapper = {
        val jsonFactory = new JsonFactory()
        jsonFactory.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
        jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)

        val mapper = new ObjectMapper(jsonFactory)
        mapper.registerModule(DefaultScalaModule)
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        val serializerModule = new SimpleModule()
        serializerModule.addSerializer(classOf[Account], new AccountSerializer())
        serializerModule.addSerializer(classOf[Envelope], new EnvelopeSerializer())
        serializerModule.addSerializer(classOf[Goal], new GoalSerializer())
        serializerModule.addSerializer(classOf[Obligation], new ObligationSerializer())
        serializerModule.addSerializer(classOf[Allocation], new AllocationSerializer())
        serializerModule.addSerializer(classOf[FundingGoal], new FundingGoalSerializer())
        serializerModule.addSerializer(classOf[Results[_]], new ResultsSerializer())
        mapper.registerModule(serializerModule)
        mapper
    }

    val log = LoggerFactory.getLogger("almoneya.http.ApiServer")

    val RequestIdAttribute = "almoneya.RequestId"
    val TenantIdAttribute = "almoneya.TenantId"
    val UserIdAttribute = "almoneya.UserId"
}
