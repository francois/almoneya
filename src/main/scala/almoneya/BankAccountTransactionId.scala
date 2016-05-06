package almoneya

import java.sql.PreparedStatement

case class BankAccountTransactionId(value: Int) extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setInt(1 + index, value)
}
