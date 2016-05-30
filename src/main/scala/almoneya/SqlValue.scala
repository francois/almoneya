package almoneya

import java.sql.PreparedStatement

trait SqlValue {
    def setParam(statement: PreparedStatement, index: Int): Unit
}

trait StringSqlValue extends SqlValue {
    def value: String

    final override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, value)
}

case class BasicStringSqlValue(value: String) extends StringSqlValue

trait IntSqlValue extends SqlValue {
    def value: Int

    final override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setInt(1 + index, value)
}

case class BasicIntSqlValue(value: Int) extends IntSqlValue
