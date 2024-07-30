package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
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
