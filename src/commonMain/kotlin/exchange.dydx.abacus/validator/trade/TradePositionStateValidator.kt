package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradePositionStateValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    override fun validateTrade(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<Any>? {
        val marketId = parser.asString(trade["marketId"])
        val position = if (marketId != null) {
            parser.asNativeMap(
                parser.value(
                    subaccount,
                    "openPositions.$marketId",
                ),
            )
        } else {
            null
        }

        val errors = mutableListOf<Any>()
        val closeOnlyError = validateCloseOnly(
            market,
            change,
        )
        if (position != null) {
            val leverageError = validatePositionLeverage(
                position,
                trade,
                change,
                restricted,
            )
            if (leverageError != null) {
                errors.add(leverageError)
            }
            val positionSizeError = validatePositionSize(
                position,
                market,
            )
            if (positionSizeError != null) {
                errors.add(positionSizeError)
            }
            val positionFlipError = validatePositionFlip(
                change,
                trade,
            )
            if (positionFlipError != null) {
                errors.add(positionFlipError)
            }
        }
        return if (errors.size > 0) errors else null
    }

    private fun validateCloseOnly(
        market: Map<String, Any>?,
        change: PositionChange,
    ): Map<String, Any>? {
        /*
        MARKET_STATUS_CLOSE_ONLY
         */
        val status = parser.asNativeMap(market?.get("status"))
        val marketId = parser.asNativeMap(market?.get("assetId")) ?: ""
        val canTrade = parser.asBool(status?.get("canTrade")) ?: false
        val canReduce = parser.asBool(status?.get("canReduce")) ?: false
        return if (!canTrade && canReduce) {
            val isError = when (change) {
                PositionChange.CROSSING, PositionChange.NEW, PositionChange.INCREASING -> true
                else -> false
            }
            error(
                if (isError) "ERROR" else "WARNING",
                "MARKET_STATUS_CLOSE_ONLY",
                if (isError) listOf("size.size") else null,
                if (isError) "APP.TRADE.MODIFY_SIZE_FIELD" else null,
                "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                mapOf(
                    "MARKET" to mapOf(
                        "value" to marketId,
                        "format" to "string",
                    ),
                ),
            )
        } else {
            null
        }
    }

    private fun validatePositionLeverage(
        position: Map<String, Any>,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): Map<String, Any>? {
        /*
        MARKET_ORDER_CLOSE_TO_MAX_LEVERAGE

        INVALID_NEW_POSITION_LEVERAGE
        INVALID_LARGE_POSITION_LEVERAGE
         */
        return if (overMaxLeverage(
                position,
            )
        ) {
            if (parser.asString(trade["type"]) == "MARKET") {
                // no op. we removed this validation codepath in favor of relying on BE validation
                null
            } else {
                if (change == PositionChange.NEW) {
                    error(
                        "ERROR",
                        "INVALID_NEW_POSITION_LEVERAGE",
                        listOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "ERRORS.TRADE_BOX_TITLE.INVALID_NEW_POSITION_LEVERAGE",
                        "ERRORS.TRADE_BOX.INVALID_NEW_POSITION_LEVERAGE",
                    )
                } else {
                    error(
                        "ERROR",
                        "INVALID_LARGE_POSITION_LEVERAGE",
                        listOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "ERRORS.TRADE_BOX_TITLE.INVALID_LARGE_POSITION_LEVERAGE",
                        "ERRORS.TRADE_BOX.INVALID_LARGE_POSITION_LEVERAGE",
                    )
                }
            }
        } else {
            if (parser.asString(trade["type"]) == "MARKET") {
                val increasingPosition = when (change) {
                    PositionChange.NEW, PositionChange.CROSSING, PositionChange.INCREASING -> true
                    else -> false
                }

                if (increasingPosition && orderOverMaxLeverage(position)) {
                    error(
                        "WARNING",
                        "MARKET_ORDER_CLOSE_TO_MAX_LEVERAGE",
                        listOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "WARNINGS.TRADE_BOX_TITLE.MARKET_ORDER_CLOSE_TO_MAX_LEVERAGE",
                        "WARNINGS.TRADE_BOX.MARKET_ORDER_CLOSE_TO_MAX_LEVERAGE",
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun overMaxLeverage(
        position: Map<String, Any>,
    ): Boolean {
        val leverage = parser.asDouble(parser.value(position, "leverage.postOrder"))?.abs()
        val adjustedImf = parser.asDouble(parser.value(position, "adjustedImf.current"))
        return overLeverage(leverage, adjustedImf)
    }

    private fun overLeverage(leverage: Double?, adjustedImf: Double?): Boolean {
        return if (leverage != null && adjustedImf != null && adjustedImf > Numeric.double.ZERO) {
            leverage > (Numeric.double.ONE / adjustedImf) || leverage < Numeric.double.ZERO
        } else {
            false
        }
    }

    private fun orderOverMaxLeverage(
        position: Map<String, Any>,
    ): Boolean {
        val leverage = parser.asDouble(parser.value(position, "leverage.postOrder"))?.abs()
        val adjustedImf = parser.asDouble(parser.value(position, "adjustedImf.postOrder"))
        return overLeverage(leverage, adjustedImf)
    }

    private fun validatePositionSize(
        position: Map<String, Any>?,
        market: Map<String, Any>?,
    ): Map<String, Any>? {
        /*
        NEW_POSITION_SIZE_OVER_MAX
         */
        val size = parser.asDouble(parser.value(position, "size.postOrder")) ?: return null
        val maxSize =
            parser.asDouble(parser.value(market, "configs.maxPositionSize")) ?: Numeric.double.ZERO
        if (maxSize == Numeric.double.ZERO) {
            return null
        }
        val symbol = parser.asString(market?.get("assetId")) ?: return null
        return if (size > maxSize) {
            error(
                "ERROR",
                "NEW_POSITION_SIZE_OVER_MAX",
                listOf("size.size"),
                "APP.TRADE.MODIFY_SIZE_FIELD",
                "ERRORS.TRADE_BOX_TITLE.NEW_POSITION_SIZE_OVER_MAX",
                "ERRORS.TRADE_BOX.NEW_POSITION_SIZE_OVER_MAX",
                mapOf(
                    "MAX_SIZE" to mapOf(
                        "value" to maxSize,
                        "format" to "size",
                    ),
                    "SYMBOL" to mapOf(
                        "value" to symbol,
                        "format" to "string",
                    ),
                ),
            )
        } else {
            null
        }
    }

    private fun validatePositionFlip(
        change: PositionChange,
        trade: Map<String, Any>,
    ): Map<String, Any>? {
        /*
        ORDER_WOULD_FLIP_POSITION
         */
        val needsReduceOnly = parser.asBool(parser.value(trade, "options.needsReduceOnly")) ?: false
        return if (needsReduceOnly && parser.asBool(trade["reduceOnly"]) == true) {
            when (change) {
                PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING -> error(
                    "ERROR",
                    "ORDER_WOULD_FLIP_POSITION",
                    listOf("size.size"),
                    "APP.TRADE.MODIFY_SIZE_FIELD",
                    "ERRORS.TRADE_BOX_TITLE.ORDER_WOULD_FLIP_POSITION",
                    "ERRORS.TRADE_BOX.ORDER_WOULD_FLIP_POSITION",
                )

                else -> null
            }
        } else {
            null
        }
    }
}
