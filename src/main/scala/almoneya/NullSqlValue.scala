package almoneya

import java.sql.{PreparedStatement, Types}

object NullSqlValue extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setNull(1 + index, Types.VARCHAR)
}
