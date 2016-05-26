package almoneya

import java.sql.{Connection, ResultSet}

import scala.util.Try

trait QueryExecutor {
    def beginTransaction: Try[Unit]

    def commit: Try[Unit]

    def rollback: Try[Unit]

    def connection: Connection

    def findOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[Option[A]]

    def findAll[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[Seq[A]]

    def insertOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[A]

    def insertMany[A](query: Query, params: Seq[Seq[SqlValue]])(mapper: (ResultSet) => A): Try[Seq[A]]
}
