package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol
import kollections.iListOf
import kollections.iMutableListOf

internal class TradeBracketOrdersValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    override fun validateTrade(
        subaccount: IMap<String, Any>?,
        market: IMap<String, Any>?,
        trade: IMap<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): IList<Any>? {
        if (parser.asBool(parser.value(trade, "options.needsBrackets")) == true) {
            val marketId = parser.asString(trade["marketId"]) ?: return null
            val position =
                parser.asMap(parser.value(subaccount, "openPositions.$marketId")) ?: return null
            val price = parser.asDouble(parser.value(trade, "summary.price")) ?: return null
            val tickSize = parser.asString(parser.value(market, "configs.tickSize")) ?: "0.01"
            return validateBrackets(
                position,
                trade,
                price,
                tickSize
            )
        } else {
            return null
        }
    }

    private fun validateBrackets(
        position: IMap<String, Any>,
        trade: IMap<String, Any>,
        price: Double,
        tickSize: String,
    ): IList<Any>? {
        val errors = iMutableListOf<Any>()
        val takeProfitError = validateTakeProfit(
            position,
            trade,
            price,
            tickSize
        )
        if (takeProfitError != null) {
            errors.add(takeProfitError)
        }
        val stopError = validateStopLoss(
            position,
            trade,
            price,
            tickSize
        )
        if (stopError != null) {
            errors.add(stopError)
        }
        return if (errors.size > 0) errors else null
    }

    private fun validateTakeProfit(
        position: IMap<String, Any>,
        trade: IMap<String, Any>,
        price: Double,
        tickSize: String,
    ): IMap<String, Any>? {
        val triggerPrice =
            parser.asDouble(parser.value(trade, "brackets.takeProfit.triggerPrice")) ?: return null
        return validateTakeProfitTriggerToMarketPrice(trade, triggerPrice, price, tickSize)
            ?: validateTakeProfitTriggerToLiquidationPrice(
                trade,
                position,
                triggerPrice,
                tickSize
            )
            ?: validateTakeProfitReduceOnly(trade, position)
    }

    private fun validateTakeProfitTriggerToMarketPrice(
        trade: IMap<String, Any>,
        triggerPrice: Double,
        price: Double,
        tickSize: String,
    ): IMap<String, Any>? {
        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (triggerPrice >= price) {
                    triggerPriceError(
                        "BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        iListOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE",
                        iMapOf(
                            "EXPECTED_PRICE" to iMapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize
                            )
                        )
                    )
                } else null
            }

            "BUY" -> {
                if (triggerPrice <= price) {
                    triggerPriceError(
                        "BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        iListOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                        iMapOf(
                            "EXPECTED_PRICE" to iMapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize
                            )
                        )
                    )
                } else null
            }

            else -> null
        }
    }

    private fun validateTakeProfitTriggerToLiquidationPrice(
        trade: IMap<String, Any>,
        position: IMap<String, Any>,
        triggerPrice: Double,
        tickSize: String,
    ): IMap<String, Any>? {
        val sizePostOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: Numeric.double.ZERO
        val liquidationPrice =
            parser.asDouble(parser.value(position, "size.liquidationPrice")) ?: return null

        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (sizePostOrder > Numeric.double.ZERO && triggerPrice < liquidationPrice) {
                    triggerPriceError(
                        "BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        iListOf(
                            "brackets.takeProfit.triggerPrice",
                            "brackets.takeProfit.reduceOnly"
                        ),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        iMapOf(
                            "TRIGGER_PRICE_LIMIT" to iMapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize
                            )
                        )
                    )
                } else null
            }

            "BUY" -> {
                if (sizePostOrder < Numeric.double.ZERO && triggerPrice > liquidationPrice) {
                    triggerPriceError(
                        "BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        iListOf(
                            "brackets.takeProfit.triggerPrice",
                            "brackets.takeProfit.reduceOnly"
                        ),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        iMapOf(
                            "TRIGGER_PRICE_LIMIT" to iMapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize
                            )
                        )
                    )
                } else null
            }

            else -> null
        }
    }

    private fun validateTakeProfitReduceOnly(
        trade: IMap<String, Any>,
        position: IMap<String, Any>,
    ): IMap<String, Any>? {
        val reduceOnly =
            parser.asBool(parser.value(trade, "brackets.takeProfit.reduceOnly")) ?: false
        val sizePostOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: 0.0
        return if (reduceOnly) when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (sizePostOrder > 0.0) {
                    reduceOnlyError(
                        iListOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly")
                    )
                } else null
            }

            "BUY" -> {
                if (sizePostOrder < 0.0) {
                    reduceOnlyError(
                        iListOf("brackets.takeProfit.triggerPrice", "brackets.takeProfit.reduceOnly")
                    )
                } else null
            }

            else -> null
        } else null
    }


    private fun validateStopLoss(
        position: IMap<String, Any>,
        trade: IMap<String, Any>,
        price: Double,
        tickSize: String,
    ): IMap<String, Any>? {
        val triggerPrice =
            parser.asDouble(parser.value(trade, "brackets.stopLoss.triggerPrice")) ?: return null
        return validateStopLossTriggerToMarketPrice(trade, triggerPrice, price, tickSize)
            ?: validateStopLossTriggerToLiquidationPrice(
                trade,
                position,
                triggerPrice,
                tickSize
            )
            ?: validateStopLossReduceOnly(trade, position)
    }

    private fun validateStopLossTriggerToMarketPrice(
        trade: IMap<String, Any>,
        triggerPrice: Double,
        price: Double,
        tickSize: String,
    ): IMap<String, Any>? {
        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (triggerPrice <= price) {
                    triggerPriceError(
                        "BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        iListOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE",
                        iMapOf(
                            "EXPECTED_PRICE" to iMapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize
                            )
                        )
                    )
                } else null
            }

            "BUY" -> {
                if (triggerPrice >= price) {
                    triggerPriceError(
                        "BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        iListOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                        iMapOf(
                            "EXPECTED_PRICE" to iMapOf(
                                "value" to price,
                                "format" to "price",
                                "tickSize" to tickSize
                            )
                        )
                    )
                } else null
            }

            else -> null
        }
    }

    private fun validateStopLossTriggerToLiquidationPrice(
        trade: IMap<String, Any>,
        position: IMap<String, Any>,
        triggerPrice: Double,
        tickSize: String,
    ): IMap<String, Any>? {
        val sizePostOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: Numeric.double.ZERO
        val liquidationPrice =
            parser.asDouble(parser.value(position, "size.liquidationPrice")) ?: return null

        return when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (sizePostOrder < Numeric.double.ZERO && triggerPrice > liquidationPrice) {
                    triggerPriceError(
                        "BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        iListOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        iMapOf(
                            "TRIGGER_PRICE_LIMIT" to iMapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize
                            )
                        )
                    )
                } else null
            }

            "BUY" -> {
                if (sizePostOrder > Numeric.double.ZERO && triggerPrice < liquidationPrice) {
                    triggerPriceError(
                        "BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        iListOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.percent"),
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        iMapOf(
                            "TRIGGER_PRICE_LIMIT" to iMapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize
                            )
                        )
                    )
                } else null
            }

            else -> null
        }
    }

    private fun validateStopLossReduceOnly(
        trade: IMap<String, Any>,
        position: IMap<String, Any>,
    ): IMap<String, Any>? {
        val reduceOnly = parser.asBool(parser.value(trade, "brackets.stopLoss.reduceOnly")) ?: false
        val sizePostOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: 0.0
        return if (reduceOnly) when (parser.asString(trade["side"])) {
            "SELL" -> {
                if (sizePostOrder > 0.0) {
                    reduceOnlyError(
                        iListOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.reduceOnly")
                    )
                } else null
            }

            "BUY" -> {
                if (sizePostOrder < 0.0) {
                    reduceOnlyError(
                        iListOf("brackets.stopLoss.triggerPrice", "brackets.stopLoss.reduceOnly")
                    )
                } else null
            }

            else -> null
        } else null
    }


    private fun triggerPriceError(
        errorCode: String,
        fields: IList<String>,
        title: String,
        text: String,
        params: IMap<String, Any>?,
    ): IMap<String, Any> {
        return error(
            "ERROR",
            errorCode,
            fields,
            "APP.TRADE.ENTER_TRIGGER_PRICE",
            title,
            text,
            params
        )
    }

    private fun reduceOnlyError(
        field: IList<String>,
    ): IMap<String, Any> {
        return error(
            "ERROR",
            "WOULD_NOT_REDUCE_UNCHECK",
            field,
            "APP.TRADE.ENTER_TRIGGER_PRICE",
            "ERRORS.TRADE_BOX_TITLE.WOULD_NOT_REDUCE_UNCHECK",
            "ERRORS.TRADE_BOX.WOULD_NOT_REDUCE_UNCHECK"
        )
    }
}
