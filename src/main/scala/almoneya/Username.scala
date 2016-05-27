package almoneya

case class Username(value: String) extends StringSqlValue {
    def getName: String = value
}
