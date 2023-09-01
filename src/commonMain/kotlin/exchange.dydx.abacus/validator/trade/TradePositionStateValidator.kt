package exchange.dydx.abacus.validator.trade

import abs
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

internal class TradePositionStateValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    override fun validateTrade(
        subaccount: IMap<String, Any>?,
        market: IMap<String, Any>?,
        configs: IMap<String, Any>?,
        trade: IMap<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): IList<Any>? {
        val marketId = parser.asString(trade["marketId"])
        val position = if (marketId != null) parser.asMap(
            parser.value(
                subaccount,
                "openPositions.$marketId"
            )
        ) else null

        val errors = iMutableListOf<Any>()
        val closeOnlyError = validateCloseOnly(
            market,
            change
        )
        if (position != null) {
            val leverageError = validatePositionLeverage(
                position,
                trade,
                change,
                restricted
            )
            if (leverageError != null) {
                errors.add(leverageError)
            }
            val positionSizeError = validatePositionSize(
                position,
                market
            )
            if (positionSizeError != null) {
                errors.add(positionSizeError)
            }
            val positionFlipError = validatePositionFlip(
                change,
                trade
            )
            if (positionFlipError != null) {
                errors.add(positionFlipError)
            }
        }
        return if (errors.size > 0) errors else null
    }


    private fun validateCloseOnly(
        market: IMap<String, Any>?,
        change: PositionChange,
    ): IMap<String, Any>? {
        /*
        MARKET_STATUS_CLOSE_ONLY
         */
        val status = parser.asMap(market?.get("status"))
        val marketId = parser.asMap(market?.get("assetId")) ?: ""
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
                if (isError) iListOf("size.size") else null,
                if (isError) "APP.TRADE.MODIFY_SIZE_FIELD" else null,
                "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                iMapOf(
                    "MARKET" to iMapOf(
                        "value" to marketId,
                        "format" to "string"
                    )
                )
            )
        } else null
    }

    private fun validatePositionLeverage(
        position: IMap<String, Any>,
        trade: IMap<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): IMap<String, Any>? {
        /*
        MARKET_ORDER_CLOSE_TO_MAX_LEVERAGE
        MARKET_ORDER_PRICE_IMPACT_AT_MAX_LEVERAGE

        INVALID_NEW_POSITION_LEVERAGE
        INVALID_LARGE_POSITION_LEVERAGE
         */
        return if (overMaxLeverage(
                position
            )
        ) {
            if (parser.asString(trade["type"]) == "MARKET") {
                val increasingPosition = when (change) {
                    PositionChange.NEW, PositionChange.CROSSING, PositionChange.INCREASING -> true
                    else -> false
                }

                error(
                    if (increasingPosition) "ERROR" else "WARNING",
                    "MARKET_ORDER_PRICE_IMPACT_AT_MAX_LEVERAGE",
                    if (increasingPosition) iListOf("size.size") else null,
                    if (increasingPosition) "APP.TRADE.MODIFY_SIZE_FIELD" else null,
                    "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_PRICE_IMPACT_AT_MAX_LEVERAGE",
                    "ERRORS.TRADE_BOX.MARKET_ORDER_PRICE_IMPACT_AT_MAX_LEVERAGE"
                )
            } else {
                if (change == PositionChange.NEW) error(
                    "ERROR",
                    "INVALID_NEW_POSITION_LEVERAGE",
                    iListOf("size.size"),
                    "APP.TRADE.MODIFY_SIZE_FIELD",
                    "ERRORS.TRADE_BOX_TITLE.INVALID_NEW_POSITION_LEVERAGE",
                    "ERRORS.TRADE_BOX.INVALID_NEW_POSITION_LEVERAGE"
                )
                else error(
                    "ERROR",
                    "INVALID_LARGE_POSITION_LEVERAGE",
                    iListOf("size.size"),
                    "APP.TRADE.MODIFY_SIZE_FIELD",
                    "ERRORS.TRADE_BOX_TITLE.INVALID_LARGE_POSITION_LEVERAGE",
                    "ERRORS.TRADE_BOX.INVALID_LARGE_POSITION_LEVERAGE"
                )
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
                        iListOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "WARNINGS.TRADE_BOX_TITLE.MARKET_ORDER_CLOSE_TO_MAX_LEVERAGE",
                        "WARNINGS.TRADE_BOX.MARKET_ORDER_CLOSE_TO_MAX_LEVERAGE"
                    )
                } else null
            } else null
        }
    }

    private fun overMaxLeverage(
        position: IMap<String, Any>,
    ): Boolean {
        val leverage = parser.asDouble(parser.value(position, "leverage.postOrder"))?.abs()
        val adjustedImf = parser.asDouble(parser.value(position, "adjustedImf.current"))
        return overLeverage(leverage, adjustedImf)
    }

    private fun overLeverage(leverage: Double?, adjustedImf: Double?): Boolean {
        return if (leverage != null && adjustedImf != null && adjustedImf > Numeric.double.ZERO) {
            leverage > (Numeric.double.ONE / adjustedImf) || leverage < Numeric.double.ZERO
        } else false
    }

    private fun orderOverMaxLeverage(
        position: IMap<String, Any>,
    ): Boolean {
        val leverage = parser.asDouble(parser.value(position, "leverage.postOrder"))?.abs()
        val adjustedImf = parser.asDouble(parser.value(position, "adjustedImf.postOrder"))
        return overLeverage(leverage, adjustedImf)
    }

    private fun validatePositionSize(
        position: IMap<String, Any>?,
        market: IMap<String, Any>?,
    ): IMap<String, Any>? {
        /*
        NEW_POSITION_SIZE_OVER_MAX
         */
        val size = parser.asDecimal(parser.value(position, "size.postOrder")) ?: return null
        val maxSize =
            parser.asDecimal(parser.value(market, "configs.maxPositionSize")) ?: Numeric.decimal.ZERO
        if (maxSize == Numeric.decimal.ZERO) {
            return null
        }
        val symbol = parser.asString(market?.get("assetId")) ?: return null
        return if (size > maxSize) error(
            "ERROR",
            "NEW_POSITION_SIZE_OVER_MAX",
            iListOf("size.size"),
            "APP.TRADE.MODIFY_SIZE_FIELD",
            "ERRORS.TRADE_BOX_TITLE.NEW_POSITION_SIZE_OVER_MAX",
            "ERRORS.TRADE_BOX.NEW_POSITION_SIZE_OVER_MAX",
            iMapOf(
                "MAX_SIZE" to iMapOf(
                    "value" to maxSize,
                    "format" to "size"
                ),
                "SYMBOL" to iMapOf(
                    "value" to symbol,
                    "format" to "string"
                )
            )
        ) else null
    }

    private fun validatePositionFlip(
        change: PositionChange,
        trade: IMap<String, Any>,
    ): IMap<String, Any>? {
        /*
        ORDER_WOULD_FLIP_POSITION
         */
        val needsReduceOnly = parser.asBool(parser.value(trade, "options.needsReduceOnly")) ?: false
        return if (needsReduceOnly && parser.asBool(trade["reduceOnly"]) == true && change == PositionChange.CROSSING) error(
            "ERROR",
            "ORDER_WOULD_FLIP_POSITION",
            iListOf("size.size"),
            "APP.TRADE.MODIFY_SIZE_FIELD",
            "ERRORS.TRADE_BOX_TITLE.ORDER_WOULD_FLIP_POSITION",
            "ERRORS.TRADE_BOX.ORDER_WOULD_FLIP_POSITION"
        ) else null
    }
}
