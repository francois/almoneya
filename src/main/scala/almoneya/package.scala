package object almoneya {
    implicit def bool2SqlValue(bool: Boolean): SqlValue = BoolSqlValue(bool)
}
