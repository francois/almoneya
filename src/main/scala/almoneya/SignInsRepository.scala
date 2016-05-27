package almoneya

import scala.util.Try

/**
  * Repository and mapper for SignIn objects.
  *
  * @param executor The object responsible for actually running queries.
  */
class SignInsRepository(val executor: QueryExecutor) extends Repository {

    import SignInsRepository.{insertSignInSql, insertUserPassSignInSql}

    def create(signIn: SignIn): Try[SignIn] = {
        executor.insertOne(insertSignInSql, signIn.sourceIp, signIn.userAgent, signIn.method, signIn.successful) { rs =>
            signIn.copy(id = Some(SignInId(rs.getInt("sign_in_id"))),
                sourceIp = IpAddress(rs.getString("source_ip")),
                userAgent = UserAgent(rs.getString("user_agent")),
                method = SignInMethod.fromString(rs.getString("method")).get, // the database constraints guarantee that the value is correct
                successful = rs.getBoolean("successful"))
        }.flatMap { basicSignIn =>
            executor.insertOne(insertUserPassSignInSql, basicSignIn.id.get, signIn.username) { rs =>
                basicSignIn.copy(username = Username(rs.getString("username")))
            }
        }
    }
}

object SignInsRepository {
    val insertSignInSql = Query("INSERT INTO credentials.sign_ins(source_ip, user_agent, method, successful) VALUES (?::inet, ?, ?, ?)",
        Seq(Column("sign_in_id"), Column("source_ip"), Column("user_agent"), Column("method"), Column("successful")))
    val insertUserPassSignInSql = Query("INSERT INTO credentials.userpass_sign_ins(sign_in_id, username) VALUES (?, ?)", Seq(Column("username")))
}
