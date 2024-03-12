package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.GoodTil
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol
import kotlin.time.Duration.Companion.days

/*
Covers basic check of required fields
Covers checking
LIMIT_MUST_ABOVE_TRIGGER_PRICE
LIMIT_MUST_BELOW_TRIGGER_PRICE
AMOUNT_INPUT_STEP_SIZE
USER_MAX_ORDERS
ORDER_SIZE_BELOW_MIN_SIZE

TODO

LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_HIGHER
LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_LOWER

 */

internal data class EquityTier(
    val requiredTotalNetCollateralUSD: Double,
    val maxOrders: Int,
) {
    var nextLevelRequiredTotalNetCollateralUSD: Double? = null
}

internal class TradeInputDataValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {

    @Suppress("PropertyName")
    private val MAX_NUM_OPEN_UNTRIGGERED_ORDERS: Int = 20

    override fun validateTrade(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<Any>? {
        return validateTradeInput(subaccount, market, configs, trade, environment)
    }

    private fun validateTradeInput(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        environment: V4Environment?,
    ): List<Any>? {
        val errors = mutableListOf<Any>()
        validateOrder(trade, subaccount, configs, environment)?.let {
            /*
            USER_MAX_ORDERS
             */
            errors.addAll(it)
        }
        validateSize(trade, market)?.let {
            /*
            AMOUNT_INPUT_STEP_SIZE
            ORDER_SIZE_BELOW_MIN_SIZE
             */
            errors.addAll(it)
        }
        validateLimitPrice(trade, market)?.let {
            /*
            LIMIT_MUST_ABOVE_TRIGGER_PRICE
            LIMIT_MUST_BELOW_TRIGGER_PRICE
            LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_HIGHER
            LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_LOWER
             */
            errors.addAll(it)
        }

        validateTimeInForce(trade, market)?.let {
            /*
            LIMIT_MUST_ABOVE_TRIGGER_PRICE
            LIMIT_MUST_BELOW_TRIGGER_PRICE
            LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_HIGHER
            LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_LOWER
             */
            errors.addAll(it)
        }

        return if (errors.size > 0) errors else null
    }

    private fun validateOrder(
        trade: Map<String, Any>,
        subaccount: Map<String, Any>?,
        configs: Map<String, Any>?,
        environment: V4Environment?,
    ): List<Any>? {
        /*
        USER_MAX_ORDERS
         */
        val fallbackMaxNumOrders = MAX_NUM_OPEN_UNTRIGGERED_ORDERS
        val orderType = parser.asString(trade["type"])
        val timeInForce = parser.asString(trade["timeInForce"])

        if (orderType == null || timeInForce == null) return null

        val equityTier =
            equityTier(isStatefulOrder(orderType, timeInForce), subaccount, configs)
        val equityTierLimit = equityTier?.maxOrders ?: fallbackMaxNumOrders
        val nextLevelRequiredTotalNetCollateralUSD =
            equityTier?.nextLevelRequiredTotalNetCollateralUSD
        val numOrders = orderCount(isStatefulOrder(orderType, timeInForce), subaccount)

        // Equity tier limit is not applicable for `MARKET` orders and `LIMIT` orders with FOK or IOC time in force
        val isEquityTierLimitApplicable = orderType != "MARKET" &&
            !(orderType == "LIMIT" && (timeInForce == "FOK" || timeInForce == "IOC"))

        val documentation = environment?.links?.documentation
        val link = if (documentation != null) "$documentation/trading/other_limits" else null
        return if (numOrders >= equityTierLimit && isEquityTierLimitApplicable) {
            listOf(
                if (nextLevelRequiredTotalNetCollateralUSD != null) {
                    error(
                        "ERROR",
                        "USER_MAX_ORDERS",
                        null,
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
                        null,
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
        isStatefulOrder: Boolean,
        subaccount: Map<String, Any>?,
        configs: Map<String, Any>?
    ): EquityTier? {
        /*
         USER_MAX_ORDERS according to Equity Tier
         */
        var equityTier: EquityTier? = null
        val equity: Double = parser.asDouble(parser.value(subaccount, "equity.current")) ?: 0.0
        val equityTierKey: String =
            if (isStatefulOrder) "statefulOrderEquityTiers" else "shortTermOrderEquityTiers"
        parser.asNativeMap(parser.value(configs, "equityTiers"))?.let { equityTiers ->
            parser.asNativeList(equityTiers[equityTierKey])?.let { tiers ->
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
        shouldCountStatefulOrders: Boolean,
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
                            (status == "OPEN" || status == "PENDING" || status == "UNTRIGGERED" || status == "PARTIALLY_FILLED") &&
                            (isCurrentOrderStateful == shouldCountStatefulOrders)
                        ) {
                            count += 1
                        }
                    }
                }
            }
        }

        return count
    }

    private fun validateSize(
        trade: Map<String, Any>,
        market: Map<String, Any>?,
    ): List<Any>? {
        /*
        AMOUNT_INPUT_STEP_SIZE
        ORDER_SIZE_BELOW_MIN_SIZE
         */
        val symbol = parser.asString(market?.get("assetId")) ?: return null
        parser.asDouble(parser.value(trade, "size.size"))?.let { size ->
            parser.asNativeMap(market?.get("configs"))?.let { configs ->
                val errors = mutableListOf<Map<String, Any>>()
                parser.asDouble(configs["stepSize"])?.let { stepSize ->
                    if (Rounder.round(size, stepSize) != size) {
                        errors.add(
                            error(
                                "ERROR",
                                "AMOUNT_INPUT_STEP_SIZE",
                                null,
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
                                null,
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

    private fun validateLimitPrice(
        trade: Map<String, Any>,
        market: Map<String, Any>?,
    ): List<Any>? {
        /*
        LIMIT_MUST_ABOVE_TRIGGER_PRICE
        LIMIT_MUST_BELOW_TRIGGER_PRICE
         */
        return when (parser.asString(trade["type"])) {
            "STOP_LIMIT", "TAKE_PROFIT" -> {
                val execution = parser.asString(trade["execution"])
                if (execution == "IOC" || execution == "FOK") {
                    parser.asString(parser.value(trade, "side"))?.let { side ->
                        parser.asDouble(parser.value(trade, "price.limitPrice"))
                            ?.let { limitPrice ->
                                parser.asDouble(parser.value(trade, "price.triggerPrice"))
                                    ?.let { triggerPrice ->
                                        if (side == "BUY" && limitPrice < triggerPrice) {
                                            // BUY
                                            return listOf(
                                                error(
                                                    "ERROR",
                                                    "LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                                                    listOf("price.triggerPrice"),
                                                    "APP.TRADE.MODIFY_TRIGGER_PRICE",
                                                    "ERRORS.TRADE_BOX_TITLE.LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                                                    "ERRORS.TRADE_BOX.LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                                                ),
                                            )
                                        } else if (side == "SELL" && limitPrice > triggerPrice) {
                                            // SELL
                                            return listOf(
                                                error(
                                                    "ERROR",
                                                    "LIMIT_MUST_BELOW_TRIGGER_PRICE",
                                                    listOf("price.triggerPrice"),
                                                    "APP.TRADE.MODIFY_TRIGGER_PRICE",
                                                    "ERRORS.TRADE_BOX_TITLE.LIMIT_MUST_BELOW_TRIGGER_PRICE",
                                                    "ERRORS.TRADE_BOX.LIMIT_MUST_BELOW_TRIGGER_PRICE",
                                                ),
                                            )
                                        } else {
                                            null
                                        }
                                    }
                            }
                    }
                }
                null
            }

            else -> null
        }
    }

    private fun validateTimeInForce(
        trade: Map<String, Any>,
        market: Map<String, Any>?,
    ): List<Any>? {
        val fields = parser.asNativeList(trade["fields"])
        val field = fields?.firstOrNull {
            val field = parser.asNativeMap(it)?.get("field")
            field == "goodTil"
        }

        return if (fields != null && parser.asBool(
                parser.value(
                    trade,
                    "options.needsGoodUntil",
                ),
            ) == true
        ) {
            val goodTil = parser.asNativeMap(trade["goodTil"])
            // null is handled by FieldsInputValidator
            val timeInterval = GoodTil.duration(goodTil, parser)
            if (timeInterval != null && timeInterval > 90.days) {
                listOf(
                    error(
                        "ERROR",
                        "INVALID_GOOD_TIL",
                        listOf("goodTil"),
                        "APP.TRADE.MODIFY_GOOD_TIL",
                        "ERRORS.TRADE_BOX_TITLE.INVALID_GOOD_TIL",
                        "ERRORS.TRADE_BOX.INVALID_GOOD_TIL_MAX_90_DAYS",
                    ),
                )
            } else {
                null
            }
        } else {
            null
        }
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
}
