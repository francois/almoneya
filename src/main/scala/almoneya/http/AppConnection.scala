package almoneya.http

import java.sql.Connection

object AppConnection {
    val currentConnection: ThreadLocal[Option[Connection]] = new ThreadLocal[Option[Connection]]() {
        override def initialValue(): Option[Connection] = None
    }
}
