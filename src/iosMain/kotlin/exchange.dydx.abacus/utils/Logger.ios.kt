package exchange.dydx.abacus.utils

actual fun platformDebugLog(message: String) {
    println("DEBUG: $message")
}

actual fun platformErrorLog(message: String) {
    println("ERROR: $message")
}
