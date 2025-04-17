package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.mergeWithIds
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.models.IndexerCompositeFillObject

internal class FillsProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
    private val fillProcessor: FillProcessorProtocol = FillProcessor(parser = parser, localizer = localizer),
) : BaseProcessor(parser) {

    fun process(
        existing: List<SubaccountFill>?,
        payload: List<IndexerCompositeFillObject>,
        subaccountNumber: Int
    ): List<SubaccountFill> {
        val new = payload.mapNotNull { eachPayload ->
            fillProcessor.process(
                payload = eachPayload,
                subaccountNumber = subaccountNumber,
            )
        }
        existing?.let {
            return mergeWithIds(new, existing) { item -> item.id }
        }
        return new
    }
}
