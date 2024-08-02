package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
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

    private fun liquidity(
        trade: Map<String, Any>,
        restricted: Boolean
    ): Map<String, Any>? {
        /*
        MARKET_ORDER_NOT_ENOUGH_LIQUIDITY
         */
        val filled = parser.asBool(parser.value(trade, "marketOrder.filled"))

        if (filled == false) {
            return createTradeBoxWarningOrError(
                errorLevel = if (restricted) "WARNING" else "ERROR",
                errorCode = "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
            )
        }

        val summary = parser.asNativeMap(trade["summary"]) ?: return null
        // if there's liquidity for market order to be filled but is missing orderbook slippage (mid price)
        // it is a one sided liquidity situation and should place limit order instead
        parser.asDouble(summary["slippage"])
            ?: return createTradeBoxWarningOrError(
                errorLevel = if (restricted) "WARNING" else "ERROR",
                errorCode = "MARKET_ORDER_ONE_SIDED_LIQUIDITY",
                fields = listOf("size.size"),
                actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
            )

        return null
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
            minSlippageValue >= marketOrderErrorSlippage -> createTradeBoxWarningOrError(
                errorLevel = if (restricted) "WARNING" else "ERROR",
                errorCode = "MARKET_ORDER_ERROR_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                slippagePercentValue = minSlippageValue,
            )
            minSlippageValue >= marketOrderWarningSlippage -> createTradeBoxWarningOrError(
                errorLevel = "WARNING",
                errorCode = "MARKET_ORDER_WARNING_${slippageType}_SLIPPAGE",
                actionStringKey = "APP.TRADE.PLACE_LIMIT_ORDER",
                slippagePercentValue = minSlippageValue,
            )
            else -> null
        }
    }

    private fun createTradeBoxWarningOrError(
        errorLevel: String,
        errorCode: String,
        fields: List<String>? = null,
        actionStringKey: String? = null,
        slippagePercentValue: Double? = null
    ): Map<String, Any> {
        return error(
            type = errorLevel,
            errorCode,
            fields,
            actionStringKey,
            "ERRORS.TRADE_BOX_TITLE.$errorCode",
            "ERRORS.TRADE_BOX.$errorCode",
            slippagePercentValue?.let {
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
