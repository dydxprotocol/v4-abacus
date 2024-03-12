package exchange.dydx.abacus.processor

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class RewardsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val rewardsParamsKeyMap = mapOf(
        "string" to mapOf(
            "denom" to "denom",
        ),
        "double" to mapOf(
            "denomExponent" to "denomExponent",
            "feeMultiplierPpm" to "feeMultiplierPpm",
            "marketId" to "marketId",
        ),
    )

    private val tokenPriceMap = mapOf(
        "double" to mapOf(
            "price" to "price",
            "exponent" to "exponent",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return transform(existing, payload, rewardsParamsKeyMap)
    }

    fun receivedTokenPrice(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return receivedObject(existing, "tokenPrice", payload) { existing, payload ->
            transform(parser.asNativeMap(existing), parser.asNativeMap(payload), tokenPriceMap)
        }
    }
}
