package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.mergeWithIds
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerFillResponseObject

internal class FillsProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
    private val fillProcessor: FillProcessorProtocol = FillProcessor(parser = parser, localizer = localizer),
) : BaseProcessor(parser) {

    fun process(
        existing: List<SubaccountFill>?,
        payload: List<IndexerFillResponseObject>,
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

    fun receivedDeprecated(existing: List<Any>?, payload: List<Any>, subaccountNumber: Int): List<Any>? {
        val new = payload.mapNotNull { eachPayload ->
            parser.asNativeMap(eachPayload)?.let { eachPayloadData ->
                val modified = eachPayloadData.toMutableMap()

                val itemProcessor = fillProcessor as FillProcessor
                itemProcessor.receivedDeprecated(
                    null,
                    modified,
                    subaccountNumber,
                )
            }
        }
        existing?.let {
            return mergeWithIds(new, existing) { data -> parser.asNativeMap(data)?.let { parser.asString(it["id"]) } }
        }
        return new
    }
}
