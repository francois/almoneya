package almoneya

import java.sql.PreparedStatement

case class UserId(value: Int) extends SqlValue {
    def setParam(statement: PreparedStatement, index: Int): Unit = statement.setInt(1 + index, value)
}
