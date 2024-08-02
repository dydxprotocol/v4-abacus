package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.GoodTil
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol
import kotlin.time.Duration.Companion.days

/*
Covers basic check of required fields
Covers checking
LIMIT_MUST_ABOVE_TRIGGER_PRICE
LIMIT_MUST_BELOW_TRIGGER_PRICE
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
        return validateTradeInput(market, trade)
    }

    private fun validateTradeInput(
        market: Map<String, Any>?,
        trade: Map<String, Any>,
    ): List<Any>? {
        val errors = mutableListOf<Any>()
        validateSize(trade, market)?.let {
            /*
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

    private fun validateSize(
        trade: Map<String, Any>,
        market: Map<String, Any>?,
    ): List<Any>? {
        /*
        ORDER_SIZE_BELOW_MIN_SIZE
         */
        val symbol = parser.asString(market?.get("assetId")) ?: return null
        parser.asDouble(parser.value(trade, "size.size"))?.let { size ->
            parser.asNativeMap(market?.get("configs"))?.let { configs ->
                val errors = mutableListOf<Map<String, Any>>()
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
                if (execution == "IOC") {
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
}
