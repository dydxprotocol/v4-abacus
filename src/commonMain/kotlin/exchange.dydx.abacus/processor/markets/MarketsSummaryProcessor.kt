package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalStatePerpetualMarket
import exchange.dydx.abacus.state.internalstate.InternalStatePerpetualMarkets
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.typedSafeSet
import indexer.codegen.IndexerPerpetualMarketStatus
import indexer.codegen.IndexerPerpetualMarketType

@Suppress("UNCHECKED_CAST")
internal class MarketsSummaryProcessor(parser: ParserProtocol, calculateSparklines: Boolean = false) :
    BaseProcessor(parser) {
    private val marketsProcessor = MarketsProcessor(parser, calculateSparklines)

    internal var groupingMultiplier: Int
        get() = marketsProcessor.groupingMultiplier
        set(value) {
            marketsProcessor.groupingMultiplier = value
        }

    internal fun subscribed(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.subscribed(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    internal fun testSubscribed(content: Map<String, Any>): InternalStatePerpetualMarkets {
        val payload = parser.asNativeMap(content["markets"])
        return if (payload != null) {
            val markets = mutableMapOf<String, InternalStatePerpetualMarket>()
            for ((market, data) in payload) {
                val marketPayload = parser.asNativeMap(data)
                if (marketPayload != null) {
                    val clobPairId = parser.asInt(marketPayload["clobPairId"]) ?: error("clobPairId is null")
                    val ticker = parser.asString(marketPayload["ticker"]) ?: error("ticker is null")
                    val status = IndexerPerpetualMarketStatus.valueOf(parser.asString(marketPayload["status"]) ?: error("status is null"))
                    val oraclePrice = parser.asDouble(marketPayload["oraclePrice"]) ?: error("oraclePrice is null")
                    val priceChange24H = parser.asDouble(marketPayload["priceChange24H"]) ?: error("priceChange24H is null")
                    val volume24H = parser.asDouble(marketPayload["volume24H"]) ?: error("volume24H is null")
                    val trades24H = parser.asInt(marketPayload["trades24H"]) ?: error("trades24H is null")
                    val nextFundingRate = parser.asDouble(marketPayload["nextFundingRate"]) ?: error("nextFundingRate is null")
                    val initialMarginFraction = parser.asDouble(marketPayload["initialMarginFraction"]) ?: error("initialMarginFraction is null")
                    val maintenanceMarginFraction = parser.asDouble(marketPayload["maintenanceMarginFraction"]) ?: error("maintenanceMarginFraction is null")
                    val openInterest = parser.asDouble(marketPayload["openInterest"]) ?: error("openInterest is null")
                    val atomicResolution = parser.asInt(marketPayload["atomicResolution"]) ?: error("atomicResolution is null")
                    val quantumConversionExponent = parser.asInt(marketPayload["quantumConversionExponent"]) ?: error("quantumConversionExponent is null")
                    val tickSize = parser.asDouble(marketPayload["tickSize"]) ?: error("tickSize is null")
                    val stepSize = parser.asDouble(marketPayload["stepSize"]) ?: error("stepSize is null")
                    val stepBaseQuantums = parser.asInt(marketPayload["stepBaseQuantums"]) ?: error("stepBaseQuantums is null")
                    val subticksPerTick = parser.asInt(marketPayload["subticksPerTick"]) ?: error("subticksPerTick is null")
                    val marketType = IndexerPerpetualMarketType.valueOf(parser.asString(marketPayload["marketType"]) ?: error("marketType is null"))
                    val openInterestLowerCap = parser.asDouble(marketPayload["openInterestLowerCap"]) ?: error("openInterestLowerCap is null")
                    val openInterestUpperCap = parser.asDouble(marketPayload["openInterestUpperCap"]) ?: error("openInterestUpperCap is null")
                    val baseOpenInterest = parser.asDouble(marketPayload["baseOpenInterest"]) ?: error("baseOpenInterest is null")

                    val receivedMarket = InternalStatePerpetualMarket(
                        clobPairId = clobPairId,
                        ticker = ticker,
                        status = status,
                        oraclePrice = oraclePrice,
                        priceChange24H = priceChange24H,
                        volume24H = volume24H,
                        trades24H = trades24H,
                        nextFundingRate = nextFundingRate,
                        initialMarginFraction = initialMarginFraction,
                        maintenanceMarginFraction = maintenanceMarginFraction,
                        openInterest = openInterest,
                        atomicResolution = atomicResolution,
                        quantumConversionExponent = quantumConversionExponent,
                        tickSize = tickSize,
                        stepSize = stepSize,
                        stepBaseQuantums = stepBaseQuantums,
                        subticksPerTick = subticksPerTick,
                        marketType = marketType,
                        openInterestLowerCap = openInterestLowerCap,
                        openInterestUpperCap = openInterestUpperCap,
                        baseOpenInterest = baseOpenInterest,
                    )
                    markets[market] = receivedMarket
                }
            }
            InternalStatePerpetualMarkets(
                markets = markets
            )
        } else {
            InternalStatePerpetualMarkets()
        }
    }

    private fun processMarketUpdate(internalStatePerpetualMarket: InternalStatePerpetualMarket, marketUpdate: Map<String, Any>, type: String): InternalStatePerpetualMarket {
        when (type) {
            "trading" -> {
                val baseOpenInterest = parser.asDouble(marketUpdate["baseOpenInterest"])
                val nextFundingRate = parser.asDouble(marketUpdate["nextFundingRate"])
                val openInterest = parser.asDouble(marketUpdate["openInterest"])
                val trades24H = parser.asInt(marketUpdate["trades24H"])
                val volume24H = parser.asDouble(marketUpdate["volume24H"])

                return internalStatePerpetualMarket.copy(
                    baseOpenInterest = baseOpenInterest ?: internalStatePerpetualMarket.baseOpenInterest,
                    nextFundingRate = nextFundingRate ?: internalStatePerpetualMarket.nextFundingRate,
                    openInterest = openInterest ?: internalStatePerpetualMarket.openInterest,
                    trades24H = trades24H ?: internalStatePerpetualMarket.trades24H,
                    volume24H = volume24H ?: internalStatePerpetualMarket.volume24H
                )
            }
            "oraclePrices" -> {
                val oraclePrice = parser.asDouble(marketUpdate["oraclePrice"])

                return internalStatePerpetualMarket.copy(
                    oraclePrice = oraclePrice ?: internalStatePerpetualMarket.oraclePrice
                )
            }
            else -> {
                error("Unsupported PerpetualMarket update type")
            }
        }
    }

    internal fun channelBatchData(internalStatePerpetualMarkets: InternalStatePerpetualMarkets, contents: List<Map<String, Map<String, Map<String, Any>>>>): InternalStatePerpetualMarkets? {
        for (item in contents) {
            for ((updateType, update) in item) {
                when (updateType) {
                    "trading" -> {
                        for ((market, marketUpdate) in update) {
                            var perpetualMarket = internalStatePerpetualMarkets.markets[market]
                            if (perpetualMarket != null) {
                                perpetualMarket = processMarketUpdate(
                                    internalStatePerpetualMarket = perpetualMarket,
                                    marketUpdate = marketUpdate,
                                    type = "trading"
                                )

                                val markets = internalStatePerpetualMarkets.markets.mutable()
                                markets.typedSafeSet(market, perpetualMarket)
                                internalStatePerpetualMarkets.markets = markets
                            }
                        }
                    }
                    "oraclePrices" -> {
                        for ((market, marketUpdate) in update) {
                            var perpetualMarket = internalStatePerpetualMarkets.markets[market]
                            if (perpetualMarket != null) {
                                perpetualMarket = processMarketUpdate(
                                    internalStatePerpetualMarket = perpetualMarket,
                                    marketUpdate = marketUpdate,
                                    type = "oraclePrices"
                                )

                                val markets = internalStatePerpetualMarkets.markets.mutable()
                                markets.typedSafeSet(market, perpetualMarket)
                                internalStatePerpetualMarkets.markets = markets
                            }
                        }
                    }
                    else -> {
                        error("Unsupported update type")
                    }
                }
            }
        }

        return null
    }


    @Suppress("FunctionName")
    internal fun channel_data(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.channel_data(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    @Suppress("FunctionName")
    internal fun channel_batch_data(
        existing: Map<String, Any>?,
        content: List<Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.channel_batch_data(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    internal fun receivedConfigurations(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val markets =
            marketsProcessor.receivedConfigurations(parser.asNativeMap(existing?.get("markets")), payload)
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

    internal fun receivedTrades(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedTrades(parser.asNativeMap(existing?.get("markets")), market, payload)
        return modify(existing, markets)
    }

    internal fun receivedTradesChanges(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedTradesChanges(
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

    internal fun receivedSparklines(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedSparklines(parser.asNativeMap(existing?.get("markets")), payload)
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
