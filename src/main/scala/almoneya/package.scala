import org.joda.time.LocalDate

package object almoneya {
    implicit def bool2SqlValue(bool: Boolean): SqlValue = BoolSqlValue(bool)

    implicit def scalaBigDecimal2javaBigDecimal(scalaBigD: scala.BigDecimal): java.math.BigDecimal = scalaBigD.bigDecimal

    implicit def javaBigDecimal2scalaBigDecimal(javaBigD: java.math.BigDecimal): scala.BigDecimal = scala.BigDecimal(javaBigD)

    implicit def localDate2SqlValue(date: LocalDate): SqlValue = LocalDateSqlValue(date)

    implicit def option2SqlValue[A <: SqlValue](option: Option[A]): SqlValue = option match {
        case Some(v) => v
        case None => NullSqlValue
    }
}
