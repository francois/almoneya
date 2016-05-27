package almoneya

import scala.util.{Failure, Success, Try}

trait Repository {
    def executor: QueryExecutor

    def transaction[A](fn: => Try[A]): Try[A] = {
        executor.beginTransaction
        fn match {
            case success: Success[A] =>
                executor.commit
                success

            case failure: Failure[A] =>
                executor.rollback
                failure
        }
    }
}
