package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import kollections.toIMap

@Suppress("UNCHECKED_CAST")
internal class EquityTierProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val equityTierKeyMap = iMapOf(
        "decimal" to iMapOf(
            "requiredTotalNetCollateralUSD" to "requiredTotalNetCollateralUSD",
        ),
        "int" to iMapOf(
            "maxOrders" to "maxOrders"
        ).toIMap()
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val received = transform(existing, payload, equityTierKeyMap)

        val requiredTotalNetCollateralUSD = payload["usdTncRequired"]
        if (requiredTotalNetCollateralUSD != null) {
            received["requiredTotalNetCollateralUSD"] = requiredTotalNetCollateralUSD
        }

        val maxOrders = payload["limit"]
        if (maxOrders != null) {
            received["maxOrders"] = maxOrders
        }

        return received
    }
}