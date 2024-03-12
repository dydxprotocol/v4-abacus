package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.LoggerProtocol
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier

private class NapierLogger(val tag: String, val minLevel: LogLevel) : LoggerProtocol {
    init {
        Napier.base(DebugAntilog())
    }

    private val minLevelInt = LogLevel.values().indexOf(minLevel)

    private fun shouldLog(logLevel: LogLevel): Boolean {
        return minLevelInt <= LogLevel.values().indexOf(logLevel)
    }

    override fun verbose(text: String) {
        if (shouldLog(LogLevel.VERBOSE)) {
            Napier.v(text, tag = this.tag)
        }
    }

    override fun debug(text: String) {
        if (shouldLog(LogLevel.DEBUG)) {
            Napier.d(text, tag = this.tag)
        }
    }

    override fun info(text: String) {
        if (shouldLog(LogLevel.INFO)) {
            Napier.i(text, tag = this.tag)
        }
    }

    override fun warning(text: String) {
        if (shouldLog(LogLevel.WARNING)) {
            Napier.w(text, tag = this.tag)
        }
    }

    override fun error(text: String, e: Exception?) {
        if (shouldLog(LogLevel.ERROR)) {
            Napier.e(text, e, tag = this.tag)
        }
    }

    override fun crash(text: String, e: Exception) {
        if (shouldLog(LogLevel.ASSERT)) {
            Napier.wtf(text, e, tag = this.tag)
        }
    }
}

internal class DebugLogger {
    companion object {
        private var logger: LoggerProtocol? = null
        internal fun enable(tag: String = "AbacusLogger", logLevel: LogLevel = LogLevel.INFO) {
            logger = NapierLogger(tag, logLevel)
        }
        internal fun log(text: String) {
            logger?.verbose(text)
        }

        internal fun debug(text: String) {
            logger?.debug(text)
        }

        internal fun info(text: String) {
            logger?.info(text)
        }

        internal fun warning(text: String) {
            logger?.warning(text)
        }

        internal fun error(text: String, e: Exception? = null) {
            logger?.error(text, e)
        }

        internal fun crash(text: String, e: Exception) {
            logger?.crash(text, e)
        }
    }
}
