package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

class AccountTransformer() {
    private val subaccountTransformer = SubaccountTransformer()
    internal fun applyTradeToAccount(
        account: Map<String, Any>?,
        subaccountNumber: Int?,
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        parser: ParserProtocol,
        period: String,
    ): Map<String, Any>? {
        val modified = account?.mutable() ?: return null
        val childSubaccountNumber =
            MarginModeCalculator.getChildSubaccountNumberForIsolatedMarginTrade(
                parser,
                account,
                subaccountNumber ?: 0,
                trade,
            )
        if (subaccountNumber == childSubaccountNumber) {
            // CROSS
            val subaccount = parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$subaccountNumber",
                ),
            ) ?: mapOf()
            val modifiedSubaccount =
                subaccountTransformer.applyTradeToSubaccount(
                    subaccount,
                    trade,
                    market,
                    parser,
                    period,
                )
            modified.safeSet("subaccounts.$subaccountNumber", modifiedSubaccount)
            return modified
        } else {
            val transferAmount = calculateIsolatedMarginTransferAmount(
                parser,
                trade,
            ) ?: 0.0

            val subaccount = parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$subaccountNumber",
                ),
            ) ?: mapOf()

            val modifiedSubaccount =
                subaccountTransformer.applyTransferToSubaccount(
                    subaccount,
                    transferAmount * -1.0,
                    parser,
                    period,
                )
            modified.safeSet("subaccounts.$subaccountNumber", modifiedSubaccount)

            val childSubaccount = parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$childSubaccountNumber",
                ),
            ) ?: mapOf()

            val modifiedChildSubaccount =
                subaccountTransformer.applyTradeToSubaccount(
                    childSubaccount,
                    trade,
                    market,
                    parser,
                    period,
                    transferAmount,
                )
            modified.safeSet("subaccounts.$childSubaccountNumber", modifiedChildSubaccount)
            return modified
        }
    }

    fun calculateIsolatedMarginTransferAmount(
        parser: ParserProtocol,
        trade: Map<String, Any>,
    ): Double? {
        val marketOrderUsdcSize = parser.asDouble(parser.value(trade, "marketOrder.usdcSize"))
        val targetLeverage = parser.asDouble(trade["targetLeverage"]) ?: 1.0

        return if (targetLeverage == 0.0) {
            null
        } else {
            val usdcSize = marketOrderUsdcSize ?: parser.asDouble(parser.value(trade, "size.usdcSize")) ?: return null
            usdcSize / targetLeverage
        }
    }
}
