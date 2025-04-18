package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalMarketSummaryState
import indexer.codegen.IndexerCandleResponse
import indexer.codegen.IndexerCandleResponseObject
import indexer.codegen.IndexerHistoricalFundingResponse
import indexer.codegen.IndexerHistoricalFundingResponseObject
import indexer.codegen.IndexerOrderbookResponseObject
import indexer.codegen.IndexerSparklineTimePeriod
import indexer.codegen.IndexerTradeResponse
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketUpdateResponse
import indexer.models.IndexerWsOrderbookUpdateResponse

internal class MarketsSummaryProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : BaseProcessor(parser) {
    private val marketsProcessor = MarketsProcessor(parser)
    private val orderbookProcessor = OrderbookProcessor(parser)
    private val candlesProcessor = CandlesProcessor(parser)
    private val tradesProcessor = TradesProcessor(TradeProcessor(parser, localizer))
    private val historicalFundingsProcessor = HistoricalFundingsProcessor(parser)

    internal var groupingMultiplier: Int
        get() = orderbookProcessor.groupingMultiplier
        set(value) {
            orderbookProcessor.groupingMultiplier = value
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
        period: IndexerSparklineTimePeriod,
    ): InternalMarketSummaryState {
        marketsProcessor.processSparklines(existing, content, period)
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

    fun processHistoricalFundings(
        existing: InternalMarketSummaryState,
        payload: IndexerHistoricalFundingResponse?,
    ): InternalMarketSummaryState {
        val marketPaylaods = mutableMapOf<String, List<IndexerHistoricalFundingResponseObject>>()
        for (funding in payload?.historicalFunding?.toList() ?: emptyList()) {
            val marketId = funding.ticker
            if (marketId != null) {
                val list = marketPaylaods[marketId] ?: emptyList()
                marketPaylaods[marketId] = list + listOf(funding)
            }
        }

        for ((marketId, funding) in marketPaylaods) {
            val marketState = existing.markets[marketId] ?: InternalMarketState()
            historicalFundingsProcessor.process(
                existing = marketState,
                payload = funding,
            )
        }
        return existing
    }
}
