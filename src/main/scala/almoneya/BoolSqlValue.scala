package almoneya

import java.sql.PreparedStatement

case class BoolSqlValue(value: Boolean) extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setBoolean(1 + index, value)
}
