package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kotlin.time.Duration.Companion.seconds

@Suppress("UNCHECKED_CAST")
internal class MarketProcessor(parser: ParserProtocol, private val calculateSparklines: Boolean) :
    BaseProcessor(parser) {
    private val tradesProcessor = TradesProcessor(parser)
    private val orderbookProcessor = OrderbookProcessor(parser)
    private val candlesProcessor = CandlesProcessor(parser)
    private val historicalFundingsProcessor = HistoricalFundingsProcessor(parser)

    private val fallbackStepSize = 0.0001
    private val fallbackTickSize = 0.01

    internal var groupingMultiplier: Int
        get() = orderbookProcessor.groupingMultiplier
        set(value) {
            orderbookProcessor.groupingMultiplier = value
        }

    private val candleOptions = listOf(
        mapOf("stringKey" to "GERNERAL.TIME_STRINGS.1MIN", "value" to "1MIN", "seconds" to 60),
        mapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.5MINS",
            "value" to "5MINS",
            "seconds" to 60 * 5,
        ),
        mapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.15MINS",
            "value" to "15MINS",
            "seconds" to 60 * 15,
        ),
        mapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.30MINS",
            "value" to "30MINS",
            "seconds" to 60 * 30,
        ),
        mapOf("stringKey" to "GERNERAL.TIME_STRINGS.1H", "value" to "1HOUR", "seconds" to 60 * 60),
        mapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.4H",
            "value" to "4HOURS",
            "seconds" to 60 * 60 * 4,
        ),
        mapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.1D",
            "value" to "1DAY",
            "seconds" to 60 * 60 * 24,
        ),
    )

    private val configsV4KeyMap = mapOf(
        "int" to mapOf(
            "clobPairId" to "clobPairId",
            "atomicResolution" to "atomicResolution",
            "stepBaseQuantums" to "stepBaseQuantums",
            "quantumConversionExponent" to "quantumConversionExponent",
            "subticksPerTick" to "subticksPerTick",
        ),
    )

    private val configsKeyMap = mapOf(
        "string" to mapOf(
            "perpetualMarketType" to "perpetualMarketType",
        ),
        "double" to mapOf(
            "maintenanceMarginFraction" to "maintenanceMarginFraction",
            "incrementalInitialMarginFraction" to "incrementalInitialMarginFraction",
            "incrementalPositionSize" to "incrementalPositionSize",
            "stepSize" to "stepSize",
            "tickSize" to "tickSize",
            "minOrderSize" to "minOrderSize",
            "initialMarginFraction" to "initialMarginFraction",
            "maxPositionSize" to "maxPositionSize",
            "baselinePositionSize" to "baselinePositionSize",
            "basePositionNotional" to "basePositionNotional",
        ),
        "int" to mapOf(
            "clobPairId" to "clobPairId",
        ),
    )

    private val displayConfigsKeyMap = mapOf(
        "double" to mapOf(
            "displayStepSize" to "displayStepSize",
            "displayTickSize" to "displayTickSize",
        ),
    )

    private val marketKeyMap = mapOf(
        "string" to mapOf(
            "market" to "market",
            "ticker" to "ticker",
            "baseAsset" to "assetId",
        ),
        "double" to mapOf(
            "oraclePrice" to "oraclePrice",
            "price" to "oraclePrice",
            "priceChange24H" to "priceChange24H",
        ),
    )

    private val perpetualKeyMap = mapOf(
        "double" to mapOf(
            "volume24H" to "volume24H",
            "openInterest" to "openInterest",
            "openInterestLowerCap" to "openInterestLowerCap",
            "openInterestUpperCap" to "openInterestUpperCap",
            "nextFundingRate" to "nextFundingRate",
        ),
        "datetime" to mapOf(
            "nextFundingAt" to "nextFundingAt",
        ),
        "int" to mapOf(
            "trades24H" to "trades24H",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val output = transform(existing, payload, marketKeyMap)
        val oraclePrice = parser.asDouble(output.get("oraclePrice"))
        var name = parser.asString(output["market"])
        if (name == null) {
            name = parser.asString(output["ticker"])
            output.safeSet("market", name)
        }

        if (name != null) {
            output["id"] = name
            output.safeSet("assetId", MarketId.assetid(name))
        }
        output["status"] = status(payload)
        output["configs"] = configs(parser.asNativeMap(existing?.get("configs")), payload)
        output["perpetual"] = perpetual(parser.asNativeMap(existing?.get("perpetual")), payload, oraclePrice)
        output.safeSet("line", line(output))
        return calculate(output)
    }

    internal fun receivedDelta(
        market: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = transform(market, payload, marketKeyMap)
        modified.safeSet("line", line(modified))

        val oraclePrice = parser.asDouble(modified.get("oraclePrice"))
        modified["perpetual"] =
            perpetual(parser.asNativeMap(modified["perpetual"]), payload, oraclePrice)
        return calculate(modified)
    }

    private fun calculate(market: Map<String, Any>): Map<String, Any> {
        val priceChange24H = parser.asDouble(market["priceChange24H"])
        val oraclePrice =
            parser.asDouble(parser.value(market, "oraclePrice"))
        val modified = market.mutable()
        modified.safeSet(
            "priceChange24HPercent",
            if (priceChange24H != null && oraclePrice != null && oraclePrice > priceChange24H) {
                val basePrice = (oraclePrice - priceChange24H)
                if (basePrice > Numeric.double.ZERO) (priceChange24H / basePrice) else null
            } else {
                null
            },
        )

        return modified
    }

    internal fun receivedConfigurations(
        market: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = market?.mutable() ?: mutableMapOf()
        val configs =
            transform(parser.asNativeMap(market?.get("configs")), payload, displayConfigsKeyMap)
        modified.safeSet("configs", configs)
        return modified
    }

    private fun status(payload: Map<String, Any>): Map<String, Any> {
        val status = mutableMapOf<String, Any>()
        when (parser.asString(payload["status"])) {
            "ONLINE", "ACTIVE" -> {
                status["canTrade"] = true
                status["canReduce"] = true
            }

            "CLOSE_ONLY" -> {
                status["canTrade"] = false
                status["canReduce"] = true
            }

            else -> {
                status["canTrade"] = false
                status["canReduce"] = false
            }
        }
        return status
    }

    private fun configs(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val configs = transform(existing, payload, configsKeyMap)
        val configsV4 = transform(null, payload, configsV4KeyMap)
        configs.safeSet("v4", configsV4)
        val minOrderSize = parser.asDouble(configs["minOrderSize"])
        if (minOrderSize == null || minOrderSize == 0.0) {
            configs.safeSet("minOrderSize", parser.asDouble(payload["stepSize"]))
        }
        val maxPositionSize = parser.asDouble(configs["maxPositionSize"])
        configs.safeSet("maxPositionSize", maxPositionSize)
        configs["candleOptions"] = candleOptions
        if (configs["baselinePositionSize"] == null) {
            configs.safeSet(
                "baselinePositionSize",
                parser.asDouble(payload["basePositionSize"]),
            ) // v4
        }
        return configs
    }

    private fun perpetual(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        oraclePrice: Double?,
    ): Map<String, Any> {
        val perpetual = transform(existing, payload, perpetualKeyMap)
        oraclePrice?.let {
            parser.asDouble(perpetual["openInterest"])?.let {
                perpetual["openInterestUSDC"] = it * oraclePrice
            }
        }
        return perpetual
    }

    internal fun receivedTrades(
        market: Map<String, Any>,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        val trades = tradesProcessor.subscribed(payload)
        modified.safeSet("trades", trades)
        return modified
    }

    internal fun receivedTradesChanges(
        market: Map<String, Any>,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        val trades =
            tradesProcessor.channel_data(market["trades"] as? List<Map<String, Any>>, payload)
        modified.safeSet("trades", trades)
        return modified
    }

    internal fun receivedBatchedTradesChanges(
        market: Map<String, Any>,
        payload: List<Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        var trades = market["trades"] as? List<Any>
        for (partialPayload in payload) {
            parser.asNativeMap(partialPayload)?.let { it ->
                trades = tradesProcessor.channel_data(trades, it)
            }
        }
        modified.safeSet("trades", trades)
        return modified
    }

    internal fun receivedOrderbook(
        market: Map<String, Any>,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val orderbookRaw = orderbookProcessor.subscribed(payload)
        return processRawOrderbook(market, orderbookRaw)
    }

    private fun processRawOrderbook(
        market: Map<String, Any>,
        orderbookRaw: Map<String, Any>
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        modified["orderbook_raw"] = orderbookRaw
        val stepSize = parser.asDouble(parser.value(market, "configs.stepSize")) ?: fallbackStepSize
        val orderbookConsolidated = orderbookProcessor.consolidate(orderbookRaw, stepSize)
        modified.safeSet("orderbook_consolidated", orderbookConsolidated)
        val tickSize = parser.asDouble(parser.value(market, "configs.tickSize")) ?: fallbackTickSize
        val orderbook = orderbookProcessor.group(orderbookConsolidated, tickSize)
        modified.safeSet("orderbook", orderbook)
        return modified
    }

    internal fun receivedBatchOrderbookChanges(
        market: Map<String, Any>,
        payload: List<Any>,
    ): Map<String, Any> {
        val orderbookRaw = orderbookProcessor.channel_batch_data(
            parser.asNativeMap(market["orderbook_raw"]),
            payload,
        )
        return processRawOrderbook(market, orderbookRaw)
    }

    internal fun receivedCandles(
        market: Map<String, Any>,
        payload: List<Any>,
    ): Map<String, Any> {
        if (payload.isNotEmpty()) {
            (parser.asNativeMap(payload.firstOrNull()))?.let {
                parser.asString(it["resolution"])?.let { resolution ->
                    val modified = market.toMutableMap()
                    val candles =
                        parser.asNativeMap(market["candles"])?.toMutableMap()
                            ?: mutableMapOf()
                    val existingResolution =
                        parser.asNativeList(candles[resolution])
                    val modifiedResolution = candlesProcessor.received(
                        existingResolution,
                        payload,
                    )
                    candles.safeSet(resolution, modifiedResolution)
                    modified["candles"] = candles
                    if (resolution == "1HOUR") {
                        modified.safeSet("line", line(modified))
                    }
                    return modified
                }
            }
            return market
        } else {
            return market
        }
    }

    internal fun receivedSparklines(
        market: Map<String, Any>,
        payload: List<Any>,
    ): Map<String, Any> {
        return if (payload.isNotEmpty()) {
            val modified = market.toMutableMap()
            val perpetual =
                parser.asNativeMap(modified["perpetual"])?.toMutableMap() ?: mutableMapOf()
            perpetual["line"] = payload.mapNotNull {
                parser.asDouble(it)
            }.reversed().toList()
            modified.safeSet("perpetual", perpetual)
            modified
        } else {
            market
        }
    }

    internal fun receivedCandles(
        market: Map<String, Any>,
        resolution: String,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        val candles =
            candlesProcessor.subscribed(parser.asNativeMap(market["candles"]), resolution, payload)
        modified.safeSet("candles", candles)
        return modified
    }

    internal fun receivedCandlesChanges(
        market: Map<String, Any>,
        resolution: String,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        val candles =
            candlesProcessor.channel_data(
                parser.asNativeMap(market["candles"]),
                resolution,
                payload,
            )
        modified.safeSet("candles", candles)
        return modified
    }

    internal fun receivedBatchedCandlesChanges(
        market: Map<String, Any>,
        resolution: String,
        payload: List<Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        var candles = parser.asNativeMap(market["candles"])
        for (partialPayload in payload) {
            parser.asNativeMap(partialPayload)?.let { it ->
                candles = candlesProcessor.channel_data(candles, resolution, it)
            }
        }
        modified.safeSet("candles", candles)
        return modified
    }

    private fun line(market: Map<String, Any>): List<Double>? {
        if (calculateSparklines) {
            parser.asNativeMap(market["candles"])?.let {
                parser.asNativeList(it["1HOUR"])?.let { candles ->
                    val now = ServerTime.now()
                    val hour = 3600.seconds
                    val day = (24 * 3600).seconds
                    val begin = now.minus(day)
                    val closes: MutableList<Double> = candles.mapNotNull { candle ->
                        parser.asNativeMap(candle)?.let { data ->
                            parser.asDatetime(data["startedAt"])?.let { startTime ->
                                val endTime = startTime.plus(hour)
                                if (endTime > begin) {
                                    parser.asDouble(data["close"])
                                }
                            }
                        }
                        null
                    }.toList().mutable()
                    val oraclePrice = parser.asDouble(market["oraclePrice"])
                    if (oraclePrice != null) {
                        closes.add(oraclePrice)
                    }
                    closes
                }
            }
            return null
        } else {
            return parser.asNativeList(market.get("line")) as? List<Double>
        }
    }

    internal fun receivedHistoricalFundings(
        market: Map<String, Any>,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        val historicalFundings =
            historicalFundingsProcessor.received(
                parser.asNativeList(market["historicalFunding"]) as? List<Map<String, Any>>,
                payload,
            )
        modified.safeSet("historicalFunding", historicalFundings)
        return modified
    }

    internal fun groupOrderbook(existing: Map<String, Any>?): Map<String, Any>? {
        return if (existing != null) {
            val modified = existing.toMutableMap()
            val orderbookConsolidated = parser.asNativeMap(existing["orderbook_consolidated"])
            val tickSize = parser.asDouble(parser.value(existing, "configs.tickSize")) ?: 0.01
            val orderbook = orderbookProcessor.group(orderbookConsolidated, tickSize)
            modified.safeSet("orderbook", orderbook)
            return modified
        } else {
            null
        }
    }
}
