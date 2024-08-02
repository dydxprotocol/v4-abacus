package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

enum class RelativeToPrice(val rawValue: String) {
    ABOVE("ABOVE"),
    BELOW("BELOW");

    companion object {
        operator fun invoke(rawValue: String) =
            values().firstOrNull { it.rawValue == rawValue }
    }
}

internal class TradeTriggerPriceValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    /*
    They are still used to calculate payload, but no longer used for validation
    private val stopMarketSlippageBufferBTC = 0.05; // 5% for Stop Market
    private val takeProfitMarketSlippageBufferBTC = 0.1; // 10% for Take Profit Market
    private val stopMarketSlippageBuffer = 0.1; // 10% for Stop Market
    private val takeProfitMarketSlippageBuffer = 0.2; // 20% for Take Profit Market
     */

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
            when (val triggerToIndex = requiredTriggerToIndexPrice(type, side)) {
                /*
                TRIGGER_MUST_ABOVE_INDEX_PRICE
                TRIGGER_MUST_BELOW_INDEX_PRICE
                 */
                RelativeToPrice.ABOVE -> {
                    if (triggerPrice <= oraclePrice) {
                        errors.add(
                            triggerToIndexError(
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
                            triggerToIndexError(
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
            val triggerToLiquidation = requiredTriggerToLiquidationPrice(type, side, change)
            if (triggerToLiquidation != null) {
                /*
                SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
                BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE
                 */
                val liquidationPrice = liquidationPrice(subaccount, trade)
                if (liquidationPrice != null) {
                    when (triggerToLiquidation) {
                        RelativeToPrice.ABOVE -> {
                            if (triggerPrice <= liquidationPrice) {
                                errors.add(
                                    triggerToLiquidationError(
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
                                    triggerToLiquidationError(
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

    private fun requiredTriggerToIndexPrice(type: String, side: String): RelativeToPrice? {
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
            RelativeToPrice.ABOVE -> error(
                "ERROR",
                "TRIGGER_MUST_ABOVE_INDEX_PRICE",
                fields,
                action,
                "ERRORS.TRADE_BOX_TITLE.TRIGGER_MUST_ABOVE_INDEX_PRICE",
                "ERRORS.TRADE_BOX.TRIGGER_MUST_ABOVE_INDEX_PRICE",
                params,
            )

            else -> error(
                "ERROR",
                "TRIGGER_MUST_BELOW_INDEX_PRICE",
                fields,
                action,
                "ERRORS.TRADE_BOX_TITLE.TRIGGER_MUST_BELOW_INDEX_PRICE",
                "ERRORS.TRADE_BOX.TRIGGER_MUST_BELOW_INDEX_PRICE",
                params,
            )
        }
    }

    private fun requiredTriggerToLiquidationPrice(
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
            RelativeToPrice.ABOVE -> error(
                "ERROR",
                "SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                fields,
                action,
                "ERRORS.TRADE_BOX_TITLE.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                "ERRORS.TRADE_BOX.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                params,
            )

            RelativeToPrice.BELOW -> error(
                "ERROR",
                "BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                fields,
                action,
                "ERRORS.TRADE_BOX_TITLE.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                "ERRORS.TRADE_BOX.BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                params,
            )
        }
    }
}
