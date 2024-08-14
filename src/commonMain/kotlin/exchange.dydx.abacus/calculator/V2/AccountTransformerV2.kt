package exchange.dydx.abacus.calculator.v2

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.SubaccountTransformer
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.utils.safeSet

internal class AccountTransformerV2(
    val parser: ParserProtocol,
    private val subaccountTransformer: SubaccountTransformerV2 = SubaccountTransformerV2()
) {
    fun applyTradeToAccount(
        account: InternalAccountState,
        subaccountNumber: Int,
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        period: CalculationPeriod,
    )  {
        val childSubaccountNumber = if (trade.marginMode == MarginMode.Isolated) {
            val marketId = trade.marketId
            if (marketId != null) {
                MarginCalculator.getChildSubaccountNumberForIsolatedMarginTrade(
                    parser = parser,
                    subaccounts = account.subaccounts,
                    subaccountNumber = subaccountNumber,
                    marketId = marketId,
                )
            } else {
                null
            }
        } else {
            subaccountNumber
        }

    if (subaccountNumber == childSubaccountNumber) {
        // CROSS
        val subaccount = account.subaccounts[subaccountNumber]
        subaccountTransformer.applyTradeToSubaccount(
                subaccount = subaccount,
                trade = trade,
                market = market,
                period = period,
            )
    } else if (childSubaccountNumber != null) {
        val childSubaccount = account.subaccounts[childSubaccountNumber]

        var transferAmountAppliedToParent = 0.0
        var transferAmountAppliedToChild = 0.0

        val shouldTransferCollateralToChild = MarginCalculator.getShouldTransferInCollateral(
            trade = trade,
            subaccount = childSubaccount,
        )
        val shouldTransferOutRemainingCollateralFromChild =
            MarginCalculator.getShouldTransferOutRemainingCollateral(
                parser,
                subaccount = childSubaccount,
                trade
            )

        if (shouldTransferCollateralToChild) {
            val transferAmount = MarginCalculator.calculateIsolatedMarginTransferAmount(
                parser,
                trade,
                market,
                subaccount = childSubaccount
            ) ?: 0.0
            transferAmountAppliedToParent = transferAmount * -1
            transferAmountAppliedToChild = transferAmount
        } else if (shouldTransferOutRemainingCollateralFromChild) {
            val remainingCollateral =
                MarginCalculator.getEstimateRemainingCollateralAfterClosePosition(
                    parser,
                    subaccount = childSubaccount,
                    trade
                ) ?: 0.0
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

}