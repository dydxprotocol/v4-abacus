package exchange.dydx.abacus.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class ServerTime {
    companion object {
        /*
        Use overWrite for time sensitive unit tests.
        For example, return objects of HistoricalPNL are based on now().
         */
        internal var overWrite: Instant? = null
        internal var delta: Duration? = null

        internal fun now(): Instant {
            val overWrite = overWrite
            return if (overWrite != null) {
                overWrite
            } else {
                val now = Clock.System.now()
                val delta = delta
                if (delta != null) now.plus(delta) else now
            }
        }
    }
}
