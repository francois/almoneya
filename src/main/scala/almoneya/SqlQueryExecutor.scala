package almoneya

import java.sql.{Connection, ResultSet}
import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory

import scala.util.Try

/**
  * A QueryExecutor instance that works against a JDBC connection instance.
  *
  * @param connection The Connection on which queries will be run. Transactions are the responsibility of the caller.
  */
class SqlQueryExecutor(override val connection: Connection) extends QueryExecutor {

    import SqlQueryExecutor.logger

    override def findOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[Option[A]] =
        findAll(query, params: _*)(mapper).map(_.headOption)

    override def findAll[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[Seq[A]] = Try {
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

    override def insertOne[A](query: Query, params: SqlValue*)(mapper: (ResultSet) => A): Try[A] =
        findAll(query, params: _*)(mapper).map(_.head)

    override def insertMany[A](query: Query, params: Seq[Seq[SqlValue]])(mapper: (ResultSet) => A): Try[Seq[A]] =
        if (params.isEmpty) {
            Try(Seq.empty[A])
        } else {
            val oneRow = "(" + params.head.indices.map(_ => "?").mkString(", ") + ")"
            val sqlValues = params.indices.map(_ => oneRow).mkString(", ")
            findAll(query.replaceOrAppend(sqlValues), params.flatten: _*)(mapper)
        }

    override def beginTransaction: Try[Unit] = Try(connection.setAutoCommit(false))

    override def rollback: Try[Unit] = Try {
        connection.rollback()
        connection.setAutoCommit(true)
    }

    override def commit: Try[Unit] = Try {
        connection.commit()
        connection.setAutoCommit(true)
    }
}

object SqlQueryExecutor {
    val logger = LoggerFactory.getLogger(classOf[SqlQueryExecutor])
}
