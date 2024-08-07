package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketUpdateResponse

internal class MarketsSummaryProcessor(
    parser: ParserProtocol,
    calculateSparklines: Boolean = false
) : BaseProcessor(parser) {
    private val marketsProcessor = MarketsProcessor(parser, calculateSparklines)

    internal var groupingMultiplier: Int
        get() = marketsProcessor.groupingMultiplier
        set(value) {
            marketsProcessor.groupingMultiplier = value
        }

    fun processSubscribed(
        existing: InternalMarketSummaryState,
        content: Map<String, IndexerCompositeMarketObject>?,
    ): InternalMarketSummaryState {
        val markets = marketsProcessor.processSubscribed(existing, content)
        return existing
    }

    fun processChannelData(
        existing: InternalMarketSummaryState,
        content: IndexerWsMarketUpdateResponse?,
    ): InternalMarketSummaryState {
        val markets = marketsProcessor.processChannelData(existing, content)
        return existing
    }

    fun processChannelBatchData(
        existing: InternalMarketSummaryState,
        content: List<IndexerWsMarketUpdateResponse>?,
    ): InternalMarketSummaryState {
        val markets = marketsProcessor.processChannelBatchData(existing, content)
        return existing
    }

    fun processSparklines(
        existing: InternalMarketSummaryState,
        content: Map<String, List<String>>?,
    ): InternalMarketSummaryState {
        val markets = marketsProcessor.processSparklines(existing, content)
        return existing
    }

    internal fun subscribedDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.processSubscribedDeprecated(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    @Suppress("FunctionName")
    internal fun channel_dataDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.processChannelDataDeprecated(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    @Suppress("FunctionName")
    internal fun channel_batch_dataDeprecated(
        existing: Map<String, Any>?,
        content: List<Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.processChannelBatchDataDeprecated(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    internal fun receivedConfigurationsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val markets =
            marketsProcessor.receivedConfigurationsDeprecated(parser.asNativeMap(existing?.get("markets")), payload)
        return modify(existing, markets)!!
    }

    internal fun receivedOrderbook(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedOrderbook(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchOrderbookChanges(
        existing: Map<String, Any>?,
        market: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedBatchOrderbookChanges(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    @Deprecated("static-typing")
    internal fun receivedTradesDeprecated(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedTradesDeprecated(parser.asNativeMap(existing?.get("markets")), market, payload)
        return modify(existing, markets)
    }

    @Deprecated("static-typing")
    internal fun receivedTradesChangesDeprecated(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedTradesChangesDeprecated(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchedTradesChanges(
        existing: Map<String, Any>?,
        market: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedBatchedTradesChanges(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedCandles(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedCandles(parser.asNativeMap(existing?.get("markets")), payload)
        return modify(existing, markets)
    }

    internal fun receivedSparklinesDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedSparklinesDeprecated(parser.asNativeMap(existing?.get("markets")), payload)
        return modify(existing, markets)
    }

    internal fun receivedCandles(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedCandles(
                parser.asNativeMap(existing?.get("markets")),
                market,
                resolution,
                payload,
            )
        return modify(existing, markets)
    }

    internal fun receivedCandlesChanges(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedCandlesChanges(
            parser.asNativeMap(existing?.get("markets")),
            market,
            resolution,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchedCandlesChanges(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedBatchedCandlesChanges(
            parser.asNativeMap(existing?.get("markets")),
            market,
            resolution,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedHistoricalFundings(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedHistoricalFundings(
            parser.asNativeMap(existing?.get("markets")),
            payload,
        )
        return modify(existing, markets)
    }

    private fun modify(
        existing: Map<String, Any>?,
        markets: Map<String, Any>?,
        key: String = "markets"
    ): Map<String, Any>? {
        return if (markets != null) {
            val modified = existing?.mutable() ?: mutableMapOf()
            modified.safeSet(key, markets)
            modified
        } else {
            val modified = existing?.mutable()
            modified?.safeSet(key, null)
            modified
        }
    }

    internal fun groupOrderbook(existing: Map<String, Any>?, market: String?): Map<String, Any>? {
        val markets =
            marketsProcessor.groupOrderbook(parser.asNativeMap(existing?.get("markets")), market)
        return modify(existing, markets)
    }
}
