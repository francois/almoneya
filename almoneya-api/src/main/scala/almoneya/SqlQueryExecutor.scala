package almoneya

import java.sql.{Connection, ResultSet}
import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory

/**
  * A QueryExecutor instance that works against a JDBC connection instance.
  */
class SqlQueryExecutor extends QueryExecutor {

    import SqlQueryExecutor.logger

    override def findOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A)(implicit connection:Connection): Option[A] =
        findAll(query, params: _*)(mapper).headOption

    override def findAll[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A)(implicit connection: Connection): Seq[A] = {
        val statement = connection.prepareStatement(query.sql)
        val startedAt = System.nanoTime()
        try {
            params.zipWithIndex.foreach { case (value, index) => value.setParam(statement, index) }
            if (statement.execute()) {
                val resultSet = statement.getResultSet
                var results = Vector.empty[A]
                while (resultSet.next()) results = results :+ mapper(resultSet)
                results.toSeq
            } else {
                Seq.empty
            }
        } finally {
            val finishedAt = System.nanoTime()
            if (logger.isInfoEnabled) {
                logger.info("[%7.3f ms] %s".format((finishedAt - startedAt).toDouble / TimeUnit.MILLISECONDS.toNanos(1), query.sql))
            }
        }
    }

    override def insertOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A)(implicit connection: Connection): A =
        findAll(query, params: _*)(mapper).head

    override def insertMany[A](query: Query, params: Seq[Seq[SqlValue]])(mapper: (ResultSet) => A)(implicit connection: Connection): Seq[A] =
        if (params.isEmpty) {
            Seq.empty[A]
        } else {
            val oneRow = "(" + params.head.indices.map(_ => "?").mkString(", ") + ")"
            val sqlValues = params.indices.map(_ => oneRow).mkString(", ")
            findAll(query.replaceOrAppend(sqlValues), params.flatten: _*)(mapper)
        }
}

object SqlQueryExecutor {
    val logger = LoggerFactory.getLogger(classOf[SqlQueryExecutor])
}
