package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalPerpetualPosition
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.InternalTradeInputState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradeBracketOrdersValidator(
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
        if (!trade.options.needsBrackets) {
            return null
        }

        val marketId = trade.marketId ?: return null
        val market = internalState.marketsSummary.markets[marketId] ?: return null
        val subaccount = internalState.wallet.account.subaccounts[subaccountNumber]
        val position = subaccount?.openPositions?.get(marketId) ?: return null
        val price = trade.summary?.price ?: return null
        val tickSize = market.perpetualMarket?.configs?.tickSize?.toString() ?: "0.01"
        return validateBrackets(
            position = position,
            trade = trade,
            price = price,
            tickSize = tickSize,
        )
    }

    private fun validateBrackets(
        position: InternalPerpetualPosition,
        trade: InternalTradeInputState,
        price: Double,
        tickSize: String,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        validateTakeProfit(
            position = position,
            trade = trade,
            price = price,
            tickSize = tickSize,
        )?.let {
            errors.add(it)
        }

        validateStopLoss(
            position = position,
            trade = trade,
            price = price,
            tickSize = tickSize,
        )?.let {
            errors.add(it)
        }

        return errors
    }

    private fun validateTakeProfit(
        position: InternalPerpetualPosition,
        trade: InternalTradeInputState,
        price: Double,
        tickSize: String,
    ): ValidationError? {
        val triggerPrice = trade.brackets?.takeProfit?.triggerPrice ?: return null
        return validateTakeProfitTriggerToMarketPrice(
            trade = trade,
            triggerPrice = triggerPrice,
            price = price,
            tickSize = tickSize,
        ) ?: validateTakeProfitTriggerToLiquidationPrice(
            trade = trade,
            position = position,
            triggerPrice = triggerPrice,
            tickSize = tickSize,
        ) ?: validateTakeProfitReduceOnly(
            trade = trade,
            position = position,
        )
    }

    private fun validateTakeProfitTriggerToMarketPrice(
        trade: InternalTradeInputState,
        triggerPrice: Double,
        price: Double,
        tickSize: String,
    ): ValidationError? {
        when (trade.side) {
            OrderSide.Sell -> {
                if (triggerPrice >= price) {
                    return triggerPriceError(
                        errorCode = "BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        fields = listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.percent"),
                        title = "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        text = "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        params = mapOf(
                            "EXPECTED_PRICE" to mapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    return null
                }
            }
            OrderSide.Buy -> {
                if (triggerPrice <= price) {
                    return triggerPriceError(
                        errorCode = "BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        fields = listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.percent"),
                        title = "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        text = "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        params = mapOf(
                            "EXPECTED_PRICE" to mapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    return null
                }
            }
            else -> return null
        }
    }

    private fun validateTakeProfitTriggerToLiquidationPrice(
        trade: InternalTradeInputState,
        position: InternalPerpetualPosition,
        triggerPrice: Double,
        tickSize: String,
    ): ValidationError? {
        val sizePostOrder = position.calculated[CalculationPeriod.post]?.size ?: Numeric.double.ZERO
        val liquidationPrice = position.calculated[CalculationPeriod.post]?.liquidationPrice
            ?: return null

        when (trade.side) {
            OrderSide.Sell -> {
                if (sizePostOrder < Numeric.double.ZERO && triggerPrice > liquidationPrice) {
                    return triggerPriceError(
                        errorCode = "BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        fields = listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly"),
                        title = "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        text = "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        params = mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    return null
                }
            }
            OrderSide.Buy -> {
                if (sizePostOrder > Numeric.double.ZERO && triggerPrice < liquidationPrice) {
                    return triggerPriceError(
                        errorCode = "BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        fields = listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly"),
                        title = "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        text = "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        params = mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    return null
                }
            }
            else -> return null
        }
    }

    private fun validateTakeProfitReduceOnly(
        trade: InternalTradeInputState,
        position: InternalPerpetualPosition,
    ): ValidationError? {
        val reduceOnly = trade.brackets?.takeProfit?.reduceOnly ?: false
        val sizePostOrder = position.calculated[CalculationPeriod.post]?.size ?: Numeric.double.ZERO
        return if (reduceOnly) {
            when (trade.side) {
                OrderSide.Sell -> {
                    if (sizePostOrder > Numeric.double.ZERO) {
                        reduceOnlyError(
                            field = listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly"),
                        )
                    } else {
                        null
                    }
                }
                OrderSide.Buy -> {
                    if (sizePostOrder < Numeric.double.ZERO) {
                        reduceOnlyError(
                            field = listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly"),
                        )
                    } else {
                        null
                    }
                }
                else -> null
            }
        } else {
            null
        }
    }

    private fun validateStopLoss(
        position: InternalPerpetualPosition,
        trade: InternalTradeInputState,
        price: Double,
        tickSize: String,
    ): ValidationError? {
        val triggerPrice = trade.brackets?.stopLoss?.triggerPrice ?: return null
        return validateStopLossTriggerToMarketPrice(
            trade = trade,
            triggerPrice = triggerPrice,
            price = price,
            tickSize = tickSize,
        ) ?: validateStopLossTriggerToLiquidationPrice(
            trade = trade,
            position = position,
            triggerPrice = triggerPrice,
            tickSize = tickSize,
        ) ?: validateStopLossReduceOnly(
            trade = trade,
            position = position,
        )
    }

    private fun validateStopLossTriggerToMarketPrice(
        trade: InternalTradeInputState,
        triggerPrice: Double,
        price: Double,
        tickSize: String,
    ): ValidationError? {
        when (trade.side) {
            OrderSide.Sell -> {
                if (triggerPrice <= price) {
                    return triggerPriceError(
                        errorCode = "BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        fields = listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        title = "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        text = "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        params = mapOf(
                            "EXPECTED_PRICE" to mapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    return null
                }
            }
            OrderSide.Buy -> {
                if (triggerPrice >= price) {
                    return triggerPriceError(
                        errorCode = "BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        fields = listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        title = "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        text = "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        params = mapOf(
                            "EXPECTED_PRICE" to mapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    return null
                }
            }
            else -> return null
        }
    }

    private fun validateStopLossTriggerToLiquidationPrice(
        trade: InternalTradeInputState,
        position: InternalPerpetualPosition,
        triggerPrice: Double,
        tickSize: String,
    ): ValidationError? {
        val sizePostOrder = position.calculated[CalculationPeriod.post]?.size ?: Numeric.double.ZERO
        val liquidationPrice = position.calculated[CalculationPeriod.post]?.liquidationPrice
            ?: return null

        when (trade.side) {
            OrderSide.Sell -> {
                if (sizePostOrder < Numeric.double.ZERO && triggerPrice > liquidationPrice) {
                    return triggerPriceError(
                        errorCode = "BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        fields = listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        title = "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        text = "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        params = mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    return null
                }
            }
            OrderSide.Buy -> {
                if (sizePostOrder > Numeric.double.ZERO && triggerPrice < liquidationPrice) {
                    return triggerPriceError(
                        errorCode = "BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        fields = listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        title = "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        text = "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        params = mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    return null
                }
            }
            else -> return null
        }
    }

    private fun validateStopLossReduceOnly(
        trade: InternalTradeInputState,
        position: InternalPerpetualPosition,
    ): ValidationError? {
        val reduceOnly = trade.brackets?.stopLoss?.reduceOnly ?: false
        val sizePostOrder = position.calculated[CalculationPeriod.post]?.size ?: Numeric.double.ZERO
        return if (reduceOnly) {
            when (trade.side) {
                OrderSide.Sell -> {
                    if (sizePostOrder > Numeric.double.ZERO) {
                        reduceOnlyError(
                            field = listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.reduceOnly"),
                        )
                    } else {
                        null
                    }
                }
                OrderSide.Buy -> {
                    if (sizePostOrder < Numeric.double.ZERO) {
                        reduceOnlyError(
                            field = listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.reduceOnly"),
                        )
                    } else {
                        null
                    }
                }
                else -> null
            }
        } else {
            null
        }
    }

    private fun triggerPriceError(
        errorCode: String,
        fields: List<String>,
        title: String,
        text: String,
        params: Map<String, Any>?,
    ): ValidationError {
        return error(
            type = ErrorType.error,
            errorCode = errorCode,
            fields = fields,
            actionStringKey = "APP.TRADE.ENTER_TRIGGER_PRICE",
            titleStringKey = title,
            textStringKey = text,
            textParams = params,
        )
    }

    private fun reduceOnlyError(
        field: List<String>,
    ): ValidationError {
        return error(
            type = ErrorType.error,
            errorCode = "WOULD_NOT_REDUCE_UNCHECK",
            fields = field,
            actionStringKey = "APP.TRADE.ENTER_TRIGGER_PRICE",
            titleStringKey = "ERRORS.TRADE_BOX_TITLE.WOULD_NOT_REDUCE_UNCHECK",
            textStringKey = "ERRORS.TRADE_BOX.WOULD_NOT_REDUCE_UNCHECK",
        )
    }
}
