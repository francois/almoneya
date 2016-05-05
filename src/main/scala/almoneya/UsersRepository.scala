package almoneya

import org.joda.time.DateTime

import scala.util.Try

/**
  * Repository and mapper for User objects.
  *
  * @param connectionPool The object responsible for actually running queries.
  */
class UsersRepository(connectionPool: QueryExecutor) {

    import UsersRepository.{findByIdSql, findCredentialsByUsernameQuery}

    def findById(id: UserId): Try[Option[User]] = {
        connectionPool.findOne(findByIdSql, id)(rs =>
            User(Some(UserId(rs.getInt("user_id"))),
                TenantId(rs.getInt("tenant_id")),
                Surname(rs.getString("surname")),
                Option(rs.getString("rest_of_name")).map(RestOfName.apply),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))))
    }

    def findCredentialsByUsername(username: Username): Try[Option[UserPassCredentials]] = {
        connectionPool.findOne(findCredentialsByUsernameQuery, username)(rs =>
            UserPassCredentials(userId = Some(UserId(rs.getInt("user_id"))),
                tenantId = TenantId(rs.getInt("tenant_id")),
                username = Username(rs.getString("username")),
                passwordHash = PasswordHash(rs.getString("password_hash")),
                createdAt = new DateTime(rs.getTimestamp("created_at")),
                updatedAt = new DateTime(rs.getTimestamp("updated_at")))
        )
    }
}

object UsersRepository {
    val findByIdSql = Query("SELECT * FROM public.users WHERE user_id = ?")

    val findCredentialsByUsernameQuery = Query("" +
            "SELECT user_userpass_credentials.user_id, users.tenant_id, user_userpass_credentials.username, user_userpass_credentials.password_hash, users.created_at, users.updated_at " +
            "FROM credentials.user_userpass_credentials " +
            "JOIN users USING (user_id) " +
            "WHERE username = ?")
}
