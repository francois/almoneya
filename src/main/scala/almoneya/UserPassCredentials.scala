package almoneya

import java.security.Principal

case class UserPassCredentials(userId: Option[UserId] = None,
                               tenantId: TenantId,
                               username: Username,
                               passwordHash: PasswordHash) extends Principal {
    def authenticatesWith(password: Password) = passwordHash.matches(password)

    override def getName: String = username.getName
}
