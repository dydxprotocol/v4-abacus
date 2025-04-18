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
import exchange.dydx.abacus.state.InternalPerpetualPosition
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.InternalSubaccountState
import exchange.dydx.abacus.state.InternalTradeInputState
import exchange.dydx.abacus.state.helper.Formatter
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

    private fun reducingWithLimit(
        change: PositionChange,
        type: OrderType?,
    ): Boolean {
        return when (change) {
            PositionChange.CLOSING, PositionChange.DECREASING -> true
            else -> false
        } && type == OrderType.Limit
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
}
