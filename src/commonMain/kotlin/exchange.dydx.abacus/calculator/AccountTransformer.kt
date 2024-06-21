package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

class AccountTransformer() {
    private val subaccountTransformer = SubaccountTransformer()
    internal fun applyTradeToAccount(
        account: Map<String, Any>?,
        subaccountNumber: Int,
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        parser: ParserProtocol,
        period: String,
    ): Map<String, Any>? {
        val modified = account?.mutable() ?: return null
        val childSubaccountNumber =
            MarginCalculator.getChildSubaccountNumberForIsolatedMarginTrade(
                parser,
                account,
                subaccountNumber ?: 0,
                trade,
            )
        val subaccount = parser.asNativeMap(
            parser.value(
                account,
                "subaccounts.$subaccountNumber",
            ),
        ) ?: mapOf()
        if (subaccountNumber == childSubaccountNumber) {
            // CROSS
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
            val childSubaccount = parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$childSubaccountNumber",
                ),
            ) ?: mapOf()

            val transferAmount = if (MarginCalculator.getShouldTransferCollateral(
                    parser,
                    subaccount = childSubaccount,
                    tradeInput = trade,
                )
            ) {
                MarginCalculator.calculateIsolatedMarginTransferAmount(
                    parser,
                    trade,
                    market,
                    subaccount = childSubaccount,
                ) ?: 0.0
            } else {
                0.0
            }

            val modifiedSubaccount =
                subaccountTransformer.applyTransferToSubaccount(
                    subaccount,
                    transferAmount * -1.0,
                    parser,
                    period,
                )
            modified.safeSet("subaccounts.$subaccountNumber", modifiedSubaccount)

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
}
