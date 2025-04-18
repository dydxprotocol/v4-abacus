package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.InternalSubaccountState
import exchange.dydx.abacus.state.InternalTradeInputState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradeOrderInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    private val marketOrderErrorSlippage = 0.1
    private val marketOrderWarningSlippage = 0.05
    private val isolatedLimitOrderMinimumEquity = 20.0

    override fun validateTrade(
        internalState: InternalState,
        subaccountNumber: Int?,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?
    ): List<ValidationError>? {
        val subaccount = internalState.wallet.account.subaccounts[subaccountNumber] ?: return null
        val trade = when (internalState.input.currentType) {
            InputType.TRADE -> internalState.input.trade
            InputType.CLOSE_POSITION -> internalState.input.closePosition
            else -> return null
        }

        return when (trade.type) {
            OrderType.Market -> {
                val errors = mutableListOf<ValidationError>()
                validateLiquidity(trade)?.let {
                    errors.add(it)
                }
                validateOrderbookOrIndexSlippage(trade, restricted)?.let {
                    errors.add(it)
                }
                errors
            }
            OrderType.Limit, OrderType.StopLimit, OrderType.TakeProfitLimit -> {
                val errors = mutableListOf<ValidationError>()
                validateIsolatedMarginMinSize(subaccount, trade, environment)?.let {
                    errors.add(it)
                }
                errors
            }
            else -> null
        }
    }

    private fun validateIsolatedMarginMinSize(subaccount: InternalSubaccountState, trade: InternalTradeInputState, environment: V4Environment?): ValidationError? {
        return when (trade.marginMode) {
            MarginMode.Isolated -> {
                val currentFreeCollateral = subaccount.calculated.get(CalculationPeriod.current)?.freeCollateral ?: return null
                val postFreeCollateral = subaccount.calculated.get(CalculationPeriod.post)?.freeCollateral ?: return null
                val orderEquity = currentFreeCollateral - postFreeCollateral
                val isReducingPosition = orderEquity < Numeric.double.ZERO

                if (postFreeCollateral >= Numeric.double.ZERO && !isReducingPosition && orderEquity < isolatedLimitOrderMinimumEquity) {
                    return createTradeBoxWarningOrError(
                        errorLevel = ErrorType.error,
                        errorCode = "ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM",
                        fields = listOf("size.size"),
                        actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                        textParams = mapOf(
                            "MIN_VALUE" to mapOf(
                                "value" to isolatedLimitOrderMinimumEquity,
                                "format" to "usdcPrice",
                            ),
                        ),
                        learnMoreLink = environment?.links?.equityTiersLearnMore,
                    )
                }
                return null
            }
            else -> null
        }
    }

    private fun validateLiquidity(
        trade: InternalTradeInputState,
    ): ValidationError? {
        /*
        MARKET_ORDER_NOT_ENOUGH_LIQUIDITY
         */
        val filled = trade.marketOrder?.filled

        if (filled == false) {
            return createTradeBoxWarningOrError(
                errorLevel = ErrorType.error,
                errorCode = "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
            )
        }

        val summary = trade.summary
        // if there's liquidity for market order to be filled but is missing orderbook slippage (mid price)
        // it is a one sided liquidity situation and should place limit order instead
        if (summary != null && summary.slippage == null) {
            return createTradeBoxWarningOrError(
                errorLevel = ErrorType.error,
                errorCode = "MARKET_ORDER_ONE_SIDED_LIQUIDITY",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
            )
        }

        return null
    }

    private fun validateOrderbookOrIndexSlippage(
        trade: InternalTradeInputState,
        restricted: Boolean,
    ): ValidationError? {
        /*
        MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE
        MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE

        MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE
        MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE
         */
        val summary = trade.summary ?: return null

        // missing orderbook slippage is due to a one sided liquidity situation
        // and should be caught by liquidity validation
        val orderbookSlippage = summary.slippage ?: return null
        val orderbookSlippageValue = orderbookSlippage.abs()
        val indexSlippage = summary.indexSlippage

        var slippageType = "ORDERBOOK"
        var minSlippageValue = orderbookSlippageValue
        if (indexSlippage != null && indexSlippage < orderbookSlippageValue) {
            slippageType = "INDEX_PRICE"
            minSlippageValue = indexSlippage
        }

        val textParams = mapOf(
            "SLIPPAGE" to mapOf(
                "value" to minSlippageValue,
                "format" to "percent",
            ),
        )

        return when {
            minSlippageValue >= marketOrderErrorSlippage -> createTradeBoxWarningOrError(
                errorLevel = if (restricted) ErrorType.warning else ErrorType.error,
                errorCode = "MARKET_ORDER_ERROR_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                textParams = textParams,
            )
            minSlippageValue >= marketOrderWarningSlippage -> createTradeBoxWarningOrError(
                errorLevel = ErrorType.warning,
                errorCode = "MARKET_ORDER_WARNING_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                textParams = textParams,
            )
            else -> null
        }
    }

    private fun createTradeBoxWarningOrError(
        errorLevel: ErrorType,
        errorCode: String,
        fields: List<String>? = null,
        actionStringKey: String? = null,
        textParams: Map<String, Any>? = null,
        learnMoreLink: String? = null,
    ): ValidationError {
        return error(
            type = errorLevel,
            errorCode = errorCode,
            fields = fields,
            actionStringKey = actionStringKey,
            titleStringKey = "ERRORS.TRADE_BOX_TITLE.$errorCode",
            textStringKey = "ERRORS.TRADE_BOX.$errorCode",
            textParams = textParams,
            link = learnMoreLink,
            linkText = if (learnMoreLink != null) {
                "APP.GENERAL.LEARN_MORE_ARROW"
            } else {
                null
            },
        )
    }
}
