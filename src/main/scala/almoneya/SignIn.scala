package almoneya

import java.sql.PreparedStatement

import org.joda.time.DateTime

sealed trait SignInMethod extends SqlValue

object SignInMethod {
    def fromString(value: String): Option[SignInMethod] = value match {
        case "userpass" => Some(UserpassSignIn)
        case "twitter" => Some(TwitterSignIn)
        case "facebook" => Some(FacebookSignIn)
        case _ => None
    }
}

case object UserpassSignIn extends SignInMethod {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, "userpass")
}

case object TwitterSignIn extends SignInMethod {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, "twitter")
}

case object FacebookSignIn extends SignInMethod {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, "facebook")
}

case class SignIn(id: Option[SignInId] = None,
                  username: Username,
                  sourceIp: IpAddress,
                  userAgent: UserAgent,
                  method: SignInMethod,
                  successful: Boolean,
                  createdAt: DateTime = new DateTime(),
                  updatedAt: DateTime = new DateTime())
