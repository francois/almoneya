package almoneya

trait Repository {
    def executor: QueryExecutor

    def transaction[A](fn: => A): A = {
        executor.beginTransaction()
        try {
            val results = fn
            executor.commit()
            results
        } catch {
            case ex: Throwable => executor.rollback()
                throw ex
        }
    }
}
