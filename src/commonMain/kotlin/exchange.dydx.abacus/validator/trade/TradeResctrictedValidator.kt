package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradeResctrictedValidator(
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
        val marketId = trade.marketId ?: return null
        val market = internalState.marketsSummary.markets[marketId]
        val closeOnlyError =
            validateClosingOnly(
                market = market,
                assets = internalState.assets,
                change = change,
                restricted = restricted,
            )

        return closeOnlyError?.let { listOf(it) }
    }

    private fun validateClosingOnly(
        market: InternalMarketState?,
        assets: Map<String, Asset>?,
        change: PositionChange,
        restricted: Boolean,
    ): ValidationError? {
        val assetId = market?.perpetualMarket?.assetId ?: return null
        val marketId = assets?.get(assetId)?.displayableAssetId ?: return null
        val canTrade = market?.perpetualMarket?.status?.canTrade ?: true
        val canReduce = market?.perpetualMarket?.status?.canReduce ?: true

        return if (canTrade) {
            if (restricted) {
                when (change) {
                    PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING ->
                        error(
                            type = ErrorType.error,
                            errorCode = "RESTRICTED_USER",
                            fields = null,
                            actionStringKey = null,
                            titleStringKey = "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_CLOSE_POSITION_ONLY",
                            textStringKey = "ERRORS.TRADE_BOX.MARKET_ORDER_CLOSE_POSITION_ONLY",
                        )

                    else -> null
                }
            } else {
                return null
            }
        } else if (canReduce) {
            when (change) {
                PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING ->
                    error(
                        type = ErrorType.error,
                        errorCode = "CLOSE_ONLY_MARKET",
                        fields = listOf("size.size"),
                        actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                        titleStringKey = "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                        textStringKey = "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                        textParams = mapOf(
                            "MARKET" to mapOf(
                                "value" to marketId,
                                "format" to "string",
                            ),
                        ),
                    )

                else -> null
            }
        } else {
            error(
                type = ErrorType.error,
                errorCode = "CLOSED_MARKET",
                fields = null,
                actionStringKey = null,
                titleStringKey = "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                textStringKey = "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                textParams = mapOf(
                    "MARKET" to mapOf(
                        "value" to marketId,
                        "format" to "string",
                    ),
                ),
            )
        }
    }
}
