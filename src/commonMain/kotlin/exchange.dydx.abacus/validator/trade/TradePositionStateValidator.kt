package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradePositionStateValidator(
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
        val subaccountNumber = subaccountNumber ?: return null
        val subaccount = internalState.wallet.account.subaccounts[subaccountNumber]
        val position = subaccount?.openPositions?.get(trade.marketId)

        val errors = mutableListOf<ValidationError>()
        validatePositionSize(
            position = position,
            market = internalState.marketsSummary.markets[trade.marketId],
        )?.let {
            errors.add(it)
        }
        validatePositionFlip(
            change = change,
            trade = trade,
        )?.let {
            errors.add(it)
        }

        return errors
    }

    override fun validateTradeDeprecated(
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
        if (position != null) {
            val positionSizeError = validatePositionSizeDeprecated(
                position,
                market,
            )
            if (positionSizeError != null) {
                errors.add(positionSizeError)
            }
            val positionFlipError = validatePositionFlipDeprecated(
                change,
                trade,
            )
            if (positionFlipError != null) {
                errors.add(positionFlipError)
            }
        }
        return if (errors.size > 0) errors else null
    }

    private fun validatePositionSize(
        position: InternalPerpetualPosition?,
        market: InternalMarketState?,
    ): ValidationError? {
        /*
        NEW_POSITION_SIZE_OVER_MAX
         */
        val size = position?.calculated?.get(CalculationPeriod.post)?.size ?: return null
        val maxSize = market?.perpetualMarket?.configs?.maxPositionSize ?: Numeric.double.ZERO
        if (maxSize == Numeric.double.ZERO) {
            return null
        }
        val symbol = market?.perpetualMarket?.assetId ?: return null
        return if (size > maxSize) {
            error(
                type = ErrorType.error,
                errorCode = "NEW_POSITION_SIZE_OVER_MAX",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.NEW_POSITION_SIZE_OVER_MAX",
                textStringKey = "ERRORS.TRADE_BOX.NEW_POSITION_SIZE_OVER_MAX",
                textParams = mapOf(
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

    private fun validatePositionSizeDeprecated(
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
            errorDeprecated(
                type = "ERROR",
                errorCode = "NEW_POSITION_SIZE_OVER_MAX",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.NEW_POSITION_SIZE_OVER_MAX",
                textStringKey = "ERRORS.TRADE_BOX.NEW_POSITION_SIZE_OVER_MAX",
                textParams = mapOf(
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
        trade: InternalTradeInputState,
    ): ValidationError? {
        /*
        ORDER_WOULD_FLIP_POSITION
         */
        val needsReduceOnly = trade.options.needsReduceOnly
        return if (needsReduceOnly && trade.reduceOnly) {
            when (change) {
                PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING -> error(
                    type = ErrorType.error,
                    errorCode = "ORDER_WOULD_FLIP_POSITION",
                    fields = listOf("size.size"),
                    actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                    titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_WOULD_FLIP_POSITION",
                    textStringKey = "ERRORS.TRADE_BOX.ORDER_WOULD_FLIP_POSITION",
                )

                else -> null
            }
        } else {
            null
        }
    }

    private fun validatePositionFlipDeprecated(
        change: PositionChange,
        trade: Map<String, Any>,
    ): Map<String, Any>? {
        /*
        ORDER_WOULD_FLIP_POSITION
         */
        val needsReduceOnly = parser.asBool(parser.value(trade, "options.needsReduceOnly")) ?: false
        return if (needsReduceOnly && parser.asBool(trade["reduceOnly"]) == true) {
            when (change) {
                PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING -> errorDeprecated(
                    type = "ERROR",
                    errorCode = "ORDER_WOULD_FLIP_POSITION",
                    fields = listOf("size.size"),
                    actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                    titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_WOULD_FLIP_POSITION",
                    textStringKey = "ERRORS.TRADE_BOX.ORDER_WOULD_FLIP_POSITION",
                )

                else -> null
            }
        } else {
            null
        }
    }
}
