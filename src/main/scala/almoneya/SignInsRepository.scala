package almoneya

import org.joda.time.DateTime

import scala.util.Try

/**
  * Repository and mapper for SignIn objects.
  *
  * @param executor The object responsible for actually running queries.
  */
class SignInsRepository(executor: QueryExecutor) {

    import SignInsRepository.{insertSignInSql, insertUserPassSignInSql}

    def create(signIn: SignIn): Try[SignIn] = {
        executor.insertOne(insertSignInSql, signIn.sourceIp, signIn.userAgent, signIn.method, signIn.successful) { rs =>
            signIn.copy(id = Some(SignInId(rs.getInt("sign_in_id"))),
                sourceIp = IpAddress(rs.getString("source_ip")),
                userAgent = UserAgent(rs.getString("user_agent")),
                method = SignInMethod.fromString(rs.getString("method")).get, // the database constraints guarantee that the value is correct
                successful = rs.getBoolean("successful"),
                createdAt = new DateTime(rs.getTimestamp("created_at")),
                updatedAt = new DateTime(rs.getTimestamp("updated_at")))
        }.map(_.head).flatMap { basicSignIn =>
            executor.insertOne(insertUserPassSignInSql, basicSignIn.id.get, signIn.username) { rs =>
                basicSignIn.copy(username = Username(rs.getString("username")))
            }.map(_.head)
        }
    }
}

object SignInsRepository {
    val insertSignInSql = Query("INSERT INTO credentials.sign_ins(source_ip, user_agent, method, successful) VALUES (?::inet, ?, ?, ?)",
        Seq(Column("sign_in_id"), Column("source_ip"), Column("user_agent"), Column("method"), Column("successful"), Column("created_at"), Column("updated_at")))
    val insertUserPassSignInSql = Query("INSERT INTO credentials.userpass_sign_ins(sign_in_id, username) VALUES (?, ?)", Seq(Column("username")))
}
