package almoneya

import java.sql.PreparedStatement

import org.joda.time.DateTime

sealed trait SignInMethod extends SqlValue

case object UserpassSignIn extends SignInMethod {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, "userpass")
}

case class SignIn(id: Option[SignInId] = None,
                  username: Username,
                  sourceIp: IpAddress,
                  userAgent: UserAgent,
                  method: SignInMethod,
                  successful: Boolean,
                  createdAt: DateTime = new DateTime(),
                  updatedAt: DateTime = new DateTime())
