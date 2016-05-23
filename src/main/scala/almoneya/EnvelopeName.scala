package almoneya

import java.sql.PreparedStatement

case class EnvelopeName(value: String) extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, value)
}
