package almoneya

import java.sql.{Connection, ResultSet}

import scala.collection.mutable
import scala.util.Try

class SqlQueryExecutor(override val connection: Connection) extends QueryExecutor {
    def findOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[Option[A]] = Try {
        val statement = connection.prepareStatement(query.value)
        params.zipWithIndex.foreach { case (value, index) => value.setParam(statement, index) }
        if (statement.execute()) {
            val resultSet = statement.getResultSet
            if (resultSet.next()) {
                Some(mapper(resultSet))
            } else {
                None
            }
        } else {
            None
        }
    }

    override def insertReturning[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[Seq[A]] = Try {
        val statement = connection.prepareStatement(query.value)
        params.zipWithIndex.foreach { case (value, index) => value.setParam(statement, index) }
        if (statement.execute()) {
            val resultSet = statement.getResultSet
            var result = List.empty[A]
            while(resultSet.next())
            result = result :+ mapper(resultSet)
            result.toSeq
        } else {
            List.empty
        }
    }
}
