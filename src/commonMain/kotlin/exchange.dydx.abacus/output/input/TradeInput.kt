package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalTradeInputOptions
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputSummary
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIList
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

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
            Logger.d { "creating Selection Option\n" }

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
            Logger.d { "Selection Option not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class Tooltip(
    val titleStringKey: String,
    val bodyStringKey: String,
)

@JsExport
@Serializable
data class TradeInputOptions(
    val needsMarginMode: Boolean,
    val needsSize: Boolean,
    val needsLeverage: Boolean,
    val needsBalancePercent: Boolean,
    val maxLeverage: Double?,
    val needsLimitPrice: Boolean,
    val needsTargetLeverage: Boolean,
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
    val marginModeOptions: IList<SelectionOption>?,
    val reduceOnlyTooltip: Tooltip?,
    val postOnlyTooltip: Tooltip?,
) {
    companion object {
        private val typeOptionsV4Array =
            iListOf(
                SelectionOption(
                    type = OrderType.Limit.rawValue,
                    string = null,
                    stringKey = "APP.TRADE.LIMIT_ORDER_SHORT",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = OrderType.Market.rawValue,
                    string = null,
                    stringKey = "APP.TRADE.MARKET_ORDER_SHORT",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = OrderType.StopLimit.rawValue,
                    string = null,
                    stringKey = "APP.TRADE.STOP_LIMIT",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = OrderType.StopMarket.rawValue,
                    string = null,
                    stringKey = "APP.TRADE.STOP_MARKET",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = OrderType.TakeProfitLimit.rawValue,
                    string = null,
                    stringKey = "APP.TRADE.TAKE_PROFIT",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = OrderType.TakeProfitMarket.rawValue,
                    string = null,
                    stringKey = "APP.TRADE.TAKE_PROFIT_MARKET",
                    iconUrl = null,
                ),
            )

        private val sideOptionsArray =
            iListOf(
                SelectionOption(
                    type = OrderSide.Buy.rawValue,
                    string = null,
                    stringKey = "APP.GENERAL.BUY",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = OrderSide.Sell.rawValue,
                    string = null,
                    stringKey = "APP.GENERAL.SELL",
                    iconUrl = null,
                ),
            )

        private val goodTilUnitOptionsArray =
            iListOf(
                SelectionOption(
                    type = "M",
                    string = null,
                    stringKey = "APP.GENERAL.TIME_STRINGS.MINUTES_SHORT",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = "H",
                    string = null,
                    stringKey = "APP.GENERAL.TIME_STRINGS.HOURS",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = "D",
                    string = null,
                    stringKey = "APP.GENERAL.TIME_STRINGS.DAYS",
                    iconUrl = null,
                ),
                SelectionOption(
                    type = "W",
                    string = null,
                    stringKey = "APP.GENERAL.TIME_STRINGS.WEEKS",
                    iconUrl = null,
                ),
            )

        internal fun create(
            state: InternalTradeInputOptions?,
        ): TradeInputOptions? {
            if (state == null) {
                return null
            }

            return TradeInputOptions(
                needsMarginMode = state.needsMarginMode,
                needsSize = state.needsSize,
                needsLeverage = state.needsLeverage,
                needsBalancePercent = state.needsBalancePercent,
                maxLeverage = state.maxLeverage,
                needsLimitPrice = state.needsLimitPrice,
                needsTargetLeverage = state.needsTargetLeverage,
                needsTriggerPrice = state.needsTriggerPrice,
                needsTrailingPercent = state.needsTrailingPercent,
                needsGoodUntil = state.needsGoodUntil,
                needsReduceOnly = state.needsReduceOnly,
                needsPostOnly = state.needsPostOnly,
                needsBrackets = state.needsBrackets,
                typeOptions = state.orderTypeOptions?.toIList() ?: iListOf(),
                sideOptions = state.sideOptions?.toIList() ?: iListOf(),
                timeInForceOptions = state.timeInForceOptions?.toIList(),
                goodTilUnitOptions = state.goodTilUnitOptions?.toIList() ?: iListOf(),
                executionOptions = state.executionOptions?.toIList(),
                marginModeOptions = state.marginModeOptions?.toIList(),
                reduceOnlyTooltip = state.reduceOnlyTooltip,
                postOnlyTooltip = state.postOnlyTooltip,
            )
        }

        internal fun create(
            existing: TradeInputOptions?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputOptions? {
            Logger.d { "creating Trade Input Options\n" }

            data?.let {
                val needsMarginMode = parser.asBool(data["needsMarginMode"]) ?: true
                val needsSize = parser.asBool(data["needsSize"]) ?: false
                val needsLeverage = parser.asBool(data["needsLeverage"]) ?: false
                val needsBalancePercent = parser.asBool(data["needsBalancePercent"]) ?: false
                val maxLeverage = parser.asDouble(data["maxLeverage"])
                val needsLimitPrice = parser.asBool(data["needsLimitPrice"]) ?: false
                val needsTargetLeverage = parser.asBool(data["needsTargetLeverage"]) ?: false
                val needsTriggerPrice = parser.asBool(data["needsTriggerPrice"]) ?: false
                val needsTrailingPercent = parser.asBool(data["needsTrailingPercent"]) ?: false
                val needsGoodUntil =
                    parser.asBool(data["needsGoodUntil"]) ?: false
                val needsReduceOnly = parser.asBool(data["needsReduceOnly"]) ?: false
                val needsPostOnly = parser.asBool(data["needsPostOnly"]) ?: false
                val needsBrackets = parser.asBool(data["needsBrackets"]) ?: false
                val reduceOnlyTooltip = buildToolTip(parser.asString(data["reduceOnlyPromptStringKey"]))
                val postOnlyOnlyTooltip = buildToolTip(parser.asString(data["postOnlyPromptStringKey"]))

                var marginModeOptions: IMutableList<SelectionOption>? = null
                parser.asList(data["marginModeOptions"])?.let { data ->
                    marginModeOptions = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        SelectionOption.create(
                            existing = existing?.marginModeOptions?.getOrNull(i),
                            parser = parser,
                            data = parser.asMap(item),
                        )?.let {
                            marginModeOptions?.add(it)
                        }
                    }
                }

                var timeInForceOptions: IMutableList<SelectionOption>? = null
                parser.asList(data["timeInForceOptions"])?.let { data ->
                    timeInForceOptions = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        SelectionOption.create(
                            existing?.timeInForceOptions?.getOrNull(i),
                            parser,
                            parser.asMap(item),
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
                            parser,
                            parser.asMap(item),
                        )?.let {
                            executionOptions?.add(it)
                        }
                    }
                }

                val timeInForceOptionsArray =
                    timeInForceOptions
                val executionOptionsArray =
                    executionOptions

                return if (
                    existing?.needsMarginMode != needsMarginMode ||
                    existing.needsSize != needsSize ||
                    existing.needsLeverage != needsLeverage ||
                    existing.needsBalancePercent != needsBalancePercent ||
                    existing.maxLeverage != maxLeverage ||
                    existing.needsLimitPrice != needsLimitPrice ||
                    existing.needsTargetLeverage != needsTargetLeverage ||
                    existing.needsTriggerPrice != needsTriggerPrice ||
                    existing.needsTrailingPercent != needsTrailingPercent ||
                    existing.needsGoodUntil != needsGoodUntil ||
                    existing.needsReduceOnly != needsReduceOnly ||
                    existing.needsPostOnly != needsPostOnly ||
                    existing.needsBrackets != needsBrackets ||
                    existing.timeInForceOptions != timeInForceOptionsArray ||
                    existing.marginModeOptions != marginModeOptions ||
                    existing.executionOptions != executionOptionsArray ||
                    existing.reduceOnlyTooltip != reduceOnlyTooltip
                ) {
                    val typeOptions = typeOptionsV4Array

                    TradeInputOptions(
                        needsMarginMode,
                        needsSize,
                        needsLeverage,
                        needsBalancePercent,
                        maxLeverage,
                        needsLimitPrice,
                        needsTargetLeverage,
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
                        marginModeOptions,
                        reduceOnlyTooltip,
                        postOnlyOnlyTooltip,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Trade Input Options not valid" }
            return null
        }

        private fun buildToolTip(stringKey: String?): Tooltip? {
            return if (stringKey != null) {
                Tooltip(titleStringKey = "$stringKey.TITLE", bodyStringKey = "$stringKey.BODY")
            } else {
                null
            }
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
    val positionMargin: Double?,
    val positionLeverage: Double?
) {
    companion object {
        internal fun create(
            state: InternalTradeInputSummary?,
        ): TradeInputSummary {
            return TradeInputSummary(
                price = state?.price,
                payloadPrice = state?.payloadPrice,
                size = state?.size,
                usdcSize = state?.usdcSize,
                slippage = state?.slippage,
                fee = state?.fee,
                total = state?.total,
                reward = state?.reward,
                filled = state?.filled ?: false,
                positionMargin = state?.positionMargin,
                positionLeverage = state?.positionLeverage,
            )
        }

        internal fun create(
            existing: TradeInputSummary?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputSummary? {
            Logger.d { "creating Trade Input Summary\n" }

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
                val positionMargin = parser.asDouble(data["positionMargin"])
                val positionLeverage = parser.asDouble(data["positionLeverage"])

                return if (
                    existing?.price != price ||
                    existing?.payloadPrice != payloadPrice ||
                    existing?.size != size ||
                    existing?.usdcSize != usdcSize ||
                    existing?.slippage != slippage ||
                    existing?.fee != fee ||
                    existing?.total != total ||
                    existing?.positionMargin != positionMargin ||
                    existing?.positionLeverage != positionLeverage ||
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
                        filled,
                        positionMargin,
                        positionLeverage,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Trade Input Summary not valid" }
            return null
        }
    }
}

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
            Logger.d { "creating Orderbook Line\n" }
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
    val balancePercent: Double?,
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
            Logger.d { "creating Trade Input Market Order\n" }

            data?.let {
                val size = parser.asDouble(data["size"])
                val usdcSize = parser.asDouble(data["usdcSize"])
                val balancePercent = parser.asDouble(data["balancePercent"])
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
                            parser,
                            parser.asMap(item),
                        )?.let {
                            orderbook?.add(it)
                        }
                    }
                }
                return if (existing?.size != size ||
                    existing?.usdcSize != usdcSize ||
                    existing?.balancePercent != balancePercent ||
                    existing?.price != price ||
                    existing?.worstPrice != worstPrice ||
                    existing?.filled != filled ||
                    existing.orderbook != orderbook
                ) {
                    TradeInputMarketOrder(
                        size,
                        usdcSize,
                        balancePercent,
                        price,
                        worstPrice,
                        filled,
                        orderbook,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Trade Input Market Order not valid" }
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
    val balancePercent: Double?,
    val input: String?,
) {
    companion object {
        internal fun create(
            existing: TradeInputSize?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputSize? {
            Logger.d { "creating Trade Input Size\n" }

            data?.let {
                val size = parser.asDouble(data["size"])
                val usdcSize = parser.asDouble(data["usdcSize"])
                val leverage = parser.asDouble(data["leverage"])
                val balancePercent = parser.asDouble(data["balancePercent"])
                val input = parser.asString(data["input"])
                return if (existing?.size != size ||
                    existing?.usdcSize != usdcSize ||
                    existing?.leverage != leverage ||
                    existing?.balancePercent != balancePercent ||
                    existing?.input != input
                ) {
                    TradeInputSize(size, usdcSize, leverage, balancePercent, input)
                } else {
                    existing
                }
            }
            Logger.d { "Trade Input Size not valid" }
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
            Logger.d { "creating Trade Input Price\n" }

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
            Logger.d { "Trade Input Price not valid" }
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
    internal val timeInterval: Duration?
        get() =
            if (duration != null && unit != null) {
                when (unit) {
                    "M" -> duration.minutes
                    "H" -> duration.hours
                    "D" -> duration.days
                    "W" -> (duration * 7).days
                    else -> null
                }
            } else {
                null
            }

    companion object {
        internal fun create(
            existing: TradeInputGoodUntil?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInputGoodUntil? {
            Logger.d { "creating Trade Input Good Until\n" }

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
            Logger.d { "Trade Input Price not valid" }
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
            Logger.d { "creating Trade Input Good Until\n" }

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
            Logger.d { "Trade Input Price not valid" }
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
            Logger.d { "creating Trade Input Bracket\n" }

            data?.let {
                val stopLoss = TradeInputBracketSide.create(
                    existing?.stopLoss,
                    parser,
                    parser.asMap(data["stopLoss"]),
                )
                val takeProfit =
                    TradeInputBracketSide.create(
                        existing?.takeProfit,
                        parser,
                        parser.asMap(data["takeProfit"]),
                    )
                val goodTil = TradeInputGoodUntil.create(
                    existing?.goodTil,
                    parser,
                    parser.asMap(data["goodTil"]),
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
            Logger.d { "Trade Input Bracket not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
enum class MarginMode(val rawValue: String) {
    Isolated("ISOLATED"),
    Cross("CROSS");

    companion object {
        operator fun invoke(rawValue: String?) =
            MarginMode.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class OrderType(val rawValue: String) {
    Market("MARKET"),
    StopMarket("STOP_MARKET"),
    TakeProfitMarket("TAKE_PROFIT_MARKET"),
    Limit("LIMIT"),
    StopLimit("STOP_LIMIT"),
    TakeProfitLimit("TAKE_PROFIT"),
    TrailingStop("TRAILING_STOP"),
    Liquidated("LIQUIDATED"),
    Liquidation("LIQUIDATION"),
    Offsetting("OFFSETTING"),
    Deleveraged("DELEVERAGED"),
    FinalSettlement("FINAL_SETTLEMENT"),
    ;

    companion object {
        operator fun invoke(rawValue: String?) =
            entries.firstOrNull { it.rawValue == rawValue }
    }

    val isSlTp: Boolean
        get() = listOf(
            StopMarket,
            TakeProfitMarket,
            StopLimit,
            TakeProfitLimit,
        ).contains(this)
}

@JsExport
@Serializable
enum class OrderSide(val rawValue: String) {
    Buy("BUY"),
    Sell("SELL");

    companion object {
        operator fun invoke(rawValue: String?) =
            OrderSide.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class OrderStatus(val rawValue: String) {
    Canceled("CANCELED"),
    Canceling("BEST_EFFORT_CANCELED"),
    Filled("FILLED"),
    Open("OPEN"),
    Pending("PENDING"), // indexer returns order as BEST_EFFORT_OPENED, or BEST_EFFORT_CANCELED when order is IOC
    Untriggered("UNTRIGGERED"),
    PartiallyFilled("PARTIALLY_FILLED"), // indexer returns order as OPEN but order is partially filled
    PartiallyCanceled("PARTIALLY_CANCELED"); // indexer returns order as CANCELED but order is partially filled

    companion object {
        operator fun invoke(rawValue: String?): OrderStatus? {
            return if (rawValue == "BEST_EFFORT_OPENED") {
                Pending
            } else {
                OrderStatus.values().firstOrNull { it.rawValue == rawValue }
            }
        }
    }

    val isFinalized: Boolean
        // once an order is filled, canceled, or canceled with partial fill
        // there is no need to update status again
        get() = listOf(Filled, Canceled, PartiallyCanceled).contains(this)

    val isOpen: Boolean
        get() = listOf(Open, Pending, Untriggered, PartiallyFilled).contains(this)
}

@JsExport
@Serializable
enum class OrderTimeInForce(val rawValue: String) {
    GTT("GTT"),
    IOC("IOC");

    companion object {
        operator fun invoke(rawValue: String?) =
            OrderTimeInForce.values().firstOrNull { it.rawValue == rawValue }
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
    val marginMode: MarginMode,
    val targetLeverage: Double,
    val bracket: TradeInputBracket?,
    val marketOrder: TradeInputMarketOrder?,
    val options: TradeInputOptions?,
    val summary: TradeInputSummary?,
) {
    companion object {
        internal fun create(
            state: InternalTradeInputState?
        ): TradeInput? {
            if (state == null) {
                return null
            }

            return TradeInput(
                type = state.type,
                side = state.side,
                marketId = state.marketId,
                size = state.size,
                price = state.price,
                timeInForce = state.timeInForce,
                goodTil = state.goodTil,
                execution = state.execution,
                reduceOnly = state.reduceOnly,
                postOnly = state.postOnly,
                fee = state.fee,
                marginMode = state.marginMode ?: MarginMode.Cross,
                targetLeverage = state.targetLeverage ?: 1.0,
                bracket = state.brackets,
                marketOrder = state.marketOrder,
                options = TradeInputOptions.create(state.options),
                summary = TradeInputSummary.create(state.summary),
            )
        }

        internal fun create(
            existing: TradeInput?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeInput? {
            Logger.d { "creating Trade Input\n" }

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

                val marginMode = parser.asString(data["marginMode"])?.let {
                    MarginMode.invoke(it)
                } ?: MarginMode.Cross

                val targetLeverage = parser.asDouble(data["targetLeverage"]) ?: 1.0

                val goodTil = TradeInputGoodUntil.create(
                    existing?.goodTil,
                    parser,
                    parser.asMap(data["goodTil"]),
                )
                val bracket = TradeInputBracket.create(
                    existing?.bracket,
                    parser,
                    parser.asMap(data["bracket"]),
                )
                val marketOrder =
                    TradeInputMarketOrder.create(
                        existing?.marketOrder,
                        parser,
                        parser.asMap(data["marketOrder"]),
                    )
                val options = TradeInputOptions.create(
                    existing?.options,
                    parser,
                    parser.asMap(data["options"]),
                )
                val summary = TradeInputSummary.create(
                    existing?.summary,
                    parser,
                    parser.asMap(data["summary"]),
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
                    existing.marginMode != marginMode ||
                    existing.targetLeverage != targetLeverage ||
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
                        marginMode,
                        targetLeverage,
                        bracket,
                        marketOrder,
                        options,
                        summary,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Trade Input not valid" }
            return null
        }
    }
}
