package exchange.dydx.abacus.calculator.v2

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInputOptions
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInputSummary
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.IsolatedMarginInputType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAdjustIsolatedMarginInputState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.utils.MARGIN_COLLATERALIZATION_CHECK_BUFFER
import exchange.dydx.abacus.utils.MAX_LEVERAGE_BUFFER_PERCENT
import exchange.dydx.abacus.utils.Numeric
import kotlin.math.max
import kotlin.math.min

internal class AdjustIsolatedMarginInputCalculatorV2(
    private val parser: ParserProtocol,
    private val subaccountTransformer: SubaccountTransformerV2 = SubaccountTransformerV2(parser)
) {
    fun calculate(
        adjustIsolatedMargin: InternalAdjustIsolatedMarginInputState,
        walletState: InternalWalletState,
        markets: Map<String, InternalMarketState>?,
        parentSubaccountNumber: Int?,
    ): InternalAdjustIsolatedMarginInputState {
        val market = markets?.get(adjustIsolatedMargin.market) ?: return adjustIsolatedMargin

        if (walletState.isAccountConnected &&
            (adjustIsolatedMargin.amount != null || adjustIsolatedMargin.amountPercent != null || adjustIsolatedMargin.market != null)
        ) {
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

            calculateAmounts(
                adjustIsolatedMargin = adjustIsolatedMargin,
                parentSubaccount = walletState.account.subaccounts[parentSubaccountNumber],
                childSubaccount = walletState.account.subaccounts[childSubaccountNumber],
                market = market,
                type = type,
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

    private fun calculateAmounts(
        adjustIsolatedMargin: InternalAdjustIsolatedMarginInputState,
        parentSubaccount: InternalSubaccountState?,
        childSubaccount: InternalSubaccountState?,
        market: InternalMarketState,
        type: IsolatedMarginAdjustmentType,
    ): InternalAdjustIsolatedMarginInputState {
        val modified = adjustIsolatedMargin
        val inputType = modified.amountInput

        if (inputType != null) {
            val notionalTotal = childSubaccount?.calculated?.get(CalculationPeriod.current)?.notionalTotal ?: return modified
            val equity = childSubaccount.calculated[CalculationPeriod.current]?.equity
            val availableCollateralToTransfer = parentSubaccount?.calculated?.get(CalculationPeriod.current)?.freeCollateral

            val baseAmount = when (type) {
                IsolatedMarginAdjustmentType.Add -> availableCollateralToTransfer
                IsolatedMarginAdjustmentType.Remove -> equity
            }

            val amountPercent = modified.amountPercent
            val amountValue = modified.amount

            val maxMarketLeverage = market.perpetualMarket?.configs?.maxMarketLeverage ?: Numeric.double.ONE

            when (inputType) {
                IsolatedMarginInputType.Amount -> {
                    if (baseAmount != null && baseAmount > Numeric.double.ZERO && amountValue != null) {
                        when (type) {
                            IsolatedMarginAdjustmentType.Add -> {
                                modified.amountPercent = amountValue / baseAmount
                            }
                            IsolatedMarginAdjustmentType.Remove -> {
                                val maxRemovableAmount = baseAmount - notionalTotal / (maxMarketLeverage * MAX_LEVERAGE_BUFFER_PERCENT)
                                if (maxRemovableAmount > Numeric.double.ZERO) {
                                    modified.amountPercent = amountValue / maxRemovableAmount
                                } else {
                                    modified.amountPercent = null
                                }
                            }
                        }
                    } else {
                        modified.amountPercent = null
                    }
                }
                IsolatedMarginInputType.Percent -> {
                    if (baseAmount != null && baseAmount >= Numeric.double.ZERO && amountPercent != null) {
                        when (type) {
                            IsolatedMarginAdjustmentType.Add -> {
                                // The amount to add is a percentage of all add-able margin (your parent subaccount's free collateral)
                                val amount = baseAmount * amountPercent
                                // We leave behind MARGIN_COLLATERALIZATION_CHECK_BUFFER to pass collateralization checks
                                modified.amount = min(max(baseAmount - MARGIN_COLLATERALIZATION_CHECK_BUFFER, 0.0), amount)
                            }
                            IsolatedMarginAdjustmentType.Remove -> {
                                // The amount to remove is a percentage of all remov-able margin (100% puts you at the market's max leveage)
                                // leverage = notional total / equity
                                // marketMaxLeverage = notional total / (currentEquity - amount)
                                // amount = currentEquity - notionalTotal / marketMaxLeverage
                                val amountToRemove = baseAmount - notionalTotal / (maxMarketLeverage * MAX_LEVERAGE_BUFFER_PERCENT)
                                if (amountToRemove >= Numeric.double.ZERO) {
                                    modified.amount = amountToRemove * amountPercent
                                } else {
                                    modified.amount = null
                                }
                            }
                        }
                    } else {
                        modified.amount = null
                    }
                }
            }
        }
        return modified
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
