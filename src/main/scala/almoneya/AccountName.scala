package almoneya

case class AccountName(value: String) extends StringSqlValue {
    def caseInsensitiveContains(str: String): Boolean = value.toLowerCase.contains(str.toLowerCase)

    def isEqualTo(str: String): Boolean = value == str
}
