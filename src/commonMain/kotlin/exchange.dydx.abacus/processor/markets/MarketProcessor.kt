package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iListOf
import kollections.iMutableMapOf
import kollections.toIList
import kollections.toIMap
import kollections.toIMutableMap
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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

    private val candleOptions = iListOf(
        iMapOf("stringKey" to "GERNERAL.TIME_STRINGS.1MIN", "value" to "1MIN", "seconds" to 60),
        iMapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.5MINS",
            "value" to "5MINS",
            "seconds" to 60 * 5
        ),
        iMapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.15MINS",
            "value" to "15MINS",
            "seconds" to 60 * 15
        ),
        iMapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.30MINS",
            "value" to "30MINS",
            "seconds" to 60 * 30
        ),
        iMapOf("stringKey" to "GERNERAL.TIME_STRINGS.1H", "value" to "1HOUR", "seconds" to 60 * 60),
        iMapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.4H",
            "value" to "4HOURS",
            "seconds" to 60 * 60 * 4
        ),
        iMapOf(
            "stringKey" to "GERNERAL.TIME_STRINGS.1D",
            "value" to "1DAY",
            "seconds" to 60 * 60 * 24
        )
    )

    private val configsV4KeyMap = iMapOf(
        "int" to iMapOf(
            "clobPairId" to "clobPairId",
            "atomicResolution" to "atomicResolution",
            "stepBaseQuantums" to "stepBaseQuantums",
            "quantumConversionExponent" to "quantumConversionExponent",
            "subticksPerTick" to "subticksPerTick"
        )
    )

    private val configsKeyMap = iMapOf(
        "decimal" to iMapOf(
            "maintenanceMarginFraction" to "maintenanceMarginFraction",
            "incrementalInitialMarginFraction" to "incrementalInitialMarginFraction",
            "incrementalPositionSize" to "incrementalPositionSize"
        ),
        "double" to iMapOf(
            "stepSize" to "stepSize",
            "tickSize" to "tickSize",
            "minOrderSize" to "minOrderSize",
            "initialMarginFraction" to "initialMarginFraction",
            "maxPositionSize" to "maxPositionSize",
            "baselinePositionSize" to "baselinePositionSize",
            "basePositionNotional" to "basePositionNotional",
        ),
        "int" to iMapOf(
            "clobPairId" to "clobPairId"
        )
    )

    private val displayConfigsKeyMap = iMapOf(
        "double" to iMapOf(
            "displayStepSize" to "displayStepSize",
            "displayTickSize" to "displayTickSize"
        )
    )

    private val marketKeyMap = iMapOf(
        "decimal" to iMapOf(
            "indexPrice" to "indexPrice",
            "oraclePrice" to "oraclePrice",
            "price" to "oraclePrice"
        ),
        "string" to iMapOf(
            "market" to "market",
            "ticker" to "ticker",
            "baseAsset" to "assetId"
        ),
        "double" to iMapOf(
            "priceChange24H" to "priceChange24H"
        )
    )

    private val perpetualKeyMap = iMapOf(
        "double" to iMapOf(
            "volume24H" to "volume24H",
            "openInterest" to "openInterest",
            "nextFundingRate" to "nextFundingRate"
        ),
        "datetime" to iMapOf(
            "nextFundingAt" to "nextFundingAt"
        ),
        "int" to iMapOf(
            "trades24H" to "trades24H",
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val output = transform(existing, payload, marketKeyMap)
        val indexPrice = parser.asDouble(output.get("indexPrice"))
        val oraclePrice = parser.asDouble(output.get("oraclePrice"))
        var name = parser.asString(output["market"])
        if (name == null) {
            name = parser.asString(output["ticker"])
            output.safeSet("market", name)
        }

        if (name != null) {
            output["id"] = name
            val assetId = output["assetId"]
            if (assetId == null || assetId == "") {
                val elements = name.split("-")
                if (elements.size == 2) {
                    output["assetId"] = elements.first()
                }
            }
        }
        output["status"] = status(payload)
        output["configs"] = configs(parser.asMap(existing?.get("configs")), payload)
        output["perpetual"] = perpetual(null, payload, indexPrice ?: oraclePrice)
        output.safeSet("line", line(output))
        return calculate(output)
    }

    private fun calculateNextFundingAt(): Instant {
        val now: Instant = ServerTime.now()
        val time = now.toLocalDateTime(TimeZone.UTC)
        val minute = time.minute
        val second = time.second
        val nanosecond = time.nanosecond
        val duration =
            nanosecond.toDuration(DurationUnit.NANOSECONDS) +
                    second.toDuration(DurationUnit.SECONDS) +
                    minute.toDuration(DurationUnit.MINUTES)

        return now.minus(duration).plus(1.toDuration(DurationUnit.HOURS))
    }

    internal fun receivedDelta(
        market: IMap<String, Any>?,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val modified = transform(market, payload, marketKeyMap)
        modified.safeSet("line", line(modified))

        val indexPrice = parser.asDouble(modified.get("indexPrice"))
        val oraclePrice = parser.asDouble(modified.get("oraclePrice"))
        modified["perpetual"] =
            perpetual(parser.asMap(modified["perpetual"]), payload, indexPrice ?: oraclePrice)
        return calculate(modified)
    }

    private fun calculate(market: IMap<String, Any>): IMap<String, Any> {
        val priceChange24H = parser.asDouble(market["priceChange24H"])
        val indexPrice =
            parser.asDouble(
                parser.value(market, "indexPrice") ?:
                parser.value(market,"oraclePrice")
            )
        val modified = market.mutable()
        modified.safeSet(
            "priceChange24HPercent",
            if (priceChange24H != null && indexPrice != null && indexPrice > priceChange24H) {
                val basePrice = (indexPrice - priceChange24H)
                if (basePrice > Numeric.double.ZERO) (priceChange24H / basePrice) else null
            } else
                null
        )

        return modified
    }

    internal fun receivedConfigurations(
        market: IMap<String, Any>?,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val modified = market?.mutable() ?: iMutableMapOf()
        val configs = transform(parser.asMap(market?.get("configs")), payload, displayConfigsKeyMap)
        modified.safeSet("configs", configs)
        return modified
    }

    private fun status(payload: IMap<String, Any>): IMap<String, Any> {
        val status = iMutableMapOf<String, Any>()
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

    private fun configs(existing: IMap<String, Any>?, payload: IMap<String, Any>): IMap<String, Any> {
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
                parser.asDouble(payload["basePositionSize"])
            ) // v4
        }
        return configs
    }

    private fun perpetual(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>,
        indexPrice: Double?,
    ): IMap<String, Any> {
        val perpetual = transform(existing, payload, perpetualKeyMap)
        indexPrice?.let {
            parser.asDouble(perpetual["openInterest"])?.let {
                perpetual["openInterestUSDC"] = it * indexPrice
            }
        }
        if (perpetual["nextFundingAt"] == null) {
            perpetual.safeSet("nextFundingAt", calculateNextFundingAt())
        }
        return perpetual
    }

    internal fun receivedTrades(
        market: IMap<String, Any>,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val modified = market.toIMutableMap()
        val trades = tradesProcessor.subscribed(payload)
        modified.safeSet("trades", trades)
        return modified
    }

    internal fun receivedTradesChanges(
        market: IMap<String, Any>,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val modified = market.toIMutableMap()
        val trades =
            tradesProcessor.channel_data(market["trades"] as? IList<IMap<String, Any>>, payload)
        modified.safeSet("trades", trades)
        return modified
    }

    internal fun receivedBatchedTradesChanges(
        market: IMap<String, Any>,
        payload: IList<Any>,
    ): IMap<String, Any> {
        val modified = market.toIMutableMap()
        var trades = market["trades"] as? IList<Any>
        for (partialPayload in payload) {
            parser.asMap(partialPayload)?.let { it ->
                trades = tradesProcessor.channel_data(trades, it)
            }
        }
        modified.safeSet("trades", trades)
        return modified
    }

    internal fun receivedOrderbook(
        market: IMap<String, Any>,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val modified = market.toIMutableMap()
        val orderbookRaw = orderbookProcessor.subscribed(payload)
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
        market: IMap<String, Any>,
        payload: IList<Any>,
    ): IMap<String, Any> {
        val modified = market.toIMutableMap()
        val orderbookRaw = orderbookProcessor.channel_batch_data(
            parser.asMap(market["orderbook_raw"]),
            payload
        )
        modified.safeSet("orderbook_raw", orderbookRaw)
        val stepSize = parser.asDouble(market["stepSize"]) ?: fallbackStepSize
        val orderbookConsolidated = orderbookProcessor.consolidate(orderbookRaw, stepSize)
        val tickSize = parser.asDouble(parser.value(market, "configs.tickSize")) ?: fallbackTickSize
        val orderbook = orderbookProcessor.group(orderbookConsolidated, tickSize)
        modified.safeSet("orderbook", orderbook)
        return modified
    }

    internal fun receivedCandles(
        market: IMap<String, Any>,
        payload: IList<Any>,
    ): IMap<String, Any> {
        if (payload.isNotEmpty()) {
            (parser.asMap(payload.firstOrNull()))?.let {
                parser.asString(it["resolution"])?.let { resolution ->
                    val modified = market.toIMutableMap()
                    val candles =
                        parser.asMap(market["candles"])?.toIMutableMap()
                            ?: iMutableMapOf()
                    val existingResolution =
                        parser.asList(candles[resolution])
                    val modifiedResolution = candlesProcessor.received(
                        existingResolution,
                        payload
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
        market: IMap<String, Any>,
        payload: IList<Any>,
    ): IMap<String, Any> {
        return if (payload.isNotEmpty()) {
            val modified = market.toIMutableMap()
            val perpetual =
                parser.asMap(modified["perpetual"])?.toIMutableMap() ?: iMutableMapOf()
            perpetual["line"] = payload.mapNotNull {
                parser.asDouble(it)
            }.reversed().toIList()
            modified.safeSet("perpetual", perpetual)
            modified
        } else {
            market
        }
    }

    private fun line(market: IMap<String, Any>): IList<Double>? {
        if (calculateSparklines) {
            parser.asMap(market["candles"])?.let {
                parser.asList(it["1HOUR"])?.let { candles ->
                    val now = ServerTime.now()
                    val hour = 3600.seconds
                    val day = (24 * 3600).seconds
                    val begin = now.minus(day)
                    val closes: IMutableList<Double> = candles.mapNotNull { candle ->
                        parser.asMap(candle)?.let { data ->
                            parser.asDatetime(data["startedAt"])?.let { startTime ->
                                val endTime = startTime.plus(hour)
                                if (endTime > begin) {
                                    parser.asDouble(data["close"])
                                }
                            }
                        }
                        null
                    }.toIList().mutable()
                    val indexPrice =
                        parser.asDouble(market["indexPrice"])
                            ?: parser.asDouble(market["oraclePrice"])
                    if (indexPrice != null) {
                        closes.add(indexPrice)
                    }
                    closes
                }
            }
            return null
        } else {
            return parser.asList(market.get("line")) as? IList<Double>
        }
    }

    internal fun receivedHistoricalFundings(
        market: IMap<String, Any>,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val modified = market.toIMutableMap()
        val historicalFundings =
            historicalFundingsProcessor.received(
                parser.asList(market["historicalFunding"]) as? IList<IMap<String, Any>>,
                payload
            )
        modified.safeSet("historicalFunding", historicalFundings)
        return modified
    }

    internal fun groupOrderbook(existing: IMap<String, Any>?): IMap<String, Any>? {
        return if (existing != null) {
            val modified = existing.toIMutableMap()
            val orderbookConsolidated = parser.asMap(existing["orderbook_consolidated"])
            val tickSize = parser.asDouble(parser.value(existing, "configs.tickSize")) ?: 0.01
            val orderbook = orderbookProcessor.group(orderbookConsolidated, tickSize)
            modified.safeSet("orderbook", orderbook)
            return modified
        } else null
    }
}
