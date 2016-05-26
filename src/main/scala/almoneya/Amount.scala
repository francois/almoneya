package almoneya

import java.sql.PreparedStatement

case class Amount(value: BigDecimal) extends SqlValue with Comparable[Amount] {
    def *(n: Int) = Amount(value * n)

    def toNumeric = value

    def isPositive = value > 0

    override def compareTo(amount: Amount) = value.compareTo(amount.value)

    // Unfortunately, I can't use the + method, since it's bound to StringLike...
    def add(amount: Amount): Amount = Amount(value + amount.value)

    def /(denominator: Int): Amount = value.divideAndRemainder(BigDecimal(denominator)) match {
        case Array(integral, decimal) if decimal > 0 => Amount(integral + 1)
        case Array(integral, _) => Amount(integral)
    }

    def <(amount: Amount) = value < amount.value

    def -(amount: Amount) = Amount(value - amount.value)

    def >(amount: Amount) = value > amount.value

    def <=(amount: Amount) = value <= amount.value

    def >=(amount: Amount) = value >= amount.value

    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setBigDecimal(1 + index, value)
}
