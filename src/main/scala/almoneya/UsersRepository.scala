package almoneya

import scala.util.Try

/**
  * Repository and mapper for User objects.
  *
  * @param executor The object responsible for actually running queries.
  */
class UsersRepository(val executor: QueryExecutor) extends Repository {

    import UsersRepository.{findByIdSql, findCredentialsByUsernameQuery}

    def findById(id: UserId): Option[User] = {
        executor.findOne(findByIdSql, id) { rs =>
            User(Some(UserId(rs.getInt("user_id"))),
                TenantId(rs.getInt("tenant_id")),
                Surname(rs.getString("surname")),
                Option(rs.getString("rest_of_name")).map(RestOfName.apply))
        }
    }

    def findCredentialsByUsername(username: Username): Option[UserPassCredentials] = {
        executor.findOne(findCredentialsByUsernameQuery, username) { rs =>
            UserPassCredentials(userId = Some(UserId(rs.getInt("user_id"))),
                tenantId = TenantId(rs.getInt("tenant_id")),
                username = Username(rs.getString("username")),
                passwordHash = PasswordHash(rs.getString("password_hash")))
        }
    }
}

object UsersRepository {
    val findByIdSql = Query("SELECT * FROM public.users WHERE user_id = ?")

    val findCredentialsByUsernameQuery = Query("" +
            "SELECT user_userpass_credentials.user_id, users.tenant_id, user_userpass_credentials.username, user_userpass_credentials.password_hash " +
            "FROM credentials.user_userpass_credentials " +
            "JOIN users USING (user_id) " +
            "WHERE username = ?")
}
