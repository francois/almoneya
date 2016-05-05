package almoneya

import org.joda.time.DateTime

import scala.util.Try

/**
  * Repository and mapper for SignIn objects.
  *
  * @param executor The object responsible for actually running queries.
  */
class SignInsRepository(executor: QueryExecutor) {

    def create(signIn: SignIn): Try[SignIn] =
        executor.insertReturning(Query("INSERT INTO credentials.sign_ins(source_ip, user_agent, method, successful) VALUES (?::inet, ?, ?, ?) RETURNING sign_in_id"), signIn.sourceIp, signIn.userAgent, signIn.method, signIn.successful) { rs =>
            signIn.copy(id = Some(SignInId(rs.getInt("sign_in_id"))))
        }.map(_.head).flatMap { basicSignIn =>
            executor.insertReturning(Query("INSERT INTO credentials.userpass_sign_ins(sign_in_id, username) VALUES (?, ?) RETURNING *"), basicSignIn.id.get, signIn.username) { rs =>
                basicSignIn.copy(createdAt = new DateTime(rs.getTimestamp("created_at")), updatedAt = new DateTime(rs.getTimestamp("updated_at")))
            }.map(_.head)
        }
}
