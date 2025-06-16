package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.SubaccountCalculator
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.InternalSubaccountCalculated
import exchange.dydx.abacus.state.InternalSubaccountState
import exchange.dydx.abacus.state.manager.BlockAndTime
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerFundingPaymentResponseObject
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionStatus
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.codegen.IndexerSubaccountResponseObject
import indexer.codegen.IndexerTransferResponseObject
import indexer.models.IndexerCompositeFillObject
import indexer.models.IndexerCompositeOrderObject

@Suppress("UNCHECKED_CAST")
internal open class SubaccountProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : BaseProcessor(parser) {
    internal val assetPositionsProcessor = AssetPositionsProcessor(parser)
    internal val ordersProcessor = OrdersProcessor(parser, localizer)
    private val perpetualPositionsProcessor = PerpetualPositionsProcessor(parser, localizer)
    private val fillsProcessor = FillsProcessor(parser, localizer)
    private val transfersProcessor = TransfersProcessor(parser, localizer)
    private val historicalPNLsProcessor = HistoricalPNLsProcessor(parser)
    private val fundingPaymentProcessor = FundingPaymentsProcessor(parser)
    private val subaccountCalculator = SubaccountCalculator(parser)

    internal fun processSubscribed(
        existing: InternalSubaccountState,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): InternalSubaccountState {
        return processSocket(existing, content, true, height)
    }

    internal fun processChannelData(
        existing: InternalSubaccountState,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): InternalSubaccountState {
        return processSocket(existing, content, false, height)
    }

    internal open fun processSocket(
        existing: InternalSubaccountState,
        content: Map<String, Any>,
        subscribed: Boolean,
        height: BlockAndTime?,
    ): InternalSubaccountState {
        var state = existing

        val subaccount = parser.asTypedObject<IndexerSubaccountResponseObject>(content["subaccount"])
        state = process(
            existing = state,
            payload = subaccount,
            firstTime = subscribed,
        )

        val assetPositions = parser.asTypedList<IndexerAssetPositionResponseObject>(content["assetPositions"])
        state = processAssetPositions(
            subaccount = state,
            payload = assetPositions,
        )

        val subaccountCalculated = state.calculated[CalculationPeriod.current] ?: InternalSubaccountCalculated()
        subaccountCalculated.quoteBalance = subaccountCalculator.calculateQuoteBalance(state.assetPositions)
        state.calculated[CalculationPeriod.current] = subaccountCalculated

        val fills = parser.asTypedList<IndexerCompositeFillObject>(content["fills"])
        state = processFills(
            subaccount = state,
            payload = fills,
            reset = false,
        )

        val orders = parser.asTypedList<IndexerCompositeOrderObject>(content["orders"])
        state = processOrders(
            subaccount = state,
            payload = orders,
            height = height,
        )

        val perpetualPositions = content["perpetualPositions"] ?: content["openPerpetualPositions"] ?: content["positions"]
        val positions = parser.asTypedList<IndexerPerpetualPositionResponseObject>(perpetualPositions)
        state = processPerpetualPositions(
            existing = state,
            payload = positions,
        )

        val transfers: List<IndexerTransferResponseObject>?
        val transferList = parser.asTypedList<IndexerTransferResponseObject>(content["transfers"])
        if (transferList != null) {
            transfers = transferList
        } else {
            val transfer = parser.asTypedObject<IndexerTransferResponseObject>(content["transfers"])
            transfers = if (transfer != null) listOf(transfer) else null
        }
        if (transfers != null) {
            state = processTransfers(
                subaccount = state,
                payload = transfers,
                reset = false,
            )
        }

        return state
    }

    internal open fun subaccountPayload(content: Map<String, Any>): Map<String, Any>? {
        return parser.asNativeMap(content["account"])
            ?: parser.asNativeMap(parser.asNativeList(content["accounts"])?.firstOrNull())
    }

    fun process(
        existing: InternalSubaccountState,
        payload: IndexerSubaccountResponseObject?,
        firstTime: Boolean,
    ): InternalSubaccountState {
        if (payload == null) {
            return existing
        }

        val subaccountNumber = parser.asInt(payload.subaccountNumber) ?: 0
        existing.subaccountNumber = subaccountNumber
        existing.address = payload.address

        val calculated = existing.calculated[CalculationPeriod.current] ?: InternalSubaccountCalculated()
        calculated.equity = parser.asDouble(payload.equity)
        calculated.freeCollateral = parser.asDouble(payload.freeCollateral)
        existing.calculated[CalculationPeriod.current] = calculated

        existing.marginEnabled = payload.marginEnabled
        existing.updatedAtHeight = payload.updatedAtHeight
        existing.latestProcessedBlockHeight = payload.latestProcessedBlockHeight

        if (firstTime) {
            existing.positions = perpetualPositionsProcessor.process(
                payload = payload.openPerpetualPositions,
            )
            existing.openPositions = existing.positions?.filterValues {
                it.status == IndexerPerpetualPositionStatus.OPEN
            }
            existing.assetPositions = assetPositionsProcessor.process(
                payload = payload.assetPositions,
            )
            existing.orders = null
        }

        val subaccountCalculated = existing.calculated[CalculationPeriod.current] ?: InternalSubaccountCalculated()
        subaccountCalculated.quoteBalance = subaccountCalculator.calculateQuoteBalance(existing.assetPositions)
        existing.calculated[CalculationPeriod.current] = subaccountCalculated

        return existing
    }

    private fun processOrders(
        subaccount: InternalSubaccountState,
        payload: List<IndexerCompositeOrderObject>?,
        height: BlockAndTime?,
    ): InternalSubaccountState {
        subaccount.orders = ordersProcessor.process(
            existing = subaccount.orders,
            payload = payload ?: emptyList(),
            subaccountNumber = subaccount.subaccountNumber,
            height = height,
        )
        return subaccount
    }

    fun updateHeight(
        existing: InternalSubaccountState,
        height: BlockAndTime?,
    ): Pair<InternalSubaccountState, Boolean> {
        if (existing.orders != null) {
            val (updatedOrders, updated) = ordersProcessor.updateHeight(
                existing = existing.orders,
                height = height,
            )
            if (updated) {
                existing.orders = updatedOrders
                return Pair(existing, true)
            } else {
                return Pair(existing, false)
            }
        } else {
            return Pair(existing, false)
        }
    }

    private fun processFills(
        subaccount: InternalSubaccountState,
        payload: List<IndexerCompositeFillObject>?,
        reset: Boolean,
    ): InternalSubaccountState {
        subaccount.fills = fillsProcessor.process(
            existing = if (reset) null else subaccount.fills,
            payload = payload ?: emptyList(),
            subaccountNumber = subaccount.subaccountNumber,
        )
        return subaccount
    }

    private fun processTransfers(
        subaccount: InternalSubaccountState,
        payload: List<IndexerTransferResponseObject>?,
        reset: Boolean,
    ): InternalSubaccountState {
        subaccount.transfers = transfersProcessor.process(
            existing = if (reset) null else subaccount.transfers,
            payload = payload ?: emptyList(),
        )
        return subaccount
    }

    private fun processAssetPositions(
        subaccount: InternalSubaccountState,
        payload: List<IndexerAssetPositionResponseObject>?,
    ): InternalSubaccountState {
        subaccount.assetPositions = assetPositionsProcessor.processChanges(
            existing = subaccount.assetPositions,
            payload = payload,
        )
        return subaccount
    }

    private fun processPerpetualPositions(
        existing: InternalSubaccountState,
        payload: List<IndexerPerpetualPositionResponseObject>?,
    ): InternalSubaccountState {
        existing.positions = perpetualPositionsProcessor.processChanges(
            existing = existing.positions,
            payload = payload,
        )
        existing.openPositions = existing.positions?.filterValues {
            it.status == IndexerPerpetualPositionStatus.OPEN
        }
        return existing
    }

    internal fun processsHistoricalPNLs(
        existing: InternalSubaccountState,
        payload: List<IndexerPnlTicksResponseObject>?,
    ): InternalSubaccountState {
        val newHistoricalPNLs = historicalPNLsProcessor.process(
            existing = existing.historicalPNLs,
            payload = payload ?: emptyList(),
        )
        if (existing.historicalPNLs != newHistoricalPNLs) {
            existing.historicalPNLs = newHistoricalPNLs
        }
        return existing
    }

    internal fun processFundingPayments(
        existing: InternalSubaccountState,
        payload: List<IndexerFundingPaymentResponseObject>?,
    ): InternalSubaccountState {
        val newFundingPayments = fundingPaymentProcessor.process(
            existing = existing.fundingPayments,
            payload = payload ?: emptyList(),
        )
        if (existing.fundingPayments != newFundingPayments) {
            existing.fundingPayments = newFundingPayments
        }
        return existing
    }

    internal fun processFills(
        existing: InternalSubaccountState,
        payload: List<IndexerCompositeFillObject>?,
    ): InternalSubaccountState {
        return processFills(existing, payload, true)
    }

    fun processTransfers(
        existing: InternalSubaccountState,
        payload: List<IndexerTransferResponseObject>?,
    ): InternalSubaccountState {
        return processTransfers(existing, payload, true)
    }

    fun orderCanceled(
        existing: InternalSubaccountState,
        orderId: String,
    ): Pair<InternalSubaccountState, Boolean> {
        if (existing.orders != null) {
            val (modifiedOrders, updated) = ordersProcessor.canceled(existing.orders, orderId)
            if (updated) {
                val modified = existing.copy(orders = modifiedOrders)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    override fun accountAddressChanged() {
        super.accountAddressChanged()
        transfersProcessor.accountAddress = accountAddress
    }
}
