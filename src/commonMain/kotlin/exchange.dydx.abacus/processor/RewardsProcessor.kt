package exchange.dydx.abacus.processor

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import kollections.toIMap

@Suppress("UNCHECKED_CAST")
internal class RewardsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val rewardsParamsKeyMap = iMapOf(
        "string" to iMapOf(
            "denom" to "denom",
        ),
        "double" to iMapOf(
            "denomExponent" to "denomExponent",
            "feeMultiplierPpm" to "feeMultiplierPpm",
            "marketId" to "marketId",
        ).toIMap()
    )

    private val tokenPriceMap = iMapOf(
        "double" to iMapOf(
            "price" to "price",
            "exponent" to "exponent",
        ).toIMap()
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        return transform(existing, payload, rewardsParamsKeyMap)
    }

    fun receivedTokenPrice(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        return receivedObject(existing, "tokenPrice", payload) { existing, payload ->
            transform(parser.asMap(existing), parser.asMap(payload), tokenPriceMap)
        }
    }
}