package exchange.dydx.abacus.utils

class IndexerResponseParsingException(override val message: String?) : Exception()

fun <T> parseException(payload: Any?): T {
    throw IndexerResponseParsingException("failed to parse: $payload")
}
