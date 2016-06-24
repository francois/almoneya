package almoneya.utils

import java.util.concurrent.TimeUnit

import org.slf4j.Logger

trait Instrumented {

    val timingLog: Logger

    def time[A](name: String)(fn: => A): A = {
        val startAt = System.nanoTime
        try {
            fn
        } finally {
            val endAt = System.nanoTime()
            timingLog.info("{}: {} ms", name, TimeUnit.NANOSECONDS.toMillis(endAt - startAt).asInstanceOf[AnyRef], null)
        }
    }
}
