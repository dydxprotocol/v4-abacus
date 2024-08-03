package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.output.MarketConfigs
import exchange.dydx.abacus.output.MarketConfigsV4
import exchange.dydx.abacus.output.MarketPerpetual
import exchange.dydx.abacus.output.MarketStatus
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.PerpetualMarketType
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.BaseProcessorProtocol
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IndexerResponseParsingException
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.parseException
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerPerpetualMarketStatus
import indexer.codegen.IndexerPerpetualMarketType
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketOraclePriceObject
import kollections.toIList
import numberOfDecimals
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

internal interface MarketProcessorProtocol : BaseProcessorProtocol {
    fun process(marketId: String, payload: IndexerCompositeMarketObject): PerpetualMarket?
    fun processOraclePrice(marketId: String, payload: IndexerWsMarketOraclePriceObject): PerpetualMarket?
    fun processSparklines(marketId: String, payload: List<String>): PerpetualMarket?
    fun clearCachedOraclePrice(marketId: String)
}

@Suppress("UNCHECKED_CAST")
internal class MarketProcessor(
    parser: ParserProtocol,
    private val calculateSparklines: Boolean,
) : BaseProcessor(parser), MarketProcessorProtocol {
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
            "marketType" to "perpetualMarketType",
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

    private var cachedIndexerMarketResponses: MutableMap<String, IndexerCompositeMarketObject> = mutableMapOf()
    private var cachedIndexerOraclePrices: MutableMap<String, IndexerWsMarketOraclePriceObject> = mutableMapOf()
    private var cachedIndexerSparklines: MutableMap<String, List<Double>> = mutableMapOf()

    override fun process(
        marketId: String,
        payload: IndexerCompositeMarketObject,
    ): PerpetualMarket? {
        val cached = cachedIndexerMarketResponses[marketId]
        if (cached != null) {
            cachedIndexerMarketResponses[marketId] = cached.copyNotNulls(payload)
        } else {
            cachedIndexerMarketResponses[marketId] = payload
        }
        return createPerpetualMarket(marketId)
    }

    override fun processOraclePrice(
        marketId: String,
        payload: IndexerWsMarketOraclePriceObject
    ): PerpetualMarket? {
        cachedIndexerOraclePrices[marketId] = payload
        return createPerpetualMarket(marketId)
    }

    override fun processSparklines(
        marketId: String,
        payload: List<String>,
    ): PerpetualMarket? {
        cachedIndexerSparklines[marketId] = payload.mapNotNull { parser.asDouble(it) }.reversed()
        return createPerpetualMarket(marketId)
    }

    override fun clearCachedOraclePrice(
        marketId: String,
    ) {
        cachedIndexerOraclePrices.remove(marketId)
    }

    private fun createPerpetualMarket(
        marketId: String,
    ): PerpetualMarket? {
        val cachedIndexerMarketResponse = cachedIndexerMarketResponses[marketId]
        val cachedIndexerOraclePrice = cachedIndexerOraclePrices[marketId]
        val payload = cachedIndexerMarketResponse ?: return null
        val name = parser.asString(payload.ticker) ?: return null
        val oraclePrice = parser.asDouble(cachedIndexerOraclePrice?.oraclePrice) ?: parser.asDouble(payload.oraclePrice)
        val status = createStatus(payload.status)
        if (status == null || !status.canDisplay) {
            return null
        }
        try {
            val newValue = PerpetualMarket(
                id = name,
                assetId = MarketId.assetid(name) ?: parseException(payload),
                oraclePrice = oraclePrice,
                market = name,
                marketCaps = null,
                priceChange24H = parser.asDouble(payload.priceChange24H),
                priceChange24HPercent = calculatePriceChange24HPercent(
                    parser.asDouble(payload.priceChange24H),
                    oraclePrice,
                ),
                status = status,
                configs = createConfigs(payload),
                perpetual = createMarketPerpetual(payload, oraclePrice, cachedIndexerSparklines[marketId]),
            )
            return newValue
        } catch (e: IndexerResponseParsingException) {
            Logger.e { "${e.message}" }
            return null
        }
    }

    private fun createStatus(
        indexerStatus: IndexerPerpetualMarketStatus?
    ): MarketStatus? {
        return when (indexerStatus) {
            IndexerPerpetualMarketStatus.ACTIVE -> MarketStatus(
                canTrade = true,
                canReduce = true,
            )
            IndexerPerpetualMarketStatus.CANCELONLY -> MarketStatus(
                canTrade = false,
                canReduce = true,
            )
            null -> null
            else -> MarketStatus(
                canTrade = false,
                canReduce = false,
            )
        }
    }

    private fun createConfigs(
        payload: IndexerCompositeMarketObject,
    ): MarketConfigs {
        val stepSize = parser.asDouble(payload.stepSize)
        val tickSize = parser.asDouble(payload.tickSize)
        return MarketConfigs(
            clobPairId = payload.clobPairId,
            largeSize = null,
            stepSize = stepSize,
            tickSize = tickSize,
            stepSizeDecimals = stepSize?.numberOfDecimals(),
            tickSizeDecimals = tickSize?.numberOfDecimals(),
            displayStepSize = stepSize,
            displayTickSize = tickSize,
            displayStepSizeDecimals = stepSize?.numberOfDecimals(),
            displayTickSizeDecimals = tickSize?.numberOfDecimals(),
            effectiveInitialMarginFraction = calculateEffectiveInitialMarginFraction(
                baseIMF = parser.asDouble(payload.initialMarginFraction),
                openInterest = parser.asDouble(payload.openInterest),
                openInterestLowerCap = parser.asDouble(payload.openInterestLowerCap),
                openInterestUpperCap = parser.asDouble(payload.openInterestUpperCap),
                oraclePrice = parser.asDouble(payload.oraclePrice),
            ),
            minOrderSize = stepSize,
            initialMarginFraction = parser.asDouble(payload.initialMarginFraction),
            maintenanceMarginFraction = parser.asDouble(payload.maintenanceMarginFraction),
            incrementalInitialMarginFraction = parser.asDouble(payload.incrementalInitialMarginFraction),
            incrementalPositionSize = parser.asDouble(payload.incrementalPositionSize),
            maxPositionSize = parser.asDouble(payload.maxPositionSize),
            basePositionNotional = null,
            baselinePositionSize = parser.asDouble(payload.basePositionSize),
            candleOptions = null,
            perpetualMarketType = when (payload.marketType) {
                IndexerPerpetualMarketType.CROSS -> PerpetualMarketType.CROSS
                IndexerPerpetualMarketType.ISOLATED -> PerpetualMarketType.ISOLATED
                else -> PerpetualMarketType.CROSS
            },
            v4 = createConfigsV4(payload),
        )
    }

    private fun createConfigsV4(
        payload: IndexerCompositeMarketObject,
    ): MarketConfigsV4? {
        val clobPairId = parser.asInt(payload.clobPairId)
        val atomicResolution = parser.asInt(payload.atomicResolution)
        val stepBaseQuantums = parser.asInt(payload.stepBaseQuantums)
        val quantumConversionExponent = parser.asInt(payload.quantumConversionExponent)
        val subticksPerTick = parser.asInt(payload.subticksPerTick)
        return if (clobPairId != null && atomicResolution != null && stepBaseQuantums != null && quantumConversionExponent != null && subticksPerTick != null) {
            MarketConfigsV4(
                clobPairId = clobPairId,
                atomicResolution = atomicResolution,
                stepBaseQuantums = stepBaseQuantums,
                quantumConversionExponent = quantumConversionExponent,
                subticksPerTick = subticksPerTick,
            )
        } else {
            null
        }
    }

    private fun createMarketPerpetual(
        payload: IndexerCompositeMarketObject,
        oraclePrice: Double?,
        line: List<Double>?,
    ): MarketPerpetual? {
        val nextFundingRate = parser.asDouble(payload.nextFundingRate)
        val openInterest = parser.asDouble(payload.openInterest)
        return if (openInterest != null) {
            MarketPerpetual(
                volume24H = parser.asDouble(payload.volume24H),
                trades24H = parser.asDouble(payload.trades24H),
                volume24HUSDC = null,
                nextFundingRate = nextFundingRate,
                nextFundingAtMilliseconds = null,
                openInterest = openInterest,
                openInterestUSDC = oraclePrice?.let { openInterest * it } ?: 0.0,
                openInterestLowerCap = parser.asDouble(payload.openInterestLowerCap),
                openInterestUpperCap = parser.asDouble(payload.openInterestUpperCap),
                line = line?.toIList(),
            )
        } else {
            null
        }
    }

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
        // This should go last as it needs to use most up-to-date perpetual and config properties
        // to calculate the effectiveInitialMarginFraction
        output.safeSet("configs.effectiveInitialMarginFraction", effectiveInitialMarginFraction(output, oraclePrice))
        return calculate(output)
    }

    private fun effectiveInitialMarginFraction(output: Map<String, Any>, oraclePrice: Double?): Double? {
        val baseIMF = parser.asDouble(parser.value(output, "configs.initialMarginFraction"))
        val openInterest = parser.asDouble(parser.value(output, "perpetual.openInterest"))
        val openInterestLowerCap = parser.asDouble(parser.value(output, "perpetual.openInterestLowerCap"))
        val openInterestUpperCap = parser.asDouble(parser.value(output, "perpetual.openInterestUpperCap"))

        return calculateEffectiveInitialMarginFraction(baseIMF, openInterest, openInterestLowerCap, openInterestUpperCap, oraclePrice)
    }

    private fun calculateEffectiveInitialMarginFraction(
        baseIMF: Double?,
        openInterest: Double?,
        openInterestLowerCap: Double?,
        openInterestUpperCap: Double?,
        oraclePrice: Double?,
    ): Double? {
        if (baseIMF === null) return null
        if (oraclePrice == null || openInterest == null || openInterestLowerCap == null || openInterestUpperCap == null) return baseIMF
        // if these are equal we can throw an error from dividing by zero
        if (openInterestUpperCap == openInterestLowerCap) return baseIMF
        val openNotional = openInterest * oraclePrice
        val scalingFactor = (openNotional - openInterestLowerCap) / (openInterestUpperCap - openInterestLowerCap)
        val imfIncrease = scalingFactor * (1 - baseIMF)

        val effectiveIMF = min(baseIMF + max(imfIncrease, 0.0), 1.0)
        return effectiveIMF
    }

    internal fun receivedDeltaDeprecated(
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
            calculatePriceChange24HPercent(priceChange24H, oraclePrice),
        )

        return modified
    }

    private fun calculatePriceChange24HPercent(
        priceChange24H: Double?,
        oraclePrice: Double?,
    ): Double? {
        return if (priceChange24H != null && oraclePrice != null && oraclePrice > priceChange24H) {
            val basePrice = (oraclePrice - priceChange24H)
            if (basePrice > Numeric.double.ZERO) (priceChange24H / basePrice) else null
        } else {
            null
        }
    }

    internal fun receivedConfigurationsDeprecated(
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
        payload: Map<String, Any>,
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

    @Deprecated("static-typing")
    internal fun receivedTradesDeprecated(
        market: Map<String, Any>,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        val trades = tradesProcessor.subscribedDeprecated(payload)
        modified.safeSet("trades", trades)
        return modified
    }

    @Deprecated("static-typing")
    internal fun receivedTradesChangesDeprecated(
        market: Map<String, Any>,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = market.toMutableMap()
        val trades =
            tradesProcessor.channel_dataDeprecated(market["trades"] as? List<Map<String, Any>>, payload)
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
            parser.asNativeMap(partialPayload)?.let {
                trades = tradesProcessor.channel_dataDeprecated(trades, it)
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

    internal fun receivedSparklinesDeprecated(
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
            parser.asNativeMap(partialPayload)?.let {
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
