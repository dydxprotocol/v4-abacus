package exchange.dydx.abacus.jvm

import exchange.dydx.abacus.utils.DebugLogger
import io.github.aakira.napier.LogLevel

class AbacusAndroid {
    companion object{
        fun enableDebug(tag: String, logLevel: Int) {
            val values = LogLevel.values()
            val napLogLevel = if (logLevel >=0 && logLevel < values.size) {
                values[logLevel]
            } else LogLevel.DEBUG
            DebugLogger.enable(tag, napLogLevel)
        }
    }
}