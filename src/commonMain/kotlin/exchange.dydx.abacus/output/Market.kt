package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import kollections.JsExport
import kollections.toIList
import kollections.toIMap
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class MarketStatus(
    val canTrade: Boolean,
    val canReduce: Boolean,
) {
    val canDisplay: Boolean
        get() = canTrade || canReduce
}

/* for V4 only */
@JsExport
@Serializable
data class MarketConfigsV4(
    val clobPairId: Int,
    val atomicResolution: Int,
    val stepBaseQuantums: Int,
    val quantumConversionExponent: Int,
    val subticksPerTick: Int,
)

@JsExport
@Serializable
enum class PerpetualMarketType(val rawValue: String) {
    CROSS("CROSS"),
    ISOLATED("ISOLATED");

    companion object {
        operator fun invoke(rawValue: String?) =
            PerpetualMarketType.entries.firstOrNull { it.rawValue == rawValue } ?: CROSS
    }
}

/*
depending on the timing of v3_markets socket channel and /config/markets.json,
the object may contain empty fields until both payloads are received and processed
*/
@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class MarketConfigs(
    val clobPairId: String? = null,
    val largeSize: Int? = null,
    val stepSize: Double? = null,
    val tickSize: Double? = null,
    val stepSizeDecimals: Int? = null,
    val tickSizeDecimals: Int? = null,
    val displayStepSize: Double? = null,
    val displayTickSize: Double? = null,
    val displayStepSizeDecimals: Int? = null,
    val displayTickSizeDecimals: Int? = null,
    var effectiveInitialMarginFraction: Double? = null,
    val minOrderSize: Double? = null,
    val initialMarginFraction: Double? = null,
    val maintenanceMarginFraction: Double? = null,
    val incrementalInitialMarginFraction: Double? = null,
    val incrementalPositionSize: Double? = null,
    val maxPositionSize: Double? = null,
    val basePositionNotional: Double? = null,
    val baselinePositionSize: Double? = null,
    val candleOptions: IList<CandleOption>? = null,
    val perpetualMarketType: PerpetualMarketType = PerpetualMarketType.CROSS,
    val v4: MarketConfigsV4? = null,
) {
    val maxMarketLeverage: Double
        get() {
            val imf = initialMarginFraction ?: Numeric.double.ZERO
            val effectiveImf = effectiveInitialMarginFraction ?: Numeric.double.ZERO
            return if (effectiveImf > Numeric.double.ZERO) {
                Numeric.double.ONE / effectiveImf
            } else if (imf > Numeric.double.ZERO) {
                Numeric.double.ONE / imf
            } else {
                Numeric.double.ONE
            }
        }
}

@JsExport
@Serializable
data class MarketHistoricalFunding(
    val rate: Double,
    val price: Double,
    val effectiveAtMilliseconds: Double,
)

@JsExport
@Serializable
data class MarketPerpetual(
    val volume24H: Double? = null,
    val trades24H: Double? = null,
    val volume24HUSDC: Double? = null,
    val nextFundingRate: Double? = null,
    val nextFundingAtMilliseconds: Double? = null,
    val openInterest: Double,
    val openInterestUSDC: Double,
    val openInterestLowerCap: Double? = null,
    val openInterestUpperCap: Double? = null,
    val line: IList<Double>?,
    val isNew: Boolean = false,
)

@JsExport
@Serializable
data class CandleOption(
    val stringKey: String,
    val value: String,
    val seconds: Int,
)

@JsExport
@Serializable
data class MarketCandle(
    val startedAtMilliseconds: Double,
    val updatedAtMilliseconds: Double?,
    val low: Double,
    val high: Double,
    val open: Double,
    val close: Double,
    val trades: Int? = null,
    val baseTokenVolume: Double,
    val usdVolume: Double,
)

/*
    "1MIN", "5MINS", "15MINS", "30MINS", "1HOUR", "4HOURS", "1DAY"
 */
@JsExport
@Serializable
data class MarketCandles(
    val candles: IMap<String, IList<MarketCandle>>?,
)

@JsExport
@Serializable
data class MarketTradeResources(
    val sideString: String?,
    val sideStringKey: String
)

@JsExport
@Serializable
data class MarketTrade(
    val id: String?, // in case "id" is not sent, app should still function
    val side: OrderSide,
    val size: Double,
    val price: Double,
    val type: OrderType? = null,
    val createdAtMilliseconds: Double,
    val resources: MarketTradeResources,
)

@JsExport
@Serializable
data class OrderbookLine(
    val size: Double,
    val sizeCost: Double,
    val price: Double,
    val offset: Int = 0,
    val depth: Double?,
    val depthCost: Double,
)

/*
Under extreme conditions, orderbook may be obsent, or one-sided
*/

@JsExport
@Serializable
data class MarketOrderbookGrouping(
    val multiplier: OrderbookGrouping,
    val tickSize: Double?
)

@JsExport
@Serializable
data class MarketOrderbook(
    val midPrice: Double?,
    val spreadPercent: Double?,
    val spread: Double?,
    val grouping: MarketOrderbookGrouping?,
    val asks: IList<OrderbookLine>?,
    val bids: IList<OrderbookLine>?,
)

/*
depending on the timing of v3_markets socket channel and /config/markets.json,
the object may contain empty fields until both payloads are received and processed
*/
@JsExport
@Serializable
data class PerpetualMarket(
    val id: String,
    val assetId: String,
    val market: String?,
    val displayId: String?,
    val oraclePrice: Double? = null,
    val marketCaps: Double?,
    val priceChange24H: Double?,
    val priceChange24HPercent: Double?,
    val spot24hVolume: Double? = null,
    val status: MarketStatus?,
    val configs: MarketConfigs?,
    val perpetual: MarketPerpetual?,
    val isLaunched: Boolean = true,
)

/*
depending on the timing of v3_markets socket channel and /config/markets.json,
the object may contain empty fields until both payloads are received and processed
*/
@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class PerpetualMarketSummary(
    val volume24HUSDC: Double?,
    val openInterestUSDC: Double?,
    val trades24H: Double?,
    val markets: IMap<String, PerpetualMarket>?,
) {
    companion object {
        internal fun apply(
            internalState: InternalState,
        ): PerpetualMarketSummary? {
            val marketSummaryState = internalState.marketsSummary
            if (marketSummaryState.markets.isEmpty()) {
                return null
            }
            val markets: MutableMap<String, PerpetualMarket> = mutableMapOf()
            for ((marketId, market) in marketSummaryState.markets) {
                market.perpetualMarket?.let {
                    markets[marketId] = it
                }
            }

            // add to list of markets not yet launched
            for ((assetId, asset) in internalState.assets) {
                val price = marketSummaryState.launchableMarketPrices[assetId]
                val marketId = "$assetId-USD"
                if (price == null) {
                    continue
                }

                val existingMarket = markets[marketId]
                if (existingMarket != null) {
                    markets[marketId] = existingMarket.copy(
                        marketCaps = price.market_cap,
                        spot24hVolume = price.volume_24h,
                    )
                } else {
                    val market = PerpetualMarket(
                        id = marketId,
                        assetId = asset.id,
                        market = asset.name,
                        displayId = asset.displayableAssetId,
                        oraclePrice = price.price,
                        marketCaps = price.market_cap,
                        priceChange24H = price.percent_change_24h,
                        priceChange24HPercent = price.percent_change_24h,
                        spot24hVolume = price.volume_24h,
                        status = MarketStatus(
                            canTrade = false,
                            canReduce = false,
                        ),
                        configs = null,
                        perpetual = null,
                        isLaunched = false,
                    )
                    markets[marketId] = market
                }
            }

            return PerpetualMarketSummary(
                volume24HUSDC = marketSummaryState.volume24HUSDC,
                openInterestUSDC = marketSummaryState.openInterestUSDC,
                trades24H = marketSummaryState.trades24H,
                markets = markets.toIMap(),
            )
        }
    }

    fun marketIds(): IList<String>? {
        return markets?.keys?.toIList()
    }

    fun market(id: String): PerpetualMarket? {
        return markets?.get(id)
    }
}
