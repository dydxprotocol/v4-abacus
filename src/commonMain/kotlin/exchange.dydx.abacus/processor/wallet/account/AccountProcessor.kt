package exchange.dydx.abacus.processor.wallet.account

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iListOf
import kollections.iMutableSetOf
import kollections.toIMap
import kollections.toIMutableMap

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

/*
V3AccountProcess is used to process generic account data, which is used by both V3 and V4
 */
@Suppress("UNCHECKED_CAST")
internal class V3AccountProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val subaccountProcessor = V3SubaccountProcessor(parser)

    internal fun subscribed(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        height: Int?,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val subaccount = parser.asMap(parser.value(existing, "subaccounts.0"))
        val modifiedsubaccount = subaccountProcessor.subscribed(subaccount, content, height)
        modified.safeSet("subaccounts.0", modifiedsubaccount)
        return modified
    }

    internal fun channel_data(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        height: Int?,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val subaccount = parser.asMap(parser.value(existing, "subaccounts.0"))
        val modifiedsubaccount = subaccountProcessor.channel_data(subaccount, content, height)
        modified.safeSet("subaccounts.0", modifiedsubaccount)
        return modified
    }

    internal fun receivedHistoricalPnls(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
        subaccountNumber: Int,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val subaccount = parser.asMap(parser.value(existing, "subaccounts.0"))
        val modifiedsubaccount = subaccountProcessor.receivedHistoricalPnls(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun receivedFills(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
        subaccountNumber: Int,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val subaccount = parser.asMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountProcessor.receivedFills(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun receivedTransfers(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
        subaccountNumber: Int,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val subaccount = parser.asMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountProcessor.receivedTransfers(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    override fun accountAddressChanged() {
        super.accountAddressChanged()
        subaccountProcessor.accountAddress = accountAddress
    }
}

@Suppress("UNCHECKED_CAST")
internal open class SubaccountProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    internal val assetPositionsProcessor = AssetPositionsProcessor(parser)
    internal val ordersProcessor = OrdersProcessor(parser)
    private val perpetualPositionsProcessor = PerpetualPositionsProcessor(parser)
    private val fillsProcessor = FillsProcessor(parser)
    private val transfersProcessor = TransfersProcessor(parser)
    private val fundingPaymentsProcessor = FundingPaymentsProcessor(parser)
    private val historicalPNLsProcessor = HistoricalPNLsProcessor(parser)

    private val accountKeyMap = iMapOf(
        "string" to iMapOf(
            "id" to "id",
            "accountNumber" to "accountNumber",
            "starkKey" to "starkKey",
            "positionId" to "positionId"
        ),
        "double" to iMapOf(
            "pendingDeposits" to "pendingDeposits",
            "pendingWithdrawals" to "pendingWithdrawals"
        ),
        "datetime" to iMapOf(
            "createdAt" to "createdAt"
        ),
        "bool" to iMapOf(
            "marginEnabled" to "marginEnabled"
        )
    )

    private val currentAccountKeyMap = iMapOf(
        "double" to iMapOf(
            "equity" to "equity",
            "freeCollateral" to "freeCollateral",
            "quoteBalance" to "quoteBalance"
        ),
    )

    internal fun subscribed(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        height: Int?,
    ): IMap<String, Any> {
        return socket(existing, content, true, height)
    }

    internal fun channel_data(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        height: Int?,
    ): IMap<String, Any> {
        return socket(existing, content, false, height)
    }

    internal open fun socket(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        subscribed: Boolean,
        height: Int?,
    ): IMap<String, Any> {
        var subaccount = existing ?: iMutableMapOf()
        val accountPayload = subaccountPayload(content)
        if (accountPayload != null) {
            subaccount = received(subaccount, accountPayload, subscribed)
        }

        val ordersPayload = parser.asList(content["orders"]) as? IList<IMap<String, Any>>
        if (ordersPayload != null) {
            subaccount = receivedOrders(subaccount, ordersPayload, height)
        }

        val perpetualPositionsPayload =
            parser.asList(content["positions"])
                ?: parser.asList(content["perpetualPositions"])
        if (perpetualPositionsPayload != null) {
            subaccount = receivedPerpetualPositions(subaccount, perpetualPositionsPayload)
        }

        val fillsPayload = parser.asList(content["fills"])
        if (fillsPayload != null) {
            subaccount = receivedFills(subaccount, fillsPayload, false)
        }

        val transfersPayload = content["transfers"]
        val transfersPayloadList = if (transfersPayload != null) {
            parser.asList(transfersPayload)
                ?: parser.asList(iListOf(parser.asMap(transfersPayload)))
        } else null

        if (transfersPayloadList != null) {
            subaccount = receivedTransfers(subaccount, transfersPayloadList, false)
        }

        val fundingPaymentsPayload =
            parser.asList(content["fundingPayments"]) as? IList<IMap<String, Any>>
        if (fundingPaymentsPayload != null) {
            subaccount = receivedFundingPayments(subaccount, fundingPaymentsPayload, false)
        }

        return subaccount
    }

    internal open fun subaccountPayload(content: IMap<String, Any>): IMap<String, Any>? {
        return parser.asMap(content["account"])
            ?: parser.asMap(parser.asList(content["accounts"])?.firstOrNull())
    }

    internal fun received(
        subaccount: IMap<String, Any>?,
        payload: IMap<String, Any>,
        firstTime: Boolean,
    ): IMap<String, Any> {
        var modified = transform(subaccount, payload, accountKeyMap)
        modified = transform(modified, payload, "current", currentAccountKeyMap)

        if (firstTime) {
            val openPerpetualPositionsData =
                (parser.asMap(payload["openPositions"])
                    ?: parser.asMap(payload["openPerpetualPositions"]))

            val positions = perpetualPositionsProcessor.received(openPerpetualPositionsData)
            modified.safeSet(
                "positions",
                positions
            )
            modified.safeSet("openPositions", positions?.filterValues { it ->
                val data = parser.asMap(it)
                parser.asString(data?.get("status")) == "OPEN"
            }?.toIMap())

            val assetPositionsData = parser.asMap(payload["assetPositions"])
            modified.safeSet("assetPositions", assetPositionsProcessor.received(assetPositionsData))

            modified.remove("orders")
        } else {
            val assetPositionsPayload = payload["assetPositions"] as? IList<IMap<String, Any>>
            if (assetPositionsPayload != null) {
                modified = receivedAssetPositions(modified, assetPositionsPayload).mutable()
            }
        }
        modified["quoteBalance"] = calculateQuoteBalance(modified, payload)

        return modified
    }

    internal fun calculateQuoteBalance(
        subaccount: IMap<String, Any>,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val quoteBalance = parser.asMap(subaccount["quoteBalance"])?.mutable()
            ?: iMutableMapOf<String, Any>()
        parser.asDouble(payload["quoteBalance"])?.let {
            quoteBalance["current"] = it
        }
        val derivedQuoteBalance = deriveQuoteBalance(parser.asMap(subaccount["assetPositions"]))
        if (derivedQuoteBalance != null) {
            quoteBalance["current"] = derivedQuoteBalance
        }
        return quoteBalance
    }

    private fun deriveQuoteBalance(assetPositions: IMap<String, Any>?): Double? {
        val usdc = parser.asMap(assetPositions?.get("USDC"))
        return if (usdc != null) {
            val size = parser.asDouble(usdc["size"])
            if (size != null) {
                val side = parser.asString(usdc["side"])
                if (side == "LONG") size else size * -1.0
            } else null
        } else null
    }

    private fun receivedOrders(
        subaccount: IMap<String, Any>,
        payload: IList<Any>?,
        height: Int?,
    ): IMap<String, Any> {
        return if (payload != null) {
            val modified = subaccount.mutable()
            val transformed = ordersProcessor.received(
                parser.asMap(subaccount["orders"]),
                payload,
                height,
            )
            modified.safeSet("orders", transformed)
            modified
        } else {
            subaccount
        }
    }

    private fun receivedFills(
        subaccount: IMap<String, Any>,
        payload: IList<Any>?,
        reset: Boolean,
    ): IMap<String, Any> {
        return receivedObject(subaccount, "fills", payload) { existing, payload ->
            parser.asList(payload)?.let {
                fillsProcessor.received(if (reset) null else parser.asList(existing), it)
            }
        } ?: subaccount
    }

    private fun receivedTransfers(
        subaccount: IMap<String, Any>,
        payload: IList<Any>?,
        reset: Boolean,
    ): IMap<String, Any> {
        return receivedObject(subaccount, "transfers", payload) { existing, payload ->
            parser.asList(payload)?.let {
                transfersProcessor.received(if (reset) null else parser.asList(existing), it)
            }
        } ?: subaccount
    }

    private fun receivedFundingPayments(
        subaccount: IMap<String, Any>,
        payload: IList<Any>?,
        reset: Boolean,
    ): IMap<String, Any> {
        return receivedObject(subaccount, "fundingPayments", payload) { existing, payload ->
            parser.asList(payload)?.let {
                fundingPaymentsProcessor.received(if (reset) null else parser.asList(existing), it)
            }
        } ?: subaccount
    }

    private fun receivedPerpetualPositions(
        subaccount: IMap<String, Any>,
        payload: IList<Any>?,
    ): IMap<String, Any> {
        return if (payload != null) {
            val modified = subaccount.mutable()
            val positions = perpetualPositionsProcessor.receivedChanges(
                parser.asMap(subaccount["positions"]),
                payload
            )
            modified.safeSet("positions", positions)
            modified.safeSet("openPositions", positions?.filterValues { it ->
                val data = parser.asMap(it)
                parser.asString(data?.get("status")) == "OPEN"
            }?.toIMap())
            modified
        } else {
            subaccount
        }
    }

    private fun receivedAssetPositions(
        subaccount: IMap<String, Any>,
        payload: IList<IMap<String, Any>>?,
    ): IMap<String, Any> {
        return if (payload != null) {
            val modified = subaccount.mutable()
            val transformed = assetPositionsProcessor.receivedChanges(
                parser.asMap(subaccount["assetPositions"]),
                payload
            )
            modified.safeSet("assetPositions", transformed)
            modified
        } else {
            subaccount
        }
    }

    internal fun receivedHistoricalPnls(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
    ): IMap<String, Any>? {
        return receivedObject(
            existing,
            "historicalPnl",
            payload?.get("historicalPnl")
        ) { existing, payload ->
            parser.asList(payload)?.let {
                historicalPNLsProcessor.received(parser.asList(existing), it)
            }
        }
    }

    internal fun receivedFills(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        return receivedFills(modified, parser.asList(payload?.get("fills")), true)
    }

    internal fun receivedTransfers(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        return receivedTransfers(modified, parser.asList(payload?.get("transfers")), true)
    }

    private fun modify(
        positions: IMap<String, Any>?,
        orders: IList<*>?,
        state: String,
    ): IMap<String, Any> {
        val modified = positions?.mutable() ?: iMutableMapOf()
        val markets = positions?.keys?.toMutableSet() ?: iMutableSetOf()

        var fee = Numeric.double.ZERO
        if (orders != null) {
            for (item in orders) {
                parser.asMap(item)?.let { order ->
                    parser.asString(order["market"])?.let { market ->
                        val orderFee = parser.asDouble(order["fee"]) ?: Numeric.double.ZERO
                        val position = parser.asMap(positions?.get(market))
                        modified.safeSet(market, modify(position, order, market, state))
                        fee += orderFee
                        markets.remove(market)
                    }
                }
            }
        }
        for (market in markets) {
            val position = parser.asMap(positions?.get(market))
            val modifiedPosition = notEmpty(remove(position, state))
            modified.safeSet(market, modifiedPosition)
        }
        return iMapOf("fee" to fee, "positions" to modified)
    }

    private fun modify(
        position: IMap<String, Any>?,
        order: IMap<String, Any>?,
        market: String,
        state: String,
    ): IMap<String, Any>? {
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
                val modified: IMutableMap<String, Any> = position?.mutable() ?: run {
                    val position1 = iMutableMapOf<String, Any>()
                    position1["id"] = market
                    position1
                }

                modified.safeSet(
                    "entryPrice",
                    iMapOf("current" to newEntryPrice)
                )
                modified.safeSet("size", iMapOf("current" to newSize))
                return notEmpty(modified)
            }
        }
        return notEmpty(remove(position, state))
    }

    private fun remove(position: IMap<String, Any>?, state: String): IMap<String, Any>? {
        return if (position != null) {
            val modified = position.mutable()
            val entryPrice = parser.asMap(position["entryPrice"])?.mutable()
            entryPrice?.safeSet(state, null)
            val size = parser.asMap(position["size"])?.mutable()
            size?.safeSet(state, null)
            modified.safeSet("entryPrice", entryPrice)
            modified.safeSet("size", size)
            modified
        } else {
            null
        }
    }

    private fun notEmpty(position: IMap<String, Any>?): IMap<String, Any>? {
        return if (parser.value(position, "size.current") != null || parser.value(
                position,
                "size.postOrder"
            ) != null || parser.value(position, "size.postAllOrders") != null
        ) position else null
    }

    internal fun setWalletBalance(
        existing: IMap<String, Any>?,
        balance: String?,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        modified.safeSet(
            "wallet.current",
            balance?.toBigDecimal(null, Numeric.decimal.highDefinitionMode)
        )
        return modified
    }


    internal fun orderCanceled(
        existing: IMap<String, Any>,
        orderId: String,
    ): Pair<IMap<String, Any>, Boolean> {
        val orders = parser.asMap(existing["orders"])
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

internal class V3SubaccountProcessor(parser: ParserProtocol) : SubaccountProcessor(parser) {
}

internal class V4SubaccountProcessor(parser: ParserProtocol) : SubaccountProcessor(parser) {
    override fun received(
        existing: IMap<String, Any>,
        height: Int?,
    ): Pair<IMap<String, Any>, Boolean> {
        val orders = parser.asMap(existing["orders"])
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
        existing: IMap<String, Any>?,
        payload: IList<Any>?,
    ): IMap<String, Any>? {
        return if (payload != null) {
            val modified = iMutableMapOf<String, Any>()
            for (itemPayload in payload) {
                val data = parser.asMap(itemPayload)
                if (data != null) {
                    val subaccountNumber = parser.asInt(data?.get("subaccountNumber"))
                    if (subaccountNumber != null) {
                        val key = "$subaccountNumber"
                        val existing = parser.asMap(existing?.get(key))
                        val subaccount = subaccountProcessor.received(existing, data, true)
                        modified.safeSet(key, subaccount)
                    }
                }
            }
            return modified
        } else null
    }

    override fun subaccountPayload(content: IMap<String, Any>): IMap<String, Any>? {
        return parser.asMap(content["subaccount"])
            ?: parser.asMap(content["subaccounts"])
    }

    override fun socket(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        subscribed: Boolean,
        height: Int?,
    ): IMap<String, Any> {
        val modified = super.socket(existing, content, subscribed, height).mutable()

        val assetPositionsPayload =
            parser.asList(content["assetPositions"]) as? IList<IMap<String, Any>>
        if (assetPositionsPayload != null) {
            val existing = parser.asMap(modified["assetPositions"])
            modified.safeSet(
                "assetPositions",
                assetPositionsProcessor.receivedChanges(existing, assetPositionsPayload)
            )
        }
        modified["quoteBalance"] = calculateQuoteBalance(modified, content)
        return modified
    }

    override fun received(
        existing: IMap<String, Any>,
        height: Int?,
    ): Pair<IMap<String, Any>, Boolean> {
        return subaccountProcessor.received(existing, height)
    }

    internal fun orderCanceled(
        existing: IMap<String, Any>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<IMap<String, Any>, Boolean> {
        val subaccountIndex = "$subaccountNumber"
        val subaccount = parser.asMap(existing[subaccountIndex])
        if (subaccount != null) {
            val (modifiedSubaccount, updated) = subaccountProcessor.orderCanceled(
                subaccount,
                orderId
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
        existing: IMap<String, Any>?,
        payload: IList<Any>?,
    ): IMap<String, Any>? {
        return if (payload != null) {
            val modified = iMutableMapOf<String, Any>()
            for (itemPayload in payload) {
                val data = parser.asMap(itemPayload)
                if (data != null) {
                    val denom = parser.asString(data?.get("denom"))
                    if (denom != null) {
                        val key = "$denom"
                        val existing =
                            parser.asMap(existing?.get(key))?.mutable() ?: iMutableMapOf()
                        existing.safeSet("denom", denom)
                        existing.safeSet("amount", parser.asDouble(data?.get("amount")))
                        modified.safeSet(key, existing)
                    }
                }
            }
            return modified
        } else null
    }
}

@Suppress("UNCHECKED_CAST")
internal class V4AccountProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val subaccountsProcessor = V4SubaccountsProcessor(parser)
    private val balancesProcessor = V4AccountBalancesProcessor(parser)

    internal fun receivedAccountBalances(
        existing: IMap<String, Any>?,
        payload: IList<Any>?,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val balances = parser.asMap(parser.value(existing, "balances"))
        val modifiedBalances = balancesProcessor.receivedBalances(balances, payload)
        modified.safeSet("balances", modifiedBalances)
        return modified
    }

    internal fun receivedSubaccounts(
        existing: IMap<String, Any>?,
        payload: IList<Any>?,
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val subaccounts = parser.asMap(parser.value(existing, "subaccounts"))
        val modifiedsubaccounts = subaccountsProcessor.receivedSubaccounts(subaccounts, payload)
        modified.safeSet("subaccounts", modifiedsubaccounts)
        return modified
    }

    internal fun subscribed(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        height: Int?,
    ): IMap<String, Any>? {
        val subaccountNumber = parser.asInt(parser.value(content, "subaccount.subaccountNumber"))
        return if (subaccountNumber != null) {
            val modified = existing?.mutable() ?: iMutableMapOf()
            val subaccount = parser.asMap(parser.value(existing, "subaccounts.$subaccountNumber"))
            val modifiedsubaccount = subaccountsProcessor.subscribed(subaccount, content, height)
            modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
            return modified
        } else existing
    }

    internal fun channel_data(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        info: SocketInfo,
        height: Int?,
    ): IMap<String, Any>? {
        val subaccountNumber = parser.asInt(parser.value(content, "subaccounts.subaccountNumber"))
            ?: subaccountNumberFromInfo(info)

        return if (subaccountNumber != null) {
            val modified = existing?.toIMutableMap() ?: iMutableMapOf()
            val subaccount = parser.asMap(parser.value(existing, "subaccounts.$subaccountNumber"))
            val modifiedsubaccount = subaccountsProcessor.channel_data(subaccount, content, height)
            modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
            return modified
        } else existing
    }

    private fun subaccountNumberFromInfo(info: SocketInfo): Int? {
        val id = info.id
        return if (id != null) {
            val elements = id.split("/")
            if (elements.size == 2) {
                parser.asInt(elements.lastOrNull())
            } else null
        } else null
    }

    internal fun received(
        existing: IMap<String, Any>,
        subaccountNumber: Int,
        height: Int?,
    ): Pair<IMap<String, Any>, Boolean> {
        val subaccount = parser.asMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        if (subaccount != null) {
            val (modifiedsubaccount, subaccountUpdated) = subaccountsProcessor.received(
                subaccount,
                height
            )
            if (subaccountUpdated) {
                val modified = existing.toIMutableMap()
                modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    internal fun orderCanceled(
        existing: IMap<String, Any>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<IMap<String, Any>, Boolean> {
        val subaccounts = parser.asMap(parser.value(existing, "subaccounts"))?.mutable()
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
}
