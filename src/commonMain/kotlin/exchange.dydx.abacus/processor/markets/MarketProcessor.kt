package exchange.dydx.abacus.processor.markets

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
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
import exchange.dydx.abacus.utils.parseException
import indexer.codegen.IndexerPerpetualMarketStatus
import indexer.codegen.IndexerPerpetualMarketType
import indexer.codegen.IndexerSparklineTimePeriod
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketOraclePriceObject
import kollections.toIList
import numberOfDecimals
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

internal interface MarketProcessorProtocol : BaseProcessorProtocol {
    fun process(marketId: String, payload: IndexerCompositeMarketObject): PerpetualMarket?
    fun processOraclePrice(marketId: String, payload: IndexerWsMarketOraclePriceObject): PerpetualMarket?
    fun processSparklines(marketId: String, payload: List<String>, period: IndexerSparklineTimePeriod): PerpetualMarket?
    fun clearCachedOraclePrice(marketId: String)
}

@Suppress("UNCHECKED_CAST")
internal class MarketProcessor(
    parser: ParserProtocol,
) : BaseProcessor(parser), MarketProcessorProtocol {
    private val fallbackStepSize = 0.0001
    private val fallbackTickSize = 0.01

    private var cachedIndexerMarketResponses: MutableMap<String, IndexerCompositeMarketObject> = mutableMapOf()
    private var cachedIndexerOraclePrices: MutableMap<String, IndexerWsMarketOraclePriceObject> = mutableMapOf()
    private var cachedIndexerSparklines: MutableMap<String, List<Double>> = mutableMapOf()
    private var cachedIsNew: MutableMap<String, Boolean> = mutableMapOf()

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
        period: IndexerSparklineTimePeriod,
    ): PerpetualMarket? {
        cachedIndexerSparklines[marketId] = payload.mapNotNull { parser.asDouble(it) }.reversed()
        when (period) {
            IndexerSparklineTimePeriod.ONE_DAY -> {
                return createPerpetualMarket(marketId)
            }
            IndexerSparklineTimePeriod.SEVEN_DAYS -> {
                val sevenDaySparklineEntries = 42
                cachedIsNew[marketId] = payload.size < sevenDaySparklineEntries
                return createPerpetualMarket(marketId)
            }
        }
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
        try {
            val newValue = PerpetualMarket(
                id = name,
                assetId = MarketId.getAssetId(name) ?: parseException(payload),
                oraclePrice = oraclePrice,
                market = name,
                displayId = MarketId.getDisplayId(name),
                marketCaps = null,
                priceChange24H = parser.asDouble(payload.priceChange24H),
                priceChange24HPercent = calculatePriceChange24HPercent(
                    priceChange24H = parser.asDouble(payload.priceChange24H),
                    oraclePrice = oraclePrice,
                ),
                spot24hVolume = null,
                status = status,
                configs = createConfigs(payload),
                perpetual = createMarketPerpetual(
                    payload = payload,
                    oraclePrice = oraclePrice,
                    line = cachedIndexerSparklines[marketId],
                    isNew = cachedIsNew[marketId] ?: false,
                ),
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
            IndexerPerpetualMarketStatus.CANCEL_ONLY -> MarketStatus(
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
        var stepSize = parser.asDouble(payload.stepSize)
        var tickSize = parser.asDouble(payload.tickSize)

        val atomicResolution = parser.asInt(payload.atomicResolution)
        val stepBaseQuantums = parser.asInt(payload.stepBaseQuantums)
        val quantumConversionExponent = parser.asInt(payload.quantumConversionExponent)
        val subticksPerTick = parser.asInt(payload.subticksPerTick)
        if (stepSize == null) {
            if (atomicResolution != null && stepBaseQuantums != null) {
                stepSize = getStepSize(stepBaseQuantums, atomicResolution)
            } else {
                stepSize = fallbackStepSize
            }
        }
        if (tickSize == null) {
            if (subticksPerTick != null && quantumConversionExponent != null && atomicResolution != null) {
                tickSize = getTickSize(subticksPerTick, quantumConversionExponent, atomicResolution)
            } else {
                tickSize = fallbackTickSize
            }
        }

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
        isNew: Boolean,
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
                isNew = isNew,
            )
        } else {
            null
        }
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

    private fun getStepSize(
        stepBaseQuantums: Int,
        atomicResolution: Int,
    ): Double {
        val stepSize = stepBaseQuantums.toBigDecimal().times(10.0.toBigDecimal().pow(atomicResolution))
        return parser.asDouble(stepSize) ?: fallbackStepSize
    }

    private fun getTickSize(
        subticksPerTick: Int,
        quantumConversionExponent: Int,
        atomicResolution: Int
    ): Double {
        val quoteCurrencyAtomicResolution = -6
        val tickSize = subticksPerTick.toBigDecimal().times(
            10.0.toBigDecimal().pow(quantumConversionExponent),
        ).times(
            10.0.toBigDecimal().pow(quoteCurrencyAtomicResolution),
        ).divide(
            10.0.toBigDecimal().pow(atomicResolution),
        )
        return parser.asDouble(tickSize) ?: fallbackTickSize
    }
}
