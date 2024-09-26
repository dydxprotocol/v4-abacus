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
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
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

    override fun validateTradeDeprecated(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<Any>? {
        val tradeType = parser.asString(trade["type"])?.let {
            OrderType.invoke(it)
        }

        return when (tradeType) {
            OrderType.Market -> validateMarketOrder(
                trade,
                restricted,
            )
            OrderType.Limit, OrderType.StopLimit, OrderType.TakeProfitLimit -> validateLimitOrder(subaccount, trade, restricted, environment)

            else -> null
        }
    }

    private fun validateMarketOrder(
        trade: Map<String, Any>,
        restricted: Boolean
    ): List<Any>? {
        val errors = mutableListOf<Any>()

        var error = liquidity(trade, restricted)
        if (error != null) {
            errors.add(error)
        }
        error = orderbookOrIndexSlippage(trade, restricted)
        if (error != null) {
            errors.add(error)
        }

        return if (errors.size > 0) errors else null
    }

    private fun validateLimitOrder(
        subaccount: Map<String, Any>?,
        trade: Map<String, Any>,
        restricted: Boolean,
        environment: V4Environment?
    ): List<Any>? {
        val errors = mutableListOf<Any>()
        var error = isolatedMarginMinSize(subaccount, trade, restricted, environment)
        if (error != null) {
            errors.add(error)
        }
        return if (errors.size > 0) errors else null
    }

    private fun isolatedMarginMinSize(subaccount: Map<String, Any>?, trade: Map<String, Any>, restricted: Boolean, environment: V4Environment?): Map<String, Any>? {
        val marginMode = parser.asString(trade.get("marginMode"))?.let {
            MarginMode.invoke(it)
        }

        return when (marginMode) {
            MarginMode.Isolated -> {
                val currentFreeCollateral = parser.asDouble(parser.value(subaccount, "freeCollateral.current")) ?: return null
                val postFreeCollateral = parser.asDouble(parser.value(subaccount, "freeCollateral.postOrder")) ?: return null
                val orderEquity = currentFreeCollateral - postFreeCollateral

                if (postFreeCollateral >= Numeric.double.ZERO && orderEquity < isolatedLimitOrderMinimumEquity) {
                    return createTradeBoxWarningOrErrorDeprecated(
                        errorLevel = if (restricted) "WARNING" else "ERROR",
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
                } else {
                    return null
                }
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

                if (postFreeCollateral >= Numeric.double.ZERO && orderEquity < isolatedLimitOrderMinimumEquity) {
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

    private fun liquidity(
        trade: Map<String, Any>,
        restricted: Boolean
    ): Map<String, Any>? {
        /*
        MARKET_ORDER_NOT_ENOUGH_LIQUIDITY
         */
        val filled = parser.asBool(parser.value(trade, "marketOrder.filled"))

        if (filled == false) {
            return createTradeBoxWarningOrErrorDeprecated(
                errorLevel = if (restricted) "WARNING" else "ERROR",
                errorCode = "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
            )
        }

        // summary can be empty even though there's input e.g. leverage input is set to the same as current position leverage
        val summary = parser.asNativeMap(trade["summary"])?.takeIf { it.isNotEmpty() } ?: return null
        // if there's liquidity for market order to be filled but is missing orderbook slippage (mid price)
        // it is a one sided liquidity situation and should place limit order instead
        parser.asDouble(summary["slippage"])
            ?: return createTradeBoxWarningOrErrorDeprecated(
                errorLevel = if (restricted) "WARNING" else "ERROR",
                errorCode = "MARKET_ORDER_ONE_SIDED_LIQUIDITY",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
            )

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

    private fun orderbookOrIndexSlippage(
        trade: Map<String, Any>,
        restricted: Boolean
    ): Map<String, Any>? {
        /*
        MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE
        MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE

        MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE
        MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE
         */
        val summary = parser.asNativeMap(trade["summary"]) ?: return null

        // missing orderbook slippage is due to a one sided liquidity situation
        // and should be caught by liquidity validation
        val orderbookSlippage = parser.asDouble(summary["slippage"]) ?: return null
        val orderbookSlippageValue = orderbookSlippage.abs()
        val indexSlippage = parser.asDouble(summary["indexSlippage"])

        var slippageType = "ORDERBOOK"
        var minSlippageValue = orderbookSlippageValue
        if (indexSlippage != null && indexSlippage < orderbookSlippageValue) {
            slippageType = "INDEX_PRICE"
            minSlippageValue = indexSlippage
        }

        return when {
            minSlippageValue >= marketOrderErrorSlippage -> createTradeBoxWarningOrErrorDeprecated(
                errorLevel = if (restricted) "WARNING" else "ERROR",
                errorCode = "MARKET_ORDER_ERROR_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                textParams = mapOf(
                    "SLIPPAGE" to mapOf(
                        "value" to minSlippageValue,
                        "format" to "percent",
                    ),
                ),
            )
            minSlippageValue >= marketOrderWarningSlippage -> createTradeBoxWarningOrErrorDeprecated(
                errorLevel = "WARNING",
                errorCode = "MARKET_ORDER_WARNING_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                textParams = mapOf(
                    "SLIPPAGE" to mapOf(
                        "value" to minSlippageValue,
                        "format" to "percent",
                    ),
                ),
            )
            else -> null
        }
    }

    private fun createTradeBoxWarningOrErrorDeprecated(
        errorLevel: String,
        errorCode: String,
        fields: List<String>? = null,
        actionStringKey: String? = null,
        textParams: Map<String, Any>? = null,
        learnMoreLink: String? = null,
    ): Map<String, Any> {
        return errorDeprecated(
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
