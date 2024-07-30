package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradeBracketOrdersValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    override fun validateTrade(
        staticTyping: Boolean,
        internalState: InternalState,
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
            return validateBrackets(
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
        position: Map<String, Any>,
        trade: Map<String, Any>,
        price: Double,
        tickSize: String,
    ): List<Any>? {
        val errors = mutableListOf<Any>()
        val takeProfitError = validateTakeProfit(
            position,
            trade,
            price,
            tickSize,
        )
        if (takeProfitError != null) {
            errors.add(takeProfitError)
        }
        val stopError = validateStopLoss(
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
        position: Map<String, Any>,
        trade: Map<String, Any>,
        price: Double,
        tickSize: String,
    ): Map<String, Any>? {
        val triggerPrice =
            parser.asDouble(parser.value(trade, "brackets.takeProfit.triggerPrice")) ?: return null
        return validateTakeProfitTriggerToMarketPrice(trade, triggerPrice, price, tickSize)
            ?: validateTakeProfitTriggerToLiquidationPrice(
                trade,
                position,
                triggerPrice,
                tickSize,
            )
            ?: validateTakeProfitReduceOnly(trade, position)
    }

    private fun validateTakeProfitTriggerToMarketPrice(
        trade: Map<String, Any>,
        triggerPrice: Double,
        price: Double,
        tickSize: String,
    ): Map<String, Any>? {
        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (triggerPrice >= price) {
                    triggerPriceError(
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
                    triggerPriceError(
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
                    triggerPriceError(
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
                    triggerPriceError(
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
                        reduceOnlyError(
                            listOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly"),
                        )
                    } else {
                        null
                    }
                }

                "BUY" -> {
                    if (sizePostOrder < 0.0) {
                        reduceOnlyError(
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
        position: Map<String, Any>,
        trade: Map<String, Any>,
        price: Double,
        tickSize: String,
    ): Map<String, Any>? {
        val triggerPrice =
            parser.asDouble(parser.value(trade, "brackets.stopLoss.triggerPrice")) ?: return null
        return validateStopLossTriggerToMarketPrice(trade, triggerPrice, price, tickSize)
            ?: validateStopLossTriggerToLiquidationPrice(
                trade,
                position,
                triggerPrice,
                tickSize,
            )
            ?: validateStopLossReduceOnly(trade, position)
    }

    private fun validateStopLossTriggerToMarketPrice(
        trade: Map<String, Any>,
        triggerPrice: Double,
        price: Double,
        tickSize: String,
    ): Map<String, Any>? {
        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (triggerPrice <= price) {
                    triggerPriceError(
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
                    triggerPriceError(
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
                    triggerPriceError(
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
                    triggerPriceError(
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
                        reduceOnlyError(
                            listOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.reduceOnly"),
                        )
                    } else {
                        null
                    }
                }

                "BUY" -> {
                    if (sizePostOrder < 0.0) {
                        reduceOnlyError(
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

    private fun triggerPriceError(
        errorCode: String,
        fields: List<String>,
        title: String,
        text: String,
        params: Map<String, Any>?,
    ): Map<String, Any> {
        return error(
            "ERROR",
            errorCode,
            fields,
            "APP.TRADE.ENTER_TRIGGER_PRICE",
            title,
            text,
            params,
        )
    }

    private fun reduceOnlyError(
        field: List<String>,
    ): Map<String, Any> {
        return error(
            "ERROR",
            "WOULD_NOT_REDUCE_UNCHECK",
            field,
            "APP.TRADE.ENTER_TRIGGER_PRICE",
            "ERRORS.TRADE_BOX_TITLE.WOULD_NOT_REDUCE_UNCHECK",
            "ERRORS.TRADE_BOX.WOULD_NOT_REDUCE_UNCHECK",
        )
    }
}
