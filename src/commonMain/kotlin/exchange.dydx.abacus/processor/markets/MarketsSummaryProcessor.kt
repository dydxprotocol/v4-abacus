package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerCandleResponse
import indexer.codegen.IndexerCandleResponseObject
import indexer.codegen.IndexerOrderbookResponseObject
import indexer.codegen.IndexerTradeResponse
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketUpdateResponse
import indexer.models.IndexerWsOrderbookUpdateResponse

internal class MarketsSummaryProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
    calculateSparklines: Boolean = false,
    private val staticTyping: Boolean,
) : BaseProcessor(parser) {
    private val marketsProcessor = MarketsProcessor(parser, calculateSparklines)
    private val orderbookProcessor = OrderbookProcessor(parser)
    private val candlesProcessor = CandlesProcessor(parser)
    private val tradesProcessor = TradesProcessorV2(TradeProcessorV2(parser, localizer))

    internal var groupingMultiplier: Int
        get() = if (staticTyping) orderbookProcessor.groupingMultiplier else marketsProcessor.groupingMultiplier
        set(value) {
            if (staticTyping) orderbookProcessor.groupingMultiplier = value
            marketsProcessor.groupingMultiplier = value
        }

    fun processSubscribed(
        existing: InternalMarketSummaryState,
        content: Map<String, IndexerCompositeMarketObject>?,
    ): InternalMarketSummaryState {
        marketsProcessor.processSubscribed(existing, content)
        return existing
    }

    fun processChannelData(
        existing: InternalMarketSummaryState,
        content: IndexerWsMarketUpdateResponse?,
    ): InternalMarketSummaryState {
        marketsProcessor.processChannelData(existing, content)
        return existing
    }

    fun processChannelBatchData(
        existing: InternalMarketSummaryState,
        content: List<IndexerWsMarketUpdateResponse>?,
    ): InternalMarketSummaryState {
        marketsProcessor.processChannelBatchData(existing, content)
        return existing
    }

    fun processSparklines(
        existing: InternalMarketSummaryState,
        content: Map<String, List<String>>?,
    ): InternalMarketSummaryState {
        marketsProcessor.processSparklines(existing, content)
        return existing
    }

    fun processCandles(
        existing: InternalMarketSummaryState,
        marketId: String,
        resolution: String,
        content: IndexerCandleResponse?
    ): InternalMarketSummaryState {
        val marketState = existing.markets[marketId] ?: InternalMarketState()
        existing.markets[marketId] = candlesProcessor.processSubscribed(
            existing = marketState,
            resolution = resolution,
            payload = content,
        )
        return existing
    }

    fun processBatchCandlesChanges(
        existing: InternalMarketSummaryState,
        marketId: String,
        resolution: String,
        content: List<IndexerCandleResponseObject>?,
    ): InternalMarketSummaryState {
        val marketState = existing.markets[marketId] ?: InternalMarketState()
        existing.markets[marketId] = candlesProcessor.processBatchUpdate(
            existing = marketState,
            resolution = resolution,
            payload = content,
        )
        return existing
    }

    fun processCandlesChanges(
        existing: InternalMarketSummaryState,
        marketId: String,
        resolution: String,
        content: IndexerCandleResponseObject?,
    ): InternalMarketSummaryState {
        val marketState = existing.markets[marketId] ?: InternalMarketState()
        existing.markets[marketId] = candlesProcessor.processUpdate(
            existing = marketState,
            resolution = resolution,
            payload = content,
        )
        return existing
    }

    fun processOrderbook(
        existing: InternalMarketSummaryState,
        tickSize: Double?,
        marketId: String,
        content: IndexerOrderbookResponseObject?,
    ): InternalMarketSummaryState {
        val marketState = existing.markets[marketId] ?: InternalMarketState()
        existing.markets[marketId] = orderbookProcessor.processSubscribed(
            existing = marketState,
            tickSize = tickSize,
            content = content,
        )
        return existing
    }

    fun processBatchOrderbookChanges(
        existing: InternalMarketSummaryState,
        tickSize: Double?,
        marketId: String,
        content: List<IndexerWsOrderbookUpdateResponse>?,
    ): InternalMarketSummaryState {
        val marketState = existing.markets[marketId] ?: InternalMarketState()
        existing.markets[marketId] = orderbookProcessor.processChannelBatchData(
            existing = marketState,
            tickSize = tickSize,
            content = content,
        )
        return existing
    }

    fun groupOrderbook(
        existing: InternalMarketSummaryState,
        tickSize: Double?,
        marketId: String,
        groupingMultiplier: Int,
    ): InternalMarketSummaryState {
        val marketState = existing.markets[marketId] ?: InternalMarketState()
        existing.markets[marketId] = orderbookProcessor.processGrouping(
            existing = marketState,
            tickSize = tickSize,
            groupingMultiplier = groupingMultiplier,
        )
        return existing
    }

    fun processTradesSubscribed(
        existing: InternalMarketSummaryState,
        marketId: String,
        content: IndexerTradeResponse?
    ): InternalMarketSummaryState {
        val marketState = existing.markets[marketId] ?: InternalMarketState()
        existing.markets[marketId] = tradesProcessor.processSubscribed(
            existing = marketState,
            payload = content,
        )
        return existing
    }

    fun processTradesUpdates(
        existing: InternalMarketSummaryState,
        marketId: String,
        content: IndexerTradeResponse?
    ): InternalMarketSummaryState {
        val marketState = existing.markets[marketId] ?: InternalMarketState()
        existing.markets[marketId] = tradesProcessor.processChannelData(
            existing = marketState,
            payload = content,
        )
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

    internal fun receivedOrderbookDeprecated(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedOrderbookDeprecated(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchOrderbookChangesDeprecated(
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

    internal fun receivedCandlesDeprecated(
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

    internal fun groupOrderbookDeprecated(existing: Map<String, Any>?, market: String?): Map<String, Any>? {
        val markets =
            marketsProcessor.groupOrderbook(parser.asNativeMap(existing?.get("markets")), market)
        return modify(existing, markets)
    }
}
