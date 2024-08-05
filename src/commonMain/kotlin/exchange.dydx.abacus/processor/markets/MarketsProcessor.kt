package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.BaseProcessorProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
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
}

internal class MarketsProcessor(
    parser: ParserProtocol,
    calculateSparklines: Boolean,
    private val marketProcessor: MarketProcessorProtocol = MarketProcessor(parser, calculateSparklines)
) : BaseProcessor(parser), MarketsProcessorProtocol {
    private val marketProcessorDeprecated: MarketProcessor? = marketProcessor as? MarketProcessor

    internal var groupingMultiplier: Int
        get() = marketProcessorDeprecated!!.groupingMultiplier
        set(value) {
            marketProcessorDeprecated?.groupingMultiplier = value
        }

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
        return existing
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
        return existing
    }

    override fun processChannelBatchData(
        existing: InternalMarketSummaryState,
        content: List<IndexerWsMarketUpdateResponse>?,
    ): InternalMarketSummaryState {
        for (response in content ?: listOf()) {
            processChannelData(existing, response)
        }
        return existing
    }

    internal fun processSubscribedDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any>? {
        val payload = parser.asNativeMap(content["markets"])
        return if (payload != null) {
            received(existing, payload)
        } else {
            existing
        }
    }

    internal fun processChannelDataDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any> {
        return receivedChangesDeprecated(existing, content)
    }

    internal fun processChannelBatchDataDeprecated(
        existing: Map<String, Any>?,
        content: List<Any>
    ): Map<String, Any> {
        var data = existing ?: mapOf()
        for (partialPayload in content) {
            parser.asNativeMap(partialPayload)?.let {
                data = receivedChangesDeprecated(data, it)
            }
        }
        return data
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val markets = existing?.mutable() ?: mutableMapOf()
        for ((market, data) in payload) {
            val marketPayload = parser.asNativeMap(data)
            if (marketPayload != null) {
                val receivedMarket = marketProcessorDeprecated!!.received(
                    parser.asNativeMap(existing?.get(market)),
                    marketPayload,
                )
                markets[market] = receivedMarket
            }
        }
        return markets
    }

    private fun receivedChangesDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val markets = existing?.mutable() ?: mutableMapOf<String, Any>()
        val narrowedPayload = narrow(payload)
        for ((market, data) in narrowedPayload) {
            val marketPayload = parser.asNativeMap(data)
            if (marketPayload != null) {
                val receivedMarket = marketProcessorDeprecated!!.receivedDeltaDeprecated(
                    parser.asNativeMap(existing?.get(market)),
                    marketPayload,
                )
                markets[market] = receivedMarket
            }
        }
        return markets
    }

    private fun narrow(payload: Map<String, Any>): Map<String, Any> {
        return parser.asNativeMap(payload["trading"]) ?: parser.asNativeMap(payload["oraclePrices"])
            ?: parser.asNativeMap("markets") ?: payload
    }

    internal fun receivedConfigurationsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val markets = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((market, data) in payload) {
            val marketPayload = parser.asNativeMap(data)
            if (marketPayload == null) {
                Logger.d { "Market payload is null" }
            } else {
                val receivedMarket = marketProcessorDeprecated!!.receivedConfigurationsDeprecated(
                    parser.asNativeMap(existing?.get(market)),
                    marketPayload,
                )
                markets[market] = receivedMarket
            }
        }
        return markets
    }

    internal fun receivedOrderbook(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val marketData = parser.asNativeMap(existing?.get(market)) ?: mutableMapOf()
        val markets = existing?.mutable() ?: mutableMapOf()
        markets[market] = marketProcessorDeprecated!!.receivedOrderbook(marketData, payload)
        return markets
    }

    internal fun receivedBatchOrderbookChanges(
        existing: Map<String, Any>?,
        market: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val marketData = parser.asNativeMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] = marketProcessorDeprecated!!.receivedBatchOrderbookChanges(marketData, payload)
            markets
        } else {
            existing
        }
    }

    @Deprecated("static-typing")
    internal fun receivedTradesDeprecated(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val marketData = parser.asNativeMap(existing?.get(market)) ?: mutableMapOf()
        val markets = existing?.mutable() ?: mutableMapOf()
        markets[market] = marketProcessorDeprecated!!.receivedTradesDeprecated(marketData, payload)
        return markets
    }

    @Deprecated("static-typing")
    internal fun receivedTradesChangesDeprecated(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val marketData = parser.asNativeMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] = marketProcessorDeprecated!!.receivedTradesChangesDeprecated(marketData, payload)
            markets
        } else {
            existing
        }
    }

    internal fun receivedBatchedTradesChanges(
        existing: Map<String, Any>?,
        market: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val marketData = parser.asNativeMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] = marketProcessorDeprecated!!.receivedBatchedTradesChanges(marketData, payload)
            markets
        } else {
            existing
        }
    }

    internal fun receivedCandles(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        payload["candles"]?.let { candles ->
            parser.asNativeMap(candles)?.let {
                val modified = existing?.mutable() ?: mutableMapOf<String, Any>()
                for ((key, itemData) in it) {
                    parser.asString(key)?.let { market ->
                        parser.asNativeMap(existing?.get(market))?.let { marketData ->
                            parser.asNativeList(itemData)?.let { list ->
                                modified[market] = marketProcessorDeprecated!!.receivedCandles(marketData, list)
                            }
                        }
                    }
                }
                return modified
            }
            parser.asNativeList(candles)?.let { list ->
                parser.asNativeMap(list.firstOrNull())?.let { first ->
                    val market =
                        parser.asString(first["market"]) ?: parser.asString(first["ticker"])
                    if (market != null) {
                        val modified = existing?.mutable() ?: mutableMapOf<String, Any>()

                        parser.asNativeMap(existing?.get(market))?.let { marketData ->
                            modified[market] = marketProcessorDeprecated!!.receivedCandles(marketData, list)
                        }
                        return modified
                    }
                }
            }
        }
        return existing
    }

    internal fun receivedSparklines(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val modified = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((key, itemData) in payload) {
            parser.asString(key)?.let { market ->
                parser.asNativeMap(existing?.get(market))?.let { marketData ->
                    parser.asNativeList(itemData)?.let { list ->
                        modified[market] = marketProcessorDeprecated!!.receivedSparklines(marketData, list)
                    }
                }
            }
        }
        return modified
    }

    internal fun receivedCandles(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val marketData = parser.asNativeMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] = marketProcessorDeprecated!!.receivedCandles(marketData, resolution, payload)
            markets
        } else {
            existing
        }
    }

    internal fun receivedCandlesChanges(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val marketData = parser.asNativeMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] =
                marketProcessorDeprecated!!.receivedCandlesChanges(marketData, resolution, payload)
            markets
        } else {
            existing
        }
    }

    internal fun receivedBatchedCandlesChanges(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val marketData = parser.asNativeMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] =
                marketProcessorDeprecated!!.receivedBatchedCandlesChanges(marketData, resolution, payload)
            markets
        } else {
            existing
        }
    }

    internal fun receivedHistoricalFundings(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val market = parser.asString(
            parser.value(payload, "historicalFunding.0.market") ?: parser.value(
                payload,
                "historicalFunding.0.ticker",
            ),
        )
        if (market != null) {
            val marketData = parser.asNativeMap(existing?.get(market))
            if (existing != null && marketData != null) {
                val markets = existing.mutable()
                markets[market] = marketProcessorDeprecated!!.receivedHistoricalFundings(marketData, payload)
                return markets
            }
        }
        return existing
    }

    internal fun groupOrderbook(existing: Map<String, Any>?, market: String?): Map<String, Any>? {
        return if (existing != null) {
            val modified = existing.mutable()
            if (market != null) {
                val existingMarket = parser.asNativeMap(existing[market])
                modified.safeSet(market, marketProcessorDeprecated!!.groupOrderbook(existingMarket))
            } else {
                for ((key, value) in existing) {
                    val existingMarket = parser.asNativeMap(value)
                    modified.safeSet(key, marketProcessorDeprecated!!.groupOrderbook(existingMarket))
                }
            }
            modified
        } else {
            null
        }
    }
}
