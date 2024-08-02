package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.SubaccountOrderResources
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderTimeInForce
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.OrderTypeProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerAPIOrderStatus
import indexer.models.IndexerCompositeOrderObject
import kotlinx.datetime.Instant

/*

      {
        "subaccountNumber: 0, // new field
        "id": "3c5193d7a49805ffcf231af1ed446188f04aaa6756bf9df7b5913568b2763d7",
        "clientId": "69967309621008383",
        "market": "ETH-USD",
        "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
        "side": "BUY",
        "size": "0.1",
        "remainingSize": "0.1",
        "limitFee": "0.002",
        "price": "1500",
        "triggerPrice": null,
        "trailingPercent": null,
        "type": "LIMIT",
        "status": "OPEN",
        "signature": "06f422ea494514293c6da82b70aca83f30718a01beb942f3e877a3ce8411d8f700d227caf5a57357df3dd66b38e2faff07147f29db539696e7d4799f32063172",
        "timeInForce": "GTT",
        "postOnly": false,
        "cancelReason": null,
        "expiresAt": "2022-08-29T22:45:30.776Z",
        "unfillableAt": null,
        "updatedAt": "2022-08-01T22:25:31.139Z",
        "createdAt": "2022-08-01T22:25:31.111Z",
        "reduceOnly": false,
        "country": "JP",
        "client": "01",
        "reduceOnlySize": null
      }

      to

        {
          "subaccountNumber": 0,
          "id": "45537274a3ef9afca657d9de73fb5fe5762f97336ce5502da0581e394dccdeb",
          "marketId": "ETH-USD",
          "price": 1192.5,
          "triggerPrice": null,
          "trailingPercent": null,
          "size": 2,
          "remainingSize": 2,
          "createdAtMilliseconds": 80092349090234,
          "unfillableAtMilliseconds": null,
          "expiresAtMilliseconds": 80092349090234,
          "postOnly": false,
          "reduceOnly": false,
          "cancelReason": null,
          "resources": {
            "sideStringKey": "BUY",
            "typeStringKey": "LIMIT",
            "statusStringKey": "OPEN",
            "timeInForceStringKey": "GTT"
          }
        }

 */

internal interface OrderProcessorProtocol {
    fun process(
        existing: SubaccountOrder?,
        payload: IndexerCompositeOrderObject,
        subaccountNumber: Int,
        height: BlockAndTime?,
    ): SubaccountOrder?

    fun updateHeight(
        existing: SubaccountOrder,
        height: BlockAndTime?,
    ): Pair<SubaccountOrder, Boolean>

    fun canceled(
        existing: SubaccountOrder,
    ): SubaccountOrder
}

@Suppress("UNCHECKED_CAST")
internal class OrderProcessor(
    parser: ParserProtocol,
    private val localizer: LocalizerProtocol?,
) : BaseProcessor(parser), OrderProcessorProtocol {
    private val typeStringKeys = mapOf(
        "MARKET" to "APP.TRADE.MARKET_ORDER_SHORT",
        "STOP_MARKET" to "APP.TRADE.STOP_MARKET",
        "TAKE_PROFIT_MARKET" to "APP.TRADE.TAKE_PROFIT_MARKET_SHORT",
        "LIMIT" to "APP.TRADE.LIMIT_ORDER_SHORT",
        "STOP_LIMIT" to "APP.TRADE.STOP_LIMIT",
        "TAKE_PROFIT" to "APP.TRADE.TAKE_PROFIT_LIMIT_SHORT",
        "TRAILING_STOP" to "APP.TRADE.TRAILING_STOP",
        "LIQUIDATED" to "APP.TRADE.LIQUIDATED",
        "LIQUIDATION" to "APP.TRADE.LIQUIDATION",
    )
    private val sideStringKeys = mapOf(
        "BUY" to "APP.GENERAL.BUY",
        "SELL" to "APP.GENERAL.SELL",
    )
    private val statusStringKeys = mapOf(
        "OPEN" to "APP.TRADE.OPEN_STATUS",
        "CANCELED" to "APP.TRADE.CANCELED",
        "FILLED" to "APP.TRADE.ORDER_FILLED",
        "PENDING" to "APP.TRADE.PENDING",
        "BEST_EFFORT_OPENED" to "APP.TRADE.PENDING",
        "BEST_EFFORT_CANCELED" to "APP.TRADE.CANCELING",
        "UNTRIGGERED" to "APP.TRADE.UNTRIGGERED",
        "PARTIALLY_FILLED" to "APP.TRADE.PARTIALLY_FILLED",
        "PARTIALLY_CANCELED" to "APP.TRADE.PARTIALLY_FILLED",
    )
    private val timeInForceStringKeys = mapOf(
        "FOK" to "APP.TRADE.FILL_OR_KILL",
        "IOC" to "APP.TRADE.IMMEDIATE_OR_CANCEL",
        "GTT" to "APP.TRADE.GOOD_TIL_TIME",
    )
    private val cancelReasonStringKeys = mapOf(
        "COULD_NOT_FILL" to "APP.TRADE.COULD_NOT_FILL",
        "EXPIRED" to "APP.TRADE.EXPIRED",
        "FAILED" to "APP.TRADE.FAILED",
        "POST_ONLY_WOULD_CROSS" to "APP.TRADE.POST_ONLY_WOULD_CROSS",
        "SELF_TRADE" to "APP.TRADE.SELF_TRADE",
        "UNDERCOLLATERALIZED" to "APP.TRADE.UNDERCOLLATERALIZED",
        "USER_CANCELED" to "APP.TRADE.USER_CANCELED",
    )

    private val orderKeyMap = mapOf(
        "string" to mapOf(
            "id" to "id",
            "clientId" to "clientId",
            "market" to "marketId",
            "side" to "side",
            "type" to "type",
            "status" to "status",
            "timeInForce" to "timeInForce",
//            "cancelReason" to "cancelReason",
//            "removalReason" to "removalReason"
        ),
        "double" to mapOf(
            "price" to "price",
            "triggerPrice" to "triggerPrice",
            "trailingPercent" to "trailingPercent",
            "size" to "size",
//            "remainingSize" to "remainingSize",
//            "totalFilled" to "totalFilled"
        ),
        "datetime" to mapOf(
            "createdAt" to "createdAt",
            "unfillableAt" to "unfillableAt",
            "expiresAt" to "expiresAt",
            "updatedAt" to "updatedAt",
            "goodTilBlockTime" to "goodTilBlockTime",
        ),
        "bool" to mapOf(
            "postOnly" to "postOnly",
            "reduceOnly" to "reduceOnly",
        ),
        "int" to mapOf(
            "subaccountNumber" to "subaccountNumber",
            "clobPairId" to "clobPairId",
            "orderFlags" to "orderFlags",
            "goodTilBlock" to "goodTilBlock",
            "clientMetadata" to "clientMetadata",
            "createdAtHeight" to "createdAtHeight",
        ),
    )

    override fun process(
        existing: SubaccountOrder?,
        payload: IndexerCompositeOrderObject,
        subaccountNumber: Int,
        height: BlockAndTime?
    ): SubaccountOrder? {
        if (!shouldUpdate(existing, payload)) {
            return existing
        }

        val orderSubaccountNumber = payload.subaccountNumber?.toInt() ?: subaccountNumber

        val id = payload.id ?: return null

        val typeString = OrderTypeProcessor.orderType(
            type = payload.type?.name,
            clientMetadata = parser.asInt(payload.clientMetadata),
        )
        val type = typeString?.let { OrderType.invoke(rawValue = it) } ?: return null

        val side = OrderSide.invoke(payload.side?.value) ?: return null
        val status = OrderStatus.invoke(payload.status?.value) ?: return null
        val marketId = payload.ticker ?: return null
        val price = parser.asDouble(payload.price) ?: return null
        val size = parser.asDouble(payload.size) ?: return null

        val orderFlags = parser.asInt(payload.orderFlags)
        val cancelReason = payload.removalReason

        val totalFilled = parser.asDouble(payload.totalFilled) ?: Numeric.double.ZERO
        val remainingSize = size - totalFilled

        val updatedAtMilliseconds =
            parser.asDatetime(payload.updatedAt)?.toEpochMilliseconds()?.toDouble()
        var modifiedStatus =
            if (totalFilled != Numeric.double.ZERO && remainingSize != Numeric.double.ZERO) {
                when (status) {
                    OrderStatus.Open -> OrderStatus.PartiallyFilled
                    OrderStatus.Canceled -> OrderStatus.PartiallyCanceled
                    else -> status
                }
            } else {
                status
            }
        if (orderFlags != null) {
            // if order is short-term order and indexer returns best effort canceled and has no partial fill
            // treat as a pending order until it's partially filled or finalized
            val isShortTermOrder = orderFlags == 0
            val isBestEffortCanceled = modifiedStatus == OrderStatus.Canceling
            val isUserCanceled = cancelReason == "USER_CANCELED" || cancelReason == "ORDER_REMOVAL_REASON_USER_CANCELED"
            if (isShortTermOrder && isBestEffortCanceled && !isUserCanceled) {
                modifiedStatus = OrderStatus.Pending
            }
        }

        val marginMode = if (orderSubaccountNumber >= NUM_PARENT_SUBACCOUNTS) {
            MarginMode.Isolated
        } else {
            MarginMode.Cross
        }

        val timeInForce = OrderTimeInForce.invoke(payload.timeInForce?.value)

        val resources = createResources(
            side = side,
            type = type,
            status = modifiedStatus,
            timeInForce = timeInForce,
        )

        val order = SubaccountOrder(
            subaccountNumber = orderSubaccountNumber,
            id = id,
            clientId = parser.asInt(payload.clientId),
            type = type,
            side = side,
            status = modifiedStatus,
            timeInForce = timeInForce,
            marketId = marketId,
            clobPairId = parser.asInt(payload.clobPairId),
            orderFlags = orderFlags,
            price = price,
            triggerPrice = parser.asDouble(payload.triggerPrice),
            trailingPercent = null,
            size = size,
            remainingSize = remainingSize,
            totalFilled = totalFilled,
            goodTilBlock = parser.asInt(payload.goodTilBlock),
            goodTilBlockTime = parser.asDatetime(payload.goodTilBlockTime)?.epochSeconds?.toInt(),
            createdAtHeight = parser.asInt(payload.createdAtHeight),
            createdAtMilliseconds = null,
            unfillableAtMilliseconds = null,
            expiresAtMilliseconds = parser.asDatetime(payload.goodTilBlockTime)?.toEpochMilliseconds()?.toDouble(),
            updatedAtMilliseconds = updatedAtMilliseconds,
            postOnly = parser.asBool(payload.postOnly) ?: false,
            reduceOnly = parser.asBool(payload.reduceOnly) ?: false,
            cancelReason = cancelReason,
            resources = resources,
            marginMode = marginMode,
        )

        val (modified, _) = updateHeight(order, height)

        return if (existing != modified) {
            modified
        } else {
            existing
        }
    }

    private fun shouldUpdate(
        existing: SubaccountOrder?,
        payload: IndexerCompositeOrderObject,
    ): Boolean {
        val updatedAt = existing?.updatedAtMilliseconds?.let {
            Instant.fromEpochMilliseconds(it.toLong())
        } ?: existing?.createdAtMilliseconds?.let {
            Instant.fromEpochMilliseconds(it.toLong())
        }
        val incomingUpdatedAt = parser.asDatetime(payload.updatedAt)
        if (updatedAt != null) {
            if (incomingUpdatedAt != null) {
                if (updatedAt < incomingUpdatedAt) {
                    return true
                } else if (updatedAt > incomingUpdatedAt) {
                    return false
                }
                // If they are the same, fall through to the status and filled check
            }
        } else {
            if (incomingUpdatedAt != null) {
                return true
            }
            // If they are both null, fall through to the status and filled check
        }

        val filled = existing?.totalFilled ?: Numeric.double.ZERO
        val incomingFilled = parser.asDouble(payload.totalFilled) ?: Numeric.double.ZERO
        if (incomingFilled > filled) {
            return true
        } else if (incomingFilled < filled) {
            return false
        }

        // If updatedAt and totalFilled are the same, we use status for best guess
        return when (existing?.status) {
            OrderStatus.Canceling -> {
                listOf(
                    IndexerAPIOrderStatus.FILLED,
                    IndexerAPIOrderStatus.CANCELED,
                    IndexerAPIOrderStatus.BEST_EFFORT_CANCELED,
                ).contains(payload.status)
            }

            else -> existing?.status?.isFinalized == false
        }
    }

    private fun shouldUpdateDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Boolean {
        // First, use updatedAt timestamp, available in v3
        val updatedAt = parser.asDatetime(existing?.get("updatedAt"))
            ?: parser.asDatetime(existing?.get("createdAt"))
        val incomingUpdatedAt = parser.asDatetime(payload["updatedAt"])
            ?: parser.asDatetime(payload["createdAt"])
        if (updatedAt != null) {
            if (incomingUpdatedAt != null) {
                if (updatedAt < incomingUpdatedAt) {
                    return true
                } else if (updatedAt > incomingUpdatedAt) {
                    return false
                }
                // If they are the same, fall through to the status and filled check
            }
        } else {
            if (incomingUpdatedAt != null) {
                return true
            }
            // If they are both null, fall through to the status and filled check
        }
        val filled = parser.asDouble(existing?.get("totalFilled")) ?: Numeric.double.ZERO
        val incomingFilled = parser.asDouble(payload["totalFilled"]) ?: Numeric.double.ZERO
        if (incomingFilled > filled) {
            return true
        } else if (incomingFilled < filled) {
            return false
        }
        // If updatedAt and totalFilled are the same, we use status for best guess
        return when (val status = parser.asString(existing?.get("status"))) {
            "BEST_EFFORT_CANCELED" -> {
                val newStatus = parser.asString(payload["status"])
                (newStatus == "FILLED") || (newStatus == "CANCELED") || (newStatus == "BEST_EFFORT_CANCELED")
            }

            else -> !isStatusFinalizedDeprecated(status)
        }
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        return if (shouldUpdateDeprecated(existing, payload)) {
            val modified = transform(existing, payload, orderKeyMap)
            if (modified["marketId"] == null) {
                modified.safeSet("marketId", payload["ticker"])
            }
            if (modified["id"] == null) {
                modified.safeSet("id", payload["clientId"])
            }
            parser.asInt(modified["subaccountNumber"])?.run {
                modified.safeSet("subaccountNumber", this)
                // the v4_parent_subaccount message has subaccountNumber available but v4_orders does not
                modified.safeSet(
                    "marginMode",
                    if (this >= NUM_PARENT_SUBACCOUNTS) MarginMode.Isolated.rawValue else MarginMode.Cross.rawValue,
                )
            }
            parser.asDouble(payload["size"])?.let { size ->
                parser.asDouble(payload["totalFilled"])?.let { totalFilled ->
                    val remainingSize = size - totalFilled
                    modified.safeSet("totalFilled", totalFilled)
                    modified.safeSet("remainingSize", remainingSize)
                    if (totalFilled != Numeric.double.ZERO && remainingSize != Numeric.double.ZERO) {
                        when (modified["status"]) {
                            "OPEN" -> modified.safeSet("status", "PARTIALLY_FILLED")
                            "CANCELED" -> modified.safeSet(
                                "status",
                                "PARTIALLY_CANCELED",
                            ) // finalized state
                        }
                    }
                }
            }

            modified.safeSet("cancelReason", payload["removalReason"] ?: payload["cancelReason"])

            parser.asDouble(payload["orderFlags"])?.let { orderFlags ->
                // if order is short-term order and indexer returns best effort canceled and has no partial fill
                // treat as a pending order until it's partially filled or finalized
                val isShortTermOrder = orderFlags.equals(Numeric.double.ZERO)
                val isBestEffortCanceled = modified["status"] == "BEST_EFFORT_CANCELED"
                val cancelReason = parser.asString(modified["cancelReason"])
                val isUserCanceled =
                    cancelReason == "USER_CANCELED" || cancelReason == "ORDER_REMOVAL_REASON_USER_CANCELED"
                if (isShortTermOrder && isBestEffortCanceled && !isUserCanceled) {
                    modified.safeSet("status", "PENDING")
                }
            }

            modified.safeSet(
                "type",
                OrderTypeProcessor.orderType(
                    parser.asString(modified["type"]),
                    parser.asInt(modified["clientMetadata"]),
                ),
            )

            updateResourceDeprecated(modified)
            val (returnValue, updated) = updateHeightDeprecated(modified, height);
            return returnValue
        } else {
            existing
        }
    }

    private fun createResources(
        side: OrderSide,
        type: OrderType?,
        status: OrderStatus?,
        timeInForce: OrderTimeInForce?,
    ) = SubaccountOrderResources(
        sideString = sideStringKeys[side.rawValue]?.let { localizer?.localize(it) },
        sideStringKey = sideStringKeys[side.rawValue] ?: side.rawValue,
        typeString = typeStringKeys[type?.rawValue]?.let { localizer?.localize(it) },
        typeStringKey = typeStringKeys[type?.rawValue],
        statusString = statusStringKeys[status?.rawValue]?.let { localizer?.localize(it) },
        statusStringKey = statusStringKeys[status?.rawValue],
        timeInForceString = timeInForceStringKeys[timeInForce?.rawValue]?.let {
            localizer?.localize(
                it,
            )
        },
        timeInForceStringKey = timeInForceStringKeys[timeInForce?.rawValue],
    )

    private fun updateResourceDeprecated(modified: MutableMap<String, Any>) {
        val resources = parser.asNativeMap(modified["resources"])?.mutable()
            ?: mutableMapOf()

        val type = parser.asString(modified["type"])

        if (type != null) {
            typeStringKeys[type]?.let {
                resources["typeStringKey"] = it
            }
        }
        (parser.asString(modified["side"])).let {
            sideStringKeys[it]?.let {
                resources["sideStringKey"] = it
            }
        }
        (parser.asString(modified["status"])).let {
            statusStringKeys[it]?.let {
                resources["statusStringKey"] = it
            }
        }
        (parser.asString(modified["timeInForce"])).let {
            timeInForceStringKeys[it]?.let {
                resources["timeInForceStringKey"] = it
            }
        }
        (parser.asString(modified["cancelReason"])).let {
            cancelReasonStringKeys[it]?.let {
                resources["cancelReasonStringKey"] = it
            }
        }
        modified["resources"] = resources
    }

    override fun updateHeight(
        existing: SubaccountOrder,
        height: BlockAndTime?,
    ): Pair<SubaccountOrder, Boolean> {
        if (height == null) {
            return Pair(existing, false)
        }
        when (existing.status) {
            OrderStatus.Pending, OrderStatus.Canceling, OrderStatus.PartiallyFilled -> {
                val goodTilBlock = existing.goodTilBlock
                if (goodTilBlock != null && goodTilBlock != 0 && height.block >= goodTilBlock) {
                    var status = OrderStatus.Canceled
                    val updatedAtMilliseconds = height.time.toEpochMilliseconds().toDouble()

                    val totalFilled = existing.totalFilled
                    if (totalFilled != null) {
                        val remainingSize = existing.size - totalFilled
                        if (totalFilled != Numeric.double.ZERO && remainingSize != Numeric.double.ZERO) {
                            status = OrderStatus.PartiallyCanceled
                        }
                    }

                    val resources = createResources(
                        side = existing.side,
                        type = existing.type,
                        status = status,
                        timeInForce = existing.timeInForce,
                    )
                    return Pair(
                        existing.copy(
                            status = status,
                            updatedAtMilliseconds = updatedAtMilliseconds,
                            resources = resources,
                        ),
                        true,
                    )
                }
            }

            else -> {}
        }

        return Pair(existing, false)
    }

    internal fun updateHeightDeprecated(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        if (height != null) {
            when (val status = parser.asString(existing["status"])) {
                "PENDING", "BEST_EFFORT_CANCELED", "PARTIALLY_FILLED" -> {
                    val goodTilBlock = parser.asInt(existing["goodTilBlock"])
                    if (goodTilBlock != null && goodTilBlock != 0 && height.block >= goodTilBlock) {
                        val modified = existing.mutable();
                        modified["status"] = "CANCELED"
                        modified["updatedAt"] = height.time

                        parser.asDouble(existing["size"])?.let { size ->
                            parser.asDouble(existing["totalFilled"])?.let { totalFilled ->
                                val remainingSize = size - totalFilled
                                if (totalFilled != Numeric.double.ZERO && remainingSize != Numeric.double.ZERO) {
                                    modified["status"] = "PARTIALLY_CANCELED"
                                }
                            }
                        }

                        updateResourceDeprecated(modified)
                        return Pair(modified, true)
                    }
                }

                else -> {}
            }
        }
        return Pair(existing, false)
    }

    private fun isStatusFinalizedDeprecated(status: String?): Boolean {
        // once an order is filled, canceled, or canceled with partial fill
        // there is no need to update status again
        return when (status) {
            "FILLED", "CANCELED", "PARTIALLY_CANCELED" -> true
            else -> false
        }
    }

    override fun canceled(
        existing: SubaccountOrder,
    ): SubaccountOrder {
        var modified = existing
        if (!existing.status.isFinalized) {
            modified = modified.copy(
                status = OrderStatus.Canceling,
            )
        }
        modified = modified.copy(
            cancelReason = "USER_CANCELED",
        )
        val resources = createResources(
            side = modified.side,
            type = modified.type,
            status = modified.status,
            timeInForce = modified.timeInForce,
        )
        return modified.copy(
            resources = resources,
        )
    }

    internal fun canceledDeprecated(
        existing: Map<String, Any>,
    ): Map<String, Any> {
        val modified = existing.mutable()
        // show order status as canceling if frontend initiated cancel
        if (!isStatusFinalizedDeprecated(parser.asString(modified["status"]))) {
            modified["status"] = "BEST_EFFORT_CANCELED"
        }

        modified["cancelReason"] = "USER_CANCELED"
        updateResourceDeprecated(modified)
        return modified
    }
}
