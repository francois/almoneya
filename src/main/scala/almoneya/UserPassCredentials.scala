package almoneya

import org.joda.time.DateTime

case class UserPassCredentials(userId: Option[UserId] = None,
                               tenantId: TenantId,
                               username: Username,
                               passwordHash: PasswordHash,
                               createdAt: DateTime,
                               updatedAt: DateTime) {
    def authenticatesWith(password: Password) = passwordHash.matches(password)
}
