package almoneya

import java.sql.PreparedStatement

case class Amount(value: BigDecimal) extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setBigDecimal(1 + index, value)
}
