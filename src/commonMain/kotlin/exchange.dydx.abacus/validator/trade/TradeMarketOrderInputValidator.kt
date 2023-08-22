package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol
import kollections.iListOf
import kollections.iMutableListOf

internal class TradeMarketOrderInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    private val MARKET_ORDER_ERROR_SLIPPAGE = 0.02
    private val MARKET_ORDER_WARNING_SLIPPAGE = 0.005

    override fun validateTrade(
        subaccount: IMap<String, Any>?,
        market: IMap<String, Any>?,
        configs: IMap<String, Any>?,
        trade: IMap<String, Any>,
        change: PositionChange,
        restricted: Boolean
    ): IList<Any>? {
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
        trade: IMap<String, Any>,
        markets: IMap<String, Any>?,
        restricted: Boolean
    ): IList<Any>? {
        return if (parser.asString(trade["type"]) == "MARKET") {
            val errors = iMutableListOf<Any>()

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
        trade: IMap<String, Any>,
        restricted: Boolean
    ): IMap<String, Any>? {
        /*
        MARKET_ORDER_NOT_ENOUGH_LIQUIDITY
         */
        val marketOrder = parser.asMap(trade["marketOrder"]) ?: return null
        val filled = parser.asBool(marketOrder["filled"]) ?: false

        return if (filled) null else error(
             if (restricted) "WARNING" else "ERROR",
            "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
            iListOf("size.size"),
            "APP.TRADE.MODIFY_SIZE_FIELD",
            "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
            "ERRORS.TRADE_BOX.MARKET_ORDER_NOT_ENOUGH_LIQUIDITY"
        )
    }

    private fun orderbookSlippage(
        trade: IMap<String, Any>,
        restricted: Boolean
    ): IMap<String, Any>? {
        /*
        MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE
        MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE
         */
        parser.asMap(trade["summary"])?.let { summary ->
            parser.asDouble(summary["slippage"])?.let { slippage ->
                val slippageValue = slippage.abs()
                if (slippageValue >= MARKET_ORDER_ERROR_SLIPPAGE) {
                    return error(
                        if (restricted) "WARNING" else "ERROR",
                        "MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE",
                        iListOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE",
                        "ERRORS.TRADE_BOX.MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE",
                        iMapOf(
                            "SLIPPAGE" to iMapOf(
                                "value" to slippageValue,
                                "format" to "percent"
                            )
                        )
                    )
                } else if (slippageValue >= MARKET_ORDER_WARNING_SLIPPAGE) {
                    return error(
                         "WARNING",
                        "MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE",
                        iListOf("size.size"),
                        null,
                        "WARNINGS.TRADE_BOX_TITLE.MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE",
                        "WARNINGS.TRADE_BOX.MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE",
                        iMapOf(
                            "SLIPPAGE" to iMapOf(
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
        trade: IMap<String, Any>,
        restricted: Boolean
    ): IMap<String, Any>? {
        /*
        MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE
        MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE
         */
        parser.asMap(trade["summary"])?.let { summary ->
            parser.asDouble(summary["indexSlippage"])?.let { slippage ->
                val slippageValue = slippage.abs()
                if (slippageValue >= MARKET_ORDER_ERROR_SLIPPAGE) {
                    return error(
                        if (restricted) "WARNING" else "ERROR",
                        "MARKET_ORDER_ERROR_INDEX_SLIPPAGE",
                        iListOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE",
                        "ERRORS.TRADE_BOX.MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE",
                        iMapOf(
                            "SLIPPAGE" to iMapOf(
                                "value" to slippageValue,
                                "format" to "percent"
                            )
                        )
                    )
                } else if (slippageValue >= MARKET_ORDER_WARNING_SLIPPAGE) {
                    return error(
                        "WARNING",
                        "MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE",
                        iListOf("size.size"),
                        null,
                        "WARNINGS.TRADE_BOX_TITLE.MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE",
                        "WARNINGS.TRADE_BOX.MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE",
                        iMapOf(
                            "SLIPPAGE" to iMapOf(
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
