package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.typedSafeSet
import kollections.JsExport
import kollections.iMapOf
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIList
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
}

/* for V4 only */
@JsExport
@Serializable
data class MarketConfigsV4(
    val clobPairId: String,
    val atomicResolution: Int,
    val stepBaseQuantums: Int,
) {
    companion object {
        internal fun create(
            existing: MarketConfigsV4?,
            parser: ParserProtocol,
            data: Map<String, Any>,
        ): MarketConfigsV4? {
            val clobPairId = parser.asString(data["clobPairId"])
            val atomicResolution = parser.asInt(data["atomicResolution"])
            val stepBaseQuantums = parser.asInt(data["stepBaseQuantums"])

            return if (clobPairId != null && atomicResolution != null && stepBaseQuantums != null) {
                if (existing == null ||
                    existing.clobPairId != clobPairId ||
                    existing.atomicResolution != atomicResolution ||
                    existing.stepBaseQuantums != stepBaseQuantums
                ) {
                    MarketConfigsV4(
                        clobPairId,
                        atomicResolution,
                        stepBaseQuantums,
                    )
                } else {
                    existing
                }
            } else null
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
    val minOrderSize: Double? = null,
    val initialMarginFraction: Double? = null,
    val maintenanceMarginFraction: Double? = null,
    val incrementalInitialMarginFraction: Double? = null,
    val incrementalPositionSize: Double? = null,
    val maxPositionSize: Double? = null,
    val basePositionNotional: Double? = null,
    val baselinePositionSize: Double? = null,
    val candleOptions: IList<CandleOption>? = null,
) {
    companion object {
        internal fun create(
            existing: MarketConfigs?,
            parser: ParserProtocol,
            data: Map<String, Any>,
        ): MarketConfigs {
            val clobPairId = parser.asString(data["clobPairId"])
            val largeSize = parser.asInt(data["largeSize"])
            val stepSize = parser.asDouble(data["stepSize"])
            val tickSize = parser.asDouble(data["tickSize"])
            val displayStepSize = parser.asDouble(data["displayStepSize"]) ?: stepSize
            val displayTickSize = parser.asDouble(data["displayTickSize"]) ?: tickSize
            val minOrderSize = parser.asDouble(data["minOrderSize"])
            val initialMarginFraction = parser.asDouble(data["initialMarginFraction"])
            val maintenanceMarginFraction = parser.asDouble(data["maintenanceMarginFraction"])
            val incrementalInitialMarginFraction =
                parser.asDouble(data["incrementalInitialMarginFraction"])
            val incrementalPositionSize = parser.asDouble(data["incrementalPositionSize"])
            val maxPositionSize = parser.asDouble(data["maxPositionSize"])
            val basePositionNotional = parser.asDouble(data["basePositionNotional"])
            val baselinePositionSize = parser.asDouble(data["baselinePositionSize"])
            val candleOptions = CandleOption.create(
                existing?.candleOptions,
                parser,
                parser.asList(data["candleOptions"]) as? IList<IMap<String, Any>>
            )

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
                existing.candleOptions != candleOptions
            ) {
                MarketConfigs(
                    clobPairId,
                    largeSize,
                    stepSize,
                    tickSize,
                    stepSize?.numberOfDecimals(),
                    tickSize?.numberOfDecimals(),
                    displayStepSize,
                    displayTickSize,
                    displayStepSize?.numberOfDecimals(),
                    displayTickSize?.numberOfDecimals(),
                    minOrderSize,
                    initialMarginFraction,
                    maintenanceMarginFraction,
                    incrementalInitialMarginFraction,
                    incrementalPositionSize,
                    maxPositionSize,
                    basePositionNotional,
                    baselinePositionSize,
                    candleOptions
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
            DebugLogger.log("creating Market Historical Funding\n")
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
                            volume24H,
                            trades24H,
                            null,
                            nextFundingRate,
                            nextFundingAtMilliseconds,
                            openInterest,
                            openInterestUSDC ?: 0.0,
                            line,
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
            DebugLogger.log("creating Candle Option\n")
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
            } else null
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
            DebugLogger.log("creating Market Candle\n")
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
                                usdVolume
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
            DebugLogger.log("creating Market Candles\n")
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
data class MarketTradeResources(val sideStringKey: String) {
    companion object {
        internal fun create(
            existing: MarketTradeResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): MarketTradeResources? {
            DebugLogger.log("creating Market Trade Resources\n")
            data?.let {
                val sideStringKey = parser.asString(data["sideStringKey"])

                if (sideStringKey != null) {
                    return if (existing?.sideStringKey != sideStringKey) {
                        MarketTradeResources(
                            sideStringKey
                        )
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Market Trade Resources not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class MarketTrade(
    val id: String?,    // in case "id" is not sent, app should still function
    val side: OrderSide,
    val size: Double,
    val price: Double,
    val liquidation: Boolean,
    val createdAtMilliseconds: Double,
    val resources: MarketTradeResources,
) {
    companion object {
        internal fun create(
            existing: MarketTrade?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): MarketTrade? {
            DebugLogger.log("creating Market Trade\n")
            data?.let {
                val id = parser.asString(data["id"])
                val size = parser.asDouble(data["size"])
                val price = parser.asDouble(data["price"])
                val side =
                    if (parser.asString(data["side"]) == "SELL") OrderSide.sell else OrderSide.buy
                val liquidation = parser.asBool(data["liquidation"]) ?: false
                val createdAtMilliseconds =
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                val resources = parser.asMap(data["resources"])?.let {
                    MarketTradeResources.create(existing?.resources, parser, it)
                }
                if (size != null && price != null && createdAtMilliseconds != null && resources != null) {
                    return if (
                        existing?.id != id ||
                        existing?.size != size ||
                        existing.side !== side ||
                        existing.price != price ||
                        existing.liquidation != liquidation ||
                        existing.createdAtMilliseconds != createdAtMilliseconds ||
                        existing.resources !== resources
                    ) {
                        MarketTrade(
                            id,
                            side,
                            size,
                            price,
                            liquidation,
                            createdAtMilliseconds,
                            resources
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
        ): IList<MarketTrade>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as MarketTrade).createdAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: create(null, parser, parser.asMap(itemData))
            })?.toIList()
        }
    }
}

@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class OrderbookLine(
    val size: Double,
    val price: Double,
    val offset: Int = 0,
    val depth: Double?,
) {
    companion object {
        internal fun create(
            existing: OrderbookLine?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): OrderbookLine? {
            DebugLogger.log("creating Orderbook Line\n")
            data?.let {
                val size = parser.asDouble(data["size"])
                val price = parser.asDouble(data["price"])
                val offset = parser.asInt(data["offset"]) ?: 0
                val depth = parser.asDouble(data["depth"])
                if (size != null && price != null && size != 0.0) {
                    return if (existing?.size != size ||
                        existing.price != price ||
                        existing.offset != offset ||
                        existing.depth != depth
                    ) {
                        OrderbookLine(size, price, offset ?: 0, depth)
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
            DebugLogger.log("creating Market Grouping\n")
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
            DebugLogger.log("creating Market Orderbook\n")
            data?.let {
                val midPrice = parser.asDouble(data["midPrice"])
                val spreadPercent = parser.asDouble(data["spreadPercent"])
                val grouping =
                    MarketOrderbookGrouping.create(parser, parser.asMap(data["grouping"]))

                val asks = asks(existing?.asks, parser, parser.asNativeList(data["asks"]))
                val bids = bids(existing?.bids, parser, parser.asNativeList(data["bids"]))

                return if (existing?.midPrice != midPrice ||
                    existing?.spreadPercent != spreadPercent ||
                    existing?.grouping !== grouping ||
                    existing?.asks != asks ||
                    existing?.bids != bids
                ) {
                    return MarketOrderbook(midPrice, spreadPercent, grouping, asks, bids)
                } else existing
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
                val lines = iMutableListOf<OrderbookLine>()
                for (item in data) {
                    val line = OrderbookLine.create(null, parser, parser.asMap(item))
                    if (line != null) {
                        lines.add(line)
                    }
                }
                lines
            } else null

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
@Suppress("UNCHECKED_CAST")
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
            val status: MarketStatus? = parser.asMap(data["status"])?.let {
                MarketStatus.create(existing?.status, parser, it)
            }
            if (status?.canTrade != true) {
                return null
            }
            val id = parser.asString(data["id"]) ?: return null
            val assetId = parser.asString(data["assetId"]) ?: return null
            val market = parser.asString(data["market"])

            val oraclePrice = parser.asDouble(data["oraclePrice"])
            val marketCaps = parser.asDouble(data["marketCaps"])
            val priceChange24H = parser.asDouble(data["priceChange24H"])
            val priceChange24HPercent = parser.asDouble(data["priceChange24HPercent"])

            val configs: MarketConfigs? = parser.asMap(data["configs"])?.let {
                MarketConfigs.create(existing?.configs, parser, it)
            }
            val perpetual: MarketPerpetual? = parser.asMap(data["perpetual"])?.let {
                MarketPerpetual.create(existing?.perpetual, parser, it)
            }

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
            return if (!significantChange) existing else {
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
                    perpetual
                )
            }
        }

        private fun trades(
            existing: IList<MarketTrade>?,
            parser: ParserProtocol,
            data: List<Any>?,
        ): IList<MarketTrade>? {
            return ParsingHelper.merge(parser, existing?.toIList(), data, { obj, itemData ->
                val time1 = (obj as MarketTrade).createdAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: MarketTrade.create(null, parser, parser.asMap(itemData))
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
        internal fun create(
            existing: PerpetualMarketSummary?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            assets: Map<String, Any>?
        ): PerpetualMarketSummary? {
            DebugLogger.log("creating Perpetual Market Summary\n")

            val markets: IMutableMap<String, PerpetualMarket> =
                iMutableMapOf()
            val marketsData = parser.asMap(data["markets"]) ?: return null

            for ((key, value) in marketsData) {
                val marketData = parser.asMap(value) ?: iMapOf()
                PerpetualMarket.create(
                    existing?.markets?.get(key),
                    parser,
                    marketData,
                    assets,
                    false,
                    false
                )
                    ?.let { market ->
                        markets[key] = market
                    }
            }

            return perpetualMarketSummary(existing, parser, data, markets)
        }

        internal fun apply(
            existing: PerpetualMarketSummary?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            assets: Map<String, Any>?,
            changes: StateChanges,
        ): PerpetualMarketSummary? {
            val marketsData = parser.asMap(data["markets"]) ?: return null
            val changedMarkets = changes.markets ?: marketsData.keys

            val markets = existing?.markets?.mutable() ?: iMutableMapOf()
            for (marketId in changedMarkets) {
                val marketData = parser.asMap(marketsData[marketId]) ?: continue
//                val marketData = parser.asMap(configDataMap["configs"]) ?: continue
                val existingMarket = existing?.markets?.get(marketId)

                val perpMarket = PerpetualMarket.create(
                    existingMarket,
                    parser,
                    marketData,
                    assets,
                    changes.changes.contains(Changes.orderbook),
                    changes.changes.contains(Changes.trades)
                )
                if (perpMarket != null) {
                    markets[marketId] = perpMarket
                }
            }
            return perpetualMarketSummary(existing, parser, data, markets)
        }

        private fun perpetualMarketSummary(
            existing: PerpetualMarketSummary?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            newMarkets: IMutableMap<String, PerpetualMarket>,
        ): PerpetualMarketSummary? {
            val volume24HUSDC = parser.asDouble(data["volume24HUSDC"])
            val openInterestUSDC = parser.asDouble(data["openInterestUSDC"])
            val trades24H = parser.asDouble(data["trades24H"])

            val significantChanges = existing?.volume24HUSDC != volume24HUSDC
                    || existing?.openInterestUSDC != openInterestUSDC
                    || existing?.trades24H != trades24H
                    || existing?.markets != newMarkets

            return if (!significantChanges) existing else {
                PerpetualMarketSummary(
                    volume24HUSDC,
                    openInterestUSDC,
                    trades24H,
                    newMarkets
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
