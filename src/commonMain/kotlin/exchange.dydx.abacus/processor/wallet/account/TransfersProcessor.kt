package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class TransfersProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = TransferProcessor(parser = parser)

    override fun received(existing: List<Any>?, payload: List<Any>): List<Any> {
        val output = mutableListOf<Any>()
        val newItems = payload.mapNotNull {
            parser.asNativeMap(it)?.let { map ->
                itemProcessor.received(null, map)
            }
        }.toList()
        if (newItems != null) {
            output.addAll(newItems)
        }
        if (existing != null) {
            output.addAll(existing)
        }
        return output
    }

    override fun accountAddressChanged() {
        super.accountAddressChanged()
        itemProcessor.accountAddress = accountAddress
    }
}