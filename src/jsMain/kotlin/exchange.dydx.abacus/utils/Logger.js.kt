package exchange.dydx.abacus.utils

actual fun platformDebugLog(message: String) {
    console.log(message)
}

actual fun platformErrorLog(message: String) {
    console.error(message)
}
