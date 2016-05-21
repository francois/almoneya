package almoneya.http

import java.sql.DriverManager
import java.util.Collections
import javax.security.auth.Subject
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import almoneya._
import org.eclipse.jetty.security.MappedLoginService.KnownUser
import org.eclipse.jetty.security._
import org.eclipse.jetty.security.authentication.BasicAuthenticator
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler, ContextHandlerCollection}
import org.eclipse.jetty.server.{Request, Server, UserIdentity}
import org.eclipse.jetty.util.security.{Constraint, Credential}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}

object ApiServer {
    def main(args: Array[String]): Unit = {
        Class.forName("org.postgresql.Driver")

        log.info("Booting")
        log.info("Connecting to database server")
        val connection = DriverManager.getConnection("jdbc:postgresql://10.9.1.21:5432/vagrant", "vagrant", null)
        val executor: QueryExecutor = new SqlQueryExecutor(connection)


        val server = new Server(8080)
        val loginService = new RepoLoginService(new UsersRepository(executor))
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



        val context = new ContextHandler("/")
        context.setContextPath("/")
        context.setHandler(new HelloHandler("Root Hello"))

        val contextFR = new ContextHandler("/fr")
        contextFR.setHandler(new HelloHandler("Bonjour"))

        val contextIT = new ContextHandler("/it")
        contextIT.setHandler(new HelloHandler("Bongiorno"))

        val contexts = new ContextHandlerCollection()
        contexts.setHandlers(Array(context, contextFR, contextIT))

        security.setHandler(contexts)

        server.start()
        server.join()
    }

    val log = LoggerFactory.getLogger("almoneya.http.ApiServer")
}

class HelloHandler(private[this] val text: String) extends AbstractHandler {
    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        response.setContentType("text/html;charset=utf-8")
        response.getWriter.println("<h1>" + text + "</h1>")

        // SUPER IMPORTANT!
        baseRequest.setHandled(true)
    }
}

class RepoLoginService(usersRepository: UsersRepository, signInsRepository: SignInsRepository) extends MappedLoginService {
    override def login(username: String, credentials: scala.Any, request: ServletRequest): UserIdentity = {
        val result = Option(super.login(username, credentials, request))
        request match {
            case httpServletRequest: HttpServletRequest =>
                signInsRepository.create(
                    SignIn(username = Username(username),
                        sourceIp = IpAddress(request.getRemoteAddr),
                        userAgent = UserAgent(Option(httpServletRequest.getHeader("User-Agent")).getOrElse("Unknown")),
                        method = UserpassSignIn,
                        successful = result.isDefined))
        }

        result.orNull
    }

    override def loadUserInfo(userIdentifier: String): KnownUser = {
        val username = Username(userIdentifier)
        usersRepository.findCredentialsByUsername(username) match {
            case Success(Some(userPassCredentials)) =>
                new KnownUser(userIdentifier, BasicCredentials(userPassCredentials))

            case Success(None) => null // no user with this usernmae

            case Failure(ex) =>
                log.error("Failed to talk to database while attempting to authenticate", ex)
                throw ex
        }
    }

    override def loadUser(userIdentifier: String): UserIdentity = {
        val username = Username(userIdentifier)
        usersRepository.findCredentialsByUsername(username) match {
            case Success(Some(userPassCredentials)) =>
                val subject = new Subject(true, Set(userPassCredentials.username), Set.empty[AnyRef], Set.empty[AnyRef])
                new DefaultUserIdentity(subject, username, Array[String]("user"))

            case Success(None) => null // no user with this usernmae

            case Failure(ex) =>
                log.error("Failed to talk to database while attempting to authenticate", ex)
                throw ex
        }
    }

    override def loadRoleInfo(user: KnownUser): Array[String] = Array[String]("user")

    override def loadUsers(): Unit = ()

    private[this] val log = LoggerFactory.getLogger(classOf[RepoLoginService])
}

case class BasicCredentials(userPassCredentials: UserPassCredentials) extends Credential {
    override def check(credentials: scala.Any): Boolean = userPassCredentials.authenticatesWith(Password(credentials.toString))
}
