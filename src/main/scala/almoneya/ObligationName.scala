package almoneya

import java.sql.PreparedStatement

case class ObligationName(value: String) extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, value)

    def compare(other: ObligationName) = value.compare(other.value)

    def toLowerCase: ObligationName = ObligationName(value.toLowerCase)
}
