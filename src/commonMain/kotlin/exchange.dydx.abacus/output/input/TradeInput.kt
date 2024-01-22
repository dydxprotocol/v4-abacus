package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMutableList
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SelectionOption(
    val type: String,
    val string: String?,
    val stringKey: String?,
    val iconUrl: String?,
) {
    companion object {
        internal fun create(
            existing: SelectionOption?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): SelectionOption? {
            DebugLogger.log("creating Selection Option\n")

            data?.let {
                parser.asString(data["type"])?.let { type ->
                    val string = parser.asString(data["string"])
                    val stringKey = parser.asString(data["stringKey"])
                    if (string != null || stringKey != null) {
                        val iconUrl = parser.asString(data["iconUrl"])
                        return if (existing?.type != type ||
                            existing.string != string ||
                            existing.stringKey != stringKey ||
                            existing.iconUrl != iconUrl
                        ) {
                            SelectionOption(type, string, stringKey, iconUrl)
                        } else {
                            existing
                        }
                    }
                }
            }
            DebugLogger.debug("Selection Option not valid")
            return null
        }
    }
}


@JsExport
@Serializable
data class TradeInputOptions(
    val needsSize: Boolean,
    val needsLeverage: Boolean,
    val maxLeverage: Double?,
    val needsLimitPrice: Boolean,
    val needsTriggerPrice: Boolean,
    val needsTrailingPercent: Boolean,
    val needsGoodUntil: Boolean,
    val needsReduceOnly: Boolean,
    val needsPostOnly: Boolean,
    val needsBrackets: Boolean,
    val typeOptions: IList<SelectionOption>,
    val sideOptions: IList<SelectionOption>,
    val timeInForceOptions: IList<SelectionOption>?,
    val goodTilUnitOptions: IList<SelectionOption>,
    val executionOptions: IList<SelectionOption>?,
    val reduceOnlyPromptStringKey: String?
) {
    companion object {
        private val typeOptionsArray =
            iListOf(
                SelectionOption(
                    OrderType.limit.rawValue,
                    null,
                    "APP.TRADE.LIMIT_ORDER_SHORT",
                    null
                ),
                SelectionOption(
                    OrderType.market.rawValue,
                    null,
                    "APP.TRADE.MARKET_ORDER_SHORT",
                    null
                ),
                SelectionOption(OrderType.stopLimit.rawValue, null, "APP.TRADE.STOP_LIMIT", null),
                SelectionOption(OrderType.stopMarket.rawValue, null, "APP.TRADE.STOP_MARKET", null),
                SelectionOption(
                    OrderType.trailingStop.rawValue,
                    null,
                    "APP.TRADE.TRAILING_STOP",
                    null
                ),
                SelectionOption(
                    OrderType.takeProfitLimit.rawValue,
                    null,
                    "APP.TRADE.TAKE_PROFIT",
                    null
                ),
                SelectionOption(
                    OrderType.takeProfitMarket.rawValue,
                    null,
                    "APP.TRADE.TAKE_PROFIT_MARKET",
                    null
                ),
            )
        private val typeOptionsV4Array =
            iListOf(
                SelectionOption(
                    OrderType.limit.rawValue,
                    null,
                    "APP.TRADE.LIMIT_ORDER_SHORT",
                    null
                ),
                SelectionOption(
                    OrderType.market.rawValue,
                    null,
                    "APP.TRADE.MARKET_ORDER_SHORT",
                    null
                ),
                SelectionOption(OrderType.stopLimit.rawValue, null, "APP.TRADE.STOP_LIMIT", null),
                SelectionOption(OrderType.stopMarket.rawValue, null, "APP.TRADE.STOP_MARKET", null),
                SelectionOption(
                    OrderType.takeProfitLimit.rawValue,
                    null,
                    "APP.TRADE.TAKE_PROFIT",
                    null
                ),
                SelectionOption(
                    OrderType.takeProfitMarket.rawValue,
                    null,
                    "APP.TRADE.TAKE_PROFIT_MARKET",
                    null
                ),
            )

        private val sideOptionsArray =
            iListOf(
                SelectionOption(OrderSide.buy.rawValue, null, "APP.GENERAL.BUY", null),
                SelectionOption(OrderSide.sell.rawValue, null, "APP.GENERAL.SELL", null)
            )

        private val goodTilUnitOptionsArray =
            iListOf(
                SelectionOption("M", null, "APP.GENERAL.TIME_STRINGS.MINUTES_SHORT", null),
                SelectionOption("H", null, "APP.GENERAL.TIME_STRINGS.HOURS", null),
                SelectionOption("D", null, "APP.GENERAL.TIME_STRINGS.DAYS", null),
                SelectionOption("W", null, "APP.GENERAL.TIME_STRINGS.WEEKS", null)
            )

        internal fun create(
            existing: TradeInputOptions?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputOptions? {
            DebugLogger.log("creating Trade Input Options\n")

            data?.let {
                val needsSize = parser.asBool(data["needsSize"]) ?: false
                val needsLeverage = parser.asBool(data["needsLeverage"]) ?: false
                val maxLeverage = parser.asDouble(data["maxLeverage"])
                val needsLimitPrice = parser.asBool(data["needsLimitPrice"]) ?: false
                val needsTriggerPrice = parser.asBool(data["needsTriggerPrice"]) ?: false
                val needsTrailingPercent = parser.asBool(data["needsTrailingPercent"]) ?: false
                val needsGoodUntil =
                    parser.asBool(data["needsGoodUntil"]) ?: false
                val needsReduceOnly = parser.asBool(data["needsReduceOnly"]) ?: false
                val needsPostOnly = parser.asBool(data["needsPostOnly"]) ?: false
                val needsBrackets = parser.asBool(data["needsBrackets"]) ?: false
                val reduceOnlyPromptStringKey = parser.asString(data["reduceOnlyPromptStringKey"])

                var timeInForceOptions: IMutableList<SelectionOption>? = null
                parser.asList(data["timeInForceOptions"])?.let { data ->
                    timeInForceOptions = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        SelectionOption.create(
                            existing?.timeInForceOptions?.getOrNull(i),
                            parser, parser.asMap(item)
                        )?.let {
                            timeInForceOptions?.add(it)
                        }
                    }
                }

                var executionOptions: IMutableList<SelectionOption>? = null
                parser.asList(data["executionOptions"])?.let { data ->
                    executionOptions = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        SelectionOption.create(
                            existing?.executionOptions?.getOrNull(i),
                            parser, parser.asMap(item)
                        )?.let {
                            executionOptions?.add(it)
                        }
                    }
                }

                val timeInForceOptionsArray =
                    timeInForceOptions
                val executionOptionsArray =
                    executionOptions

                return if (existing?.needsSize != needsSize ||
                    existing.needsLeverage != needsLeverage ||
                    existing.maxLeverage != maxLeverage ||
                    existing.needsLimitPrice != needsLimitPrice ||
                    existing.needsTriggerPrice != needsTriggerPrice ||
                    existing.needsTrailingPercent != needsTrailingPercent ||
                    existing.needsGoodUntil != needsGoodUntil ||
                    existing.needsReduceOnly != needsReduceOnly ||
                    existing.needsPostOnly != needsPostOnly ||
                    existing.needsBrackets != needsBrackets ||
                    existing.timeInForceOptions != timeInForceOptionsArray ||
                    existing.executionOptions != executionOptionsArray ||
                    existing.reduceOnlyPromptStringKey != reduceOnlyPromptStringKey
                ) {
                    val typeOptions = typeOptionsV4Array

                    TradeInputOptions(
                        needsSize,
                        needsLeverage,
                        maxLeverage,
                        needsLimitPrice,
                        needsTriggerPrice,
                        needsTrailingPercent,
                        needsGoodUntil,
                        needsReduceOnly,
                        needsPostOnly,
                        needsBrackets,
                        typeOptions,
                        sideOptionsArray,
                        timeInForceOptionsArray,
                        goodTilUnitOptionsArray,
                        executionOptionsArray,
                        reduceOnlyPromptStringKey,
                    )
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Options not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class TradeInputSummary(
    val price: Double?,
    val payloadPrice: Double?,
    val size: Double?,
    val usdcSize: Double?,
    val slippage: Double?,
    val fee: Double?,
    val total: Double?,
    val reward: Double?,
    val filled: Boolean,
) {
    companion object {
        internal fun create(
            existing: TradeInputSummary?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputSummary? {
            DebugLogger.log("creating Trade Input Summary\n")

            data?.let {
                val price = parser.asDouble(data["price"])
                val payloadPrice = parser.asDouble(data["payloadPrice"])
                val size = parser.asDouble(data["size"])
                val usdcSize = parser.asDouble(data["usdcSize"])
                val slippage = parser.asDouble(data["slippage"])
                val fee = parser.asDouble(data["fee"])
                val total = parser.asDouble(data["total"])
                val reward = parser.asDouble(data["reward"])
                val filled = parser.asBool(data["filled"]) ?: false

                return if (
                    existing?.price != price ||
                    existing?.payloadPrice != payloadPrice ||
                    existing?.size != size ||
                    existing?.usdcSize != usdcSize ||
                    existing?.slippage != slippage ||
                    existing?.fee != fee ||
                    existing?.total != total ||
                    existing?.filled != filled
                ) {
                    TradeInputSummary(
                        price,
                        payloadPrice,
                        size,
                        usdcSize,
                        slippage,
                        fee,
                        total,
                        reward,
                        filled
                    )
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Summary not valid")
            return null
        }
    }
}

@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class OrderbookUsage(
    val size: Double,
    val price: Double,
) {
    companion object {
        internal fun create(
            existing: OrderbookUsage?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): OrderbookUsage? {
            DebugLogger.log("creating Orderbook Line\n")
            data?.let {
                val size = parser.asDouble(data["size"])
                val price = parser.asDouble(data["price"])
                if (size != null && price != null) {
                    return if (existing?.size != size ||
                        existing.price != price
                    ) {
                        OrderbookUsage(size, price)
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

@JsExport
@Serializable
data class TradeInputMarketOrder(
    val size: Double?,
    val usdcSize: Double?,
    val price: Double?,
    val worstPrice: Double?,
    val filled: Boolean,
    val orderbook: IList<OrderbookUsage>?,
) {
    companion object {
        internal fun create(
            existing: TradeInputMarketOrder?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputMarketOrder? {
            DebugLogger.log("creating Trade Input Market Order\n")

            data?.let {
                val size = parser.asDouble(data["size"])
                val usdcSize = parser.asDouble(data["usdcSize"])
                val price = parser.asDouble(data["price"])
                val worstPrice = parser.asDouble(data["worstPrice"])
                val filled = parser.asBool(data["filled"]) ?: false
                var orderbook: IMutableList<OrderbookUsage>? = null
                parser.asList(data["orderbook"])?.let { data ->
                    orderbook = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        OrderbookUsage.create(
                            existing?.orderbook?.getOrNull(i),
                            parser, parser.asMap(item)
                        )?.let {
                            orderbook?.add(it)
                        }
                    }

                }
                return if (existing?.size != size ||
                    existing?.usdcSize != usdcSize ||
                    existing?.price != price ||
                    existing?.worstPrice != worstPrice ||
                    existing?.filled != filled ||
                    existing.orderbook != orderbook
                ) {
                    TradeInputMarketOrder(
                        size,
                        usdcSize,
                        price,
                        worstPrice,
                        filled,
                        orderbook
                    )
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Market Order not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class TradeInputSize(
    val size: Double?,
    val usdcSize: Double?,
    val leverage: Double?,
    val input: String?,
) {
    companion object {
        internal fun create(
            existing: TradeInputSize?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputSize? {
            DebugLogger.log("creating Trade Input Size\n")

            data?.let {
                val size = parser.asDouble(data["size"])
                val usdcSize = parser.asDouble(data["usdcSize"])
                val leverage = parser.asDouble(data["leverage"])
                val input = parser.asString(data["input"])
                return if (existing?.size != size ||
                    existing?.usdcSize != usdcSize ||
                    existing?.leverage != leverage ||
                    existing?.input != input
                ) {
                    TradeInputSize(size, usdcSize, leverage, input)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Size not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class TradeInputPrice(
    val limitPrice: Double?,
    val triggerPrice: Double?,
    val trailingPercent: Double?,
) {
    companion object {
        internal fun create(
            existing: TradeInputPrice?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputPrice? {
            DebugLogger.log("creating Trade Input Price\n")

            data?.let {
                val limitPrice = parser.asDouble(data["limitPrice"])
                val triggerPrice = parser.asDouble(data["triggerPrice"])
                val trailingPercent = parser.asDouble(data["trailingPercent"])
                return if (existing?.limitPrice != limitPrice ||
                    existing?.triggerPrice != triggerPrice ||
                    existing?.trailingPercent != trailingPercent
                ) {
                    TradeInputPrice(limitPrice, triggerPrice, trailingPercent)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Price not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class TradeInputGoodUntil(
    val duration: Double?,
    val unit: String?,
) {
    companion object {
        internal fun create(
            existing: TradeInputGoodUntil?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputGoodUntil? {
            DebugLogger.log("creating Trade Input Good Until\n")

            data?.let {
                val duration = parser.asDouble(data["duration"])
                val unit = parser.asString(data["unit"])
                return if (existing?.duration != duration ||
                    existing?.unit != unit
                ) {
                    TradeInputGoodUntil(duration, unit)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Price not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class TradeInputBracketSide(
    val triggerPrice: Double?,
    val percent: Double?,
    val reduceOnly: Boolean,
) {
    companion object {
        internal fun create(
            existing: TradeInputBracketSide?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputBracketSide? {
            DebugLogger.log("creating Trade Input Good Until\n")

            data?.let {
                val triggerPrice = parser.asDouble(data["triggerPrice"])
                val percent = parser.asDouble(data["percent"])
                val reduceOnly = parser.asBool(data["reduceOnly"]) ?: false
                return if (existing?.triggerPrice != triggerPrice ||
                    existing?.percent != percent ||
                    existing?.reduceOnly != reduceOnly
                ) {
                    TradeInputBracketSide(triggerPrice, percent, reduceOnly)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Price not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class TradeInputBracket(
    val stopLoss: TradeInputBracketSide?,
    val takeProfit: TradeInputBracketSide?,
    val goodTil: TradeInputGoodUntil?,
    val execution: String?,
) {
    companion object {
        internal fun create(
            existing: TradeInputBracket?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputBracket? {
            DebugLogger.log("creating Trade Input Bracket\n")

            data?.let {
                val stopLoss = TradeInputBracketSide.create(
                    existing?.stopLoss,
                    parser,
                    parser.asMap(data["stopLoss"])
                )
                val takeProfit =
                    TradeInputBracketSide.create(
                        existing?.takeProfit,
                        parser,
                        parser.asMap(data["takeProfit"])
                    )
                val goodTil = TradeInputGoodUntil.create(
                    existing?.goodTil,
                    parser,
                    parser.asMap(data["goodTil"])
                )
                val execution = parser.asString(data["execution"])
                return if (existing?.stopLoss != stopLoss ||
                    existing?.takeProfit != takeProfit ||
                    existing?.goodTil != goodTil ||
                    existing?.execution != execution
                ) {
                    TradeInputBracket(stopLoss, takeProfit, goodTil, execution)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Bracket not valid")
            return null
        }
    }
}

@JsExport
@Serializable
enum class OrderType(val rawValue: String) {
    market("MARKET"),
    stopMarket("STOP_MARKET"),
    takeProfitMarket("TAKE_PROFIT_MARKET"),
    limit("LIMIT"),
    stopLimit("STOP_LIMIT"),
    takeProfitLimit("TAKE_PROFIT"),
    trailingStop("TRAILING_STOP"),
    liquidated("LIQUIDATED"),
    liquidation("LIQUIDATION"),
    offsetting("OFFSETTING"),
    deleveraged("DELEVERAGED"),
    finalSettlement("FINAL_SETTLEMENT"),
    ;

    companion object {
        operator fun invoke(rawValue: String?) =
            OrderType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class OrderSide(val rawValue: String) {
    buy("BUY"), sell("SELL");

    companion object {
        operator fun invoke(rawValue: String) =
            OrderSide.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class OrderStatus(val rawValue: String) {
    cancelled("CANCELED"),
    canceling("BEST_EFFORT_CANCELED"),
    filled("FILLED"),
    `open`("OPEN"),
    pending("PENDING"),
    untriggered("UNTRIGGERED"),
    partiallyFilled("PARTIALLY_FILLED");

    companion object {
        operator fun invoke(rawValue: String): OrderStatus? {
            return if (rawValue == "BEST_EFFORT_OPENED") OrderStatus.pending else
                OrderStatus.values().firstOrNull { it.rawValue == rawValue }
        }
    }
}

@JsExport
@Serializable
enum class OrderTimeInForce(val rawValue: String) {
    GTT("GTT"),
    IOC("IOC"),
    FOK("FOK");

    companion object {
        operator fun invoke(rawValue: String) =
            OrderTimeInForce.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class ReceiptLine(val rawValue: String) {
    equity("EQUITY"),
    buyingPower("BUYING_POWER"),
    marginUsage("MARGIN_USAGE"),
    expectedPrice("EXPECTED_PRICE"),
    fee("FEE"),
    total("TOTAL"),
    walletBalance("WALLET_BALANCE"),
    bridgeFee("BRIDGE_FEE"),
    exchangeRate("EXCHANGE_RATE"),
    exchangeReceived("EXCHANGE_RECEIVED"),
    slippage("SLIPPAGE"),
    gasFee("GAS_FEES"),
    reward("REWARD"),
    transferRouteEstimatedDuration("TRANSFER_ROUTE_ESTIMATE_DURATION");

    companion object {
        operator fun invoke(rawValue: String) =
            ReceiptLine.values().firstOrNull { it.rawValue == rawValue }


        internal fun create(
            parser: ParserProtocol,
            data: List<Any>?,
        ): IList<ReceiptLine>? {
            return data?.mapNotNull {
                val string = parser.asString(it)
                if (string != null) invoke(string) else null
            }?.toIList()
        }
    }
}


@JsExport
@Serializable
data class TradeInput(
    val type: OrderType?,
    val side: OrderSide?,
    val marketId: String?,
    val size: TradeInputSize?,
    val price: TradeInputPrice?,
    val timeInForce: String?,
    val goodTil: TradeInputGoodUntil?,
    val execution: String?,
    val reduceOnly: Boolean,
    val postOnly: Boolean,
    val fee: Double?,
    val bracket: TradeInputBracket?,
    val marketOrder: TradeInputMarketOrder?,
    val options: TradeInputOptions?,
    val summary: TradeInputSummary?,
) {
    companion object {
        internal fun create(
            existing: TradeInput?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInput? {
            DebugLogger.log("creating Trade Input\n")

            data?.let {
                val type = parser.asString(data["type"])?.let {
                    OrderType.invoke(it)
                }
                val side = parser.asString(data["side"])?.let {
                    OrderSide.invoke(it)
                }
                val marketId = parser.asString(data["marketId"])

                val size = TradeInputSize.create(existing?.size, parser, parser.asMap(data["size"]))
                val price =
                    TradeInputPrice.create(existing?.price, parser, parser.asMap(data["price"]))

                val timeInForce = parser.asString(data["timeInForce"])
                val execution = parser.asString(data["execution"])

                val reduceOnly = parser.asBool(data["reduceOnly"]) ?: false
                val postOnly = parser.asBool(data["postOnly"]) ?: false

                val fee = parser.asDouble(data["fee"])

                val goodTil = TradeInputGoodUntil.create(
                    existing?.goodTil,
                    parser,
                    parser.asMap(data["goodTil"])
                )
                val bracket = TradeInputBracket.create(
                    existing?.bracket,
                    parser,
                    parser.asMap(data["bracket"])
                )
                val marketOrder =
                    TradeInputMarketOrder.create(
                        existing?.marketOrder,
                        parser,
                        parser.asMap(data["marketOrder"])
                    )
                val options = TradeInputOptions.create(
                    existing?.options,
                    parser,
                    parser.asMap(data["options"]),
                )
                val summary = TradeInputSummary.create(
                    existing?.summary,
                    parser,
                    parser.asMap(data["summary"])
                )

                return if (existing?.type != type ||
                    existing?.side != side ||
                    existing?.marketId != marketId ||
                    existing?.size != size ||
                    existing?.price != price ||
                    existing?.timeInForce != timeInForce ||
                    existing?.goodTil != goodTil ||
                    existing?.execution != execution ||
                    existing?.reduceOnly != reduceOnly ||
                    existing.postOnly != postOnly ||
                    existing.fee != fee ||
                    existing.bracket != bracket ||
                    existing.marketOrder != marketOrder ||
                    existing.options != options ||
                    existing.summary != summary
                ) {
                    TradeInput(
                        type,
                        side,
                        marketId,
                        size,
                        price,
                        timeInForce,
                        goodTil,
                        execution,
                        reduceOnly,
                        postOnly,
                        fee,
                        bracket,
                        marketOrder,
                        options,
                        summary
                    )
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input not valid")
            return null
        }
    }
}
