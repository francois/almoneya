package almoneya

import java.sql.PreparedStatement

case class Amount(value: BigDecimal) extends SqlValue with Comparable[Amount] {
    def toNumeric = value

    def isPositive = value > 0

    override def compareTo(amount: Amount) = value.compareTo(amount.value)

    def /(denominator: Int): Amount = value.divideAndRemainder(BigDecimal(denominator)) match {
        case Array(integral, decimal) if decimal > 0 => Amount(integral + 1)
        case Array(integral, _) => Amount(integral)
    }

    def +(other: Amount) = Amount(value + other.value)

    def *(n: Int) = Amount(value * n)

    def <(amount: Amount) = value < amount.value

    def -(amount: Amount) = Amount(value - amount.value)

    def >(amount: Amount) = value > amount.value

    def <=(amount: Amount) = value <= amount.value

    def >=(amount: Amount) = value >= amount.value

    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setBigDecimal(1 + index, value)
}

object Amount {
    val VALID_RE = """-?\d+(?:\.\d+)?""".r
}
