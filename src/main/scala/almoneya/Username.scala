package almoneya

import java.security.Principal
import java.sql.PreparedStatement

case class Username(value: String) extends SqlValue with Principal {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, value)

    override def getName: String = value
}
