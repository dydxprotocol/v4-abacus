package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

internal class FundingPaymentProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val fundingPaymentKeyMap = iMapOf(
        "string" to iMapOf(
            "market" to "marketId"
        ),
        "double" to iMapOf(
            "payment" to "payment",
            "rate" to "rate",
            "positionSize" to "positionSize",
            "price" to "price"
        ),
        "datetime" to iMapOf(
            "effectiveAt" to "effectiveAt"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        return transform(existing, payload, fundingPaymentKeyMap)
    }
}