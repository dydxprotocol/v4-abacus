package exchange.dydx.abacus.processor.wallet.account

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerFillResponseObject
import indexer.models.IndexerCompositeOrderObject
import kotlinx.serialization.json.JsonNull.content

@Suppress("UNCHECKED_CAST")
internal open class SubaccountProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : BaseProcessor(parser) {
    internal val assetPositionsProcessor = AssetPositionsProcessor(parser)
    internal val ordersProcessor = OrdersProcessor(parser, localizer)
    private val perpetualPositionsProcessor = PerpetualPositionsProcessor(parser)
    private val fillsProcessor = FillsProcessor(parser, localizer)
    private val transfersProcessor = TransfersProcessor(parser)
    private val fundingPaymentsProcessor = FundingPaymentsProcessor(parser)
    private val historicalPNLsProcessor = HistoricalPNLsProcessor(parser)

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

        val fills = parser.asTypedList<IndexerFillResponseObject>(content["fills"])
        state = processFills(state, fills, false)

        val orders = parser.asTypedList<IndexerCompositeOrderObject>(content["orders"])
        state = processOrders(state, orders, height)

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
            subaccount = received(subaccount, accountPayload, subscribed)
        }

        val ordersPayload = parser.asNativeList(content["orders"]) as? List<Map<String, Any>>
        if (ordersPayload != null) {
            subaccount = receivedOrders(subaccount, ordersPayload, height)
        }

        val perpetualPositionsPayload =
            parser.asNativeList(content["positions"])
                ?: parser.asNativeList(content["perpetualPositions"])
        if (perpetualPositionsPayload != null) {
            subaccount = receivedPerpetualPositions(subaccount, perpetualPositionsPayload)
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
            subaccount = receivedTransfers(subaccount, transfersPayloadList, false)
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

    internal fun received(
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
            modified.safeSet("assetPositions", assetPositionsProcessor.received(assetPositionsData))

            modified.remove("orders")
        } else {
            val assetPositionsPayload = payload["assetPositions"] as? List<Map<String, Any>>
            if (assetPositionsPayload != null) {
                modified = receivedAssetPositions(modified, assetPositionsPayload).mutable()
            }
        }
        modified["quoteBalance"] = calculateQuoteBalance(modified, payload)

        return modified
    }

    internal fun calculateQuoteBalance(
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
        val newOrders = ordersProcessor.process(
            existing = subaccount.orders,
            payload = payload ?: emptyList(),
            subaccountNumber = subaccount.subaccountNumber,
            height = height,
        )
        if (subaccount.orders != newOrders) {
            subaccount.orders = newOrders
        }
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

    internal fun updateHeight(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        val orders = parser.asNativeMap(existing["orders"])
        if (orders != null) {
            val (updatedOrders, updated) = ordersProcessor.updateHeight(
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
        val newFills = fillsProcessor.process(
            existing = if (reset) null else subaccount.fills,
            payload = payload ?: emptyList(),
            subaccountNumber = subaccount.subaccountNumber,
        )
        if (subaccount.fills != newFills) {
            subaccount.fills = newFills
        }
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

    private fun receivedTransfers(
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

    private fun receivedPerpetualPositions(
        subaccount: Map<String, Any>,
        payload: List<Any>?,
    ): Map<String, Any> {
        return if (payload != null) {
            val modified = subaccount.mutable()
            val positions = perpetualPositionsProcessor.receivedChanges(
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

    private fun receivedAssetPositions(
        subaccount: Map<String, Any>,
        payload: List<Map<String, Any>>?,
    ): Map<String, Any> {
        return if (payload != null) {
            val modified = subaccount.mutable()
            val transformed = assetPositionsProcessor.receivedChanges(
                parser.asNativeMap(subaccount["assetPositions"]),
                payload,
            )
            modified.safeSet("assetPositions", transformed)
            modified
        } else {
            subaccount
        }
    }

    internal fun receivedHistoricalPnls(
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

    internal fun receivedTransfers(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        return receivedTransfers(modified, parser.asNativeList(payload?.get("transfers")), true)
    }

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

    internal fun orderCanceled(
        existing: Map<String, Any>,
        orderId: String,
    ): Pair<Map<String, Any>, Boolean> {
        val orders = parser.asNativeMap(existing["orders"])
        if (orders != null) {
            val (modifiedOrders, updated) = ordersProcessor.canceled(orders, orderId)
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

internal class V4SubaccountProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : SubaccountProcessor(parser, localizer) {
    override fun received(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        val orders = parser.asNativeMap(existing["orders"])
        if (orders != null) {
            val updated = false
            val (modifiedOrders, ordersUpdated) = ordersProcessor.received(
                orders,
                height,
            )
            if (ordersUpdated) {
                val modified = existing.mutable()
                modified.safeSet("orders", modifiedOrders)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }
}

internal class V4SubaccountsProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : SubaccountProcessor(parser, localizer) {
    private val subaccountProcessor = V4SubaccountProcessor(parser, localizer)
    internal fun receivedSubaccounts(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val modified = mutableMapOf<String, Any>()
            for (itemPayload in payload) {
                val data = parser.asNativeMap(itemPayload)
                if (data != null) {
                    val subaccountNumber = parser.asInt(data?.get("subaccountNumber"))
                    if (subaccountNumber != null) {
                        val key = "$subaccountNumber"
                        val existing = parser.asNativeMap(existing?.get(key))
                        val subaccount = subaccountProcessor.received(existing, data, true)
                        modified.safeSet(key, subaccount)
                    }
                }
            }
            return modified
        } else {
            null
        }
    }

    override fun subaccountPayload(content: Map<String, Any>): Map<String, Any>? {
        return parser.asNativeMap(content["subaccount"])
            ?: parser.asNativeMap(content["subaccounts"])
    }

    override fun socketDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        subscribed: Boolean,
        height: BlockAndTime?,
    ): Map<String, Any> {
        val modified = super.socketDeprecated(existing, content, subscribed, height).mutable()

        val assetPositionsPayload =
            parser.asNativeList(content["assetPositions"]) as? List<Map<String, Any>>
        if (assetPositionsPayload != null) {
            val existing = parser.asNativeMap(modified["assetPositions"])
            modified.safeSet(
                "assetPositions",
                assetPositionsProcessor.receivedChanges(existing, assetPositionsPayload),
            )
        }
        modified["quoteBalance"] = calculateQuoteBalance(modified, content)
        return modified
    }

    override fun received(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        return subaccountProcessor.received(existing, height)
    }

    internal fun updateSubaccountsHeight(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Triple<Map<String, Any>, Boolean, List<Int>?> {
        var updated = false
        val modifiedSubaccounts = existing.mutable()
        val modifiedSubaccountIds = mutableListOf<Int>()
        for ((key, value) in existing) {
            val keyInt = parser.asInt(key)
            if (keyInt == null) {
                Logger.e { "Invalid subaccount key: $key" }
                continue
            }
            val subaccount = parser.asNativeMap(value)
            if (subaccount != null) {
                val (modifiedSubaccount, subaccountUpdated) = subaccountProcessor.updateHeight(
                    subaccount,
                    height,
                )
                if (subaccountUpdated) {
                    modifiedSubaccounts.safeSet(key, modifiedSubaccount)
                    updated = true
                    modifiedSubaccountIds.add(key.toInt())
                }
            }
        }
        if (updated) {
            return Triple(modifiedSubaccounts, true, modifiedSubaccountIds)
        }
        return Triple(existing, false, null)
    }

    internal fun orderCanceled(
        existing: Map<String, Any>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<Map<String, Any>, Boolean> {
        val subaccountIndex = "$subaccountNumber"
        val subaccount = parser.asNativeMap(existing[subaccountIndex])
        if (subaccount != null) {
            val (modifiedSubaccount, updated) = subaccountProcessor.orderCanceled(
                subaccount,
                orderId,
            )
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet(subaccountIndex, modifiedSubaccount)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }
}
