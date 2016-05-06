package almoneya

import java.sql.PreparedStatement

import org.joda.time.LocalDate

case class LocalDateSqlValue(date: LocalDate) extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setDate(1 + index, new java.sql.Date(date.toDate.getTime))
}
