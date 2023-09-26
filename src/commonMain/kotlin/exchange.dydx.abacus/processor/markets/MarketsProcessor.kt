package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMapOf
import kollections.iMutableMapOf

@Suppress("UNCHECKED_CAST")
internal class MarketsProcessor(parser: ParserProtocol, calculateSparklines: Boolean) :
    BaseProcessor(parser) {
    private val marketProcessor = MarketProcessor(parser, calculateSparklines)

    internal var groupingMultiplier: Int
        get() = marketProcessor.groupingMultiplier
        set(value) {
            marketProcessor.groupingMultiplier = value
        }

    internal fun subscribed(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>
    ): IMap<String, Any>? {
        val payload = parser.asMap(content["markets"])
        return if (payload != null) {
            received(existing, payload)
        } else {
            existing
        }
    }

    internal fun channel_data(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>
    ): IMap<String, Any> {
        return receivedChanges(existing, content)
    }

    internal fun channel_batch_data(
        existing: IMap<String, Any>?,
        content: IList<Any>
    ): IMap<String, Any> {
        var data = existing ?: iMapOf()
        for (partialPayload in content) {
            parser.asMap(partialPayload)?.let {
                data = receivedChanges(data, it)
            }
        }
        return data
    }

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val markets = existing?.mutable() ?: iMutableMapOf()
        for ((market, data) in payload) {
            val marketPayload = parser.asMap(data)
            if (marketPayload != null) {
                val receivedMarket = marketProcessor.received(
                    parser.asMap(existing?.get(market)),
                    marketPayload
                )
                markets[market] = receivedMarket
            }
        }
        return markets
    }

    private fun receivedChanges(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val markets = existing?.mutable() ?: iMutableMapOf<String, Any>()
        val narrowedPayload = narrow(payload)
        for ((market, data) in narrowedPayload) {
            val marketPayload = parser.asMap(data)
            if (marketPayload != null) {
                val receivedMarket = marketProcessor.receivedDelta(
                    parser.asMap(existing?.get(market)),
                    marketPayload
                )
                markets[market] = receivedMarket
            }
        }
        return markets
    }

    private fun narrow(payload: IMap<String, Any>): IMap<String, Any> {
        return parser.asMap(payload["trading"]) ?: parser.asMap(payload["oraclePrices"])
        ?: parser.asMap("markets") ?: payload
    }

    internal fun receivedConfigurations(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val markets = existing?.mutable() ?: iMutableMapOf<String, Any>()
        for ((market, data) in payload) {
            val marketPayload = parser.asMap(data)
            if (marketPayload == null) {
                DebugLogger.warning("Market payload is null")
            } else {
                val receivedMarket = marketProcessor.receivedConfigurations(
                    parser.asMap(existing?.get(market)),
                    marketPayload
                )
                markets[market] = receivedMarket
            }
        }
        return markets
    }

    internal fun receivedOrderbook(
        existing: IMap<String, Any>?,
        market: String,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val marketData = parser.asMap(existing?.get(market)) ?: iMutableMapOf()
        val markets = existing?.mutable() ?: iMutableMapOf()
        markets[market] = marketProcessor.receivedOrderbook(marketData, payload)
        return markets
    }

    internal fun receivedBatchOrderbookChanges(
        existing: IMap<String, Any>?,
        market: String,
        payload: IList<Any>
    ): IMap<String, Any>? {
        val marketData = parser.asMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] = marketProcessor.receivedBatchOrderbookChanges(marketData, payload)
            markets
        } else {
            existing
        }
    }

    internal fun receivedTrades(
        existing: IMap<String, Any>?,
        market: String,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val marketData = parser.asMap(existing?.get(market)) ?: iMutableMapOf()
        val markets = existing?.mutable() ?: iMutableMapOf()
        markets[market] = marketProcessor.receivedTrades(marketData, payload)
        return markets
    }

    internal fun receivedTradesChanges(
        existing: IMap<String, Any>?,
        market: String,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val marketData = parser.asMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] = marketProcessor.receivedTradesChanges(marketData, payload)
            markets
        } else {
            existing
        }
    }


    internal fun receivedBatchedTradesChanges(
        existing: IMap<String, Any>?,
        market: String,
        payload: IList<Any>
    ): IMap<String, Any>? {
        val marketData = parser.asMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] = marketProcessor.receivedBatchedTradesChanges(marketData, payload)
            markets
        } else {
            existing
        }
    }


    internal fun receivedCandles(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        payload["candles"]?.let { candles ->
            parser.asMap(candles)?.let {
                val modified = existing?.mutable() ?: iMutableMapOf<String, Any>()
                for ((key, itemData) in it) {
                    parser.asString(key)?.let { market ->
                        parser.asMap(existing?.get(market))?.let { marketData ->
                            parser.asList(itemData)?.let { list ->
                                modified[market] = marketProcessor.receivedCandles(marketData, list)
                            }
                        }
                    }
                }
                return modified
            }
            parser.asList(candles)?.let { list ->
                parser.asMap(list.firstOrNull())?.let { first ->
                    val market =
                        parser.asString(first["market"]) ?: parser.asString(first["ticker"])
                    if (market != null) {
                        val modified = existing?.mutable() ?: iMutableMapOf<String, Any>()

                        parser.asMap(existing?.get(market))?.let { marketData ->
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
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val modified = existing?.mutable() ?: iMutableMapOf<String, Any>()
        for ((key, itemData) in payload) {
            parser.asString(key)?.let { market ->
                parser.asMap(existing?.get(market))?.let { marketData ->
                    parser.asList(itemData)?.let { list ->
                        modified[market] = marketProcessor.receivedSparklines(marketData, list)
                    }
                }
            }
        }
        return modified
    }

    internal fun receivedCandles(
        existing: IMap<String, Any>?,
        market: String,
        resolution: String,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val marketData = parser.asMap(existing?.get(market))
        return if (existing != null && marketData != null) {
            val markets = existing.mutable()
            markets[market] = marketProcessor.receivedCandles(marketData, resolution, payload)
            markets
        } else {
            existing
        }
    }

    internal fun receivedCandlesChanges(
        existing: IMap<String, Any>?,
        market: String,
        resolution: String,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val marketData = parser.asMap(existing?.get(market))
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
        existing: IMap<String, Any>?,
        market: String,
        resolution: String,
        payload: IList<Any>
    ): IMap<String, Any>? {
        val marketData = parser.asMap(existing?.get(market))
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
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val market = parser.asString(
            parser.value(payload, "historicalFunding.0.market") ?: parser.value(
                payload,
                "historicalFunding.0.ticker"
            )
        )
        if (market != null) {
            val marketData = parser.asMap(existing?.get(market))
            if (existing != null && marketData != null) {
                val markets = existing.mutable()
                markets[market] = marketProcessor.receivedHistoricalFundings(marketData, payload)
                return markets
            }
        }
        return existing
    }

    internal fun groupOrderbook(existing: IMap<String, Any>?, market: String?): IMap<String, Any>? {
        return if (existing != null) {
            val modified = existing.mutable()
            if (market != null) {
                val existingMarket = parser.asMap(existing[market])
                modified.safeSet(market, marketProcessor.groupOrderbook(existingMarket))
            } else {
                for ((key, value) in existing) {
                    val existingMarket = parser.asMap(value)
                    modified.safeSet(key, marketProcessor.groupOrderbook(existingMarket))
                }
            }
            modified
        } else null
    }
}
