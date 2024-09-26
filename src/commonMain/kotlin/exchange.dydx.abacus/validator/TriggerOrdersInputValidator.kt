package exchange.dydx.abacus.validator

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalTriggerOrderState
import exchange.dydx.abacus.state.internalstate.InternalTriggerOrdersInputState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.model.TriggerOrdersInputField

enum class RelativeToPrice(val rawValue: String) {
    ABOVE("ABOVE"),
    BELOW("BELOW");

    companion object {
        operator fun invoke(rawValue: String) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

internal class TriggerOrdersInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol
) : BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {

    override fun validate(
        internalState: InternalState,
        subaccountNumber: Int?,
        currentBlockAndHeight: BlockAndTime?,
        inputType: InputType,
        environment: V4Environment?,
    ): List<ValidationError>? {
        if (inputType != InputType.TRIGGER_ORDERS) {
            return null
        }
        val errors = mutableListOf<ValidationError>()

        val triggerOrders = internalState.input.triggerOrders
        val marketId = triggerOrders.marketId ?: return null
        val market = internalState.marketsSummary.markets[marketId]
        val subaccountNumber = subaccountNumber ?: return null
        val subaccount = internalState.wallet.account.subaccounts[subaccountNumber] ?: return null
        val position = subaccount.openPositions?.get(marketId) ?: return null
        val tickSize = market?.perpetualMarket?.configs?.tickSize ?: 0.01
        val oraclePrice = market?.perpetualMarket?.oraclePrice ?: return null

        validateTriggerOrders(triggerOrders, market)?.let {
            errors.addAll(it)
        }

        val takeProfitOrder = triggerOrders.takeProfitOrder
        val stopLossOrder = triggerOrders.stopLossOrder

        if (takeProfitOrder != null) {
            validateTriggerOrder(takeProfitOrder, market, oraclePrice, tickSize, position)?.let {
                errors.addAll(it)
            }
        }
        if (stopLossOrder != null) {
            validateTriggerOrder(stopLossOrder, market, oraclePrice, tickSize, position)?.let {
                errors.addAll(it)
            }
        }

        return if (errors.size > 0) errors else null
    }

    override fun validateDeprecated(
        wallet: Map<String, Any>?,
        user: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        markets: Map<String, Any>?,
        configs: Map<String, Any>?,
        currentBlockAndHeight: BlockAndTime?,
        transaction: Map<String, Any>,
        transactionType: String,
        environment: V4Environment?
    ): List<Any>? {
        if (transactionType == "triggerOrders") {
            val errors = mutableListOf<Any>()

            val marketId = parser.asString(transaction["marketId"]) ?: return null
            val market = parser.asNativeMap(markets?.get(marketId))
            val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))
                ?: return null
            val tickSize = parser.asString(parser.value(market, "configs.tickSize")) ?: "0.01"
            val oraclePrice = parser.asDouble(
                parser.value(
                    market,
                    "oraclePrice",
                ),
            ) ?: return null

            validateTriggerOrdersDeprecated(transaction, market)?.let {
                errors.addAll(it)
            }

            val takeProfitOrder = parser.asMap(transaction["takeProfitOrder"])
            val stopLossOrder = parser.asMap(transaction["stopLossOrder"])

            val takeProfitError = if (takeProfitOrder != null) {
                validateTriggerOrderDeprecated(
                    takeProfitOrder,
                    market,
                    oraclePrice,
                    tickSize,
                    position,
                )
            } else {
                null
            }

            if (takeProfitError != null) {
                errors.addAll(takeProfitError)
            }

            val stopLossError = if (stopLossOrder != null) {
                validateTriggerOrderDeprecated(
                    stopLossOrder,
                    market,
                    oraclePrice,
                    tickSize,
                    position,
                )
            } else {
                null
            }

            if (stopLossError != null) {
                errors.addAll(stopLossError)
            }

            return if (errors.size > 0) errors else null
        }
        return null
    }

    private fun validateTriggerOrders(
        triggerOrders: InternalTriggerOrdersInputState,
        market: InternalMarketState?
    ): List<ValidationError>? {
        val errors = mutableListOf<ValidationError>()

        validateSize(triggerOrders.size, market)?.let {
            /*
            ORDER_SIZE_BELOW_MIN_SIZE
             */
            errors.addAll(it)
        }

        return if (errors.size > 0) errors else null
    }

    private fun validateTriggerOrdersDeprecated(
        triggerOrders: Map<String, Any>,
        market: Map<String, Any>?,
    ): MutableList<Any>? {
        val triggerErrors = mutableListOf<Any>()
        validateSizeDeprecated(parser.asDouble(triggerOrders["size"]), market)?.let {
            /*
                ORDER_SIZE_BELOW_MIN_SIZE
             */
            triggerErrors.addAll(it)
        }
        return if (triggerErrors.size > 0) triggerErrors else null
    }

    private fun validateTriggerOrder(
        triggerOrder: InternalTriggerOrderState,
        market: InternalMarketState?,
        oraclePrice: Double,
        tickSize: Double,
        position: InternalPerpetualPosition,
    ): List<ValidationError>? {
        val triggerErrors = mutableListOf<ValidationError>()

        validateRequiredInput(triggerOrder)?.let {
            /*
                REQUIRED_TRIGGER_PRICE
             */
            triggerErrors.addAll(it)
        }

        validateSize(triggerOrder.summary?.size, market)?.let {
            /*
                ORDER_SIZE_BELOW_MIN_SIZE
             */
            triggerErrors.addAll(it)
        }

        validateTriggerPrice(triggerOrder, oraclePrice, tickSize.toString())?.let {
            /*
                TRIGGER_MUST_ABOVE_INDEX_PRICE
                TRIGGER_MUST_BELOW_INDEX_PRICE
             */
            triggerErrors.addAll(it)
        }

        validateLimitPrice(triggerOrder)?.let {
            /*
                LIMIT_MUST_ABOVE_TRIGGER_PRICE
                LIMIT_MUST_BELOW_TRIGGER_PRICE
             */
            triggerErrors.addAll(it)
        }

        validateCalculatedPricesPositive(triggerOrder)?.let {
            /*
                PRICE_MUST_POSITIVE
             */
            triggerErrors.addAll(it)
        }

        validateTriggerToLiquidationPrice(triggerOrder, position, tickSize.toString())?.let {
            /*
                SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
                BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
             */
            triggerErrors.addAll(it)
        }

        return if (triggerErrors.size > 0) triggerErrors else null
    }

    private fun validateTriggerOrderDeprecated(
        triggerOrder: Map<String, Any>,
        market: Map<String, Any>?,
        oraclePrice: Double,
        tickSize: String,
        position: Map<String, Any>,
    ): MutableList<Any>? {
        val triggerErrors = mutableListOf<Any>()

        validateRequiredInputDeprecated(triggerOrder)?.let {
            /*
                REQUIRED_TRIGGER_PRICE
             */
            triggerErrors.addAll(it)
        }
        validateSizeDeprecated(parser.asDouble(parser.value(triggerOrder, "summary.size")), market)?.let {
            /*
                ORDER_SIZE_BELOW_MIN_SIZE
             */
            triggerErrors.addAll(it)
        }
        validateTriggerPriceDeprecated(triggerOrder, oraclePrice, tickSize)?.let {
            /*
                TRIGGER_MUST_ABOVE_INDEX_PRICE
                TRIGGER_MUST_BELOW_INDEX_PRICE
             */
            triggerErrors.addAll(it)
        }
        validateLimitPriceDeprecated(triggerOrder)?.let {
            /*
                LIMIT_MUST_ABOVE_TRIGGER_PRICE
                LIMIT_MUST_BELOW_TRIGGER_PRICE
             */
            triggerErrors.addAll(it)
        }
        validateCalculatedPricesPositiveDeprecated(triggerOrder)?.let {
            /*
                PRICE_MUST_POSITIVE
             */
            triggerErrors.addAll(it)
        }

        validateTriggerToLiquidationPriceDeprecated(triggerOrder, position, tickSize)?.let {
            /*
                SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
                BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
             */
            triggerErrors.addAll(it)
        }

        return if (triggerErrors.size > 0) triggerErrors else null
    }

    private fun validateTriggerToLiquidationPrice(
        triggerOrder: InternalTriggerOrderState,
        position: InternalPerpetualPosition,
        tickSize: String,
    ): List<ValidationError>? {
        val liquidationPrice = position.calculated[CalculationPeriod.current]?.liquidationPrice ?: return null
        val triggerPrice = triggerOrder.price?.triggerPrice ?: return null
        val type = triggerOrder.type
        val side = triggerOrder.side ?: return null

        return when (requiredTriggerToLiquidationPrice(type, side)) {
            RelativeToPrice.ABOVE -> {
                if (triggerPrice <= liquidationPrice) {
                    liquidationPriceError(
                        errorCode = "SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                        titleStringKey = "ERRORS.TRIGGERS_FORM_TITLE.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                        textStringKey = "ERRORS.TRIGGERS_FORM.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE_NO_LIMIT",
                        liquidationPrice = liquidationPrice,
                        tickSize = tickSize,
                    )
                } else {
                    null
                }
            }

            RelativeToPrice.BELOW -> {
                if (triggerPrice >= liquidationPrice) {
                    liquidationPriceError(
                        errorCode = "BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                        titleStringKey = "ERRORS.TRIGGERS_FORM_TITLE.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                        textStringKey = "ERRORS.TRIGGERS_FORM.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE_NO_LIMIT",
                        liquidationPrice = liquidationPrice,
                        tickSize = tickSize,
                    )
                } else {
                    null
                }
            }

            else -> null
        }
    }

    private fun validateTriggerToLiquidationPriceDeprecated(
        triggerOrder: Map<String, Any>,
        position: Map<String, Any>,
        tickSize: String,
    ): List<Any>? {
        val liquidationPrice = parser.asDouble(parser.value(position, "liquidationPrice.current"))
        val triggerPrice = parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
        val type = parser.asString(triggerOrder["type"])?.let { OrderType.invoke(it) }
        val side = parser.asString(triggerOrder["side"])?.let { OrderSide.invoke(it) }

        if (side == null || liquidationPrice == null || triggerPrice == null) {
            return null
        }

        return when (requiredTriggerToLiquidationPrice(type, side)) {
            RelativeToPrice.ABOVE -> {
                if (triggerPrice <= liquidationPrice) {
                    liquidationPriceErrorDeprecated(
                        "SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                        "ERRORS.TRIGGERS_FORM_TITLE.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                        "ERRORS.TRIGGERS_FORM.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE_NO_LIMIT",
                        liquidationPrice,
                        tickSize,
                    )
                } else {
                    null
                }
            }

            RelativeToPrice.BELOW -> {
                if (triggerPrice >= liquidationPrice) {
                    liquidationPriceErrorDeprecated(
                        "BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                        "ERRORS.TRIGGERS_FORM_TITLE.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                        "ERRORS.TRIGGERS_FORM.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE_NO_LIMIT",
                        liquidationPrice,
                        tickSize,
                    )
                } else {
                    null
                }
            }

            else -> null
        }
    }

    private fun liquidationPriceError(
        errorCode: String,
        titleStringKey: String,
        textStringKey: String,
        liquidationPrice: Double?,
        tickSize: String,
    ): List<ValidationError>? {
        return listOf(
            error(
                type = ErrorType.error,
                errorCode = errorCode,
                fields = listOf(TriggerOrdersInputField.stopLossPrice.rawValue),
                actionStringKey = "APP.TRADE.MODIFY_TRIGGER_PRICE",
                titleStringKey = titleStringKey,
                textStringKey = textStringKey,
                textParams = mapOf(
                    "TRIGGER_PRICE_LIMIT" to mapOf(
                        "value" to liquidationPrice,
                        "format" to "price",
                        "tickSize" to tickSize,
                    ),
                ),
            ),
        )
    }

    private fun liquidationPriceErrorDeprecated(
        errorCode: String,
        titleStringKey: String,
        textStringKey: String,
        liquidationPrice: Double?,
        tickSize: String,
    ): List<Any>? {
        return listOf(
            errorDeprecated(
                type = "ERROR",
                errorCode = errorCode,
                fields = listOf(TriggerOrdersInputField.stopLossPrice.rawValue),
                actionStringKey = "APP.TRADE.MODIFY_TRIGGER_PRICE",
                titleStringKey = titleStringKey,
                textStringKey = textStringKey,
                textParams = mapOf(
                    "TRIGGER_PRICE_LIMIT" to mapOf(
                        "value" to liquidationPrice,
                        "format" to "price",
                        "tickSize" to tickSize,
                    ),
                ),
            ),
        )
    }

    private fun validateRequiredInput(
        triggerOrder: InternalTriggerOrderState,
    ): List<ValidationError>? {
        val triggerPrice = triggerOrder.price?.triggerPrice
        val limitPrice = triggerOrder.price?.limitPrice

        if (triggerPrice == null && limitPrice != null) {
            return listOf(
                required(
                    errorCode = "REQUIRED_TRIGGER_PRICE",
                    field = "price.triggerPrice",
                    actionStringKey = "APP.TRADE.ENTER_TRIGGER_PRICE",
                ),
            )
        }

        return null
    }

    private fun validateRequiredInputDeprecated(
        triggerOrder: Map<String, Any>,
    ): List<Any>? {
        val errors = mutableListOf<Map<String, Any>>()

        val triggerPrice = parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
        val limitPrice = parser.asDouble(parser.value(triggerOrder, "price.limitPrice"))

        if (triggerPrice == null && limitPrice != null) {
            errors.add(
                requiredDeprecated(
                    "REQUIRED_TRIGGER_PRICE",
                    "price.triggerPrice",
                    "APP.TRADE.ENTER_TRIGGER_PRICE",
                ),
            )
        }

        return if (errors.size > 0) errors else null
    }

    private fun validateCalculatedPricesPositive(
        triggerOrder: InternalTriggerOrderState,
    ): List<ValidationError>? {
        val type = triggerOrder.type
        val triggerPrice = triggerOrder.price?.triggerPrice
        val limitPrice = triggerOrder.price?.limitPrice
        val inputField = triggerOrder.price?.input
        val fields = if (type == OrderType.StopLimit || type == OrderType.StopMarket) {
            if (triggerPrice != null && triggerPrice <= 0) {
                listOfNotNull(inputField)
            } else if (limitPrice != null && limitPrice <= 0) {
                listOf(TriggerOrdersInputField.stopLossLimitPrice.rawValue)
            } else {
                null
            }
        } else if (type == OrderType.TakeProfitLimit || type == OrderType.TakeProfitMarket) {
            if (triggerPrice != null && triggerPrice <= 0) {
                listOfNotNull(inputField)
            } else if (limitPrice != null && limitPrice <= 0) {
                listOf(TriggerOrdersInputField.takeProfitLimitPrice.rawValue)
            } else {
                null
            }
        } else {
            null
        }

        if (triggerPrice != null && triggerPrice <= 0 || (limitPrice != null && limitPrice <= 0)) {
            return listOf(
                error(
                    type = ErrorType.error,
                    errorCode = "PRICE_MUST_POSITIVE",
                    fields = fields,
                    actionStringKey = "APP.TRADE.MODIFY_PRICE",
                    titleStringKey = "ERRORS.TRIGGERS_FORM_TITLE.PRICE_MUST_POSITIVE",
                    textStringKey = "ERRORS.TRIGGERS_FORM.PRICE_MUST_POSITIVE",
                ),
            )
        }

        return null
    }

    private fun validateCalculatedPricesPositiveDeprecated(
        triggerOrder: Map<String, Any>,
    ): List<Any>? {
        val type = parser.asString(triggerOrder["type"])?.let {
            OrderType.invoke(it)
        }
        val triggerPrice = parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
        val limitPrice = parser.asDouble(parser.value(triggerOrder, "price.limitPrice"))
        val inputField = parser.asString(parser.value(triggerOrder, "price.input"))
        val fields = if (type == OrderType.StopLimit || type == OrderType.StopMarket) {
            if (triggerPrice != null && triggerPrice <= 0) {
                listOfNotNull(inputField)
            } else if (limitPrice != null && limitPrice <= 0) {
                listOf(TriggerOrdersInputField.stopLossLimitPrice.rawValue)
            } else {
                null
            }
        } else if (type == OrderType.TakeProfitLimit || type == OrderType.TakeProfitMarket) {
            if (triggerPrice != null && triggerPrice <= 0) {
                listOfNotNull(inputField)
            } else if (limitPrice != null && limitPrice <= 0) {
                listOf(TriggerOrdersInputField.takeProfitLimitPrice.rawValue)
            } else {
                null
            }
        } else {
            null
        }

        if (triggerPrice != null && triggerPrice <= 0 || (limitPrice != null && limitPrice <= 0)) {
            return listOf(
                errorDeprecated(
                    type = "ERROR",
                    errorCode = "PRICE_MUST_POSITIVE",
                    fields = fields,
                    actionStringKey = "APP.TRADE.MODIFY_PRICE",
                    titleStringKey = "ERRORS.TRIGGERS_FORM_TITLE.PRICE_MUST_POSITIVE",
                    textStringKey = "ERRORS.TRIGGERS_FORM.PRICE_MUST_POSITIVE",
                ),
            )
        }
        return null
    }

    private fun validateTriggerPrice(
        triggerOrder: InternalTriggerOrderState,
        oraclePrice: Double,
        tickSize: String,
    ): List<ValidationError>? {
        val type = triggerOrder.type ?: return null
        val side = triggerOrder.side ?: return null
        val triggerPrice = triggerOrder.price?.triggerPrice ?: return null
        val inputField = triggerOrder.price?.input

        when (val triggerToIndex = requiredTriggerToIndexPrice(type, side)) {
            RelativeToPrice.ABOVE -> {
                if (triggerPrice <= oraclePrice) {
                    return listOf(
                        triggerToIndexError(
                            triggerToIndex = triggerToIndex,
                            oraclePrice = oraclePrice,
                            tickSize = tickSize,
                            type = type,
                            inputField = inputField,
                        ),
                    )
                }
            }

            RelativeToPrice.BELOW -> {
                if (triggerPrice >= oraclePrice) {
                    return listOf(
                        triggerToIndexError(
                            triggerToIndex = triggerToIndex,
                            oraclePrice = oraclePrice,
                            tickSize = tickSize,
                            type = type,
                            inputField = inputField,
                        ),
                    )
                }
            }

            else -> {}
        }

        return null
    }

    private fun validateTriggerPriceDeprecated(
        triggerOrder: Map<String, Any>,
        oraclePrice: Double,
        tickSize: String,
    ): List<Any>? {
        val type = parser.asString(triggerOrder["type"])?.let {
            OrderType.invoke(it)
        }
        val side = parser.asString(triggerOrder["side"])?.let {
            OrderSide.invoke(it)
        }

        if (type == null || side == null) {
            return null
        }

        val triggerPrice =
            parser.asDouble(parser.value(triggerOrder, "price.triggerPrice")) ?: return null
        val inputField = parser.asString(parser.value(triggerOrder, "price.input"))

        when (val triggerToIndex = requiredTriggerToIndexPrice(type, side)) {
            RelativeToPrice.ABOVE -> {
                if (triggerPrice <= oraclePrice) {
                    return listOf(
                        triggerToIndexErrorDeprecated(
                            triggerToIndex,
                            oraclePrice,
                            tickSize,
                            type,
                            inputField,
                        ),
                    )
                }
            }

            RelativeToPrice.BELOW -> {
                if (triggerPrice >= oraclePrice) {
                    return listOf(
                        triggerToIndexErrorDeprecated(
                            triggerToIndex,
                            oraclePrice,
                            tickSize,
                            type,
                            inputField,
                        ),
                    )
                }
            }

            else -> {}
        }
        return null
    }

    private fun validateLimitPrice(
        triggerOrder: InternalTriggerOrderState
    ): List<ValidationError>? {
        val type = triggerOrder.type ?: return null
        val side = triggerOrder.side ?: return null

        val fields = when (type) {
            OrderType.StopLimit -> listOf(TriggerOrdersInputField.stopLossLimitPrice.rawValue)
            OrderType.TakeProfitLimit -> listOf(TriggerOrdersInputField.takeProfitLimitPrice.rawValue)
            else -> null
        }

        when (type) {
            OrderType.StopLimit, OrderType.TakeProfitLimit -> {
                val limitPrice = triggerOrder.price?.limitPrice ?: return null
                val triggerPrice = triggerOrder.price?.triggerPrice ?: return null

                if (side == OrderSide.Buy && limitPrice < triggerPrice) {
                    return limitPriceError(
                        errorCode = "LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                        fields = fields,
                        titleStringKey = if (type == OrderType.StopLimit) {
                            "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                        } else {
                            "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                        },
                        textStringKey = if (type == OrderType.StopLimit) {
                            "ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                        } else {
                            "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                        },
                    )
                } else if (side == OrderSide.Sell && limitPrice > triggerPrice) {
                    return limitPriceError(
                        errorCode = "LIMIT_MUST_BELOW_TRIGGER_PRICE",
                        fields = fields,
                        titleStringKey = if (type == OrderType.StopLimit) {
                            "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                        } else {
                            "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                        },
                        textStringKey = if (type == OrderType.StopLimit) {
                            "ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                        } else {
                            "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                        },
                    )
                }
            }

            else -> {}
        }

        return null
    }

    private fun validateLimitPriceDeprecated(
        triggerOrder: Map<String, Any>,
    ): List<Any>? {
        val type = parser.asString(triggerOrder["type"])?.let { OrderType.invoke(it) }
        val side = parser.asString(triggerOrder["side"])?.let { OrderSide.invoke(it) }

        if (type == null || side == null) {
            return null
        }

        val fields = when (type) {
            OrderType.StopLimit -> listOf(TriggerOrdersInputField.stopLossLimitPrice.rawValue)
            OrderType.TakeProfitLimit -> listOf(TriggerOrdersInputField.takeProfitLimitPrice.rawValue)
            else -> null
        }

        return when (type) {
            OrderType.StopLimit, OrderType.TakeProfitLimit -> {
                parser.asDouble(parser.value(triggerOrder, "price.limitPrice"))
                    ?.let { limitPrice ->
                        parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
                            ?.let { triggerPrice ->
                                if (side == OrderSide.Buy && limitPrice < triggerPrice) {
                                    return limitPriceErrorDeprecated(
                                        "LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                                        fields,
                                        if (type == OrderType.StopLimit) {
                                            "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                                        } else {
                                            "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                                        },
                                        if (type == OrderType.StopLimit) {
                                            "ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                                        } else {
                                            "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                                        },
                                    )
                                } else if (side == OrderSide.Sell && limitPrice > triggerPrice) {
                                    return limitPriceErrorDeprecated(
                                        "LIMIT_MUST_BELOW_TRIGGER_PRICE",
                                        fields,
                                        if (type == OrderType.StopLimit) {
                                            "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                        } else {
                                            "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                        },
                                        if (type == OrderType.StopLimit) {
                                            "ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                        } else {
                                            "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                        },
                                    )
                                } else {
                                    null
                                }
                            }
                    }
            }

            else -> null
        }
    }

    private fun limitPriceError(
        errorCode: String,
        fields: List<String>?,
        titleStringKey: String,
        textStringKey: String,
    ): List<ValidationError> {
        return listOf(
            error(
                type = ErrorType.error,
                errorCode = errorCode,
                fields = fields,
                actionStringKey = "APP.TRADE.MODIFY_TRIGGER_PRICE",
                titleStringKey = titleStringKey,
                textStringKey = textStringKey,
            ),
        )
    }

    private fun limitPriceErrorDeprecated(
        errorCode: String,
        fields: List<String>?,
        titleStringKey: String,
        textStringKey: String,
    ): List<Any>? {
        return listOf(
            errorDeprecated(
                type = "ERROR",
                errorCode = errorCode,
                fields = fields,
                actionStringKey = "APP.TRADE.MODIFY_TRIGGER_PRICE",
                titleStringKey = titleStringKey,
                textStringKey = textStringKey,
            ),
        )
    }

    private fun validateSize(
        orderSize: Double?,
        market: InternalMarketState?,
    ): List<ValidationError>? {
        val symbol = market?.perpetualMarket?.assetId ?: return null
        val orderSize = orderSize ?: return null
        val minOrderSize = market.perpetualMarket?.configs?.minOrderSize ?: return null

        if (orderSize.abs() < minOrderSize) {
            return listOf(
                error(
                    type = ErrorType.error,
                    errorCode = "ORDER_SIZE_BELOW_MIN_SIZE",
                    fields = listOf(TriggerOrdersInputField.size.rawValue),
                    actionStringKey = null,
                    titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_SIZE_BELOW_MIN_SIZE",
                    textStringKey = "ERRORS.TRADE_BOX.ORDER_SIZE_BELOW_MIN_SIZE",
                    textParams = mapOf(
                        "MIN_SIZE" to mapOf(
                            "value" to minOrderSize,
                            "format" to "size",
                        ),
                        "SYMBOL" to mapOf(
                            "value" to symbol,
                            "format" to "string",
                        ),
                    ),
                ),
            )
        }
        return null
    }

    private fun validateSizeDeprecated(
        orderSize: Double?,
        market: Map<String, Any>?,
    ): List<Any>? {
        val symbol = parser.asString(market?.get("assetId")) ?: return null

        orderSize?.let { size ->
            parser.asNativeMap(market?.get("configs"))?.let { configs ->
                val errors = mutableListOf<Map<String, Any>>()

                parser.asDouble(configs["minOrderSize"])?.let { minOrderSize ->
                    if (size.abs() < minOrderSize) {
                        errors.add(
                            errorDeprecated(
                                type = "ERROR",
                                errorCode = "ORDER_SIZE_BELOW_MIN_SIZE",
                                fields = listOf(TriggerOrdersInputField.size.rawValue),
                                actionStringKey = null,
                                titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_SIZE_BELOW_MIN_SIZE",
                                textStringKey = "ERRORS.TRADE_BOX.ORDER_SIZE_BELOW_MIN_SIZE",
                                textParams = mapOf(
                                    "MIN_SIZE" to mapOf(
                                        "value" to minOrderSize,
                                        "format" to "size",
                                    ),
                                    "SYMBOL" to mapOf(
                                        "value" to symbol,
                                        "format" to "string",
                                    ),
                                ),
                            ),
                        )
                    }
                }
                return if (errors.size > 0) errors else null
            }
        }
        return null
    }

    private fun triggerToIndexError(
        triggerToIndex: RelativeToPrice,
        oraclePrice: Double,
        tickSize: String,
        type: OrderType,
        inputField: String?,
    ): ValidationError {
        val action = "APP.TRADE.MODIFY_TRIGGER_PRICE"
        val params = mapOf(
            "INDEX_PRICE" to
                mapOf(
                    "value" to oraclePrice,
                    "format" to "price",
                    "tickSize" to tickSize,
                ),
        )
        val fields = listOfNotNull(inputField)
        val isStopLoss = type == OrderType.StopLimit || type == OrderType.StopMarket

        return when (triggerToIndex) {
            RelativeToPrice.ABOVE -> error(
                type = ErrorType.error,
                errorCode = "TRIGGER_MUST_ABOVE_INDEX_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = if (isStopLoss) {
                    "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                } else {
                    "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                },
                textStringKey = if (isStopLoss) {
                    "ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                } else {
                    "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                },
                textParams = params,
            )

            else -> error(
                type = ErrorType.error,
                errorCode = "TRIGGER_MUST_BELOW_INDEX_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = if (isStopLoss) {
                    "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE"
                } else {
                    "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_BELOW_INDEX_PRICE"
                },
                textStringKey = if (isStopLoss) {
                    "ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE"
                } else {
                    "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_BELOW_INDEX_PRICE"
                },
                textParams = params,
            )
        }
    }

    private fun triggerToIndexErrorDeprecated(
        triggerToIndex: RelativeToPrice,
        oraclePrice: Double,
        tickSize: String,
        type: OrderType,
        inputField: String?,
    ): Map<String, Any> {
        val action = "APP.TRADE.MODIFY_TRIGGER_PRICE"
        val params = mapOf(
            "INDEX_PRICE" to
                mapOf(
                    "value" to oraclePrice,
                    "format" to "price",
                    "tickSize" to tickSize,
                ),
        )
        val fields = listOfNotNull(inputField)
        val isStopLoss = type == OrderType.StopLimit || type == OrderType.StopMarket

        return when (triggerToIndex) {
            RelativeToPrice.ABOVE -> errorDeprecated(
                type = "ERROR",
                errorCode = "TRIGGER_MUST_ABOVE_INDEX_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = if (isStopLoss) {
                    "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                } else {
                    "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                },
                textStringKey = if (isStopLoss) {
                    "ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                } else {
                    "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                },
                textParams = params,
            )

            else -> errorDeprecated(
                type = "ERROR",
                errorCode = "TRIGGER_MUST_BELOW_INDEX_PRICE",
                fields = fields,
                actionStringKey = action,
                titleStringKey = if (isStopLoss) {
                    "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE"
                } else {
                    "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_BELOW_INDEX_PRICE"
                },
                textStringKey = if (isStopLoss) {
                    "ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE"
                } else {
                    "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_BELOW_INDEX_PRICE"
                },
                textParams = params,
            )
        }
    }
}

private fun requiredTriggerToLiquidationPrice(type: OrderType?, side: OrderSide): RelativeToPrice? {
    return when (type) {
        OrderType.StopMarket ->
            when (side) {
                OrderSide.Buy -> RelativeToPrice.BELOW
                OrderSide.Sell -> RelativeToPrice.ABOVE
            }
        else -> null
    }
}

private fun requiredTriggerToIndexPrice(type: OrderType, side: OrderSide): RelativeToPrice? {
    return when (type) {
        OrderType.StopLimit, OrderType.StopMarket ->
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
