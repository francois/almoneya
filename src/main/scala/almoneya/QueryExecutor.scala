package almoneya

import java.sql.{Connection, ResultSet}

import scala.util.Try

trait QueryExecutor {
    def connection: Connection

    def findOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[Option[A]]

    def insertReturning[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[Seq[A]]
}
