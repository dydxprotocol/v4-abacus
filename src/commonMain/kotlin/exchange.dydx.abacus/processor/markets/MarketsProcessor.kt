package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class MarketsProcessor(
    parser: ParserProtocol,
    calculateSparklines: Boolean
) : BaseProcessor(parser) {
    private val marketProcessor = MarketProcessor(parser, calculateSparklines)

    internal var groupingMultiplier: Int
        get() = marketProcessor.groupingMultiplier
        set(value) {
            marketProcessor.groupingMultiplier = value
        }

    internal fun processSubscribed(
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

    internal fun processChannelData(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any> {
        return receivedChanges(existing, content)
    }

    internal fun processChannelBatchData(
        existing: Map<String, Any>?,
        content: List<Any>
    ): Map<String, Any> {
        var data = existing ?: mapOf()
        for (partialPayload in content) {
            parser.asNativeMap(partialPayload)?.let {
                data = receivedChanges(data, it)
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
                val receivedMarket = marketProcessor.received(
                    parser.asNativeMap(existing?.get(market)),
                    marketPayload,
                )
                markets[market] = receivedMarket
            }
        }
        return markets
    }

    private fun receivedChanges(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val markets = existing?.mutable() ?: mutableMapOf<String, Any>()
        val narrowedPayload = narrow(payload)
        for ((market, data) in narrowedPayload) {
            val marketPayload = parser.asNativeMap(data)
            if (marketPayload != null) {
                val receivedMarket = marketProcessor.receivedDelta(
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

    internal fun receivedConfigurations(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val markets = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((market, data) in payload) {
            val marketPayload = parser.asNativeMap(data)
            if (marketPayload == null) {
                Logger.d { "Market payload is null" }
            } else {
                val receivedMarket = marketProcessor.receivedConfigurations(
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
        markets[market] = marketProcessor.receivedOrderbook(marketData, payload)
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
            markets[market] = marketProcessor.receivedBatchOrderbookChanges(marketData, payload)
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
        markets[market] = marketProcessor.receivedTradesDeprecated(marketData, payload)
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
            markets[market] = marketProcessor.receivedTradesChangesDeprecated(marketData, payload)
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
            markets[market] = marketProcessor.receivedBatchedTradesChanges(marketData, payload)
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
                                modified[market] = marketProcessor.receivedCandles(marketData, list)
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
                            modified[market] = marketProcessor.receivedCandles(marketData, list)
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
                        modified[market] = marketProcessor.receivedSparklines(marketData, list)
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
            markets[market] = marketProcessor.receivedCandles(marketData, resolution, payload)
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
                marketProcessor.receivedCandlesChanges(marketData, resolution, payload)
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
                marketProcessor.receivedBatchedCandlesChanges(marketData, resolution, payload)
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
                markets[market] = marketProcessor.receivedHistoricalFundings(marketData, payload)
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
                modified.safeSet(market, marketProcessor.groupOrderbook(existingMarket))
            } else {
                for ((key, value) in existing) {
                    val existingMarket = parser.asNativeMap(value)
                    modified.safeSet(key, marketProcessor.groupOrderbook(existingMarket))
                }
            }
            modified
        } else {
            null
        }
    }
}
