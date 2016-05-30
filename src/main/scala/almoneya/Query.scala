package almoneya

case class Query(querySql: String, returning: Seq[Column] = Seq.empty) {

    import Query.REPLACEABLE_RE

    def append(sql: String): Query = copy(querySql = querySql + " " + sql)

    def replaceOrAppend(newSql: String): Query = {
        val firstIn = REPLACEABLE_RE.findFirstIn(querySql)
        if (firstIn.isDefined) {
            Query(REPLACEABLE_RE.replaceFirstIn(querySql, newSql), returning)
        } else {
            Query(querySql + newSql, returning)
        }
    }

    def sql: String = if (returning.isEmpty) {
        querySql
    } else {
        querySql + " RETURNING " + returning.map(_.name).mkString(", ")
    }
}

object Query {
    val REPLACEABLE_RE = """[.]{3}""".r
}
