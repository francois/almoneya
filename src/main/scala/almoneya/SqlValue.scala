package almoneya

import java.sql.PreparedStatement

trait SqlValue {
    def setParam(statement: PreparedStatement, index: Int): Unit
}
