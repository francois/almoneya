package almoneya

import java.sql.PreparedStatement

case class AccountLast4(value: String) extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, value)
}
