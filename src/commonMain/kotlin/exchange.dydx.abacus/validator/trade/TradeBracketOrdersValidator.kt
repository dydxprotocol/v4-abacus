package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
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

    override fun validateTradeDeprecated(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<Any>? {
        if (parser.asBool(parser.value(trade, "options.needsBrackets")) == true) {
            val marketId = parser.asString(trade["marketId"]) ?: return null
            val position =
                parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId")) ?: return null
            val price = parser.asDouble(parser.value(trade, "summary.price")) ?: return null
            val tickSize = parser.asString(parser.value(market, "configs.tickSize")) ?: "0.01"
            return validateBracketsDeprecated(
                position,
                trade,
                price,
                tickSize,
            )
        } else {
            return null
        }
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

    private fun validateBracketsDeprecated(
        position: Map<String, Any>,
        trade: Map<String, Any>,
        price: Double,
        tickSize: String,
    ): List<Any>? {
        val errors = mutableListOf<Any>()
        val takeProfitError = validateTakeProfitDeprecated(
            position,
            trade,
            price,
            tickSize,
        )
        if (takeProfitError != null) {
            errors.add(takeProfitError)
        }
        val stopError = validateStopLossDeprecated(
            position,
            trade,
            price,
            tickSize,
        )
        if (stopError != null) {
            errors.add(stopError)
        }
        return if (errors.size > 0) errors else null
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

    private fun validateTakeProfitDeprecated(
        position: Map<String, Any>,
        trade: Map<String, Any>,
        price: Double,
        tickSize: String,
    ): Map<String, Any>? {
        val triggerPrice =
            parser.asDouble(parser.value(trade, "brackets.takeProfit.triggerPrice")) ?: return null
        return validateTakeProfitTriggerToMarketPriceDeprecated(trade, triggerPrice, price, tickSize)
            ?: validateTakeProfitTriggerToLiquidationPriceDeprecated(
                trade,
                position,
                triggerPrice,
                tickSize,
            )
            ?: validateTakeProfitReduceOnlyDeprecated(trade, position)
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

    private fun validateTakeProfitTriggerToMarketPriceDeprecated(
        trade: Map<String, Any>,
        triggerPrice: Double,
        price: Double,
        tickSize: String,
    ): Map<String, Any>? {
        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (triggerPrice >= price) {
                    triggerPriceErrorDeprecated(
                        "BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        mapOf(
                            "EXPECTED_PRICE" to mapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    null
                }
            }

            "BUY" -> {
                if (triggerPrice <= price) {
                    triggerPriceErrorDeprecated(
                        "BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        mapOf(
                            "EXPECTED_PRICE" to mapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    null
                }
            }

            else -> null
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

    private fun validateTakeProfitTriggerToLiquidationPriceDeprecated(
        trade: Map<String, Any>,
        position: Map<String, Any>,
        triggerPrice: Double,
        tickSize: String,
    ): Map<String, Any>? {
        val sizePostOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: Numeric.double.ZERO
        val liquidationPrice =
            parser.asDouble(parser.value(position, "size.liquidationPrice")) ?: return null

        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (sizePostOrder > Numeric.double.ZERO && triggerPrice < liquidationPrice) {
                    triggerPriceErrorDeprecated(
                        "BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        listOf(
                            "brackets.takeProfit.triggerPrice",
                            "brackets.takeProfit.reduceOnly",
                        ),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    null
                }
            }

            "BUY" -> {
                if (sizePostOrder < Numeric.double.ZERO && triggerPrice > liquidationPrice) {
                    triggerPriceErrorDeprecated(
                        "BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        listOf(
                            "brackets.takeProfit.triggerPrice",
                            "brackets.takeProfit.reduceOnly",
                        ),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    null
                }
            }

            else -> null
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

    private fun validateTakeProfitReduceOnlyDeprecated(
        trade: Map<String, Any>,
        position: Map<String, Any>,
    ): Map<String, Any>? {
        val reduceOnly =
            parser.asBool(parser.value(trade, "brackets.takeProfit.reduceOnly")) ?: false
        val sizePostOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: 0.0
        return if (reduceOnly) {
            when (parser.asString(trade["side"])) {
                "SELL" -> {
                    if (sizePostOrder > 0.0) {
                        reduceOnlyErrorDeprecated(
                            listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly"),
                        )
                    } else {
                        null
                    }
                }

                "BUY" -> {
                    if (sizePostOrder < 0.0) {
                        reduceOnlyErrorDeprecated(
                            listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly"),
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

    private fun validateStopLossDeprecated(
        position: Map<String, Any>,
        trade: Map<String, Any>,
        price: Double,
        tickSize: String,
    ): Map<String, Any>? {
        val triggerPrice =
            parser.asDouble(parser.value(trade, "brackets.stopLoss.triggerPrice")) ?: return null
        return validateStopLossTriggerToMarketPriceDeprecated(trade, triggerPrice, price, tickSize)
            ?: validateStopLossTriggerToLiquidationPriceDeprecated(
                trade,
                position,
                triggerPrice,
                tickSize,
            )
            ?: validateStopLossReduceOnlyDeprecated(trade, position)
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

    private fun validateStopLossTriggerToMarketPriceDeprecated(
        trade: Map<String, Any>,
        triggerPrice: Double,
        price: Double,
        tickSize: String,
    ): Map<String, Any>? {
        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (triggerPrice <= price) {
                    triggerPriceErrorDeprecated(
                        "BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        mapOf(
                            "EXPECTED_PRICE" to mapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    null
                }
            }

            "BUY" -> {
                if (triggerPrice >= price) {
                    triggerPriceErrorDeprecated(
                        "BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        mapOf(
                            "EXPECTED_PRICE" to mapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    null
                }
            }

            else -> null
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

    private fun validateStopLossTriggerToLiquidationPriceDeprecated(
        trade: Map<String, Any>,
        position: Map<String, Any>,
        triggerPrice: Double,
        tickSize: String,
    ): Map<String, Any>? {
        val sizePostOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: Numeric.double.ZERO
        val liquidationPrice =
            parser.asDouble(parser.value(position, "size.liquidationPrice")) ?: return null

        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (sizePostOrder < Numeric.double.ZERO && triggerPrice > liquidationPrice) {
                    triggerPriceErrorDeprecated(
                        "BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    null
                }
            }

            "BUY" -> {
                if (sizePostOrder > Numeric.double.ZERO && triggerPrice < liquidationPrice) {
                    triggerPriceErrorDeprecated(
                        "BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    )
                } else {
                    null
                }
            }

            else -> null
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

    private fun validateStopLossReduceOnlyDeprecated(
        trade: Map<String, Any>,
        position: Map<String, Any>,
    ): Map<String, Any>? {
        val reduceOnly = parser.asBool(parser.value(trade, "brackets.stopLoss.reduceOnly")) ?: false
        val sizePostOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: 0.0
        return if (reduceOnly) {
            when (parser.asString(trade["side"])) {
                "SELL" -> {
                    if (sizePostOrder > 0.0) {
                        reduceOnlyErrorDeprecated(
                            listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.reduceOnly"),
                        )
                    } else {
                        null
                    }
                }

                "BUY" -> {
                    if (sizePostOrder < 0.0) {
                        reduceOnlyErrorDeprecated(
                            listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.reduceOnly"),
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

    private fun triggerPriceErrorDeprecated(
        errorCode: String,
        fields: List<String>,
        title: String,
        text: String,
        params: Map<String, Any>?,
    ): Map<String, Any> {
        return errorDeprecated(
            type = "ERROR",
            errorCode = errorCode,
            fields = fields,
            actionStringKey = "APP.TRADE.ENTER_TRIGGER_PRICE",
            titleStringKey = title,
            textStringKey = text,
            textParams = params,
        )
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

    private fun reduceOnlyErrorDeprecated(
        field: List<String>,
    ): Map<String, Any> {
        return errorDeprecated(
            type = "ERROR",
            errorCode = "WOULD_NOT_REDUCE_UNCHECK",
            fields = field,
            actionStringKey = "APP.TRADE.ENTER_TRIGGER_PRICE",
            titleStringKey = "ERRORS.TRADE_BOX_TITLE.WOULD_NOT_REDUCE_UNCHECK",
            textStringKey = "ERRORS.TRADE_BOX.WOULD_NOT_REDUCE_UNCHECK",
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
