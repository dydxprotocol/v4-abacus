package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradeMarketOrderInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    private val MARKET_ORDER_ERROR_SLIPPAGE = 0.02
    private val MARKET_ORDER_WARNING_SLIPPAGE = 0.005

    override fun validateTrade(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean
    ): List<Any>? {
        return if (parser.asString(trade["type"]) == "MARKET") {
            validateMarketOrder(
                trade,
                market,
                restricted
            )
        } else null
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
            error = orderbookSlippage(trade, restricted)
            if (error != null) {
                errors.add(error)
            }
            error = indexPriceSlippage(trade, restricted)
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

        return if (filled != false) null else error(
            if (restricted) "WARNING" else "ERROR",
            "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
            listOf("size.size"),
            "APP.TRADE.MODIFY_SIZE_FIELD",
            "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
            "ERRORS.TRADE_BOX.MARKET_ORDER_NOT_ENOUGH_LIQUIDITY"
        )
    }

    private fun orderbookSlippage(
        trade: Map<String, Any>,
        restricted: Boolean
    ): Map<String, Any>? {
        /*
        MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE
        MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE
         */
        parser.asNativeMap(trade["summary"])?.let { summary ->
            parser.asDouble(summary["slippage"])?.let { slippage ->
                val slippageValue = slippage.abs()
                if (slippageValue >= MARKET_ORDER_ERROR_SLIPPAGE) {
                    return error(
                        if (restricted) "WARNING" else "ERROR",
                        "MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE",
                        listOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE",
                        "ERRORS.TRADE_BOX.MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE",
                        mapOf(
                            "SLIPPAGE" to mapOf(
                                "value" to slippageValue,
                                "format" to "percent"
                            )
                        )
                    )
                } else if (slippageValue >= MARKET_ORDER_WARNING_SLIPPAGE) {
                    return error(
                        "WARNING",
                        "MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE",
                        listOf("size.size"),
                        null,
                        "WARNINGS.TRADE_BOX_TITLE.MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE",
                        "WARNINGS.TRADE_BOX.MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE",
                        mapOf(
                            "SLIPPAGE" to mapOf(
                                "value" to slippageValue,
                                "format" to "percent"
                            )
                        )
                    )
                }
            }
        }
        return null
    }

    private fun indexPriceSlippage(
        trade: Map<String, Any>,
        restricted: Boolean
    ): Map<String, Any>? {
        /*
        MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE
        MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE
         */
        parser.asNativeMap(trade["summary"])?.let { summary ->
            parser.asDouble(summary["indexSlippage"])?.let { slippage ->
                val slippageValue = slippage
                if (slippageValue >= MARKET_ORDER_ERROR_SLIPPAGE) {
                    return error(
                        if (restricted) "WARNING" else "ERROR",
                        "MARKET_ORDER_ERROR_INDEX_SLIPPAGE",
                        listOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE",
                        "ERRORS.TRADE_BOX.MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE",
                        mapOf(
                            "SLIPPAGE" to mapOf(
                                "value" to slippageValue,
                                "format" to "percent"
                            )
                        )
                    )
                } else if (slippageValue >= MARKET_ORDER_WARNING_SLIPPAGE) {
                    return error(
                        "WARNING",
                        "MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE",
                        listOf("size.size"),
                        null,
                        "WARNINGS.TRADE_BOX_TITLE.MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE",
                        "WARNINGS.TRADE_BOX.MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE",
                        mapOf(
                            "SLIPPAGE" to mapOf(
                                "value" to slippageValue,
                                "format" to "percent"
                            )
                        )
                    )
                }
            }
        }
        return null
    }
}
