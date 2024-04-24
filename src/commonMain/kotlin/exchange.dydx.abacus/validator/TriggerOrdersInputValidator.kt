package exchange.dydx.abacus.validator
import abs
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.Logger

internal data class EquityTier(
    val requiredTotalNetCollateralUSD: Double,
    val maxOrders: Int,
) {
    var nextLevelRequiredTotalNetCollateralUSD: Double? = null
}

enum class RelativeToPrice(val rawValue: String) {
    ABOVE("ABOVE"),
    BELOW("BELOW");

    companion object {
        operator fun invoke(rawValue: String) =
            values().firstOrNull { it.rawValue == rawValue }
    }
}

internal class TriggerOrdersInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol
) :
    BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {

    @Suppress("PropertyName")
    private val MAX_NUM_OPEN_UNTRIGGERED_ORDERS: Int = 20

    override fun validate(
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
            val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId")) ?: return null
            val tickSize = parser.asString(parser.value(market, "configs.tickSize")) ?: "0.01"
            val oraclePrice = parser.asDouble(
                parser.value(
                    market,
                    "oraclePrice",
                ),
            ) ?: return null

            validateTriggerOrders(transaction, market, subaccount, configs, environment)?.let {
                errors.addAll(it)
            }

            val takeProfitOrder = parser.asMap(transaction["takeProfitOrder"])
            val stopLossOrder = parser.asMap(transaction["stopLossOrder"])

            val takeProfitError = if (takeProfitOrder != null) {
                validateTriggerOrder(
                    takeProfitOrder,
                    market,
                    oraclePrice,
                    tickSize,
                    validateTakeProfitTriggerToLiquidationPrice(takeProfitOrder, position, tickSize)
                )
            } else {
                null
            }

            if (takeProfitError != null) {
                errors.addAll(takeProfitError)
            }

            val stopLossError = if (stopLossOrder != null) {
                validateTriggerOrder(
                    stopLossOrder,
                    market,
                    oraclePrice,
                    tickSize,
                    validateStopLossTriggerToLiquidationPrice(stopLossOrder, position, tickSize)
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
        triggerOrders: Map<String, Any>,
        market: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        configs: Map<String, Any>?,
        environment: V4Environment?,
    ): MutableList<Any>? {
        val triggerErrors = mutableListOf<Any>()
        validateOrderCount(triggerOrders, subaccount, configs, environment)?.let {
            /*
                USER_MAX_ORDERS
             */
            triggerErrors.addAll(it)
        }
        validateSize(parser.asDouble(triggerOrders["size"]), market)?.let {
            /*
                AMOUNT_INPUT_STEP_SIZE
                ORDER_SIZE_BELOW_MIN_SIZE
             */
            triggerErrors.addAll(it)
        }
        return if (triggerErrors.size > 0) triggerErrors else null
    }

    private fun validateStopLossTriggerToLiquidationPrice(
        triggerOrder: Map<String, Any>,
        position: Map<String, Any>,
        tickSize: String,
    ): List<Any>? {
        val liquidationPrice = parser.asDouble(parser.value(position, "size.liquidationPrice")) ?: return null
        val triggerPrice =
            parser.asDouble(parser.value(triggerOrder, "price.triggerPrice")) ?: return null
        
        val side = parser.value(triggerOrder, "side")

        Logger.e { "xcxc stop loss, ${liquidationPrice}, ${triggerPrice}, ${side}"}
        return when (parser.value(triggerOrder, "side")) {
            "SELL" -> {
                if (triggerPrice > liquidationPrice) {
                    listOf(error(
                        "ERROR",
                        "BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        listOf(TriggerOrdersInputField.stopLossPrice.rawValue),
                        "APP.TRADE.MODIFY_TRIGGER_PRICE",
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE",
                        mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    ))
                } else {
                    null
                }
            }
            "BUY" -> {
                if (triggerPrice < liquidationPrice) {
                    listOf(error(
                        "ERROR",
                        "BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        listOf(TriggerOrdersInputField.stopLossPrice.rawValue),
                        "APP.TRADE.MODIFY_TRIGGER_PRICE",
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE",
                        mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    ))
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun validateTakeProfitTriggerToLiquidationPrice(
        triggerOrder: Map<String, Any>,
        position: Map<String, Any>,
        tickSize: String,
    ): List<Any>? {
        
        val liquidationPrice = parser.asDouble(parser.value(position, "size.liquidationPrice")) ?: return null
        val triggerPrice =
            parser.asDouble(parser.value(triggerOrder, "price.triggerPrice")) ?: return null

        val side = parser.value(triggerOrder, "side")
        Logger.e { "xcxc take profit, ${liquidationPrice}, ${triggerPrice}, ${side}"}
        return when (parser.value(triggerOrder, "side")) {
            "SELL" -> {
                if (triggerPrice < liquidationPrice) {
                    listOf(error(
                        "ERROR",
                        "BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        listOf(TriggerOrdersInputField.stopLossPrice.rawValue),
                        "APP.TRADE.MODIFY_TRIGGER_PRICE",
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE",
                        mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    ))
                } else {
                    null
                }
            }
            "BUY" -> {
                if (triggerPrice > liquidationPrice) {
                    listOf(error(
                        "ERROR",
                        "BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        listOf(TriggerOrdersInputField.stopLossPrice.rawValue),
                        "APP.TRADE.MODIFY_TRIGGER_PRICE",
                        "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE",
                        mapOf(
                            "TRIGGER_PRICE_LIMIT" to mapOf(
                                "value" to liquidationPrice,
                                "format" to "price",
                                "tickSize" to tickSize,
                            ),
                        ),
                    ))
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun validateTriggerOrder(
        triggerOrder: Map<String, Any>,
        market: Map<String, Any>?,
        oraclePrice: Double,
        tickSize: String,
        liquidationPriceErrors: List<Any>?,
    ): MutableList<Any>? {
        val triggerErrors = mutableListOf<Any>()

        Logger.e { "xcxc validating trigger order "}

        validateRequiredInput(triggerOrder)?.let {
            /*
                REQUIRED_TRIGGER_PRICE
             */
            triggerErrors.addAll(it)
        }
        validateSize(parser.asDouble(parser.value(triggerOrder, "summary.size")), market)?.let {
            /*
                AMOUNT_INPUT_STEP_SIZE
                ORDER_SIZE_BELOW_MIN_SIZE
             */
            triggerErrors.addAll(it)
        }
        validateTriggerPrice(triggerOrder, oraclePrice, tickSize)?.let {
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
        
        liquidationPriceErrors?.let {
            /*
                xcxc
             */
            Logger.e { "xcxc, ${it} "}
            triggerErrors.addAll(it)
        }

        return if (triggerErrors.size > 0) triggerErrors else null
    }

    private fun validateOrderCount(
        triggerOrders: Map<String, Any>,
        subaccount: Map<String, Any>?,
        configs: Map<String, Any>?,
        environment: V4Environment?,
    ): List<Any>? {
        val equityTier = equityTier(subaccount, configs)

        val fallbackMaxNumOrders = MAX_NUM_OPEN_UNTRIGGERED_ORDERS

        val equityTierLimit = equityTier?.maxOrders ?: fallbackMaxNumOrders
        val nextLevelRequiredTotalNetCollateralUSD =
            equityTier?.nextLevelRequiredTotalNetCollateralUSD
        val numOrders = orderCount(subaccount)
        var numOrdersToCreate = 0
        var numOrdersToCancel = 0

        if (parser.value(triggerOrders, "stopLossOrder.price.triggerPrice") != null && parser.value(triggerOrders, "stopLossOrder.orderId") == null) {
            numOrdersToCreate += 1
        } else if (parser.value(triggerOrders, "stopLossOrder.price.triggerPrice") == null && parser.value(triggerOrders, "stopLossOrder.orderId") != null) {
            numOrdersToCancel += 1
        }
        if (parser.value(triggerOrders, "takeProfitOrder.price.triggerPrice") != null && parser.value(triggerOrders, "takeProfitOrder.orderId") == null) {
            numOrdersToCreate += 1
        } else if (parser.value(triggerOrders, "takeProfitOrder.price.triggerPrice") == null && parser.value(triggerOrders, "takeProfitOrder.orderId") != null) {
            numOrdersToCancel += 1
        }

        val documentation = environment?.links?.documentation
        val link = if (documentation != null) "$documentation/trading/other_limits" else null

        return if ((numOrders + numOrdersToCreate - numOrdersToCancel) > equityTierLimit) {
            listOf(
                if (nextLevelRequiredTotalNetCollateralUSD != null) {
                    error(
                        "ERROR",
                        "USER_MAX_ORDERS",
                        null, // No input field since the error is not related to a specific field
                        null,
                        "ERRORS.TRADE_BOX_TITLE.USER_MAX_ORDERS",
                        "ERRORS.TRADE_BOX.USER_MAX_ORDERS_FOR_CURRENT_EQUITY_TIER",
                        mapOf(
                            "EQUITY" to mapOf(
                                "value" to nextLevelRequiredTotalNetCollateralUSD,
                                "format" to "price",
                            ),
                            "LIMIT" to mapOf(
                                "value" to equityTierLimit,
                                "format" to "string",
                            ),
                        ),
                        null,
                        link,
                    )
                } else {
                    error(
                        "ERROR",
                        "USER_MAX_ORDERS",
                        null, // No input field since the error is not related to a specific field
                        null,
                        "ERRORS.TRADE_BOX_TITLE.USER_MAX_ORDERS",
                        "ERRORS.TRADE_BOX.USER_MAX_ORDERS_FOR_TOP_EQUITY_TIER",
                        mapOf(
                            "LIMIT" to mapOf(
                                "value" to equityTierLimit,
                                "format" to "string",
                            ),
                        ),
                        null,
                        link,
                    )
                },
            )
        } else {
            null
        }
    }

    private fun equityTier(
        subaccount: Map<String, Any>?,
        configs: Map<String, Any>?
    ): EquityTier? {
        var equityTier: EquityTier? = null
        val equity: Double = parser.asDouble(parser.value(subaccount, "equity.current")) ?: 0.0
        parser.asNativeMap(parser.value(configs, "equityTiers"))?.let { equityTiers ->
            parser.asNativeList(equityTiers["statefulOrderEquityTiers"])?.let { tiers ->
                if (tiers.isEmpty()) return null
                for (tier in tiers) {
                    parser.asNativeMap(tier)?.let { item ->
                        val requiredTotalNetCollateralUSD =
                            parser.asDouble(item["requiredTotalNetCollateralUSD"]) ?: 0.0
                        if (requiredTotalNetCollateralUSD <= equity) {
                            val maxNumOrders = parser.asInt(item["maxOrders"]) ?: 0
                            equityTier = EquityTier(
                                requiredTotalNetCollateralUSD,
                                maxNumOrders,
                            )
                        } else if (equityTier?.nextLevelRequiredTotalNetCollateralUSD == null) {
                            equityTier?.nextLevelRequiredTotalNetCollateralUSD =
                                requiredTotalNetCollateralUSD
                        }
                    }
                }
            }
        } ?: run {
            return null
        }
        return equityTier
    }

    private fun orderCount(
        subaccount: Map<String, Any>?,
    ): Int {
        var count = 0
        parser.asNativeMap(subaccount?.get("orders"))?.let { orders ->
            for ((_, item) in orders) {
                parser.asNativeMap(item)?.let { order ->
                    val status = parser.asString(order["status"])
                    val orderType = parser.asString(order["type"])
                    val timeInForce = parser.asString(order["timeInForce"])
                    if (orderType != null && timeInForce != null) {
                        val isCurrentOrderStateful = isStatefulOrder(orderType, timeInForce)
                        // Short term with IOC or FOK should not be counted
                        val isShortTermAndRequiresImmediateExecution =
                            !isCurrentOrderStateful && (timeInForce == "IOC" || timeInForce == "FOK")
                        if (!isShortTermAndRequiresImmediateExecution &&
                            (status == "OPEN" || status == "PENDING" || status == "UNTRIGGERED" || status == "PARTIALLY_FILLED")
                        ) {
                            count += 1
                        }
                    }
                }
            }
        }

        return count
    }

    private fun isStatefulOrder(orderType: String, timeInForce: String): Boolean {
        return when (orderType) {
            "MARKET" -> false

            "LIMIT" -> {
                when (parser.asString(timeInForce)) {
                    "GTT" -> true
                    else -> false
                }
            }

            else -> true
        }
    }

    private fun validateRequiredInput(
        triggerOrder: Map<String, Any>,
    ): List<Any>? {
        val errors = mutableListOf<Map<String, Any>>()

        val triggerPrice = parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
        val limitPrice = parser.asDouble(parser.value(triggerOrder, "price.limitPrice"))

        if (triggerPrice == null && limitPrice != null) {
            errors.add(
                required("REQUIRED_TRIGGER_PRICE", "price.triggerPrice", "APP.TRADE.ENTER_TRIGGER_PRICE"),
            )
        }

        return if (errors.size > 0) errors else null
    }

    private fun validateCalculatedPricesPositive(
        triggerOrder: Map<String, Any>,
    ): List<Any>? {
        val type = parser.asString(triggerOrder["type"])
        val triggerPrice = parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
        val limitPrice = parser.asDouble(parser.value(triggerOrder, "price.limitPrice"))
        val inputField = parser.asString(parser.value(triggerOrder, "price.input"))
        val fields = if (type == "STOP_LIMIT" || type == "STOP_MARKET") {
            if (triggerPrice != null && triggerPrice <= 0) {
                listOfNotNull(inputField)
            } else if (limitPrice != null && limitPrice <= 0) {
                listOf(TriggerOrdersInputField.stopLossLimitPrice.rawValue)
            } else {
                null
            }
        } else if (type == "TAKE_PROFIT" || type == "TAKE_PROFIT_MARKET") {
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
                    "ERROR",
                    "PRICE_MUST_POSITIVE",
                    fields,
                    "APP.TRADE.MODIFY_PRICE",
                    "ERRORS.TRIGGERS_FORM_TITLE.PRICE_MUST_POSITIVE",
                    "ERRORS.TRIGGERS_FORM.PRICE_MUST_POSITIVE",
                ),
            )
        }
        return null
    }

    private fun validateTriggerPrice(
        triggerOrder: Map<String, Any>,
        oraclePrice: Double,
        tickSize: String,
    ): List<Any>? {
        val type = parser.asString(triggerOrder["type"]) ?: return null
        val side = parser.asString(triggerOrder["side"]) ?: return null

        val triggerPrice =
            parser.asDouble(parser.value(triggerOrder, "price.triggerPrice")) ?: return null
        val inputField = parser.asString(parser.value(triggerOrder, "price.input"))

        when (val triggerToIndex = requiredTriggerToIndexPrice(type, side)) {
            RelativeToPrice.ABOVE -> {
                if (triggerPrice <= oraclePrice) {
                    return listOf(
                        triggerToIndexError(
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
                        triggerToIndexError(
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
        triggerOrder: Map<String, Any>,
    ): List<Any>? {
        val type = parser.asString(triggerOrder["type"])
        val fields = if (type == "STOP_LIMIT") {
            listOf(
                TriggerOrdersInputField.stopLossLimitPrice.rawValue,
            )
        } else if (type == "TAKE_PROFIT") {
            listOf(
                TriggerOrdersInputField.takeProfitLimitPrice.rawValue,
            )
        } else {
            null
        }
        return when (type) {
            "STOP_LIMIT", "TAKE_PROFIT" -> {
                parser.asString(parser.value(triggerOrder, "side"))?.let { side ->
                    parser.asDouble(parser.value(triggerOrder, "price.limitPrice"))
                        ?.let { limitPrice ->
                            parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
                                ?.let { triggerPrice ->
                                    if (side == "BUY" && limitPrice < triggerPrice) {
                                        return listOf(
                                            error(
                                                "ERROR",
                                                "LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                                                fields,
                                                "APP.TRADE.MODIFY_TRIGGER_PRICE",
                                                if (type == "STOP_LIMIT") "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_ABOVE_TRIGGER_PRICE" else "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                                                if (type == "STOP_LIMIT") "ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_ABOVE_TRIGGER_PRICE" else "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                                            ),
                                        )
                                    } else if (side == "SELL" && limitPrice > triggerPrice) {
                                        return listOf(
                                            error(
                                                "ERROR",
                                                "LIMIT_MUST_BELOW_TRIGGER_PRICE",
                                                fields,
                                                "APP.TRADE.MODIFY_TRIGGER_PRICE",
                                                if (type == "STOP_LIMIT") "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE" else "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE",
                                                if (type == "STOP_LIMIT") "ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE" else "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE",
                                            ),
                                        )
                                    } else {
                                        null
                                    }
                                }
                        }
                }
            }
            else -> null
        }
    }

    private fun validateSize(
        orderSize: Double?,
        market: Map<String, Any>?,
    ): List<Any>? {
        val symbol = parser.asString(market?.get("assetId")) ?: return null

        orderSize?.let { size ->
            parser.asNativeMap(market?.get("configs"))?.let { configs ->
                val errors = mutableListOf<Map<String, Any>>()

                parser.asDouble(configs["stepSize"])?.let {
                        stepSize ->
                    if (Rounder.round(size, stepSize) != size) {
                        errors.add(
                            error(
                                "ERROR",
                                "AMOUNT_INPUT_STEP_SIZE",
                                listOf(TriggerOrdersInputField.size.rawValue),
                                null,
                                "ERRORS.TRADE_BOX_TITLE.AMOUNT_INPUT_STEP_SIZE",
                                "ERRORS.TRADE_BOX.AMOUNT_INPUT_STEP_SIZE",
                                mapOf(
                                    "STEP_SIZE" to mapOf(
                                        "value" to stepSize,
                                        "format" to "size",
                                    ),
                                ),
                            ),
                        )
                    }
                }
                parser.asDouble(configs["minOrderSize"])?.let { minOrderSize ->
                    if (size.abs() < minOrderSize) {
                        errors.add(
                            error(
                                "ERROR",
                                "ORDER_SIZE_BELOW_MIN_SIZE",
                                listOf(TriggerOrdersInputField.size.rawValue),
                                null,
                                "ERRORS.TRADE_BOX_TITLE.ORDER_SIZE_BELOW_MIN_SIZE",
                                "ERRORS.TRADE_BOX.ORDER_SIZE_BELOW_MIN_SIZE",
                                mapOf(
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

    private fun requiredTriggerToIndexPrice(type: String, side: String): RelativeToPrice? {
        return when (type) {
            "STOP_LIMIT", "STOP_MARKET" ->
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
        oraclePrice: Double,
        tickSize: String,
        type: String,
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

        return when (triggerToIndex) {
            RelativeToPrice.ABOVE -> error(
                "ERROR",
                "TRIGGER_MUST_ABOVE_INDEX_PRICE",
                fields,
                action,
                if (type == "STOP_LIMIT" || type == "STOP_MARKET") "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_ABOVE_INDEX_PRICE" else "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE",
                if (type == "STOP_LIMIT" || type == "STOP_MARKET") "ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_ABOVE_INDEX_PRICE" else "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE",
                params,
            )

            else -> error(
                "ERROR",
                "TRIGGER_MUST_BELOW_INDEX_PRICE",
                fields,
                action,
                if (type == "STOP_LIMIT" || type == "STOP_MARKET") "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE" else "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_BELOW_INDEX_PRICE",
                if (type == "STOP_LIMIT" || type == "STOP_MARKET") "ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE" else "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_BELOW_INDEX_PRICE",
                params,
            )
        }
    }
}
