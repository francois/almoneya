package almoneya

case class TransactionId(value: Int) extends IntSqlValue

object TransactionId {
    val VALID_RE = """^\d+$""".r
}
