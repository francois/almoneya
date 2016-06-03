package almoneya

import java.sql.{Connection, ResultSet}

trait QueryExecutor {
    def findOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A)(implicit connection: Connection): Option[A]

    def findAll[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A)(implicit connection: Connection): Seq[A]

    def insertOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A)(implicit connection: Connection): A

    def insertMany[A](query: Query, params: Seq[Seq[SqlValue]])(mapper: (ResultSet) => A)(implicit connection: Connection): Seq[A]
}
