package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class FundingPaymentsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = FundingPaymentProcessor(parser = parser)

    override fun received(existing: List<Any>?, payload: List<Any>): List<Any>? {
        val output = mutableListOf<Any>()
        val newItems = payload.mapNotNull {
            parser.asNativeMap(it)?.let { map ->
                itemProcessor.received(null, map)
            }
        }
        if (newItems != null) {
            output.addAll(newItems)
        }
        if (existing != null) {
            output.addAll(existing)
        }
        return output
    }
}