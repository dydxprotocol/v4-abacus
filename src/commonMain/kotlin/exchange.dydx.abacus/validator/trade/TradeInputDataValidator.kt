package exchange.dydx.abacus.validator.trade

import abs
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.*
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol
import kotlin.time.Duration.Companion.days

/*
Covers basic check of required fields
Covers checking
TRIGGER_MUST_ABOVE_INDEX_PRICE
TRIGGER_MUST_BELOW_INDEX_PRICE
LIMIT_MUST_ABOVE_TRIGGER_PRICE
LIMIT_MUST_BELOW_TRIGGER_PRICE
AMOUNT_INPUT_STEP_SIZE
USER_MAX_ORDERS
ORDER_SIZE_BELOW_MIN_SIZE

TODO

LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_HIGHER
LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_LOWER

 */

internal class TradeInputDataValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    private val MAX_NUM_OPEN_UNTRIGGERED_ORDERS = 20

    override fun validateTrade(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): List<Any>? {
        return validateTradeInput(subaccount, market, configs, trade)
    }

    private fun validateTradeInput(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
    ): List<Any>? {
        val errors = mutableListOf<Any>()
        validateOrder(trade, subaccount, configs)?.let {
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
        validateTriggerPrice(trade, market)?.let {
            /*
            TRIGGER_MUST_ABOVE_INDEX_PRICE
            TRIGGER_MUST_BELOW_INDEX_PRICE
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
    ): List<Any>? {
        /*
        USER_MAX_ORDERS
        */
        val orderType = parser.asString(trade["type"])
        val timeInForce = parser.asString(trade["timeInForce"])

        if (orderType == null || timeInForce == null) return null

        val equityTierLimit =
            maxOrdersForEquityTier(isStatefulOrder(orderType, timeInForce), subaccount, configs)
        val numOrders = orderCount(isStatefulOrder(orderType, timeInForce), subaccount)

        return if (numOrders >= equityTierLimit) {
            listOf(
                error(
                    "ERROR",
                    "USER_MAX_ORDERS",
                    null,
                    null,
                    "ERRORS.TRADE_BOX_TITLE.USER_MAX_ORDERS",
                    "ERRORS.TRADE_BOX.USER_MAX_ORDERS_FOR_EQUITY_TIER",
                    mapOf(
                        "LIMIT" to mapOf(
                            "value" to equityTierLimit,
                            "format" to "string"
                        )
                    )
                )
            )
        } else null
    }

    private fun maxOrdersForEquityTier(
        isStatefulOrder: Boolean,
        subaccount: Map<String, Any>?,
        configs: Map<String, Any>?
    ): Int {
        /*
         USER_MAX_ORDERS according to Equity Tier
         */
        val fallbackMaxNumOrders = MAX_NUM_OPEN_UNTRIGGERED_ORDERS
        var maxNumOrders: Int = 0
        val equity: Double = parser.asDouble(parser.value(subaccount, "equity.current")) ?: 0.0
        val equityTierKey: String =
            if (isStatefulOrder) "statefulOrderEquityTiers" else "shortTermOrderEquityTiers"
        parser.asNativeMap(parser.value(configs, "equityTiers"))?.let { equityTiers ->
            parser.asNativeList(equityTiers[equityTierKey])?.let { tiers ->
                if (tiers.size == 0) return fallbackMaxNumOrders
                for (tier in tiers) {
                    parser.asNativeMap(tier)?.let { item ->
                        val requiredTotalNetCollateralUSD =
                            parser.asDouble(item["requiredTotalNetCollateralUSD"]) ?: 0.0
                        if (requiredTotalNetCollateralUSD <= equity) {
                            maxNumOrders = parser.asInt(item["maxOrders"]) ?: 0
                        }
                    }
                }
            }
        } ?: run {
            return fallbackMaxNumOrders
        }

        return maxNumOrders
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
                        if (!isShortTermAndRequiresImmediateExecution && (status == "OPEN" || status == "PENDING" || status == "UNTRIGGERED") && (isCurrentOrderStateful == shouldCountStatefulOrders)) {
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
                                        "format" to "size"
                                    )
                                )
                            )
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
                                        "format" to "size"
                                    ),
                                    "SYMBOL" to mapOf(
                                        "value" to symbol,
                                        "format" to "string"
                                    )
                                )
                            )
                        )
                    }
                }
                return if (errors.size > 0) errors else null
            }

        }
        return null
    }

    private fun validateTriggerPrice(
        trade: Map<String, Any>,
        market: Map<String, Any>?,
    ): List<Any>? {
        /*
        TRIGGER_MUST_ABOVE_INDEX_PRICE
        TRIGGER_MUST_BELOW_INDEX_PRICE
        */
        val tickSize = parser.asString(parser.value(market, "configs.tickSize")) ?: "0.01"
        parser.asDouble(parser.value(trade, "size.size"))?.let { size ->
            val signedSize = if (parser.asString(trade["side"]) == "BUY") size else -size
            parser.asDouble(parser.value(trade, "price.triggerPrice"))?.let { triggerPrice ->
                val oraclePrice = parser.asDouble(
                    parser.value(
                        market,
                        "oraclePrice"
                    )
                )
                if (oraclePrice != null) {
                    val error = when (parser.asString(trade["type"])) {
                        "STOP_MARKET", "STOP_LIMIT" -> {
                            if (signedSize > Numeric.double.ZERO && triggerPrice < oraclePrice) triggerPriceError(
                                true,
                                oraclePrice,
                                tickSize
                            ) else if (signedSize < Numeric.double.ZERO && triggerPrice > oraclePrice) triggerPriceError(
                                false,
                                oraclePrice,
                                tickSize
                            )
                            else null
                        }

                        "TAKE_PROFIT_MARKET", "TAKE_PROFIT" -> {
                            if (signedSize > Numeric.double.ZERO && triggerPrice < oraclePrice) triggerPriceError(
                                true,
                                oraclePrice,
                                tickSize
                            ) else if (signedSize < Numeric.double.ZERO && triggerPrice > oraclePrice) triggerPriceError(
                                false,
                                oraclePrice,
                                tickSize
                            )
                            else null
                        }

                        else -> null
                    }
                    return if (error != null) listOf(error) else null
                }
            }
        }
        return null
    }

    private fun triggerPriceError(
        aboveIndexPrice: Boolean,
        oraclePrice: Double,
        tickSize: String,
    ): Map<String, Any> {
        return error(
            "ERROR",
            if (aboveIndexPrice) "TRIGGER_MUST_ABOVE_INDEX_PRICE" else "TRIGGER_MUST_BELOW_INDEX_PRICE",
            listOf("price.triggerPrice"),
            "APP.TRADE.MODIFY_TRIGGER_PRICE",
            if (aboveIndexPrice) "ERRORS.TRADE_BOX_TITLE.TRIGGER_MUST_ABOVE_INDEX_PRICE" else "ERRORS.TRADE_BOX_TITLE.TRIGGER_MUST_BELOW_INDEX_PRICE",
            if (aboveIndexPrice) "ERRORS.TRADE_BOX.TRIGGER_MUST_ABOVE_INDEX_PRICE" else "ERRORS.TRADE_BOX.TRIGGER_MUST_BELOW_INDEX_PRICE",
            mapOf(
                "INDEX_PRICE" to
                        mapOf(
                            "value" to oraclePrice,
                            "format" to "price",
                            "tickSize" to tickSize
                        )
            )
        )
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
                                                    "ERRORS.TRADE_BOX.LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                                                )
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
                                                    "ERRORS.TRADE_BOX.LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                                )
                                            )
                                        } else null
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
                    "options.needsGoodUntil"
                )
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
                        "ERRORS.TRADE_BOX.INVALID_GOOD_TIL_MAX_90_DAYS"
                    )
                )
            } else null
        } else null
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
