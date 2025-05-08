package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.BaseProcessorProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalMarketSummaryState
import indexer.codegen.IndexerSparklineTimePeriod
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketUpdateResponse

internal interface MarketsProcessorProtocol : BaseProcessorProtocol {
    fun processSubscribed(
        existing: InternalMarketSummaryState,
        content: Map<String, IndexerCompositeMarketObject>?
    ): InternalMarketSummaryState

    fun processChannelData(
        existing: InternalMarketSummaryState,
        content: IndexerWsMarketUpdateResponse?,
    ): InternalMarketSummaryState

    fun processChannelBatchData(
        existing: InternalMarketSummaryState,
        content: List<IndexerWsMarketUpdateResponse>?,
    ): InternalMarketSummaryState

    fun processSparklines(
        existing: InternalMarketSummaryState,
        content: Map<String, List<String>>?,
        period: IndexerSparklineTimePeriod,
    ): InternalMarketSummaryState
}

internal class MarketsProcessor(
    parser: ParserProtocol,
    private val marketProcessor: MarketProcessorProtocol = MarketProcessor(parser)
) : BaseProcessor(parser), MarketsProcessorProtocol {

    override fun processSubscribed(
        existing: InternalMarketSummaryState,
        content: Map<String, IndexerCompositeMarketObject>?
    ): InternalMarketSummaryState {
        for ((marketId, marketData) in content ?: mapOf()) {
            marketProcessor.clearCachedOraclePrice(marketId) // Clear cached oracle price if it's new subscription
            val receivedMarket = marketProcessor.process(
                marketId = marketId,
                payload = marketData,
            )
            val marketState = existing.markets[marketId] ?: InternalMarketState()
            marketState.perpetualMarket = receivedMarket
            existing.markets[marketId] = marketState
        }
        return updateMarketIsNew(existing)
    }

    override fun processChannelData(
        existing: InternalMarketSummaryState,
        content: IndexerWsMarketUpdateResponse?,
    ): InternalMarketSummaryState {
        if (content != null) {
            if (!content.trading.isNullOrEmpty()) {
                for ((marketId, marketData) in content.trading) {
                    val marketState = existing.markets[marketId] ?: InternalMarketState()
                    val receivedMarket = marketProcessor.process(
                        marketId = marketId,
                        payload = marketData,
                    )
                    if (receivedMarket != marketState.perpetualMarket) {
                        marketState.perpetualMarket = receivedMarket
                        existing.markets[marketId] = marketState
                    }
                }
            }
            if (!content.oraclePrices.isNullOrEmpty()) {
                for ((marketId, oracleData) in content.oraclePrices) {
                    val marketState = existing.markets[marketId] ?: InternalMarketState()
                    val receivedMarket = marketProcessor.processOraclePrice(
                        marketId = marketId,
                        payload = oracleData,
                    )
                    if (receivedMarket != marketState.perpetualMarket) {
                        marketState.perpetualMarket = receivedMarket
                        existing.markets[marketId] = marketState
                    }
                }
            }
        }
        return updateMarketIsNew(existing)
    }

    override fun processChannelBatchData(
        existing: InternalMarketSummaryState,
        content: List<IndexerWsMarketUpdateResponse>?,
    ): InternalMarketSummaryState {
        for (response in content ?: listOf()) {
            processChannelData(existing, response)
        }
        return updateMarketIsNew(existing)
    }

    override fun processSparklines(
        existing: InternalMarketSummaryState,
        content: Map<String, List<String>>?,
        period: IndexerSparklineTimePeriod,
    ): InternalMarketSummaryState {
        for ((marketId, sparklines) in content ?: mapOf()) {
            val marketState = existing.markets[marketId] ?: InternalMarketState()
            val receivedMarket = marketProcessor.processSparklines(
                marketId = marketId,
                payload = sparklines,
                period = period,
            )
            if (receivedMarket != marketState.perpetualMarket) {
                marketState.perpetualMarket = receivedMarket
                existing.markets[marketId] = marketState
            }
        }
        return existing
    }

    private fun updateMarketIsNew(
        existing: InternalMarketSummaryState,
    ): InternalMarketSummaryState {
        val allClobIds: List<String> = existing.markets.values
            .mapNotNull { it.perpetualMarket?.configs?.clobPairId }
            .sortedByDescending { parser.asLong(it) }
        if (allClobIds.count() < 5) {
            return existing
        }
        val clobIds = allClobIds.subList(0, 4)

        for ((marketId, marketState) in existing.markets) {
            val isNew = clobIds.contains(marketState.perpetualMarket?.configs?.clobPairId)
            val perpetual = marketState.perpetualMarket?.perpetual
            val newPerpetual = marketState.perpetualMarket?.perpetual?.copy(
                isNew = isNew || perpetual?.isNew == true,
            )
            marketState.perpetualMarket = marketState.perpetualMarket?.copy(
                perpetual = newPerpetual,
            )
            existing.markets[marketId] = marketState
        }
        return existing
    }
}
