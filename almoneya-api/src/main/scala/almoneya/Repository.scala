package almoneya

import java.sql.Connection

trait Repository {
    def executor: QueryExecutor
}
