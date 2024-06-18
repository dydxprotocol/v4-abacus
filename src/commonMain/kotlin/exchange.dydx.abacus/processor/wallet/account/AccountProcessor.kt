package exchange.dydx.abacus.processor.wallet.account

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.output.AccountBalance
import exchange.dydx.abacus.output.StakingRewards
import exchange.dydx.abacus.output.UnbondingDelegation
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableListOf

/*
"account": {
      "starkKey": "01de24f8468f0590b80c12c87ae9e247ef3278d4683d875296051495b2ad0100",
      "positionId": "30915",
      "equity": "205935.352966",
      "freeCollateral": "187233.155294",
      "pendingDeposits": "0.000000",
      "pendingWithdrawals": "0.000000",
      "openPositions": {
        "ETH-USD": {
          "market": "ETH-USD",
          "status": "OPEN",
          "side": "LONG",
          "size": "93.57",
          "maxSize": "100",
          "entryPrice": "1091.812076",
          "exitPrice": "1091.236219",
          "unrealizedPnl": "61455.547636",
          "realizedPnl": "-4173.521266",
          "createdAt": "2022-06-30T01:01:10.234Z",
          "closedAt": null,
          "sumOpen": "218.92",
          "sumClose": "125.35",
          "netFunding": "-4101.337527"
        },
        " v": {
          "market": "LINK-USD",
          "status": "OPEN",
          "side": "SHORT",
          "size": "-11",
          "maxSize": "-11",
          "entryPrice": "7.175000",
          "exitPrice": "0.000000",
          "unrealizedPnl": "-9.916131",
          "realizedPnl": "2.022104",
          "createdAt": "2022-07-20T18:24:29.570Z",
          "closedAt": null,
          "sumOpen": "11",
          "sumClose": "0",
          "netFunding": "2.022104"
        },
        "UNI-USD": {
          "market": "UNI-USD",
          "status": "OPEN",
          "side": "LONG",
          "size": "11548.4",
          "maxSize": "11548.4",
          "entryPrice": "7.065650",
          "exitPrice": "0.000000",
          "unrealizedPnl": "23552.454293",
          "realizedPnl": "142.629215",
          "createdAt": "2022-07-18T20:37:23.893Z",
          "closedAt": null,
          "sumOpen": "11548.4",
          "sumClose": "0",
          "netFunding": "142.629215"
        },
        "SUSHI-USD": {
          "market": "SUSHI-USD",
          "status": "OPEN",
          "side": "LONG",
          "size": "12",
          "maxSize": "12",
          "entryPrice": "1.464000",
          "exitPrice": "0.000000",
          "unrealizedPnl": "0.729203",
          "realizedPnl": "0.271316",
          "createdAt": "2022-07-18T20:36:17.165Z",
          "closedAt": null,
          "sumOpen": "12",
          "sumClose": "0",
          "netFunding": "0.271316"
        }
      },
      "accountNumber": "0",
      "id": "dace1648-c854-5aed-9879-88899bf647a3",
      "quoteBalance": "-62697.279528",
      "createdAt": "2021-04-20T18:27:38.698Z"
    },

    to

    "account": {
      "ethereumeAddress": "0xc3ad9aB721765560F05AFA7696D5e167CAD010e7",
      "positionId": "30915",
      "user": {
        "isRegistered": false,
        "email": "johnqh@yahoo.com",
        "username": "johnqh",
        "makerFeeRate": 0.00015,
        "takerFeeRate": 0.0004,
        "makerVolume30D": 0,
        "takerVolume30D": 1483536.2848,
        "fees30D": 626.566513,
        "isEmailVerified": false,
        "country": "CK",
        "favorited": [
          "BTC-USD",
          "CRV-USD",
          "UMA-USD",
          "ETH-USD",
          "RUNE-USD",
          "MKR-USD"
        ],
        "walletId": "METAMASK"
      },
      "pnlTotal": 23.34,
      "pnl24h": 3.34,
      "pnl24hPercent": 0.03,
      "historicalPnl": [
        {
          "equity": 138463.2724,
          "totalPnl": 78334.1124,
          "createdAtMilliseconds": 23099045345,
          "netTransfers": 0
        }
      ],
      "quoteBalance": {
        "current": 2349.234,
        "postOrder": 2349.234,
        "postAllOrders": 2349.234
      },
      "notionalTotal": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 234230.34
      },
      "valueTotal": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 234230.34
      },
      "initialRiskTotal": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 234230.34
      },
      "adjustedImf": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 234230.34
      },
      "equity": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 798234
      },
      "freeCollateral": {
        "current": 0.03,
        "postOrder": 0.04,
        "postAllOrders": 0.03
      },
      "leverage": {
        "current": 7.64,
        "postOrder": 8.76,
        "postAllOrders": 6.54
      },
      "marginUsage": {
        "current": 0.102,
        "postOrder": 0.105,
        "postAllOrders": 0.093
      },
      "buyingPower": {
        "current": 98243520.45,
        "postOrder": 234899345.34,
        "postAllOrders": 98243520.45
      },
 */

@Suppress("UNCHECKED_CAST")
internal open class SubaccountProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    internal val assetPositionsProcessor = AssetPositionsProcessor(parser)
    internal val ordersProcessor = OrdersProcessor(parser)
    private val perpetualPositionsProcessor = PerpetualPositionsProcessor(parser)
    private val fillsProcessor = FillsProcessor(parser)
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

    internal fun subscribed(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any> {
        return socket(existing, content, true, height)
    }

    @Suppress("FunctionName")
    internal fun channel_data(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any> {
        return socket(existing, content, false, height)
    }

    internal open fun socket(
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
            subaccount = receivedFills(subaccount, fillsPayload, false)
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
            val openPositions = positions?.filterValues { it ->
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

    private fun receivedFills(
        subaccount: Map<String, Any>,
        payload: List<Any>?,
        reset: Boolean,
    ): Map<String, Any> {
        val subaccountNumber = parser.asInt(subaccount["subaccountNumber"]) ?: 0
        return receivedObject(subaccount, "fills", payload) { existing, payload ->
            parser.asNativeList(payload)?.let {
                fillsProcessor.received(if (reset) null else parser.asNativeList(existing), it, subaccountNumber)
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
            val openPositions = positions?.filterValues { it ->
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

    internal fun receivedFills(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        return receivedFills(modified, parser.asNativeList(payload?.get("fills")), true)
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

internal class V4SubaccountProcessor(parser: ParserProtocol) : SubaccountProcessor(parser) {
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

internal class V4SubaccountsProcessor(parser: ParserProtocol) : SubaccountProcessor(parser) {
    private val subaccountProcessor = V4SubaccountProcessor(parser)
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

    override fun socket(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        subscribed: Boolean,
        height: BlockAndTime?,
    ): Map<String, Any> {
        val modified = super.socket(existing, content, subscribed, height).mutable()

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

@Suppress("UNCHECKED_CAST")
private class V4AccountBalancesProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    fun receivedBalances(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val modified = mutableMapOf<String, Any>()
            for (itemPayload in payload) {
                val data = parser.asNativeMap(itemPayload)
                if (data != null) {
                    val denom = parser.asString(data?.get("denom"))
                    if (denom != null) {
                        val key = "$denom"
                        val existing =
                            parser.asNativeMap(existing?.get(key))?.mutable() ?: mutableMapOf()
                        existing.safeSet("denom", denom)
                        existing.safeSet("amount", parser.asDecimal(data?.get("amount")))
                        modified.safeSet(key, existing)
                    }
                }
            }
            return modified
        } else {
            null
        }
    }
}

private class V4AccountDelegationsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    fun received(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val modified = mutableMapOf<String, Any>()
            for (itemPayload in payload) {
                val item = parser.asNativeMap(itemPayload)
                val balance = parser.asNativeMap(item?.get("balance"))

                if (balance != null) {
                    val denom = parser.asString(balance["denom"])
                    if (denom != null) {
                        val key = "$denom"
                        val current =
                            parser.asNativeMap(modified[key])?.mutable()
                        if (current == null) {
                            modified.safeSet(
                                key,
                                mapOf(
                                    "denom" to denom,
                                    "amount" to parser.asDecimal(
                                        balance["amount"],
                                    ),
                                ),
                            )
                        } else {
                            val amount = parser.asDecimal(balance["amount"]);
                            val existingAmount = parser.asDecimal(current["amount"]);
                            if (amount != null && existingAmount != null) {
                                current.safeSet("amount", amount + existingAmount)
                            }
                        }
                    }
                }
            }
            return modified
        } else {
            null
        }
    }

    fun receivedDelegations(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): List<Any>? {
        return if (payload != null) {
            val modified = mutableListOf<Any>()
            for (itemPayload in payload) {
                val item = parser.asNativeMap(itemPayload)
                val validator = parser.asString(parser.value(item, "delegation.validatorAddress"))
                val amount = parser.asDecimal(parser.value(item, "balance.amount"))
                val denom = parser.asString(parser.value(item, "balance.denom"))
                if (validator != null && amount != null) {
                    modified.add(
                        mapOf(
                            "validator" to validator,
                            "amount" to amount,
                            "denom" to denom,
                        ),
                    )
                }
            }
            return modified
        } else {
            null
        }
    }
}

private class V4AccountTradingRewardsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val historicalTradingRewardsProcessor =
        HistoricalTradingRewardsProcessor(parser = parser)

    fun receivedTotalTradingRewards(
        existing: Map<String, Any>?,
        payload: Any?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf<String, Any>()
        val totalTradingRewards = parser.asDouble(payload)
        if (totalTradingRewards != null) {
            modified.safeSet("total", totalTradingRewards)
        }
        return modified
    }

    fun recievedHistoricalTradingRewards(
        existing: List<Any>?,
        payload: List<Any>?,
    ): List<Any>? {
        return if (payload != null) {
            historicalTradingRewardsProcessor.received(
                existing,
                payload,
            )
        } else {
            null
        }
    }

    fun recievedBlockTradingReward(
        existing: List<Any>?,
        payload: Any?,
    ): List<Any>? {
        return if (payload != null) {
            historicalTradingRewardsProcessor.receivedBlockTradingReward(
                existing,
                payload,
            )
        } else {
            null
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal class V4AccountProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val subaccountsProcessor = V4SubaccountsProcessor(parser)
    private val balancesProcessor = V4AccountBalancesProcessor(parser)
    private val delegationsProcessor = V4AccountDelegationsProcessor(parser)
    private val tradingRewardsProcessor = V4AccountTradingRewardsProcessor(parser)
    private var launchIncentivePointsProcessor = LaunchIncentivePointsProcessor(parser)

    internal fun receivedAccountBalances(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val balances = parser.asNativeMap(parser.value(existing, "balances"))
        val modifiedBalances = balancesProcessor.receivedBalances(balances, payload)
        modified.safeSet("balances", modifiedBalances)
        return modified
    }

    internal fun receivedDelegations(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val delegations = parser.asNativeMap(parser.value(existing, "stakingBalances"))
        val modifiedStakingBalance = delegationsProcessor.received(delegations, payload)
        modified.safeSet("stakingBalances", modifiedStakingBalance)
        val modifiedDelegations = delegationsProcessor.receivedDelegations(delegations, payload)
        modified.safeSet("stakingDelegations", modifiedDelegations)
        return modified
    }

    internal fun receivedUnbonding(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        if (payload == null) {
            return existing
        }
        val modified = existing?.mutable() ?: mutableMapOf()
        val unbondingDelegations: IMutableList<UnbondingDelegation> = iMutableListOf()

        for (validator in payload) {
            val validatorAddress = parser.asString(parser.value(validator, "validatorAddress")) ?: continue
            val entries = parser.asList(parser.value(validator, "entries")) ?: continue

            for (entry in entries) {
                val completionTime = parser.asString(parser.value(entry, "completionTime")) ?: continue
                val balance = parser.asString(parser.value(entry, "balance")) ?: continue

                unbondingDelegations.add(UnbondingDelegation(validatorAddress, completionTime, balance))
            }
        }
        modified.safeSet("unbondingDelegation", unbondingDelegations)
        return modified
    }

    internal fun receivedStakingRewards(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        val rewards = parser.asList(parser.value(payload, "rewards"))
        val total = parser.asList(parser.value(payload, "total"))
        if (payload == null || rewards == null || total == null) {
            return existing
        }
        val modified = existing?.mutable() ?: mutableMapOf()
        val validators: IMutableList<String> = iMutableListOf()
        val totalRewards: IMutableList<AccountBalance> = iMutableListOf()
        for (validator in rewards) {
            val validatorAddress = parser.asString(parser.value(validator, "validatorAddress")) ?: continue
            validators.add(validatorAddress)
        }
        for (reward in total) {
            val denom = parser.asString(parser.value(reward, "denom")) ?: continue
            val amount = parser.asString(parser.value(reward, "amount")) ?: continue
            totalRewards.add(AccountBalance(denom, amount))
        }
        modified.safeSet("stakingRewards", StakingRewards(validators, totalRewards))
        return modified
    }

    internal fun receivedHistoricalPnls(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountsProcessor.receivedHistoricalPnls(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun receivedFills(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountsProcessor.receivedFills(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun receivedTransfers(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountsProcessor.receivedTransfers(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun receivedHistoricalTradingRewards(
        existing: Map<String, Any>?,
        payload: List<Any>?,
        period: String?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val historicalTradingRewards =
            parser.asNativeList(parser.value(existing, "tradingRewards.historical.$period"))
        val modifiedHistoricalTradingRewards =
            tradingRewardsProcessor.recievedHistoricalTradingRewards(
                historicalTradingRewards,
                payload,
            )
        modified.safeSet("tradingRewards.historical.$period", modifiedHistoricalTradingRewards)
        return modified
    }

    internal fun receivedAccount(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        var modified = existing?.mutable() ?: mutableMapOf()
        val subaccounts = parser.asNativeMap(parser.value(existing, "subaccounts"))
        val modifiedSubaccounts = subaccountsProcessor.receivedSubaccounts(
            subaccounts,
            parser.asList(payload?.get("subaccounts")),
        )
        modified.safeSet("subaccounts", modifiedSubaccounts)

        val tradingRewards = parser.asNativeMap(parser.value(existing, "tradingRewards"))
        val modifiedTradingRewards = tradingRewardsProcessor.receivedTotalTradingRewards(
            tradingRewards,
            payload?.get("totalTradingRewards"),
        )
        modified.safeSet("tradingRewards", modifiedTradingRewards)

        val test = parser.value(payload, "subaccounts.0.tradingRewards")
        /* block trading rewards are only sent in subaccounts.0 channel */
        val tradingRewardsPayload =
            parser.asNativeList(parser.value(payload, "subaccounts.0.tradingRewards"))
        if (tradingRewardsPayload != null) {
            for (item in tradingRewardsPayload) {
                modified = receivedBlockTradingReward(modified, item)
            }
        }
        return modified
    }

    internal fun subscribed(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        var modified = existing?.mutable() ?: mutableMapOf()
        val subaccountNumber = parser.asInt(parser.value(content, "subaccount.subaccountNumber"))
        if (subaccountNumber != null) {
            val subaccount =
                parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
            val modifiedsubaccount = subaccountsProcessor.subscribed(subaccount, content, height)
            modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        } else {
            val parentSubaccountNumber =
                parser.asInt(parser.value(content, "subaccount.parentSubaccountNumber"))
            if (parentSubaccountNumber != null) {
                modified = subscribedParentSubaccount(modified, content, height).mutable()
            }
        }

        /* block trading rewards are only sent in subaccounts.0 channel */
        val tradingRewardsPayload =
            parser.value(content, "tradingReward")
        if (tradingRewardsPayload != null) {
            modified = receivedBlockTradingReward(modified, tradingRewardsPayload)
        }
        return modified
    }

    private fun subscribedParentSubaccount(
        existing: Map<String, Any>,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any> {
        /*
        We will go through all segments in the content, regroup them based on subaccountNumber,
        and send to subaccountProcessor's existing code
         */
        val contentBySubaccountNumber = mutableMapOf<Int, MutableMap<String, Any>>()
        for ((key, value) in content) {
            val subpayloadList = when (key) {
                "subaccount" -> parser.asNativeList(parser.value(value, "childSubaccounts"))
                else -> parser.asNativeList(value)
            }
            if (subpayloadList != null) {
                // Go through the list, and group by subaccountNumber
                val subPayloadBySubaccount = mutableMapOf<Int, MutableList<Any>>()
                for (subpayload in subpayloadList) {
                    val subaccountNumber =
                        parser.asInt(parser.value(subpayload, "subaccountNumber"))
                    if (subaccountNumber != null) {
                        val list =
                            subPayloadBySubaccount.getOrPut(subaccountNumber) { mutableListOf() }
                        list.add(subpayload)
                    }
                }
                for ((subaccountNumber, subPayloadList) in subPayloadBySubaccount) {
                    val subaccount =
                        contentBySubaccountNumber.getOrPut(subaccountNumber) { mutableMapOf() }
                    if (key == "subaccount") {
                        // There should be a single subaccount object
                        subaccount.safeSet(key, subPayloadList.firstOrNull())
                    } else {
                        subaccount[key] = subPayloadList
                    }
                }
            }
        }
        /*
        Now we have a map of subaccountNumber to content, we can send it to subaccountProcessor
         */
        val modified = existing.mutable()
        for ((subaccountNumber, subaccountContent) in contentBySubaccountNumber) {
            val subaccount =
                parser.asNativeMap(parser.value(modified, "subaccounts.$subaccountNumber"))
            val modifiedsubaccount =
                subaccountsProcessor.subscribed(subaccount, subaccountContent, height)
            modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        }
        return modified
    }

    @Suppress("FunctionName")
    internal fun channel_data(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        info: SocketInfo,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        val subaccountNumber = info.childSubaccountNumber ?: parser.asInt(parser.value(content, "subaccounts.subaccountNumber"))
            ?: subaccountNumberFromInfo(info)

        return if (subaccountNumber != null) {
            var modified = existing?.toMutableMap() ?: mutableMapOf()
            val subaccount =
                parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
            val modifiedsubaccount = subaccountsProcessor.channel_data(subaccount, content, height)
            modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)

            /* block trading rewards are only sent in subaccounts.0 channel */
            val tradingRewardsPayload = content["tradingReward"]
            if (tradingRewardsPayload != null) {
                modified = receivedBlockTradingReward(modified, tradingRewardsPayload)
            }
            return modified
        } else {
            existing
        }
    }

    private fun receivedBlockTradingReward(
        existing: Map<String, Any>,
        payload: Any,
    ): MutableMap<String, Any> {
        val modified = existing.mutable()
        val blockRewards =
            parser.asNativeList(parser.value(existing, "tradingRewards.blockRewards"))
        val modifiedTradingRewards = tradingRewardsProcessor.recievedBlockTradingReward(
            blockRewards,
            payload,
        )
        modified.safeSet("tradingRewards.blockRewards", modifiedTradingRewards)
        return modified
    }

    internal fun updateHeight(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Triple<Map<String, Any>, Boolean, List<Int>?> {
        val subaccounts = parser.asNativeMap(parser.value(existing, "subaccounts"))
        if (subaccounts != null) {
            val (modifiedSubaccounts, updated, subaccountIds) = subaccountsProcessor.updateSubaccountsHeight(
                subaccounts,
                height,
            )
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet("subaccounts", modifiedSubaccounts)
                return Triple(modified, true, subaccountIds)
            }
        }
        return Triple(existing, false, null)
    }

    private fun subaccountNumberFromInfo(info: SocketInfo): Int? {
        val id = info.id
        return if (id != null) {
            val elements = id.split("/")
            if (elements.size == 2) {
                parser.asInt(elements.lastOrNull())
            } else {
                null
            }
        } else {
            null
        }
    }

    internal fun received(
        existing: Map<String, Any>,
        subaccountNumber: Int,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        if (subaccount != null) {
            val (modifiedsubaccount, subaccountUpdated) = subaccountsProcessor.received(
                subaccount,
                height,
            )
            if (subaccountUpdated) {
                val modified = existing.toMutableMap()
                modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    internal fun orderCanceled(
        existing: Map<String, Any>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<Map<String, Any>, Boolean> {
        val subaccounts = parser.asNativeMap(parser.value(existing, "subaccounts"))?.mutable()
        if (subaccounts != null) {
            val (modifiedSubaccounts, updated) =
                subaccountsProcessor.orderCanceled(subaccounts, orderId, subaccountNumber)
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet("subaccounts", modifiedSubaccounts)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    override fun accountAddressChanged() {
        super.accountAddressChanged()
        subaccountsProcessor.accountAddress = accountAddress
    }

    internal fun receivedLaunchIncentivePoint(
        existing: Map<String, Any>,
        season: String,
        payload: Any,
    ): Map<String, Any> {
        /*
        launchIncentive.{season}...
         */
        val data = parser.asNativeMap(payload) ?: return existing
        val modified = existing.mutable()
        val points = launchIncentivePointsProcessor.received(
            season,
            parser.asNativeMap(existing["launchIncentivePoints"]),
            data,
        )

        modified.safeSet("launchIncentivePoints", points)
        return modified
    }
}
