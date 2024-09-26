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

    fun e(context: Map<String, Any>? = null, error: Error? = null, message: () -> String) {
        clientLogger?.let {
            it.e(TAG, message(), context?.toJsonObject(), error)
        } ?: platformErrorLog(message())
    }

    fun ddInfo(context: Map<String, Any>? = null, message: () -> String) {
        if (isDebugEnabled) {
            clientLogger?.let {
                it.ddInfo(TAG, message(), context?.toJsonObject())
            }
        }
    }
}

expect fun platformDebugLog(message: String)

expect fun platformErrorLog(message: String)
