package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerPnlTicksResponseObject
import kotlinx.datetime.Instant

internal class HistoricalPNLsProcessor(
    parser: ParserProtocol,
    private val pnlProcessor: HistoricalPNLProcessorProtocol = HistoricalPNLProcessor(parser = parser)
) : BaseProcessor(parser) {
    private val itemProcessor: HistoricalPNLProcessor? = pnlProcessor as? HistoricalPNLProcessor

    fun process(
        existing: List<SubaccountHistoricalPNL>?,
        payload: List<IndexerPnlTicksResponseObject>,
    ): List<SubaccountHistoricalPNL>? {
        val new = payload.reversed().mapNotNull { eachPayload ->
            pnlProcessor.process(
                existing = null,
                payload = eachPayload,
            )
        }
        return merge(
            parser = parser,
            existing = existing,
            incoming = new,
            timeField = { item ->
                item?.createdAtMilliseconds?.toLong()?.let {
                    Instant.fromEpochMilliseconds(it)
                }
            },
            ascending = true,
        )
    }

    override fun received(
        existing: List<Any>?,
        payload: List<Any>
    ): List<Any>? {
        val history = mutableListOf<Any>()
        for (item in payload.reversed()) {
            parser.asNativeMap(item)?.let {
                if (itemProcessor != null) {
                    history.add(itemProcessor.received(null, it))
                }
            }
        }
        return mergeDeprecated(
            parser,
            existing,
            history,
            "createdAt",
            true,
        )
    }
}
