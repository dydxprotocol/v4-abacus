package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class FundingPaymentProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val fundingPaymentKeyMap = mapOf(
        "string" to mapOf(
            "market" to "marketId",
        ),
        "double" to mapOf(
            "payment" to "payment",
            "rate" to "rate",
            "positionSize" to "positionSize",
            "price" to "price",
        ),
        "datetime" to mapOf(
            "effectiveAt" to "effectiveAt",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return transform(existing, payload, fundingPaymentKeyMap)
    }
}
