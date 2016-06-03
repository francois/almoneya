package almoneya

case class ObligationName(value: String) extends StringSqlValue with Comparable[ObligationName] {
    override def compareTo(name: ObligationName) = value.compareTo(name.value)

    def toLowerCase: ObligationName = ObligationName(value.toLowerCase)
}

object ObligationName {
    def fromAccountName(name: AccountName): ObligationName = ObligationName(name.value)
}
