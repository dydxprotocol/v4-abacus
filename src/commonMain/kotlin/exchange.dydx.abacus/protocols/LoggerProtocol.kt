package exchange.dydx.abacus.protocols

interface LoggerProtocol {
    fun verbose(text: String)
    fun debug(text: String)
    fun info(text: String)
    fun warning(text: String)
    fun error(text: String, e: Exception?)
    fun crash(text: String, e: Exception)
}
