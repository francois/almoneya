package almoneya

import java.sql.PreparedStatement

trait SqlValue {
    def setParam(statement: PreparedStatement, index: Int): Unit
}

trait StringSqlValue extends SqlValue {
    def value: String

    final override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, value)
}

trait IntSqlValue extends SqlValue {
    def value: Int

    final override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setInt(1 + index, value)
}
