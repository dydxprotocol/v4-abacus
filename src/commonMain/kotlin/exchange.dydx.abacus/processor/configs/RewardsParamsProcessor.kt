package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import kollections.toIMap

@Suppress("UNCHECKED_CAST")
internal class RewardsParamsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val rewardsParamsKeyMap = iMapOf(
        "string" to iMapOf(
            "denom" to "denom",
        ),
        "double" to iMapOf(
            "denomExponent" to "denomExponent",
            "feeMultiplierPpm" to "feeMultiplierPpm"
        ),
        "int" to iMapOf(
            "marketId" to "marketId"
        ).toIMap()
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        return transform(existing, payload, rewardsParamsKeyMap)
    }
}