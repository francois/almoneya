package almoneya.http

import java.util.UUID
import javax.security.auth.Subject
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest
import javax.sql.DataSource

import almoneya._
import org.eclipse.jetty.security.MappedLoginService.KnownUser
import org.eclipse.jetty.security.{DefaultUserIdentity, MappedLoginService}
import org.eclipse.jetty.server.UserIdentity
import org.eclipse.jetty.util.security.Credential
import org.slf4j.{LoggerFactory, MDC}

import scala.collection.JavaConversions._

class RepoLoginService(val usersRepository: UsersRepository,
                       val signInsRepository: SignInsRepository,
                       val dataSource: DataSource) extends MappedLoginService {
    override def login(username: String, credentials: scala.Any, request: ServletRequest): UserIdentity = {
        // Unrelated to the task of logging in... but required by the hidden implicit parameter to Repository methods
        implicit val connection = dataSource.getConnection
        log.debug("Acquired database connection {}", connection.hashCode())

        // Unrelated to the task of logging in... but useful in order to match all calls to
        // the database with the specific request that generated it
        MDC.put(ApiServer.RequestIdAttribute, UUID.randomUUID().toString)

        try {
            val result = Option(super.login(username, credentials, request))
            request match {
                case httpServletRequest: HttpServletRequest =>
                    try {
                        connection.setAutoCommit(false)
                        signInsRepository.create(
                            SignIn(username = Username(username),
                                sourceIp = IpAddress(request.getRemoteAddr),
                                userAgent = UserAgent(Option(httpServletRequest.getHeader("User-Agent")).getOrElse("Unknown")),
                                method = UserpassSignIn,
                                successful = result.isDefined))
                        connection.commit()
                        connection.setAutoCommit(true)
                    } catch {
                        case ex: Throwable =>
                            connection.rollback()
                            throw ex
                    }
            }

            result match {
                case None => null
                case Some(identity: DefaultUserIdentity) =>
                    identity.getUserPrincipal match {
                        case user: AlmoneyaKnownUser =>
                            MDC.put(ApiServer.TenantIdAttribute, user.tenantId.value.toString)
                            MDC.put(ApiServer.UserIdAttribute, user.name.toString)
                            request.setAttribute(ApiServer.TenantIdAttribute, user.tenantId)
                        case _ => ()
                    }

                    identity

                case Some(other) => other
            }
        } finally {
            log.debug("Closing database connection {}", connection.hashCode())
            connection.close()
        }
    }

    override def loadUserInfo(userIdentifier: String): KnownUser = {
        implicit val connection = dataSource.getConnection()
        log.debug("Acquired database connection {}", connection.hashCode())
        try {
            val username = Username(userIdentifier)
            usersRepository.findCredentialsByUsername(username) match {
                case Some(userPassCredentials) =>
                    new AlmoneyaKnownUser(userIdentifier, BasicCredentials(userPassCredentials))

                case None => null // no user with this usernmae
            }
        } finally {
            log.debug("Closing database connection {}", connection.hashCode())
            connection.close()
        }
    }

    override def loadUser(userIdentifier: String): UserIdentity = {
        implicit val connection = dataSource.getConnection
        log.debug("Acquired database connection {}", connection.hashCode())
        try {
            val username = Username(userIdentifier)
            usersRepository.findCredentialsByUsername(username) match {
                case Some(userPassCredentials) =>
                    val subject = new Subject(true, Set(userPassCredentials), Set.empty[AnyRef], Set.empty[AnyRef])
                    new DefaultUserIdentity(subject, userPassCredentials, Array[String]("user"))

                case None => null // no user with this usernmae
            }
        } finally {
            log.debug("Closing database connection {}", connection.hashCode())
            connection.close()
        }
    }

    override def loadRoleInfo(user: KnownUser): Array[String] = Array[String]("user")

    override def loadUsers(): Unit = ()

    case class AlmoneyaKnownUser(name: String, credential: BasicCredentials) extends KnownUser(name, credential) {
        def tenantId: TenantId = credential.tenantId
    }

    private[this] val log = LoggerFactory.getLogger(classOf[RepoLoginService])
}

case class BasicCredentials(userPassCredentials: UserPassCredentials) extends Credential {
    override def check(credentials: scala.Any): Boolean = userPassCredentials.authenticatesWith(Password(credentials.toString))

    def userId: UserId = userPassCredentials.userId.get

    def tenantId: TenantId = userPassCredentials.tenantId
}
