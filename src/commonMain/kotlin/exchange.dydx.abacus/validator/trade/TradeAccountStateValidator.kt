package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradeAccountStateValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    override fun validateTrade(
        internalState: InternalState,
        subaccountNumber: Int?,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?
    ): List<ValidationError>? {
        val trade = when (internalState.input.currentType) {
            InputType.TRADE -> internalState.input.trade
            InputType.CLOSE_POSITION -> internalState.input.closePosition
            else -> return null
        }
        val subaccountNumber = subaccountNumber ?: return null
        val subaccount = internalState.wallet.account.subaccounts[subaccountNumber] ?: return null
        val isIsolatedMarginTrade = subaccountNumber >= NUM_PARENT_SUBACCOUNTS

        val errors = mutableListOf<ValidationError>()
        when (isIsolatedMarginTrade) {
            true -> {
                validateSubaccountCrossOrders(
                    subaccount = subaccount,
                    trade = trade,
                )?.let {
                    errors.add(it)
                }

                validateSubaccountPostOrders(
                    subaccount = subaccount,
                    trade = trade,
                    change = change,
                )?.let {
                    errors.add(it)
                }
            }
            false -> {
                validateSubaccountMarginUsage(
                    subaccount = subaccount,
                    change = change,
                )?.let {
                    errors.add(it)
                }

                validateSubaccountCrossOrders(
                    subaccount = subaccount,
                    trade = trade,
                )?.let {
                    errors.add(it)
                }

                validateSubaccountPostOrders(
                    subaccount = subaccount,
                    trade = trade,
                    change = change,
                )?.let {
                    errors.add(it)
                }
            }
        }

        return errors
    }

    override fun validateTradeDeprecated(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<Any>? {
        return if (subaccount != null) {
            val subaccountNumber = parser.asInt(subaccount["subaccountNumber"])
            val isIsolatedMarginTrade = subaccountNumber == null || subaccountNumber >= NUM_PARENT_SUBACCOUNTS

            val errors = mutableListOf<Any>()

            when (isIsolatedMarginTrade) {
                true -> {
                    val crossOrdersError = validateSubaccountCrossOrdersDeprecated(
                        parser,
                        subaccount,
                        trade,
                    )
                    if (crossOrdersError != null) {
                        errors.add(crossOrdersError)
                    }
                    val postAllOrdersError = validateSubaccountPostOrdersDeprecated(
                        parser,
                        subaccount,
                        trade,
                        change,
                    )
                    if (postAllOrdersError != null) {
                        errors.add(postAllOrdersError)
                    }
                }
                false -> {
                    val marginError = validateSubaccountMarginUsageDeprecated(
                        parser,
                        subaccount,
                        change,
                    )
                    if (marginError != null) {
                        errors.add(marginError)
                    }
                    val crossOrdersError = validateSubaccountCrossOrdersDeprecated(
                        parser,
                        subaccount,
                        trade,
                    )
                    if (crossOrdersError != null) {
                        errors.add(crossOrdersError)
                    }
                    val postAllOrdersError = validateSubaccountPostOrdersDeprecated(
                        parser,
                        subaccount,
                        trade,
                        change,
                    )
                    if (postAllOrdersError != null) {
                        errors.add(postAllOrdersError)
                    }
                }
            }

            if (errors.size > 0) errors else null
        } else {
            null
        }
    }

    private fun validateSubaccountMarginUsage(
        subaccount: InternalSubaccountState,
        change: PositionChange,
    ): ValidationError? {
        /*
        INVALID_NEW_ACCOUNT_MARGIN_USAGE
         */
        return when (change) {
            PositionChange.CLOSING, PositionChange.DECREASING -> null
            else -> {
                val equity = subaccount.calculated[CalculationPeriod.post]?.equity
                val marginUsage = subaccount.calculated[CalculationPeriod.post]?.marginUsage
                if (equity != null &&
                    (
                        equity == Numeric.double.ZERO ||
                            marginUsage == null ||
                            marginUsage < Numeric.double.ZERO ||
                            marginUsage > Numeric.double.ONE
                        )
                ) {
                    error(
                        type = ErrorType.error,
                        errorCode = "INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                        fields = listOf("size.size"),
                        actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                        titleStringKey = "ERRORS.TRADE_BOX_TITLE.INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                        textStringKey = "ERRORS.TRADE_BOX.INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun validateSubaccountMarginUsageDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>,
        change: PositionChange,
    ): Map<String, Any>? {
        /*
        INVALID_NEW_ACCOUNT_MARGIN_USAGE
         */
        return when (change) {
            PositionChange.CLOSING, PositionChange.DECREASING -> null
            else -> {
                val equity = parser.asDouble(parser.value(subaccount, "equity.postOrder"))
                val marginUsage = parser.asDouble(parser.value(subaccount, "marginUsage.postOrder"))
                if (equity != null &&
                    (
                        equity == Numeric.double.ZERO ||
                            marginUsage == null ||
                            marginUsage < Numeric.double.ZERO ||
                            marginUsage > Numeric.double.ONE
                        )
                ) {
                    errorDeprecated(
                        type = "ERROR",
                        errorCode = "INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                        fields = listOf("size.size"),
                        actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                        titleStringKey = "ERRORS.TRADE_BOX_TITLE.INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                        textStringKey = "ERRORS.TRADE_BOX.INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun validateSubaccountCrossOrders(
        subaccount: InternalSubaccountState,
        trade: InternalTradeInputState,
    ): ValidationError? {
        /*
        ORDER_CROSSES_OWN_ORDER
         */
        return if (fillsExistingOrder(
                trade = trade,
                orders = subaccount.orders,
            )
        ) {
            error(
                type = ErrorType.error,
                errorCode = "ORDER_CROSSES_OWN_ORDER",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_CROSSES_OWN_ORDER",
                textStringKey = "ERRORS.TRADE_BOX.ORDER_CROSSES_OWN_ORDER",
            )
        } else {
            null
        }
    }

    private fun validateSubaccountCrossOrdersDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>,
        trade: Map<String, Any>,
    ): Map<String, Any>? {
        /*
        ORDER_CROSSES_OWN_ORDER
         */
        return if (fillsExistingOrderDeprecated(
                parser,
                trade,
                parser.asNativeMap(subaccount["orders"]),
            )
        ) {
            errorDeprecated(
                type = "ERROR",
                errorCode = "ORDER_CROSSES_OWN_ORDER",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_CROSSES_OWN_ORDER",
                textStringKey = "ERRORS.TRADE_BOX.ORDER_CROSSES_OWN_ORDER",
            )
        } else {
            null
        }
    }

    private fun fillsExistingOrder(
        trade: InternalTradeInputState,
        orders: List<SubaccountOrder>?,
    ): Boolean {
        if (orders == null) {
            return false
        }
        val type = trade.type ?: return false
        // does not apply to trigger/stop trades
        if (type.isSlTp) return false

        val price = if (type == OrderType.Market) {
            trade.marketOrder?.worstPrice
        } else {
            trade.summary?.price
        } ?: return false
        val marketId = trade.marketId ?: return false
        val side = trade.side ?: return false

        val existing = orders.firstOrNull first@{ order ->
            val orderPrice = order.price
            val orderType = order.type
            val orderMarketId = order.marketId
            val orderStatus = order.status
            val orderSide = order.side
            if (orderMarketId == marketId && orderType == OrderType.Limit && orderStatus == OrderStatus.Open) {
                when (side) {
                    OrderSide.Buy -> {
                        if (orderSide == OrderSide.Sell && price >= orderPrice) {
                            val stopPrice = trade.price?.triggerPrice
                            if (stopPrice != null) {
                                stopPrice < price
                            } else {
                                true
                            }
                        } else {
                            false
                        }
                    }

                    OrderSide.Sell -> {
                        if (orderSide == OrderSide.Buy && price <= orderPrice) {
                            val stopPrice = trade.price?.triggerPrice
                            if (stopPrice != null) {
                                stopPrice > price
                            } else {
                                true
                            }
                        } else {
                            false
                        }
                    }
                }
            } else {
                false
            }
        }

        return existing != null
    }

    private fun fillsExistingOrderDeprecated(
        parser: ParserProtocol,
        trade: Map<String, Any>,
        orders: Map<String, Any>?,
    ): Boolean {
        if (orders != null) {
            val type = parser.asString(trade["type"]) ?: return false
            if (listOf("STOP_MARKET", "TAKE_PROFIT_MARKET", "STOP_LIMIT", "TAKE_PROFIT").contains(type)) return false
            val price = parser.asDouble(
                parser.value(
                    trade,
                    if (type == "MARKET") {
                        "marketOrder.worstPrice"
                    } else {
                        "summary.price"
                    },
                ),
            ) ?: return false
            val marketId = parser.asString(trade["marketId"]) ?: return false
            val side = parser.asString(trade["side"]) ?: return false

            val existing = orders.values.firstOrNull first@{ item ->
                val order = parser.asNativeMap(item) ?: return@first false
                val orderPrice = parser.asDouble(order["price"]) ?: return@first false
                val orderType = parser.asString(order["type"]) ?: return@first false
                val orderMarketId = parser.asString(order["marketId"]) ?: return@first false
                val orderStatus = parser.asString(order["status"]) ?: return@first false
                val orderSide = parser.asString(order["side"]) ?: return@first false
                if (orderMarketId == marketId && orderType == "LIMIT" && orderStatus == "OPEN") {
                    when (side) {
                        "BUY" -> {
                            if (orderSide == "SELL" && price >= orderPrice) {
                                val stopPrice =
                                    parser.asDouble(parser.value(trade, "price.triggerPrice"))
                                if (stopPrice != null) {
                                    stopPrice < price
                                } else {
                                    true
                                }
                            } else {
                                false
                            }
                        }

                        "SELL" -> {
                            if (orderSide == "BUY" && price <= orderPrice) {
                                val stopPrice =
                                    parser.asDouble(parser.value(trade, "price.triggerPrice"))
                                if (stopPrice != null) {
                                    stopPrice > price
                                } else {
                                    true
                                }
                            } else {
                                false
                            }
                        }

                        else -> false
                    }
                } else {
                    false
                }
            }
            return existing != null
        } else {
            return false
        }
    }

    private fun validateSubaccountPostOrders(
        subaccount: InternalSubaccountState,
        trade: InternalTradeInputState,
        change: PositionChange,
    ): ValidationError? {
        /*
        ORDER_WITH_CURRENT_ORDERS_INVALID
         */
        if (reducingWithLimit(change, trade.type)) {
            return null
        }
        val positions = subaccount.openPositions
        if (positions != null) {
            var overleveraged = false
            for ((_, value) in positions) {
                val position = value
                overleveraged = positionOverLeveragedPostAllOrders(position)
                if (overleveraged) {
                    break
                }
            }
            return if (overleveraged) {
                error(
                    type = ErrorType.error,
                    errorCode = "ORDER_WITH_CURRENT_ORDERS_INVALID",
                    fields = null,
                    actionStringKey = null,
                    titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_WITH_CURRENT_ORDERS_INVALID",
                    textStringKey = "ERRORS.TRADE_BOX.ORDER_WITH_CURRENT_ORDERS_INVALID",
                )
            } else {
                null
            }
        } else {
            return null
        }
    }

    private fun validateSubaccountPostOrdersDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>,
        trade: Map<String, Any>,
        change: PositionChange,
    ): Map<String, Any>? {
        /*
        ORDER_WITH_CURRENT_ORDERS_INVALID
         */
        return if (reducingWithLimitDeprecated(parser, change, parser.asString(trade["type"]))) {
            null
        } else {
            val positions = parser.asNativeMap(subaccount["openPositions"])
            if (positions != null) {
                var overleveraged = false
                for ((_, value) in positions) {
                    val position = parser.asNativeMap(value)
                    overleveraged = positionOverleveragedPostAllOrdersDeprecated(parser, position)
                    if (overleveraged) {
                        break
                    }
                }
                if (overleveraged) {
                    errorDeprecated(
                        type = "ERROR",
                        errorCode = "ORDER_WITH_CURRENT_ORDERS_INVALID",
                        fields = null,
                        actionStringKey = null,
                        titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_WITH_CURRENT_ORDERS_INVALID",
                        textStringKey = "ERRORS.TRADE_BOX.ORDER_WITH_CURRENT_ORDERS_INVALID",
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun reducingWithLimit(
        change: PositionChange,
        type: OrderType?,
    ): Boolean {
        return when (change) {
            PositionChange.CLOSING, PositionChange.DECREASING -> true
            else -> false
        } && type == OrderType.Limit
    }

    private fun reducingWithLimitDeprecated(
        parser: ParserProtocol,
        change: PositionChange,
        type: String?,
    ): Boolean {
        return when (change) {
            PositionChange.CLOSING, PositionChange.DECREASING -> true
            else -> false
        } && type == "LIMIT"
    }

    private fun positionOverLeveragedPostAllOrders(
        position: InternalPerpetualPosition?,
    ): Boolean {
        /*
        ORDER_WITH_CURRENT_ORDERS_INVALID
         */
        val leverage = position?.calculated?.get(CalculationPeriod.settled)?.leverage ?: return false
        val adjustedImf = position?.calculated?.get(CalculationPeriod.settled)?.adjustedImf ?: return false
        return if (adjustedImf > Numeric.double.ZERO) (leverage > Numeric.double.ONE / adjustedImf) else true
    }

    private fun positionOverleveragedPostAllOrdersDeprecated(
        parser: ParserProtocol,
        position: Map<String, Any>?,
    ): Boolean {
        /*
        ORDER_WITH_CURRENT_ORDERS_INVALID
         */
        val leverage =
            parser.asDouble(parser.value(position, "leverage.postAllOrders")) ?: return false
        val adjustedImf =
            parser.asDouble(parser.value(position, "adjustedImf.postAllOrders")) ?: return false
        return if (adjustedImf > Numeric.double.ZERO) (leverage > Numeric.double.ONE / adjustedImf) else true
    }
}
