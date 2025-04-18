package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalAccountState
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalSubaccountState
import exchange.dydx.abacus.state.InternalTradeInputState

internal class AccountTransformer(
    val parser: ParserProtocol,
    private val subaccountTransformer: SubaccountTransformer = SubaccountTransformer(parser)
) {
    fun applyTradeToAccount(
        account: InternalAccountState,
        subaccountNumber: Int,
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        period: CalculationPeriod,
    ) {
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

        val subaccount = account.subaccounts[subaccountNumber] ?: InternalSubaccountState(
            subaccountNumber = subaccountNumber,
        )
        account.subaccounts[subaccountNumber] = subaccount

        if (subaccountNumber == childSubaccountNumber) {
            // CROSS
            subaccountTransformer.applyTradeToSubaccount(
                subaccount = subaccount,
                trade = trade,
                market = market,
                period = period,
            )
        } else if (childSubaccountNumber != null) {
            val childSubaccount = account.subaccounts[childSubaccountNumber] ?: InternalSubaccountState(
                subaccountNumber = childSubaccountNumber,
            )
            account.subaccounts[childSubaccountNumber] = childSubaccount

            var transferAmountAppliedToParent = 0.0
            var transferAmountAppliedToChild = 0.0

            val shouldTransferCollateralToChild = MarginCalculator.getShouldTransferInCollateral(
                subaccount = childSubaccount,
                tradeInput = trade,
            )
            val shouldTransferOutRemainingCollateralFromChild =
                MarginCalculator.getShouldTransferOutRemainingCollateral(
                    subaccount = childSubaccount,
                    tradeInput = trade,
                )

            if (shouldTransferCollateralToChild) {
                val transferAmount = MarginCalculator.calculateIsolatedMarginTransferAmount(
                    trade = trade,
                    market = market,
                    subaccount = childSubaccount,
                ) ?: 0.0
                transferAmountAppliedToParent = transferAmount * -1
                transferAmountAppliedToChild = transferAmount
            } else if (shouldTransferOutRemainingCollateralFromChild) {
                val remainingCollateral =
                    MarginCalculator.getEstimateRemainingCollateralAfterClosePosition(
                        subaccount = childSubaccount,
                        tradeInput = trade,
                    ) ?: 0.0
                transferAmountAppliedToParent = remainingCollateral
            }
            subaccountTransformer.applyTransferToSubaccount(
                subaccount = subaccount,
                transfer = transferAmountAppliedToParent,
                period = period,
            )

            // when transfer out is true, post order position margin should be null
            subaccountTransformer.applyTradeToSubaccount(
                subaccount = childSubaccount,
                trade = trade,
                market = market,
                period = period,
                transfer = transferAmountAppliedToChild,
                isTransferOut = shouldTransferOutRemainingCollateralFromChild,
            )
        }
    }
}
