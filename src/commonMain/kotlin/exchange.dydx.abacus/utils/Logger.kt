package exchange.dydx.abacus.utils

object Logger {

    var isDebugEnabled: Boolean = false

    fun d(message: () -> String) {
        if (isDebugEnabled) {
            platformDebugLog(message())
        }
    }

    fun e(message: () -> String) {
        platformErrorLog(message())
    }
}

expect fun platformDebugLog(message: String)

expect fun platformErrorLog(message: String)
