package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalPerpetualPosition
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.InternalTradeInputState
import exchange.dydx.abacus.state.helper.Formatter
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
            assets = internalState.assets,
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

    private fun validatePositionSize(
        position: InternalPerpetualPosition?,
        market: InternalMarketState?,
        assets: Map<String, Asset>?,
    ): ValidationError? {
        /*
        NEW_POSITION_SIZE_OVER_MAX
         */
        val size = position?.calculated?.get(CalculationPeriod.post)?.size ?: return null
        val maxSize = market?.perpetualMarket?.configs?.maxPositionSize ?: Numeric.double.ZERO
        if (maxSize == Numeric.double.ZERO) {
            return null
        }
        val assetId = market?.perpetualMarket?.assetId ?: return null
        val symbol = assets?.get(assetId)?.displayableAssetId ?: return null
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
}
