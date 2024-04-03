package exchange.dydx.abacus.inputs

import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import kollections.JsExport

data class AdjustMarginLeverageInputSummary(
    val crossFreeCollateral: Double,
    val crossMarginUsage: Double,
    val positionMargin: Double,
    val positionLeverage: Double,
    val liquidationPrice: Double,
)

@JsExport
object AdjustMarginLeverageInput {
    /**
     * @description Calculate the new margin and leverage for the parent subaccount
     */
    fun calculateParentSubaccount(
        amount: Double,
        parentSubaccount: Subaccount,
    ): Subaccount {

        return Subaccount(
            subaccountNumber = parentSubaccount.subaccountNumber,
            positionId = parentSubaccount.positionId,
            pnlTotal = parentSubaccount.pnlTotal,
            pnl24h = parentSubaccount.pnl24h,
            pnl24hPercent = parentSubaccount.pnl24hPercent,
            quoteBalance = parentSubaccount.quoteBalance,
            notionalTotal = parentSubaccount.notionalTotal,
            valueTotal = parentSubaccount.valueTotal,
            initialRiskTotal = parentSubaccount.initialRiskTotal,
            adjustedImf = parentSubaccount.adjustedImf,
            equity = parentSubaccount.equity,
            freeCollateral = parentSubaccount.freeCollateral,
            leverage = parentSubaccount.leverage,
            marginUsage = parentSubaccount.marginUsage,
            buyingPower = parentSubaccount.buyingPower,
            openPositions = parentSubaccount.openPositions,
            pendingPositions = parentSubaccount.pendingPositions,
            orders = parentSubaccount.orders,
            marginEnabled = parentSubaccount.marginEnabled,
        )
    }

    /**
     * @description Calculate updated SubaccountPosition after adjusting margin
     */
    fun calculatePosition(
        amount: Double,
        position: SubaccountPosition,
    ): SubaccountPosition {

        return SubaccountPosition(
            id = position.id,
            assetId = position.assetId,
            side = position.side,
            entryPrice = position.entryPrice,
            exitPrice = position.exitPrice,
            createdAtMilliseconds = position.createdAtMilliseconds,
            closedAtMilliseconds = position.closedAtMilliseconds,
            netFunding = position.netFunding,
            realizedPnl = position.realizedPnl,
            realizedPnlPercent = position.realizedPnlPercent,
            unrealizedPnl = position.unrealizedPnl,
            unrealizedPnlPercent = position.unrealizedPnlPercent,
            size = position.size,
            notionalTotal = position.notionalTotal,
            valueTotal = position.valueTotal,
            initialRiskTotal = position.initialRiskTotal,
            adjustedImf = position.adjustedImf,
            adjustedMmf = position.adjustedMmf,
            leverage = position.leverage, // edit
            maxLeverage = position.maxLeverage,
            buyingPower = position.buyingPower, // edit
            liquidationPrice = position.liquidationPrice, // edit
            resources = position.resources,
            childSubaccountNumber = position.childSubaccountNumber,
            freeCollateral = position.freeCollateral, // edit
            quoteBalance = position.quoteBalance,
            equity = position.equity,
        )
    }

    fun getAdjustIsolatedMarginPayload(
        amount: String,
        address: String,
        position: SubaccountPosition,
        parentSubaccount: Subaccount,
    ): HumanReadableSubaccountTransferPayload {
        if (position.childSubaccountNumber == null) {
            error("SubaccountPosition missing childSubaccountNumber. Required for isolated margin transfer.")
        }

        return HumanReadableSubaccountTransferPayload(
            subaccountNumber = parentSubaccount.subaccountNumber,
            amount,
            destinationAddress = address,
            destinationSubaccountNumber = position.childSubaccountNumber,
        )
    }

    fun getSummary(
        amount: Double,
        position: SubaccountPosition,
        parentSubaccount: Subaccount,
    ): AdjustMarginLeverageInputSummary {
        val parentSubaccountDiff = calculateParentSubaccount(amount, parentSubaccount)
        val positionDiff = calculatePosition(amount, position)

        return AdjustMarginLeverageInputSummary(
            crossFreeCollateral = 0.0,
            crossMarginUsage = 0.0,
            positionMargin = 0.0,
            positionLeverage = 0.0,
            liquidationPrice = 0.0,
        )
    }
}