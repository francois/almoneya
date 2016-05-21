package almoneya.http

import javax.security.auth.Subject
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest

import almoneya._
import org.eclipse.jetty.security.{DefaultUserIdentity, MappedLoginService}
import org.eclipse.jetty.security.MappedLoginService.KnownUser
import org.eclipse.jetty.server.UserIdentity
import org.eclipse.jetty.util.security.Credential
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.util.{Success, Failure}

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
