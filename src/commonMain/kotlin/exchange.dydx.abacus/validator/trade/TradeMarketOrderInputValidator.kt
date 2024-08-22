package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradeMarketOrderInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    private val marketOrderErrorSlippage = 0.1
    private val marketOrderWarningSlippage = 0.05

    override fun validateTrade(
        internalState: InternalState,
        subaccountNumber: Int?,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?
    ): List<ValidationError>? {
        val trade = internalState.input.trade
        if (trade.type != OrderType.Market) {
            return null
        }

        val errors = mutableListOf<ValidationError>()
        validateLiquidity(trade)?.let {
            errors.add(it)
        }
        validateOrderbookOrIndexSlippage(trade, restricted)?.let {
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
        return if (parser.asString(trade["type"]) == "MARKET") {
            validateMarketOrder(
                trade,
                market,
                restricted,
            )
        } else {
            null
        }
    }

    private fun accountRestricted(): Boolean {
        return false
    }

    private fun validateMarketOrder(
        trade: Map<String, Any>,
        markets: Map<String, Any>?,
        restricted: Boolean
    ): List<Any>? {
        return if (parser.asString(trade["type"]) == "MARKET") {
            val errors = mutableListOf<Any>()

            var error = liquidity(trade, restricted)
            if (error != null) {
                errors.add(error)
            }
            error = orderbookOrIndexSlippage(trade, restricted)
            if (error != null) {
                errors.add(error)
            }

            if (errors.size > 0) errors else null
        } else {
            null
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
                errorLevel = if (accountRestricted()) ErrorType.warning else ErrorType.error,
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
                errorLevel = if (accountRestricted()) ErrorType.warning else ErrorType.error,
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

        return when {
            minSlippageValue >= marketOrderErrorSlippage -> createTradeBoxWarningOrError(
                errorLevel = if (restricted) ErrorType.warning else ErrorType.error,
                errorCode = "MARKET_ORDER_ERROR_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                slippagePercentValue = minSlippageValue,
            )
            minSlippageValue >= marketOrderWarningSlippage -> createTradeBoxWarningOrError(
                errorLevel = ErrorType.warning,
                errorCode = "MARKET_ORDER_WARNING_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                slippagePercentValue = minSlippageValue,
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
                slippagePercentValue = minSlippageValue,
            )
            minSlippageValue >= marketOrderWarningSlippage -> createTradeBoxWarningOrErrorDeprecated(
                errorLevel = "WARNING",
                errorCode = "MARKET_ORDER_WARNING_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                slippagePercentValue = minSlippageValue,
            )
            else -> null
        }
    }

    private fun createTradeBoxWarningOrErrorDeprecated(
        errorLevel: String,
        errorCode: String,
        fields: List<String>? = null,
        actionStringKey: String? = null,
        slippagePercentValue: Double? = null
    ): Map<String, Any> {
        return errorDeprecated(
            type = errorLevel,
            errorCode = errorCode,
            fields = fields,
            actionStringKey = actionStringKey,
            titleStringKey = "ERRORS.TRADE_BOX_TITLE.$errorCode",
            textStringKey = "ERRORS.TRADE_BOX.$errorCode",
            textParams = slippagePercentValue?.let {
                mapOf(
                    "SLIPPAGE" to mapOf(
                        "value" to it,
                        "format" to "percent",
                    ),
                )
            },
        )
    }

    private fun createTradeBoxWarningOrError(
        errorLevel: ErrorType,
        errorCode: String,
        fields: List<String>? = null,
        actionStringKey: String? = null,
        slippagePercentValue: Double? = null
    ): ValidationError {
        return error(
            type = errorLevel,
            errorCode = errorCode,
            fields = fields,
            actionStringKey = actionStringKey,
            titleStringKey = "ERRORS.TRADE_BOX_TITLE.$errorCode",
            textStringKey = "ERRORS.TRADE_BOX.$errorCode",
            textParams = slippagePercentValue?.let {
                mapOf(
                    "SLIPPAGE" to mapOf(
                        "value" to it,
                        "format" to "percent",
                    ),
                )
            },
        )
    }
}
