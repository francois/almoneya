package almoneya

case class Query(value: String, returning: Seq[Column] = Seq.empty) {
    def append(sql: String) = Query(value + sql, returning)

    def sql: String = if (returning.isEmpty) {
        value
    } else {
        value + " RETURNING " + returning.map(_.name).mkString(", ")
    }
}
