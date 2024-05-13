package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.LoggingProtocol

object Logger {
    private const val TAG = "Abacus"

    var clientLogger: LoggingProtocol? = null

    var isDebugEnabled: Boolean = false

    fun d(message: () -> String) {
        if (isDebugEnabled) {
            clientLogger?.let {
                it.d(TAG, message())
            } ?: platformDebugLog(message())
        }
    }

    fun e(message: () -> String) {
        clientLogger?.let {
            it.e(TAG, message())
        } ?: platformErrorLog(message())
    }
}

expect fun platformDebugLog(message: String)

expect fun platformErrorLog(message: String)
