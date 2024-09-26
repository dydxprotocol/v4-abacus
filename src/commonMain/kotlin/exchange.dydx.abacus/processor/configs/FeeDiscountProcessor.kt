package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

// Not used in the project
internal class FeeDiscountProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val feeDiscountKeyMap = mapOf(
        "string" to mapOf(
            "tier" to "tier",
            "symbol" to "symbol",
        ),
        "double" to mapOf(
            "discount" to "discount",
        ),
        "int" to mapOf(
            "balance" to "balance",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val received = transform(existing, payload, feeDiscountKeyMap)
        val tier = received["tier"]
        if (tier != null) {
            received["id"] = tier
            received["resources"] = mapOf("stringKey" to "FEE_DISCOUNT.$tier")
        }
        return received
    }
}
