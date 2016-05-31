package almoneya

import java.sql.{Connection, ResultSet}

trait QueryExecutor {
    def beginTransaction(): Unit

    def commit(): Unit

    def rollback(): Unit

    def connection: Connection

    def findOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Option[A]

    def findAll[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Seq[A]

    def insertOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): A

    def insertMany[A](query: Query, params: Seq[Seq[SqlValue]])(mapper: (ResultSet) => A): Seq[A]
}
