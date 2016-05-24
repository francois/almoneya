package almoneya

import java.sql.PreparedStatement

case class ObligationName(value: String) extends SqlValue with Comparable[ObligationName] {
    override def compareTo(name: ObligationName) = value.compareTo(name.value)

    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, value)

    def toLowerCase: ObligationName = ObligationName(value.toLowerCase)
}

object ObligationName {
    def fromAccountName(name: AccountName): ObligationName = ObligationName(name.value)
}
