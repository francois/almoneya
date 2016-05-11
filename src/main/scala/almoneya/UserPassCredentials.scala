package almoneya

case class UserPassCredentials(userId: Option[UserId] = None,
                               tenantId: TenantId,
                               username: Username,
                               passwordHash: PasswordHash) {
    def authenticatesWith(password: Password) = passwordHash.matches(password)
}
