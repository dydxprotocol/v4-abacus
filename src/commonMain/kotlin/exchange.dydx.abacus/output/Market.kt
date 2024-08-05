package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.typedSafeSet
import kollections.JsExport
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIList
import kollections.toIMap
import kotlinx.serialization.Serializable
import numberOfDecimals

@JsExport
@Serializable
data class MarketStatus(
    val canTrade: Boolean,
    val canReduce: Boolean,
) {
    companion object {
        internal fun create(
            existing: MarketStatus?,
            parser: ParserProtocol,
            data: Map<String, Any>,
        ): MarketStatus? {
            val canTrade = parser.asBool(data["canTrade"])
            val canReduce = parser.asBool(data["canReduce"])
            return if (canTrade != null && canReduce != null) {
                if (existing?.canTrade != canTrade ||
                    existing.canReduce != canReduce
                ) {
                    MarketStatus(canTrade, canReduce)
                } else {
                    existing
                }
            } else {
                print("Market Status not valid")
                null
            }
        }
    }

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
) {
    companion object {
        internal fun create(
            existing: MarketConfigsV4?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
        ): MarketConfigsV4? {
            return if (data != null) {
                val clobPairId = parser.asInt(data["clobPairId"]) ?: return null
                val atomicResolution = parser.asInt(data["atomicResolution"]) ?: return null
                val stepBaseQuantums = parser.asInt(data["stepBaseQuantums"]) ?: return null
                val quantumConversionExponent =
                    parser.asInt(data["quantumConversionExponent"]) ?: return null
                val subticksPerTick = parser.asInt(data["subticksPerTick"]) ?: return null

                if (existing == null ||
                    existing.clobPairId != clobPairId ||
                    existing.atomicResolution != atomicResolution ||
                    existing.stepBaseQuantums != stepBaseQuantums ||
                    existing.quantumConversionExponent != quantumConversionExponent ||
                    existing.subticksPerTick != subticksPerTick
                ) {
                    MarketConfigsV4(
                        clobPairId,
                        atomicResolution,
                        stepBaseQuantums,
                        quantumConversionExponent,
                        subticksPerTick,
                    )
                } else {
                    existing
                }
            } else {
                null
            }
        }
    }
}

@JsExport
@Serializable
enum class PerpetualMarketType(val rawValue: String) {
    CROSS("CROSS"),
    ISOLATED("ISOLATED");

    companion object {
        operator fun invoke(rawValue: String?) =
            PerpetualMarketType.values().firstOrNull { it.rawValue == rawValue } ?: CROSS
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
    companion object {
        internal fun create(
            existing: MarketConfigs?,
            parser: ParserProtocol,
            data: Map<String, Any>,
        ): MarketConfigs? {
            val clobPairId = parser.asString(data["clobPairId"]) ?: return null
            val largeSize = parser.asInt(data["largeSize"])
            val stepSize = parser.asDouble(data["stepSize"]) ?: return null
            val tickSize = parser.asDouble(data["tickSize"]) ?: return null
            val displayStepSize = parser.asDouble(data["displayStepSize"]) ?: stepSize
            val displayTickSize = parser.asDouble(data["displayTickSize"]) ?: tickSize
            val minOrderSize = parser.asDouble(data["minOrderSize"])
            val initialMarginFraction =
                parser.asDouble(data["initialMarginFraction"]) ?: return null
            val maintenanceMarginFraction =
                parser.asDouble(data["maintenanceMarginFraction"]) ?: return null
            val incrementalInitialMarginFraction =
                parser.asDouble(data["incrementalInitialMarginFraction"])
            val incrementalPositionSize = parser.asDouble(data["incrementalPositionSize"])
            val maxPositionSize = parser.asDouble(data["maxPositionSize"])
            val basePositionNotional = parser.asDouble(data["basePositionNotional"])
            val baselinePositionSize = parser.asDouble(data["baselinePositionSize"])
            val candleOptions = CandleOption.create(
                existing?.candleOptions,
                parser,
                parser.asList(data["candleOptions"]) as? IList<IMap<String, Any>>,
            )
            val perpetualMarketType = PerpetualMarketType.invoke(
                parser.asString(data["perpetualMarketType"]),
            )
            var effectiveInitialMarginFraction = parser.asDouble(data["effectiveInitialMarginFraction"])
            val v4 = MarketConfigsV4.create(
                existing?.v4,
                parser,
                parser.asMap(data["v4"]),
            ) ?: return null

            return if (existing == null ||
                existing.largeSize != largeSize ||
                existing.stepSize != stepSize ||
                existing.tickSize != tickSize ||
                existing.displayStepSize != displayStepSize ||
                existing.displayTickSize != displayTickSize ||
                existing.minOrderSize != minOrderSize ||
                existing.initialMarginFraction != initialMarginFraction ||
                existing.maintenanceMarginFraction != maintenanceMarginFraction ||
                existing.incrementalInitialMarginFraction != incrementalInitialMarginFraction ||
                existing.incrementalPositionSize != incrementalPositionSize ||
                existing.maxPositionSize != maxPositionSize ||
                existing.baselinePositionSize != baselinePositionSize ||
                existing.basePositionNotional != basePositionNotional ||
                existing.candleOptions != candleOptions ||
                existing.perpetualMarketType != perpetualMarketType ||
                existing.v4 != v4 ||
                existing.effectiveInitialMarginFraction != effectiveInitialMarginFraction
            ) {
                MarketConfigs(
                    clobPairId = clobPairId,
                    largeSize = largeSize,
                    stepSize = stepSize,
                    tickSize = tickSize,
                    stepSizeDecimals = stepSize.numberOfDecimals(),
                    tickSizeDecimals = tickSize.numberOfDecimals(),
                    displayStepSize = displayStepSize,
                    displayTickSize = displayTickSize,
                    displayStepSizeDecimals = displayStepSize.numberOfDecimals(),
                    displayTickSizeDecimals = displayTickSize.numberOfDecimals(),
                    effectiveInitialMarginFraction = effectiveInitialMarginFraction,
                    minOrderSize = minOrderSize,
                    initialMarginFraction = initialMarginFraction,
                    maintenanceMarginFraction = maintenanceMarginFraction,
                    incrementalInitialMarginFraction = incrementalInitialMarginFraction,
                    incrementalPositionSize = incrementalPositionSize,
                    maxPositionSize = maxPositionSize,
                    basePositionNotional = basePositionNotional,
                    baselinePositionSize = baselinePositionSize,
                    candleOptions = candleOptions,
                    perpetualMarketType = perpetualMarketType,
                    v4 = v4,
                )
            } else {
                existing
            }
        }
    }
}

@JsExport
@Serializable
data class MarketHistoricalFunding(
    val rate: Double,
    val price: Double,
    val effectiveAtMilliseconds: Double,
) {
    companion object {
        internal fun create(
            existing: MarketHistoricalFunding?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): MarketHistoricalFunding? {
            Logger.d { "creating Market Historical Funding\n" }
            data?.let {
                val rate = parser.asDouble(data["rate"])
                val price = parser.asDouble(data["price"])
                val effectiveAtMilliseconds =
                    parser.asDatetime(data["effectiveAt"])?.toEpochMilliseconds()?.toDouble()
                if (price != null && rate != null && effectiveAtMilliseconds != null) {
                    return if (existing?.rate != rate ||
                        existing.price != price ||
                        existing.effectiveAtMilliseconds != effectiveAtMilliseconds
                    ) {
                        MarketHistoricalFunding(rate, price, effectiveAtMilliseconds)
                    } else {
                        existing
                    }
                }
            }
            print("Market Historical Funding not valid")
            return null
        }

        fun create(
            existing: IList<MarketHistoricalFunding>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
        ): IList<MarketHistoricalFunding>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as MarketHistoricalFunding).effectiveAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["effectiveAt"])?.toEpochMilliseconds()
                        ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, true)
            }, { _, obj, itemData ->
                obj ?: create(null, parser, parser.asMap(itemData))
            })?.toIList()
        }
    }
}

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
) {
    companion object {
        internal fun create(
            existing: MarketPerpetual?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): MarketPerpetual? {
            data?.let {
                val volume24H = parser.asDouble(data["volume24H"])
                val trades24H = parser.asDouble(data["trades24H"])
                val nextFundingRate = parser.asDouble(data["nextFundingRate"])
                val nextFundingAtMilliseconds =
                    parser.asDatetime(data["nextFundingAt"])?.toEpochMilliseconds()?.toDouble()
                val openInterestLowerCap = parser.asDouble(data["openInterestLowerCap"])
                val openInterestUpperCap = parser.asDouble(data["openInterestUpperCap"])
                val openInterest = parser.asDouble(data["openInterest"])
                val openInterestUSDC = parser.asDouble(data["openInterestUSDC"])

                val line = parser.asList(data["line"]) as? IList<Double>
                if (openInterest != null) {
                    return if (existing?.openInterest != openInterest ||
                        existing.openInterestUSDC != openInterestUSDC ||
                        existing.volume24H != volume24H ||
                        existing.trades24H != trades24H ||
                        existing.nextFundingRate != nextFundingRate ||
                        existing.nextFundingAtMilliseconds != nextFundingAtMilliseconds ||
                        existing.line != line
                    ) {
                        MarketPerpetual(
                            volume24H = volume24H,
                            trades24H = trades24H,
                            volume24HUSDC = null,
                            nextFundingRate = nextFundingRate,
                            nextFundingAtMilliseconds = nextFundingAtMilliseconds,
                            openInterest = openInterest,
                            openInterestUSDC = openInterestUSDC ?: 0.0,
                            openInterestLowerCap = openInterestLowerCap,
                            openInterestUpperCap = openInterestUpperCap,
                            line = line,
                        )
                    } else {
                        existing
                    }
                }
            }
            print("Market Perpetual not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class CandleOption(
    val stringKey: String,
    val value: String,
    val seconds: Int,
) {
    companion object {
        internal fun create(
            existing: CandleOption?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): CandleOption? {
            Logger.d { "creating Candle Option\n" }
            data?.let {
                val value = parser.asString(data["value"])
                val stringKey = parser.asString(data["stringKey"])
                val seconds = parser.asInt(data["seconds"])
                if (value != null && stringKey != null && seconds != null) {
                    return if (existing?.value != value ||
                        existing.stringKey != stringKey
                    ) {
                        CandleOption(stringKey, value, seconds)
                    } else {
                        existing
                    }
                }
            }
            print("Candle Option not valid")
            return null
        }

        fun create(
            existing: IList<CandleOption>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?
        ): IList<CandleOption>? {
            return if (data != null) {
                val map = mutableMapOf<String, CandleOption>()
                if (existing != null) {
                    for (item in existing) {
                        map[item.value] = item
                    }
                }
                val result = iMutableListOf<CandleOption>()
                for (item in data) {
                    val value = parser.asString(item["value"])
                    if (value != null) {
                        val candleOption = CandleOption.create(map[value], parser, item)
                        if (candleOption != null) {
                            result.add(candleOption)
                        }
                    }
                }

                if (result.size > 0) result else null
            } else {
                null
            }
        }
    }
}

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
) {
    companion object {
        internal fun create(
            existing: MarketCandle?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): MarketCandle? {
            Logger.d { "creating Market Candle\n" }
            data?.let {
                val startedAtMilliseconds =
                    parser.asDatetime(data["startedAt"])?.toEpochMilliseconds()?.toDouble()
                val trades = parser.asInt(data["trades"])
                if (existing?.startedAtMilliseconds != startedAtMilliseconds ||
                    existing?.trades != trades
                ) {
                    val updatedAtMilliseconds =
                        parser.asDatetime(data["updatedAt"])?.toEpochMilliseconds()?.toDouble()
                    val low = parser.asDouble(data["low"])
                    val high = parser.asDouble(data["high"])
                    val open = parser.asDouble(data["open"])
                    val close = parser.asDouble(data["close"])
                    val baseTokenVolume = parser.asDouble(data["baseTokenVolume"])
                    val usdVolume = parser.asDouble(data["usdVolume"])

                    if (startedAtMilliseconds != null &&
                        low != null &&
                        high != null &&
                        open != null &&
                        close != null &&
                        baseTokenVolume != null &&
                        usdVolume != null
                    ) {
                        return if (existing?.startedAtMilliseconds != startedAtMilliseconds ||
                            existing.trades != trades ||
                            existing.updatedAtMilliseconds != updatedAtMilliseconds ||
                            existing.low != low ||
                            existing.high != high ||
                            existing.open != open ||
                            existing.close != close ||
                            existing.baseTokenVolume != baseTokenVolume ||
                            existing.usdVolume != usdVolume
                        ) {
                            MarketCandle(
                                startedAtMilliseconds,
                                updatedAtMilliseconds,
                                low,
                                high,
                                open,
                                close,
                                trades,
                                baseTokenVolume,
                                usdVolume,
                            )
                        } else {
                            existing
                        }
                    } else {
                        print("Market Candle data not valid")
                    }
                }
            }
            return existing
        }
    }
}

/*
    "1MIN", "5MINS", "15MINS", "30MINS", "1HOUR", "4HOURS", "1DAY"
 */
@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class MarketCandles(
    val candles: IMap<String, IList<MarketCandle>>?,
) {
    companion object {
        internal fun create(
            existing: MarketCandles?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
        ): MarketCandles? {
            Logger.d { "creating Market Candles\n" }
            data?.let {
                val candlesMap = iMutableMapOf<String, IList<MarketCandle>>()
                for ((key, value) in data) {
                    val existing = existing?.candles?.get(key)
                    val candles = candles(parser, existing, parser.asList(value))
                    candlesMap.typedSafeSet(key, candles)
                }

                return if (candlesMap.size > 0) MarketCandles(candlesMap) else null
            }
            print("Market Candles not valid")
            return null
        }

        private fun candles(
            parser: ParserProtocol,
            existing: IList<MarketCandle>?,
            data: List<*>?,
        ): IList<MarketCandle>? {
            return ParsingHelper.merge(
                parser,
                existing,
                data,
                { obj, itemData ->
                    val time1 = (obj as MarketCandle).startedAtMilliseconds
                    val time2 =
                        parser.asDatetime(itemData["startedAt"])?.toEpochMilliseconds()?.toDouble()

                    ParsingHelper.compare(time1, time2 ?: 0.0, true)
                },
                { _, obj, itemData ->
                    // Candles are mutable. Even if obj is not null, we need to create a new object
                    MarketCandle.create(obj as? MarketCandle, parser, parser.asMap(itemData))
                },
                true,
            )?.toIList()
        }
    }
}

@JsExport
@Serializable
data class MarketTradeResources(val sideString: String?, val sideStringKey: String) {
    companion object {
        internal fun create(
            existing: MarketTradeResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): MarketTradeResources? {
            Logger.d { "creating Market Trade Resources\n" }
            data?.let {
                val sideStringKey = parser.asString(data["sideStringKey"])

                if (sideStringKey != null) {
                    return if (existing?.sideStringKey != sideStringKey) {
                        val sideString = localizer?.localize(sideStringKey)
                        MarketTradeResources(
                            sideString,
                            sideStringKey,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Market Trade Resources not valid" }
            return null
        }
    }
}

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
) {
    companion object {
        internal fun create(
            existing: MarketTrade?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): MarketTrade? {
            Logger.d { "creating Market Trade\n" }
            data?.let {
                val id = parser.asString(data["id"])
                val size = parser.asDouble(data["size"])
                val price = parser.asDouble(data["price"])
                val side =
                    if (parser.asString(data["side"]) == "SELL") OrderSide.Sell else OrderSide.Buy
                val type = OrderType.invoke(parser.asString(data["type"]))
                val createdAtMilliseconds =
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                val resources = parser.asMap(data["resources"])?.let {
                    MarketTradeResources.create(existing?.resources, parser, it, localizer)
                }
                if (size != null && price != null && createdAtMilliseconds != null && resources != null) {
                    return if (
                        existing?.id != id ||
                        existing?.size != size ||
                        existing.side !== side ||
                        existing.price != price ||
                        existing.type != type ||
                        existing.createdAtMilliseconds != createdAtMilliseconds ||
                        existing.resources !== resources
                    ) {
                        MarketTrade(
                            id,
                            side,
                            size,
                            price,
                            type,
                            createdAtMilliseconds,
                            resources,
                        )
                    } else {
                        existing
                    }
                }
            }
            print("Market Trade not valid")
            return null
        }

        internal fun create(
            existing: IList<MarketTrade>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
            localizer: LocalizerProtocol?,
        ): IList<MarketTrade>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as MarketTrade).createdAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: create(null, parser, parser.asMap(itemData), localizer)
            })?.toIList()
        }
    }
}

@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class OrderbookLine(
    val size: Double,
    val sizeCost: Double,
    val price: Double,
    val offset: Int = 0,
    val depth: Double?,
    val depthCost: Double,
) {
    companion object {
        internal fun create(
            existing: OrderbookLine?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            previousDepthCost: Double? = null,
        ): OrderbookLine? {
            Logger.d { "creating Orderbook Line\n" }
            data?.let {
                val size = parser.asDouble(data["size"])
                val price = parser.asDouble(data["price"])
                val sizeCost = parser.asDouble(data["sizeCost"]);
                val offset = parser.asInt(data["offset"]) ?: 0
                val depth = parser.asDouble(data["depth"])
                if (size != null && price != null && sizeCost != null && size != 0.0) {
                    val depthCost = (previousDepthCost ?: 0.0) + sizeCost
                    return if (existing?.size != size ||
                        existing.sizeCost != sizeCost ||
                        existing.price != price ||
                        existing.offset != offset ||
                        existing.depth != depth ||
                        existing.depthCost != depthCost
                    ) {
                        OrderbookLine(size, sizeCost, price, offset ?: 0, depth, depthCost)
                    } else {
                        existing
                    }
                }
            }
//            print("Orderbook Line not valid")
            return null
        }
    }
}

/*
Under extreme conditions, orderbook may be obsent, or one-sided
*/

@JsExport
@kotlinx.serialization.Serializable
data class MarketOrderbookGrouping(val multiplier: OrderbookGrouping, val tickSize: Double?) {
    companion object {
        internal fun create(
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): MarketOrderbookGrouping? {
            Logger.d { "creating Market Grouping\n" }
            if (data != null) {
                val tickSize = parser.asDouble(data["tickSize"])
                val multiplier = OrderbookGrouping.invoke(parser.asInt(data["multiplier"]) ?: 1)
                if (multiplier != null && tickSize != null) {
                    return MarketOrderbookGrouping(multiplier, tickSize)
                }
            }
            return null
        }
    }
}

@JsExport
@Serializable
data class MarketOrderbook(
    val midPrice: Double?,
    val spreadPercent: Double?,
    val spread: Double?,
    val grouping: MarketOrderbookGrouping?,
    val asks: IList<OrderbookLine>?,
    val bids: IList<OrderbookLine>?,
) {
    companion object {
        internal fun create(
            existing: MarketOrderbook?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): MarketOrderbook? {
            Logger.d { "creating Market Orderbook\n" }
            data?.let {
                val midPrice = parser.asDouble(data["midPrice"])
                val spread = parser.asDouble(data["spread"])
                val spreadPercent = parser.asDouble(data["spreadPercent"])
                val grouping =
                    MarketOrderbookGrouping.create(parser, parser.asMap(data["grouping"]))

                val asks = asks(existing?.asks, parser, parser.asNativeList(data["asks"]))
                val bids = bids(existing?.bids, parser, parser.asNativeList(data["bids"]))

                return if (existing?.midPrice != midPrice ||
                    existing?.spreadPercent != spreadPercent ||
                    existing?.spread != spread ||
                    existing?.grouping !== grouping ||
                    existing?.asks != asks ||
                    existing?.bids != bids
                ) {
                    return MarketOrderbook(midPrice, spreadPercent, spread, grouping, asks, bids)
                } else {
                    existing
                }
            }
            return null
        }

        private fun asks(
            existing: IList<OrderbookLine>?,
            parser: ParserProtocol,
            data: List<*>?,
        ): IList<OrderbookLine>? {
            return orderbook(existing, parser, data, true)
        }

        private fun bids(
            existing: IList<OrderbookLine>?,
            parser: ParserProtocol,
            data: List<*>?,
        ): IList<OrderbookLine>? {
            return orderbook(existing, parser, data, false)
        }

        private fun orderbook(
            existing: IList<OrderbookLine>?,
            parser: ParserProtocol,
            data: List<*>?,
            ascending: Boolean,
        ): IList<OrderbookLine>? {
            return if (data != null) {
                var depthCost: Double = 0.0
                val lines = iMutableListOf<OrderbookLine>()
                for (item in data) {
                    val line = OrderbookLine.create(null, parser, parser.asMap(item), depthCost)
                    if (line != null) {
                        lines.add(line)
                        depthCost = line.depthCost ?: 0.0
                    }
                }
                lines
            } else {
                null
            }

//
//            return OrderbookLines.fromArray(
//                ParsingHelper.merge(
//                    parser,
//                    existing?.a,
//                    data,
//                    { obj, itemData ->
//                        val price1 = (obj as OrderbookLine).price
//                        val price2 = parser.asDouble(itemData["price"])
//                        val priceOrder = ParsingHelper.compare(price1, price2 ?: 0.0, ascending)
//                        when (priceOrder) {
//                            ComparisonOrder.same -> ParsingHelper.compare(
//                                (obj as OrderbookLine).offset,
//                                parser.asInt(itemData["offset"]) ?: 0,
//                                true
//                            )
//                            else -> priceOrder
//                        }
//                    },
//                    { _, itemData ->
//                        OrderbookLine.create(null, parser, itemData as? IMap<*, *>)
//                    },
//                    true
//                )
//            )
        }
    }
}

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
    val oraclePrice: Double? = null,
    val marketCaps: Double?,
    val priceChange24H: Double?,
    val priceChange24HPercent: Double?,
    val status: MarketStatus?,
    val configs: MarketConfigs?,
    val perpetual: MarketPerpetual?,
) {
    companion object {
        internal fun create(
            existing: PerpetualMarket?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            assets: Map<String, Any>?,
            resetOrderbook: Boolean,
            resetTrades: Boolean,
        ): PerpetualMarket? {
            val status = parser.asMap(data["status"])?.let {
                MarketStatus.create(existing?.status, parser, it)
            } ?: return null
            if (!status.canTrade && !status.canReduce) {
                return null
            }
            val id = parser.asString(data["id"]) ?: return null
            val assetId = parser.asString(data["assetId"]) ?: return null
            val market = parser.asString(data["market"])

            val oraclePrice = parser.asDouble(data["oraclePrice"])
            val marketCaps = parser.asDouble(data["marketCaps"])
            val priceChange24H = parser.asDouble(data["priceChange24H"])
            val priceChange24HPercent = parser.asDouble(data["priceChange24HPercent"])

            val configs = parser.asMap(data["configs"])?.let {
                MarketConfigs.create(existing?.configs, parser, it)
            } ?: return null
            val perpetual = parser.asMap(data["perpetual"])?.let {
                MarketPerpetual.create(existing?.perpetual, parser, it)
            } ?: return null

            val significantChange = existing?.id != id ||
                existing.assetId != assetId ||
                existing.market != market ||
                existing.oraclePrice != oraclePrice ||
                existing.marketCaps != marketCaps ||
                existing.priceChange24H != priceChange24H ||
                existing.priceChange24HPercent != priceChange24HPercent ||
                existing.status !== status ||
                existing.configs !== configs ||
                existing.perpetual !== perpetual
            return if (!significantChange) {
                existing
            } else {
                PerpetualMarket(
                    id,
                    assetId,
                    market,
                    oraclePrice,
                    marketCaps,
                    priceChange24H,
                    priceChange24HPercent,
                    status,
                    configs,
                    perpetual,
                )
            }
        }

        private fun trades(
            existing: IList<MarketTrade>?,
            parser: ParserProtocol,
            data: List<Any>?,
            localizer: LocalizerProtocol?,
        ): IList<MarketTrade>? {
            return ParsingHelper.merge(parser, existing?.toIList(), data, { obj, itemData ->
                val time1 = (obj as MarketTrade).createdAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: MarketTrade.create(null, parser, parser.asMap(itemData), localizer)
            })?.toIList()
        }
    }
}

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
            existing: PerpetualMarketSummary?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            assets: Map<String, Any>?,
            staticTyping: Boolean,
            marketSummaryState: InternalMarketSummaryState,
            changes: StateChanges,
        ): PerpetualMarketSummary? {
            if (staticTyping) {
                if (marketSummaryState.markets.isEmpty()) {
                    return null
                }
                val markets: MutableMap<String, PerpetualMarket> = mutableMapOf()
                for ((marketId, market) in marketSummaryState.markets) {
                    market.perpetualMarket?.let {
                        markets[marketId] = it
                    }
                }
                return createPerpetualMarketSummary(existing, parser, data, markets)
            } else {
                val marketsData = parser.asMap(data["markets"]) ?: return null
                val changedMarkets = changes.markets ?: marketsData.keys

                val markets = existing?.markets?.mutable() ?: iMutableMapOf()
                for (marketId in changedMarkets) {
                    val marketData = parser.asMap(marketsData[marketId]) ?: continue
//                val marketData = parser.asMap(configDataMap["configs"]) ?: continue
                    val existingMarket = existing?.markets?.get(marketId)

                    val perpMarket = PerpetualMarket.create(
                        existing = existingMarket,
                        parser = parser,
                        data = marketData,
                        assets = assets,
                        resetOrderbook = changes.changes.contains(Changes.orderbook),
                        resetTrades = changes.changes.contains(Changes.trades),
                    )
                    markets.typedSafeSet(marketId, perpMarket)
                }
                return createPerpetualMarketSummary(existing, parser, data, markets)
            }
        }

        private fun createPerpetualMarketSummary(
            existing: PerpetualMarketSummary?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            newMarkets: Map<String, PerpetualMarket>,
        ): PerpetualMarketSummary? {
            val volume24HUSDC = parser.asDouble(data["volume24HUSDC"])
            val openInterestUSDC = parser.asDouble(data["openInterestUSDC"])
            val trades24H = parser.asDouble(data["trades24H"])

            val significantChanges = existing?.volume24HUSDC != volume24HUSDC ||
                existing?.openInterestUSDC != openInterestUSDC ||
                existing?.trades24H != trades24H ||
                existing?.markets != newMarkets

            return if (!significantChanges) {
                existing
            } else {
                PerpetualMarketSummary(
                    volume24HUSDC = volume24HUSDC,
                    openInterestUSDC = openInterestUSDC,
                    trades24H = trades24H,
                    markets = newMarkets.toIMap(),
                )
            }
        }
    }

    fun marketIds(): IList<String>? {
        return markets?.keys?.toIList()
    }

    fun market(id: String): PerpetualMarket? {
        return markets?.get(id)
    }
}
