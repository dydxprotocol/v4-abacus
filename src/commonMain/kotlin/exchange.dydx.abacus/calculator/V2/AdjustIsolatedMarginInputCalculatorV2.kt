package exchange.dydx.abacus.calculator.v2

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInputOptions
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInputSummary
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAdjustIsolatedMarginInputState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.utils.Numeric

internal class AdjustIsolatedMarginInputCalculatorV2(
    private val parser: ParserProtocol,
    private val subaccountTransformer: SubaccountTransformerV2 = SubaccountTransformerV2(parser)
) {
    fun calculate(
        adjustIsolatedMargin: InternalAdjustIsolatedMarginInputState,
        walletState: InternalWalletState,
        parentSubaccountNumber: Int?,
    ): InternalAdjustIsolatedMarginInputState {
        if (walletState.isAccountConnected && adjustIsolatedMargin.amount != null) {
            val type = adjustIsolatedMargin.type ?: IsolatedMarginAdjustmentType.Add
            val childSubaccountNumber = adjustIsolatedMargin.childSubaccountNumber

            val parentTransferDelta = getModifiedTransferDelta(
                isolatedMarginAdjustment = adjustIsolatedMargin,
                isParentSubaccount = true,
            )
            val childTransferDelta = getModifiedTransferDelta(
                isolatedMarginAdjustment = adjustIsolatedMargin,
                isParentSubaccount = false,
            )

            subaccountTransformer.applyIsolatedMarginAdjustmentToWallet(
                wallet = walletState,
                subaccountNumber = parentSubaccountNumber,
                delta = parentTransferDelta,
                period = CalculationPeriod.post,
            )

            subaccountTransformer.applyIsolatedMarginAdjustmentToWallet(
                wallet = walletState,
                subaccountNumber = childSubaccountNumber,
                delta = childTransferDelta,
                period = CalculationPeriod.post,
            )

            adjustIsolatedMargin.options = AdjustIsolatedMarginInputOptions(
                needsSize = true,
            )
            adjustIsolatedMargin.summary = summaryForType(
                parentSubaccount = walletState.account.subaccounts[parentSubaccountNumber],
                childSubaccount = walletState.account.subaccounts[childSubaccountNumber],
            )
        }
        return adjustIsolatedMargin
    }

    private fun getModifiedTransferDelta(
        isolatedMarginAdjustment: InternalAdjustIsolatedMarginInputState,
        isParentSubaccount: Boolean,
    ): Delta {
        val type = isolatedMarginAdjustment.type ?: IsolatedMarginAdjustmentType.Add
        val amount = isolatedMarginAdjustment.amount

        when (type) {
            IsolatedMarginAdjustmentType.Add -> {
                val multiplier =
                    if (isParentSubaccount) Numeric.double.NEGATIVE else Numeric.double.POSITIVE
                val usdcSize = (amount ?: Numeric.double.ZERO) * multiplier

                return Delta(usdcSize = usdcSize)
            }

            IsolatedMarginAdjustmentType.Remove -> {
                val multiplier =
                    if (isParentSubaccount) Numeric.double.POSITIVE else Numeric.double.NEGATIVE
                val usdcSize = (amount ?: Numeric.double.ZERO) * multiplier

                return Delta(usdcSize = usdcSize)
            }
        }
    }

    private fun summaryForType(
        parentSubaccount: InternalSubaccountState?,
        childSubaccount: InternalSubaccountState?,
    ): AdjustIsolatedMarginInputSummary {
        val openPositions = childSubaccount?.openPositions
        val marketId = openPositions?.keys?.firstOrNull()
        val position = openPositions?.get(marketId)

        return AdjustIsolatedMarginInputSummary(
            crossFreeCollateral = parentSubaccount?.calculated?.get(CalculationPeriod.current)?.freeCollateral,
            crossFreeCollateralUpdated = parentSubaccount?.calculated?.get(CalculationPeriod.post)?.freeCollateral,
            crossMarginUsage = parentSubaccount?.calculated?.get(CalculationPeriod.current)?.marginUsage,
            crossMarginUsageUpdated = parentSubaccount?.calculated?.get(CalculationPeriod.post)?.marginUsage,
            positionMargin = position?.calculated?.get(CalculationPeriod.current)?.marginValue,
            positionMarginUpdated = position?.calculated?.get(CalculationPeriod.post)?.marginValue,
            positionLeverage = position?.calculated?.get(CalculationPeriod.current)?.leverage,
            positionLeverageUpdated = position?.calculated?.get(CalculationPeriod.post)?.leverage,
            liquidationPrice = position?.calculated?.get(CalculationPeriod.current)?.liquidationPrice,
            liquidationPriceUpdated = position?.calculated?.get(CalculationPeriod.post)?.liquidationPrice,
        )
    }
}
