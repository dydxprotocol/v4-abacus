package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.state.InternalPerpetualPendingPosition
import exchange.dydx.abacus.state.InternalPerpetualPosition
import exchange.dydx.abacus.state.InternalSubaccountState
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kollections.toIList
import kotlinx.serialization.Serializable

/*
ethereumeAddress is passed in from client. All other fields
are filled when socket v4_subaccounts channel is subscribed
*/
@JsExport
@Serializable
data class Subaccount(
    val subaccountNumber: Int,
    val positionId: String?,
    val pnlTotal: Double?,
    val pnl24h: Double?,
    val pnl24hPercent: Double?,
    val quoteBalance: TradeStatesWithDoubleValues?,
    val notionalTotal: TradeStatesWithDoubleValues?,
    val valueTotal: TradeStatesWithDoubleValues?,
    val initialRiskTotal: TradeStatesWithDoubleValues?,
    val adjustedImf: TradeStatesWithDoubleValues?,
    val equity: TradeStatesWithDoubleValues?,
    val freeCollateral: TradeStatesWithDoubleValues?,
    val leverage: TradeStatesWithDoubleValues?,
    val marginUsage: TradeStatesWithDoubleValues?,
    val buyingPower: TradeStatesWithDoubleValues?,
    val openPositions: IList<SubaccountPosition>?,
    val pendingPositions: IList<SubaccountPendingPosition>?,
    val orders: IList<SubaccountOrder>?,
    val marginEnabled: Boolean?,
) {
    companion object {
        internal fun create(
            existing: Subaccount?,
            internalState: InternalSubaccountState?,
        ): Subaccount? {
            Logger.d { "creating Account\n" }

            if (internalState == null) {
                Logger.d { "Internal state is null" }
                return null
            }

            val positionId = null
            val pnlTotal = null
            val pnl24h = null
            val pnl24hPercent = null

            val subaccountNumber =
                internalState?.subaccountNumber ?: 0

            val quoteBalance =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.quoteBalance,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.quoteBalance,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.quoteBalance,
                )

            val notionalTotal =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.notionalTotal,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.notionalTotal,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.notionalTotal,
                )

            val valueTotal =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.valueTotal,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.valueTotal,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.valueTotal,
                )

            val initialRiskTotal =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.initialRiskTotal,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.initialRiskTotal,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.initialRiskTotal,
                )

            val adjustedImf =
                // This is not being set at the subaccount level
                TradeStatesWithDoubleValues(
                    current = null,
                    postOrder = null,
                    postAllOrders = null,
                )

            val equity =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.equity,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.equity,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.equity,
                )

            val freeCollateral =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.freeCollateral,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.freeCollateral,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.freeCollateral,
                )

            val leverage =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.leverage,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.leverage,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.leverage,
                )

            val marginUsage =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.marginUsage,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.marginUsage,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.marginUsage,
                )

            val buyingPower =
                TradeStatesWithDoubleValues(
                    current = internalState?.calculated?.get(CalculationPeriod.current)?.buyingPower,
                    postOrder = internalState?.calculated?.get(CalculationPeriod.post)?.buyingPower,
                    postAllOrders = internalState?.calculated?.get(CalculationPeriod.settled)?.buyingPower,
                )

            val openPositions =
                createOpenPositions(
                    existing = existing?.openPositions,
                    openPositions = internalState?.openPositions,
                )

            val pendingPositions =
                createPendingPositions(
                    existing = existing?.pendingPositions,
                    pendingPositions = internalState?.pendingPositions,
                )

            val orders = internalState?.orders?.toIList()

            val marginEnabled =
                internalState?.marginEnabled ?: true

            return if (existing?.subaccountNumber != subaccountNumber ||
                existing.positionId != positionId ||
                existing.pnlTotal != pnlTotal ||
                existing.pnl24h != pnl24h ||
                existing.pnl24hPercent != pnl24hPercent ||
                existing.quoteBalance !== quoteBalance ||
                existing.notionalTotal !== notionalTotal ||
                existing.valueTotal !== valueTotal ||
                existing.initialRiskTotal !== initialRiskTotal ||
                existing.adjustedImf !== adjustedImf ||
                existing.equity !== equity ||
                existing.freeCollateral !== freeCollateral ||
                existing.leverage !== leverage ||
                existing.marginUsage !== marginUsage ||
                existing.buyingPower !== buyingPower ||
                existing.openPositions != openPositions ||
                existing.pendingPositions != pendingPositions ||
                existing.orders != orders ||
                existing.marginEnabled != marginEnabled
            ) {
                Subaccount(
                    subaccountNumber = subaccountNumber,
                    positionId = positionId,
                    pnlTotal = pnlTotal,
                    pnl24h = pnl24h,
                    pnl24hPercent = pnl24hPercent,
                    quoteBalance = quoteBalance,
                    notionalTotal = notionalTotal,
                    valueTotal = valueTotal,
                    initialRiskTotal = initialRiskTotal,
                    adjustedImf = adjustedImf,
                    equity = equity,
                    freeCollateral = freeCollateral,
                    leverage = leverage,
                    marginUsage = marginUsage,
                    buyingPower = buyingPower,
                    openPositions = openPositions,
                    pendingPositions = pendingPositions,
                    orders = orders,
                    marginEnabled = marginEnabled,
                )
            } else {
                existing
            }
        }

        private fun createOpenPositions(
            existing: IList<SubaccountPosition>?,
            openPositions: Map<String, InternalPerpetualPosition>?,
        ): IList<SubaccountPosition>? {
            val newEntries: MutableList<SubaccountPosition> = mutableListOf()
            for ((key, value) in openPositions?.entries ?: emptySet()) {
                val position = SubaccountPosition.create(
                    existing = null,
                    positionId = key,
                    position = value,
                )
                if (position != null) {
                    newEntries.add(position)
                }
            }
            newEntries.sortByDescending { it.createdAtMilliseconds }
            if (newEntries.isEmpty()) {
                return null
            }
            return if (newEntries != existing) {
                newEntries.toIList()
            } else {
                existing
            }
        }

        private fun createPendingPositions(
            existing: IList<SubaccountPendingPosition>?,
            pendingPositions: List<InternalPerpetualPendingPosition>?,
        ): IList<SubaccountPendingPosition>? {
            val newEntries: MutableList<SubaccountPendingPosition> = mutableListOf()
            for (position in pendingPositions ?: emptyList()) {
                val pendingPosition = SubaccountPendingPosition.create(
                    existing = null,
                    internalState = position,
                )
                if (pendingPosition != null) {
                    newEntries.add(pendingPosition)
                }
            }

            return if (newEntries != existing) {
                newEntries.toIList()
            } else {
                existing
            }
        }

        /*
        private fun transfers(
            parser: ParserProtocol,
            existing: IList<SubaccountTransfer>?,
            data: List<*>?,
        ): IList<SubaccountTransfer>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountTransfer).updatedAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["confirmedAt"])?.toEpochMilliseconds()?.toDouble()
                        ?: parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()
                            ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                SubaccountTransfer.create(
                    null,
                    parser,
                    parser.asMap(itemData),
                )
            }, true)?.toIList()
        }

        private fun fundingPayments(
            parser: ParserProtocol,
            existing: IList<SubaccountFundingPayment>?,
            data: List<*>?,
        ): IList<SubaccountFundingPayment>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFundingPayment).effectiveAtMilliSeconds
                val time2 =
                    parser.asDatetime(itemData["effectiveAt"])?.toEpochMilliseconds()?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: SubaccountFundingPayment.create(null, parser, itemData)
            }, true)?.toIList()
        }
         */
    }
}
