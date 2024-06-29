package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
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

            val transferAmount = MarginCalculator.getIsolatedMarginTransferAmount(
                parser,
                subaccount = childSubaccount,
                trade = trade,
                market,
            ) ?: 0.0

            val shouldTransferOut = MarginCalculator.getShouldTransferOutCollateral(
                parser,
                subaccount = childSubaccount,
                trade,
            )

            val transferToReceiveByParent = if (shouldTransferOut) {
                MarginCalculator.getSubaccountFreeCollateralToTransferOut(
                    parser,
                    subaccount = childSubaccount,
                )
            } else {
                null
            }

            Logger.e { "$transferToReceiveByParent" }

            val modifiedSubaccount =
                subaccountTransformer.applyTransferToSubaccount(
                    subaccount,
                    ((if (transferAmount > 0.0) transferAmount else transferToReceiveByParent) ?: 0.0) * -1.0,
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
