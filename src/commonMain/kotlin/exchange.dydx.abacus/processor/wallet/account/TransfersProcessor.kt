package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerTransferResponseObject

internal class TransfersProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
    private val itemProcessor: TransferProcessorProtocol = TransferProcessor(parser = parser, localizer = localizer),
) : BaseProcessor(parser) {

    fun process(
        existing: List<SubaccountTransfer>?,
        payload: List<IndexerTransferResponseObject>,
    ): List<SubaccountTransfer> {
        var output = mutableListOf<SubaccountTransfer>()
        val newItems = payload.mapNotNull {
            itemProcessor.process(it)
        }.toList()
        if (newItems != null) {
            output.addAll(newItems)
        }
        if (existing != null) {
            for (item in existing) {
                if (!output.contains(item)) {
                    output.add(item)
                }
            }
        }
        return output.sortedByDescending { it.updatedAtMilliseconds }
    }

    override fun received(existing: List<Any>?, payload: List<Any>): List<Any> {
        val output = mutableListOf<Any>()
        val newItems = payload.mapNotNull {
            parser.asNativeMap(it)?.let { map ->
                val itemProcessor = itemProcessor as TransferProcessor
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
