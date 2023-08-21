package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

@Suppress("UNCHECKED_CAST")
internal class FeeDiscountProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val feeDiscountKeyMap = iMapOf(
        "string" to iMapOf(
            "tier" to "tier",
            "symbol" to "symbol"
        ),
        "double" to iMapOf(
            "discount" to "discount"
        ),
        "int" to iMapOf(
            "balance" to "balance"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val received = transform(existing, payload, feeDiscountKeyMap)
        val tier = received["tier"]
        if (tier != null) {
            received["id"] = tier
            received["resources"] = iMapOf("stringKey" to "FEE_DISCOUNT.$tier")
        }
        return received
    }
}