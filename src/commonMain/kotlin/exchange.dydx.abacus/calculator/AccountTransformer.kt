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
        } else if (childSubaccountNumber != null) {
            val childSubaccount = parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$childSubaccountNumber",
                ),
            ) ?: mapOf()

            var transferAmountAppliedToParent = 0.0
            var transferAmountAppliedToChild = 0.0

            val shouldTransferCollateralToChild = MarginCalculator.getShouldTransferInCollateral(parser, subaccount = childSubaccount, trade)
            val shouldTransferOutRemainingCollateralFromChild = MarginCalculator.getShouldTransferOutRemainingCollateral(parser, subaccount = childSubaccount, trade)

            if (shouldTransferCollateralToChild) {
                val transferAmount = MarginCalculator.calculateIsolatedMarginTransferAmount(parser, trade, market, subaccount = childSubaccount) ?: 0.0
                transferAmountAppliedToParent = transferAmount * -1
                transferAmountAppliedToChild = transferAmount
            } else if (shouldTransferOutRemainingCollateralFromChild) {
                val remainingCollateral = MarginCalculator.getEstimateRemainingCollateralAfterClosePosition(parser, subaccount = childSubaccount, trade) ?: 0.0
                transferAmountAppliedToParent = remainingCollateral
            }

            val modifiedParentSubaccount = subaccountTransformer.applyTransferToSubaccount(
                subaccount,
                transfer = transferAmountAppliedToParent,
                parser,
                period,
            )
            modified.safeSet("subaccounts.$subaccountNumber", modifiedParentSubaccount)

            // when transfer out is true, post order position margin should be null
            val modifiedChildSubaccount = subaccountTransformer.applyTradeToSubaccount(
                childSubaccount,
                trade,
                market,
                parser,
                period,
                transferAmountAppliedToChild,
                isTransferOut = shouldTransferOutRemainingCollateralFromChild,
            )
            modified.safeSet("subaccounts.$childSubaccountNumber", modifiedChildSubaccount)

            return modified
        }

        return modified
    }
}
