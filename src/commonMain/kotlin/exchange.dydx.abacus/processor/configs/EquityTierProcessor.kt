package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import exchange.dydx.abacus.utils.iMapOf
import kollections.toIMap

@Suppress("UNCHECKED_CAST")
internal class EquityTierProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val equityTierKeyMap = iMapOf(
        "int" to iMapOf(
            "maxOrders" to "maxOrders"
            ),
        "decimal" to iMapOf(
            "requiredTotalNetCollateralUSD" to "requiredTotalNetCollateralUSD",
            ),
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val received = transform(existing, payload, equityTierKeyMap)

        val requiredTotalNetCollateralUSD = parser.asDecimal(payload["usdTncRequired"])
        if (requiredTotalNetCollateralUSD != null) {
            received["requiredTotalNetCollateralUSD"] = parser.asDouble(requiredTotalNetCollateralUSD / QUANTUM_MULTIPLIER)!!
        }

        val maxOrders = payload["limit"]
        if (maxOrders != null) {
            received["maxOrders"] = maxOrders
        }

        return received
    }
}