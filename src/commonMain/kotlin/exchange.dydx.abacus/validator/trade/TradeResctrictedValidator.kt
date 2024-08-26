package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalState
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
                change = change,
                restricted = restricted,
            )

        return closeOnlyError?.let { listOf(it) }
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
        val closeOnlyError =
            validateClosingOnlyDeprecated(
                parser = parser,
                market = market,
                change = change,
                restricted = restricted,
            )

        return closeOnlyError?.let { listOf(it) }
    }

    private fun validateClosingOnly(
        market: InternalMarketState?,
        change: PositionChange,
        restricted: Boolean,
    ): ValidationError? {
        val marketId = market?.perpetualMarket?.assetId ?: ""
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

    private fun validateClosingOnlyDeprecated(
        parser: ParserProtocol,
        market: Map<String, Any>?,
        change: PositionChange,
        restricted: Boolean,
    ): Map<String, Any>? {
        val marketId = parser.asNativeMap(market?.get("assetId")) ?: ""
        val canTrade = parser.asBool(parser.value(market, "status.canTrade")) ?: true
        val canReduce = parser.asBool(parser.value(market, "status.canReduce")) ?: true
        return if (canTrade) {
            if (restricted) {
                when (change) {
                    PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING ->
                        errorDeprecated(
                            type = "ERROR",
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
                    errorDeprecated(
                        type = "ERROR",
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
            errorDeprecated(
                type = "ERROR",
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
