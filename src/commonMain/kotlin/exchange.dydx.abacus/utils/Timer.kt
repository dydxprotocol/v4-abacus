package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.TimerProtocol
import kollections.JsExport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class Timer(
    private val delay: Duration = Duration.ZERO,
    private val repeat: Duration? = null,
    action: suspend () -> Boolean,
) {
    private var keepRunning = true
    private var job: Job? = null
    private val tryAction = suspend {
        try {
            keepRunning = action()
        } catch (_: Throwable) {
        }
    }

    fun start() {
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(delay)
            if (repeat != null) {
                while (keepRunning) {
                    tryAction()
                    delay(repeat)
                }
            } else {
                if (keepRunning) {
                    tryAction()
                }
            }
        }
    }

    /**
     * Initiates an orderly shutdown, where if the timer task is currently running,
     * we will let it finish, but not run it again.
     * Invocation has no additional effect if already shut down.
     */
    fun shutdown() {
        keepRunning = false
    }

    /**
     * Immediately stops the timer task, even if the job is currently running,
     * by cancelling the underlying Koros Job.
     */
    fun cancel() {
        shutdown()
        job?.cancel("cancel() called")
    }

    companion object {
        /**
         * Runs the given `action` after the given `delay`,
         * once the `action` completes, waits the `repeat` duration
         * and runs again, until `shutdown` is called.
         *
         * if action() throws an exception, it will be swallowed and a warning will be logged.
         */
        @OptIn(DelicateCoroutinesApi::class)
        fun schedule(
            delay: Double,
            repeat: Double? = null,
            action: () -> Boolean,
        ): Timer =
            Timer(delay.seconds, repeat?.seconds, action).also { it.start() }
    }
}

@JsExport
class LocalTimer() : LocalTimerProtocol {
    internal var timer: Timer? = null
    override fun cancel() {
        timer?.cancel()
    }
}

@JsExport
class CoroutineTimer : TimerProtocol {
    companion object {
        var instance = CoroutineTimer()
    }

    override fun schedule(delay: Double, repeat: Double?, block: () -> Boolean): LocalTimerProtocol {
        val timer = Timer.schedule(delay, repeat, block)
        val localTimer = LocalTimer()
        localTimer.timer = timer
        return localTimer
    }
}
