package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import kollections.iMutableListOf
import kollections.toIList

internal class TransfersProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = TransferProcessor(parser = parser)

    override fun received(existing: IList<Any>?, payload: IList<Any>): IList<Any> {
        val output = iMutableListOf<Any>()
        val newItems = payload.mapNotNull {
            parser.asMap(it)?.let { map ->
                itemProcessor.received(null, map)
            }
        }.toIList()
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