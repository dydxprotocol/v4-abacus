package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

enum class RelativeToPrice(val rawValue: String) {
    ABOVE("ABOVE"),
    BELOW("BELOW");

    companion object {
        operator fun invoke(rawValue: String) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

internal class TradeTriggerPriceValidator(
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
        val needsTriggerPrice = trade.options.needsTriggerPrice
        if (!needsTriggerPrice) {
            return null
        }

        val errors = mutableListOf<ValidationError>()
        val type = trade.type ?: return null
        val side = trade.side ?: return null
        val subaccountNumber = subaccountNumber ?: return null
        val subaccount = internalState.wallet.account.subaccounts[subaccountNumber]

        val market = internalState.marketsSummary.markets[trade.marketId]
        val oraclePrice = market?.perpetualMarket?.oraclePrice ?: return null
        val triggerPrice = trade.price?.triggerPrice ?: return null
        val tickSize = market.perpetualMarket?.configs?.tickSize ?: 0.01

        when (val triggerToIndex = requiredTriggerToIndexPrice(type, side)) {
            /*
               TRIGGER_MUST_ABOVE_INDEX_PRICE
               TRIGGER_MUST_BELOW_INDEX_PRICE
             */
            RelativeToPrice.ABOVE -> {
                if (triggerPrice <= oraclePrice) {
                    errors.add(
                        triggerToIndexError(
                            triggerToIndex = triggerToIndex,
                            type = type,
                            oraclePrice = oraclePrice,
                            tickSize = tickSize.toString(),
                        ),
                    )
                }
            }

            RelativeToPrice.BELOW -> {
                if (triggerPrice >= oraclePrice) {
                    errors.add(
                        triggerToIndexError(
                            triggerToIndex = triggerToIndex,
                            type = type,
                            oraclePrice = oraclePrice,
                            tickSize = tickSize.toString(),
                        ),
                    )
                }
            }

            else -> {}
        }

        val triggerToLiquidation = requiredTriggerToLiquidationPrice(type, side, change)
        if (triggerToLiquidation != null) {
            /*
            SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
            BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
             */

            val subaccount = internalState.wallet.account.subaccounts[subaccountNumber ?: 0]
            val liquidationPrice = liquidationPrice(subaccount, trade)
            if (liquidationPrice != null) {
                when (triggerToLiquidation) {
                    RelativeToPrice.ABOVE -> {
                        if (triggerPrice <= liquidationPrice) {
                            errors.add(
                                triggerToLiquidationError(
                                    triggerToLiquidation = triggerToLiquidation,
                                    triggerLiquidation = liquidationPrice,
                                    tickSize = tickSize.toString(),
                                ),
                            )
                        }
                    }

                    RelativeToPrice.BELOW -> {
                        if (triggerPrice >= liquidationPrice) {
                            errors.add(
                                triggerToLiquidationError(
                                    triggerToLiquidation = triggerToLiquidation,
                                    triggerLiquidation = liquidationPrice,
                                    tickSize = tickSize.toString(),
                                ),
                            )
                        }
                    }
                }
            }
        }

        return errors
    }

    /*
    They are still used to calculate payload, but no longer used for validation
    private val stopMarketSlippageBufferBTC = 0.05; // 5% for Stop Market
    private val takeProfitMarketSlippageBufferBTC = 0.1; // 10% for Take Profit Market
    private val stopMarketSlippageBuffer = 0.1; // 10% for Stop Market
    private val takeProfitMarketSlippageBuffer = 0.2; // 20% for Take Profit Market
     */

    override fun validateTradeDeprecated(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<Any>? {
        val needsTriggerPrice =
            parser.asBool(parser.value(trade, "options.needsTriggerPrice")) ?: false
        if (needsTriggerPrice) {
            val errors = mutableListOf<Any>()
            val type = parser.asString(trade["type"]) ?: return null
            val side = parser.asString(trade["side"]) ?: return null
            val oraclePrice = parser.asDouble(
                parser.value(
                    market,
                    "oraclePrice",
                ),
            ) ?: return null
            val triggerPrice =
                parser.asDouble(parser.value(trade, "price.triggerPrice")) ?: return null
            val tickSize = parser.asString(parser.value(market, "configs.tickSize")) ?: "0.01"
            when (val triggerToIndex = requiredTriggerToIndexPriceDeprecated(type, side)) {
                /*
                TRIGGER_MUST_ABOVE_INDEX_PRICE
                TRIGGER_MUST_BELOW_INDEX_PRICE
                 */
                RelativeToPrice.ABOVE -> {
                    if (triggerPrice <= oraclePrice) {
                        errors.add(
                            triggerToIndexErrorDeprecated(
                                triggerToIndex,
                                type,
                                oraclePrice,
                                tickSize,
                            ),
                        )
                    }
                }

                RelativeToPrice.BELOW -> {
                    if (triggerPrice >= oraclePrice) {
                        errors.add(
                            triggerToIndexErrorDeprecated(
                                triggerToIndex,
                                type,
                                oraclePrice,
                                tickSize,
                            ),
                        )
                    }
                }

                else -> {}
            }
            val triggerToLiquidation = requiredTriggerToLiquidationPriceDeprecated(type, side, change)
            if (triggerToLiquidation != null) {
                /*
                SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
                BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
                 */
                val liquidationPrice = liquidationPriceDeprecated(subaccount, trade)
                if (liquidationPrice != null) {
                    when (triggerToLiquidation) {
                        RelativeToPrice.ABOVE -> {
                            if (triggerPrice <= liquidationPrice) {
                                errors.add(
                                    triggerToLiquidationErrorDeprecated(
                                        triggerToLiquidation,
                                        liquidationPrice,
                                        tickSize,
                                    ),
                                )
                            }
                        }

                        RelativeToPrice.BELOW -> {
                            if (triggerPrice >= liquidationPrice) {
                                errors.add(
                                    triggerToLiquidationErrorDeprecated(
                                        triggerToLiquidation,
                                        liquidationPrice,
                                        tickSize,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
            return errors
        }
        return null
    }

    private fun requiredTriggerToIndexPrice(
        type: OrderType,
        side: OrderSide
    ): RelativeToPrice? {
        return when (type) {
            OrderType.StopLimit, OrderType.StopMarket, OrderType.TrailingStop ->
                when (side) {
                    OrderSide.Buy -> RelativeToPrice.ABOVE
                    OrderSide.Sell -> RelativeToPrice.BELOW
                }

            OrderType.TakeProfitLimit, OrderType.TakeProfitMarket ->
                when (side) {
                    OrderSide.Buy -> RelativeToPrice.BELOW
                    OrderSide.Sell -> RelativeToPrice.ABOVE
                }

            else -> null
        }
    }

    private fun requiredTriggerToIndexPriceDeprecated(type: String, side: String): RelativeToPrice? {
        return when (type) {
            "STOP_LIMIT", "STOP_MARKET", "TRAILING_STOP" ->
                when (side) {
                    "BUY" -> RelativeToPrice.ABOVE
                    "SELL" -> RelativeToPrice.BELOW
                    else -> null
                }

            "TAKE_PROFIT", "TAKE_PROFIT_MARKET" ->
                when (side) {
                    "BUY" -> RelativeToPrice.BELOW
                    "SELL" -> RelativeToPrice.ABOVE
                    else -> null
                }

            else -> null
        }
    }

    private fun triggerToIndexError(
        triggerToIndex: RelativeToPrice,
        type: OrderType,
        oraclePrice: Double,
        tickSize: String,
    ): ValidationError {
        val fields = if (type == OrderType.TrailingStop) {
            listOf("price.trailingPercent")
        } else {
            listOf("price.triggerPrice")
        }
        val action = if (type == OrderType.TrailingStop) {
            "APP.TRADE.MODIFY_TRAILING_PERCENT"
        } else {
            "APP.TRADE.MODIFY_TRIGGER_PRICE"
        }
        val params = mapOf(
            "INDEX_PRICE" to
                mapOf(
                    "value" to oraclePrice,
                    "format" to "price",
                    "tickSize" to tickSize,
                ),
        )
        return when (triggerToIndex) {
            RelativeToPrice.ABOVE -> error(
                type = ErrorType.error,
                errorCode = "TRIGGER_MUST_ABOVE_INDEX_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.TRIGGER_MUST_ABOVE_INDEX_PRICE",
                textStringKey = "ERRORS.TRADE_BOX.TRIGGER_MUST_ABOVE_INDEX_PRICE",
                textParams = params,
            )

            RelativeToPrice.BELOW -> error(
                type = ErrorType.error,
                errorCode = "TRIGGER_MUST_BELOW_INDEX_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.TRIGGER_MUST_BELOW_INDEX_PRICE",
                textStringKey = "ERRORS.TRADE_BOX.TRIGGER_MUST_BELOW_INDEX_PRICE",
                textParams = params,
            )
        }
    }

    private fun triggerToIndexErrorDeprecated(
        triggerToIndex: RelativeToPrice,
        type: String,
        oraclePrice: Double,
        tickSize: String,
    ): Map<String, Any> {
        val fields =
            if (type == "TRAILING_STOP") {
                listOf("price.trailingPercent")
            } else {
                listOf("price.triggerPrice")
            }
        val action =
            if (type == "TRAILING_STOP") {
                "APP.TRADE.MODIFY_TRAILING_PERCENT"
            } else {
                "APP.TRADE.MODIFY_TRIGGER_PRICE"
            }
        val params = mapOf(
            "INDEX_PRICE" to
                mapOf(
                    "value" to oraclePrice,
                    "format" to "price",
                    "tickSize" to tickSize,
                ),
        )
        return when (triggerToIndex) {
            RelativeToPrice.ABOVE -> errorDeprecated(
                type = "ERROR",
                errorCode = "TRIGGER_MUST_ABOVE_INDEX_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.TRIGGER_MUST_ABOVE_INDEX_PRICE",
                textStringKey = "ERRORS.TRADE_BOX.TRIGGER_MUST_ABOVE_INDEX_PRICE",
                textParams = params,
            )

            else -> errorDeprecated(
                type = "ERROR",
                errorCode = "TRIGGER_MUST_BELOW_INDEX_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.TRIGGER_MUST_BELOW_INDEX_PRICE",
                textStringKey = "ERRORS.TRADE_BOX.TRIGGER_MUST_BELOW_INDEX_PRICE",
                textParams = params,
            )
        }
    }

    private fun requiredTriggerToLiquidationPrice(
        type: OrderType,
        side: OrderSide,
        change: PositionChange,
    ): RelativeToPrice? {
        return when (type) {
            OrderType.StopMarket ->
                when (change) {
                    PositionChange.CLOSING, PositionChange.DECREASING, PositionChange.CROSSING -> {
                        when (side) {
                            OrderSide.Sell -> RelativeToPrice.ABOVE
                            OrderSide.Buy -> RelativeToPrice.BELOW
                            else -> null
                        }
                    }

                    else -> null
                }

            else -> null
        }
    }

    private fun requiredTriggerToLiquidationPriceDeprecated(
        type: String,
        side: String,
        change: PositionChange,
    ): RelativeToPrice? {
        return when (type) {
            "STOP_MARKET" ->
                when (change) {
                    PositionChange.CLOSING, PositionChange.DECREASING, PositionChange.CROSSING -> {
                        when (side) {
                            "SELL" -> RelativeToPrice.ABOVE
                            "BUY" -> RelativeToPrice.BELOW
                            else -> null
                        }
                    }

                    else -> null
                }

            else -> null
        }
    }

    private fun liquidationPrice(
        subaccount: InternalSubaccountState?,
        trade: InternalTradeInputState,
    ): Double? {
        val marketId = trade.marketId ?: return null
        val position = subaccount?.openPositions?.get(marketId) ?: return null
        return position.calculated[CalculationPeriod.current]?.liquidationPrice
    }

    private fun liquidationPriceDeprecated(
        subaccount: Map<String, Any>?,
        trade: Map<String, Any>,
    ): Double? {
        val marketId = parser.asString(trade["marketId"]) ?: return null
        return parser.asDouble(
            parser.value(
                subaccount,
                "openPositions.$marketId.liquidationPrice.current",
            ),
        )
    }

    private fun triggerToLiquidationError(
        triggerToLiquidation: RelativeToPrice,
        triggerLiquidation: Double,
        tickSize: String,
    ): ValidationError {
        val fields = listOf("price.triggerPrice")
        val action = "APP.TRADE.MODIFY_TRIGGER_PRICE"
        // Localizations uses TRIGGER_PRICE_LIMIT as paramater name
        val params =
            mapOf(
                "TRIGGER_PRICE_LIMIT" to mapOf(
                    "value" to triggerLiquidation,
                    "format" to "price",
                    "tickSize" to tickSize,
                ),
            )
        return when (triggerToLiquidation) {
            RelativeToPrice.ABOVE -> error(
                type = ErrorType.error,
                errorCode = "SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                textStringKey = "ERRORS.TRADE_BOX.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                textParams = params,
            )

            RelativeToPrice.BELOW -> error(
                type = ErrorType.error,
                errorCode = "BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                textStringKey = "ERRORS.TRADE_BOX.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                textParams = params,
            )
        }
    }

    private fun triggerToLiquidationErrorDeprecated(
        triggerToLiquidation: RelativeToPrice,
        triggerLiquidation: Double,
        tickSize: String,
    ): Map<String, Any> {
        val fields = listOf("price.triggerPrice")
        val action = "APP.TRADE.MODIFY_TRIGGER_PRICE"
        // Localizations uses TRIGGER_PRICE_LIMIT as paramater name
        val params =
            mapOf(
                "TRIGGER_PRICE_LIMIT" to mapOf(
                    "value" to triggerLiquidation,
                    "format" to "price",
                    "tickSize" to tickSize,
                ),
            )
        return when (triggerToLiquidation) {
            RelativeToPrice.ABOVE -> errorDeprecated(
                type = "ERROR",
                errorCode = "SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                textStringKey = "ERRORS.TRADE_BOX.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                textParams = params,
            )

            RelativeToPrice.BELOW -> errorDeprecated(
                type = "ERROR",
                errorCode = "BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                textStringKey = "ERRORS.TRADE_BOX.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                textParams = params,
            )
        }
    }
}
