package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.v2.SubaccountCalculatorV2
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerFillResponseObject
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.codegen.IndexerSubaccountResponseObject
import indexer.codegen.IndexerTransferResponseObject
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
    private val fundingPaymentsProcessor = FundingPaymentsProcessor(parser)
    private val historicalPNLsProcessor = HistoricalPNLsProcessor(parser)
    private val subaccountCalculator = SubaccountCalculatorV2(parser)

    private val accountKeyMap = mapOf(
        "string" to mapOf(
            "id" to "id",
            "accountNumber" to "accountNumber",
            "starkKey" to "starkKey",
            "positionId" to "positionId",
        ),
        "int" to mapOf(
            "subaccountNumber" to "subaccountNumber",
        ),
        "double" to mapOf(
            "pendingDeposits" to "pendingDeposits",
            "pendingWithdrawals" to "pendingWithdrawals",
        ),
        "datetime" to mapOf(
            "createdAt" to "createdAt",
        ),
        "bool" to mapOf(
            "marginEnabled" to "marginEnabled",
        ),
    )

    private val currentAccountKeyMap = mapOf(
        "double" to mapOf(
            "equity" to "equity",
            "freeCollateral" to "freeCollateral",
            "quoteBalance" to "quoteBalance",
        ),
    )

    internal fun processSubscribed(
        existing: InternalSubaccountState,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): InternalSubaccountState {
        return processSocket(existing, content, true, height)
    }

    internal fun subscribedDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any> {
        return socketDeprecated(existing, content, true, height)
    }

    internal fun processChannelData(
        existing: InternalSubaccountState,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): InternalSubaccountState {
        return processSocket(existing, content, false, height)
    }

    @Suppress("FunctionName")
    internal fun channel_dataDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any> {
        return socketDeprecated(existing, content, false, height)
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
        state.quoteBalance[CalculationPeriod.current] = subaccountCalculator.calculateQuoteBalance(state.assetPositions)

        val fills = parser.asTypedList<IndexerFillResponseObject>(content["fills"])
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

        val perpetualPositions = content["perpetualPositions"] ?: content["openPerpetualPositions"]
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

    internal open fun socketDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        subscribed: Boolean,
        height: BlockAndTime?,
    ): Map<String, Any> {
        var subaccount = existing ?: mutableMapOf()
        val accountPayload = subaccountPayload(content)
        if (accountPayload != null) {
            subaccount = receivedDeprecated(subaccount, accountPayload, subscribed)
        }

        val ordersPayload = parser.asNativeList(content["orders"]) as? List<Map<String, Any>>
        if (ordersPayload != null) {
            subaccount = receivedOrders(subaccount, ordersPayload, height)
        }

        val perpetualPositionsPayload =
            parser.asNativeList(content["positions"])
                ?: parser.asNativeList(content["perpetualPositions"])
        if (perpetualPositionsPayload != null) {
            subaccount = receivedPerpetualPositionsDeprecated(subaccount, perpetualPositionsPayload)
        }

        val fillsPayload = parser.asNativeList(content["fills"])
        if (fillsPayload != null) {
            subaccount = receivedFillsDeprecated(subaccount, fillsPayload, false)
        }

        val transfersPayload = content["transfers"]
        val transfersPayloadList = if (transfersPayload != null) {
            parser.asNativeList(transfersPayload)
                ?: parser.asNativeList(listOf(parser.asNativeMap(transfersPayload)))
        } else {
            null
        }

        if (transfersPayloadList != null) {
            subaccount = receivedTransfersDeprecated(subaccount, transfersPayloadList, false)
        }

        val fundingPaymentsPayload =
            parser.asNativeList(content["fundingPayments"]) as? List<Map<String, Any>>
        if (fundingPaymentsPayload != null) {
            subaccount = receivedFundingPayments(subaccount, fundingPaymentsPayload, false)
        }

        return subaccount
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

        val modified = existing
        val subaccountNumber = parser.asInt(payload.subaccountNumber) ?: 0
        modified.subaccountNumber = subaccountNumber
        modified.address = payload.address
        modified.equity = payload.equity
        modified.freeCollateral = payload.freeCollateral
        modified.marginEnabled = payload.marginEnabled
        modified.updatedAtHeight = payload.updatedAtHeight
        modified.latestProcessedBlockHeight = payload.latestProcessedBlockHeight

        if (firstTime) {
            modified.positions = perpetualPositionsProcessor.process(
                payload = payload.openPerpetualPositions,
            )
            modified.assetPositions = assetPositionsProcessor.process(
                payload = payload.assetPositions,
            )
            modified.quoteBalance[CalculationPeriod.current] = subaccountCalculator.calculateQuoteBalance(modified.assetPositions)

            modified.orders = null
        }

        return modified
    }

    internal fun receivedDeprecated(
        subaccount: Map<String, Any>?,
        payload: Map<String, Any>,
        firstTime: Boolean,
    ): Map<String, Any> {
        var modified = transform(subaccount, payload, accountKeyMap)
        modified = transform(modified, payload, "current", currentAccountKeyMap)

        if (firstTime) {
            val subaccountNumber = parser.asInt(modified["subaccountNumber"])
            val openPerpetualPositionsData =
                (
                    parser.asNativeMap(payload["openPositions"])
                        ?: parser.asNativeMap(payload["openPerpetualPositions"])
                    )

            val positions = perpetualPositionsProcessor.received(openPerpetualPositionsData, subaccountNumber)
            modified.safeSet(
                "positions",
                positions,
            )
            val openPositions = positions?.filterValues {
                val data = parser.asNativeMap(it)
                parser.asString(data?.get("status")) == "OPEN"
            }
            modified.safeSet(
                "openPositions",
                openPositions,
            )

            val assetPositionsData = parser.asNativeMap(payload["assetPositions"])
            modified.safeSet("assetPositions", assetPositionsProcessor.receivedDeprecated(assetPositionsData))

            modified.remove("orders")
        } else {
            val assetPositionsPayload = payload["assetPositions"] as? List<Map<String, Any>>
            if (assetPositionsPayload != null) {
                modified = receivedAssetPositionsDeprecated(modified, assetPositionsPayload).mutable()
            }
        }
        modified["quoteBalance"] = calculateQuoteBalanceDeprecated(modified, payload)

        return modified
    }

    internal fun calculateQuoteBalanceDeprecated(
        subaccount: Map<String, Any>,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val quoteBalance = parser.asNativeMap(subaccount["quoteBalance"])?.mutable()
            ?: mutableMapOf<String, Any>()
        parser.asDouble(payload["quoteBalance"])?.let {
            quoteBalance["current"] = it
        }
        val derivedQuoteBalance =
            deriveQuoteBalance(parser.asNativeMap(subaccount["assetPositions"]))
        if (derivedQuoteBalance != null) {
            quoteBalance["current"] = derivedQuoteBalance
        }
        return quoteBalance
    }

    private fun deriveQuoteBalance(assetPositions: Map<String, Any>?): Double? {
        val usdc = parser.asNativeMap(assetPositions?.get("USDC"))
        return if (usdc != null) {
            val size = parser.asDouble(usdc["size"])
            if (size != null) {
                val side = parser.asString(usdc["side"])
                if (side == "LONG") size else size * -1.0
            } else {
                null
            }
        } else {
            null
        }
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

    private fun receivedOrders(
        subaccount: Map<String, Any>,
        payload: List<Any>?,
        height: BlockAndTime?,
    ): Map<String, Any> {
        val subaccountNumber = parser.asInt(subaccount["subaccountNumber"])
        return if (payload != null) {
            val modified = subaccount.mutable()
            val transformed = ordersProcessor.received(
                parser.asNativeMap(subaccount["orders"]),
                payload,
                height,
                subaccountNumber,
            )
            modified.safeSet("orders", transformed)
            modified
        } else {
            subaccount
        }
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

    internal fun updateHeightDeprecated(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        val orders = parser.asNativeMap(existing["orders"])
        if (orders != null) {
            val (updatedOrders, updated) = ordersProcessor.updateHeightDeprecated(
                orders,
                height,
            )
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet("orders", updatedOrders)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    private fun processFills(
        subaccount: InternalSubaccountState,
        payload: List<IndexerFillResponseObject>?,
        reset: Boolean,
    ): InternalSubaccountState {
        subaccount.fills = fillsProcessor.process(
            existing = if (reset) null else subaccount.fills,
            payload = payload ?: emptyList(),
            subaccountNumber = subaccount.subaccountNumber,
        )
        return subaccount
    }

    private fun receivedFillsDeprecated(
        subaccount: Map<String, Any>,
        payload: List<Any>?,
        reset: Boolean,
    ): Map<String, Any> {
        val subaccountNumber = parser.asInt(subaccount["subaccountNumber"]) ?: 0
        return receivedObject(subaccount, "fills", payload) { existing, payload ->
            parser.asNativeList(payload)?.let {
                fillsProcessor.receivedDeprecated(if (reset) null else parser.asNativeList(existing), it, subaccountNumber)
            }
        } ?: subaccount
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

    private fun receivedTransfersDeprecated(
        subaccount: Map<String, Any>,
        payload: List<Any>?,
        reset: Boolean,
    ): Map<String, Any> {
        return receivedObject(subaccount, "transfers", payload) { existing, payload ->
            parser.asNativeList(payload)?.let {
                transfersProcessor.received(if (reset) null else parser.asNativeList(existing), it)
            }
        } ?: subaccount
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

    private fun receivedFundingPayments(
        subaccount: Map<String, Any>,
        payload: List<Any>?,
        reset: Boolean,
    ): Map<String, Any> {
        return receivedObject(subaccount, "fundingPayments", payload) { existing, payload ->
            parser.asNativeList(payload)?.let {
                fundingPaymentsProcessor.received(
                    if (reset) null else parser.asNativeList(existing),
                    it,
                )
            }
        } ?: subaccount
    }

    private fun processPerpetualPositions(
        existing: InternalSubaccountState,
        payload: List<IndexerPerpetualPositionResponseObject>?,
    ): InternalSubaccountState {
        existing.positions = perpetualPositionsProcessor.processChanges(
            existing = existing.positions,
            payload = payload,
        )
        return existing
    }

    private fun receivedPerpetualPositionsDeprecated(
        subaccount: Map<String, Any>,
        payload: List<Any>?,
    ): Map<String, Any> {
        return if (payload != null) {
            val modified = subaccount.mutable()
            val positions = perpetualPositionsProcessor.receivedChangesDeprecated(
                parser.asNativeMap(subaccount["positions"]),
                payload,
            )
            modified.safeSet("positions", positions)
            val openPositions = positions?.filterValues {
                val data = parser.asNativeMap(it)
                parser.asString(data?.get("status")) == "OPEN"
            }
            modified.safeSet(
                "openPositions",
                openPositions,
            )
            modified
        } else {
            subaccount
        }
    }

    private fun receivedAssetPositionsDeprecated(
        subaccount: Map<String, Any>,
        payload: List<Map<String, Any>>?,
    ): Map<String, Any> {
        return if (payload != null) {
            val modified = subaccount.mutable()
            val transformed = assetPositionsProcessor.receivedChangesDeprecated(
                parser.asNativeMap(subaccount["assetPositions"]),
                payload,
            )
            modified.safeSet("assetPositions", transformed)
            modified
        } else {
            subaccount
        }
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

    internal fun receivedHistoricalPnlsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(
            existing,
            "historicalPnl",
            payload?.get("historicalPnl"),
        ) { existing, payload ->
            parser.asNativeList(payload)?.let {
                historicalPNLsProcessor.received(parser.asNativeList(existing), it)
            }
        }
    }

    internal fun processFills(
        existing: InternalSubaccountState,
        payload: List<IndexerFillResponseObject>?,
    ): InternalSubaccountState {
        return processFills(existing, payload, true)
    }

    internal fun receivedFillsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        return receivedFillsDeprecated(modified, parser.asNativeList(payload?.get("fills")), true)
    }

    fun processTransfers(
        existing: InternalSubaccountState,
        payload: List<IndexerTransferResponseObject>?,
    ): InternalSubaccountState {
        return processTransfers(existing, payload, true)
    }

    internal fun receivedTransfersDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        return receivedTransfersDeprecated(modified, parser.asNativeList(payload?.get("transfers")), true)
    }

    /*
    private fun modify(
        positions: Map<String, Any>?,
        orders: List<*>?,
        state: String,
    ): Map<String, Any> {
        val modified = positions?.mutable() ?: mutableMapOf()
        val markets = positions?.keys?.toMutableSet() ?: mutableSetOf()

        var fee = Numeric.double.ZERO
        if (orders != null) {
            for (item in orders) {
                parser.asNativeMap(item)?.let { order ->
                    parser.asString(order["market"])?.let { market ->
                        val orderFee = parser.asDouble(order["fee"]) ?: Numeric.double.ZERO
                        val position = parser.asNativeMap(positions?.get(market))
                        modified.safeSet(market, modify(position, order, market, state))
                        fee += orderFee
                        markets.remove(market)
                    }
                }
            }
        }
        for (market in markets) {
            val position = parser.asNativeMap(positions?.get(market))
            val modifiedPosition = notEmpty(remove(position, state))
            modified.safeSet(market, modifiedPosition)
        }
        return mapOf("fee" to fee, "positions" to modified)
    }

    private fun modify(
        position: Map<String, Any>?,
        order: Map<String, Any>?,
        market: String,
        state: String,
    ): Map<String, Any>? {
        val size = parser.asDouble(order?.get("size")) ?: Numeric.double.ZERO
        val price = parser.asDouble(order?.get("price")) ?: Numeric.double.ZERO
        if (size != Numeric.double.ZERO && price != Numeric.double.ZERO) {
            val entryPrice =
                parser.asDouble(parser.value(position, "entryPrice.current")) ?: Numeric.double.ZERO
            val existingSize =
                parser.asDouble(parser.value(position, "size.current")) ?: Numeric.double.ZERO
            val newSize = existingSize + size
            if (newSize != Numeric.double.ZERO) {
                val entryCost = entryPrice * existingSize
                val newCost = entryCost + size * price
                val newEntryPrice = newCost / newSize
                val modified: MutableMap<String, Any> = position?.mutable() ?: run {
                    val position1 = mutableMapOf<String, Any>()
                    position1["id"] = market
                    position1
                }

                modified.safeSet(
                    "entryPrice",
                    mapOf("current" to newEntryPrice),
                )
                modified.safeSet("size", mapOf("current" to newSize))
                return notEmpty(modified)
            }
        }
        return notEmpty(remove(position, state))
    }

    private fun remove(position: Map<String, Any>?, state: String): Map<String, Any>? {
        return if (position != null) {
            val modified = position.mutable()
            val entryPrice = parser.asNativeMap(position["entryPrice"])?.mutable()
            entryPrice?.safeSet(state, null)
            val size = parser.asNativeMap(position["size"])?.mutable()
            size?.safeSet(state, null)
            modified.safeSet("entryPrice", entryPrice)
            modified.safeSet("size", size)
            modified
        } else {
            null
        }
    }

    private fun notEmpty(position: Map<String, Any>?): Map<String, Any>? {
        return if (parser.value(position, "size.current") != null || parser.value(
                position,
                "size.postOrder",
            ) != null || parser.value(position, "size.postAllOrders") != null
        ) {
            position
        } else {
            null
        }
    }

    internal fun setWalletBalance(
        existing: Map<String, Any>?,
        balance: String?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        modified.safeSet(
            "wallet.current",
            balance?.toBigDecimal(null, Numeric.decimal.highDefinitionMode),
        )
        return modified
    }
     */

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

    internal fun orderCanceledDeprecated(
        existing: Map<String, Any>,
        orderId: String,
    ): Pair<Map<String, Any>, Boolean> {
        val orders = parser.asNativeMap(existing["orders"])
        if (orders != null) {
            val (modifiedOrders, updated) = ordersProcessor.canceledDeprecated(orders, orderId)
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet("orders", modifiedOrders)
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
