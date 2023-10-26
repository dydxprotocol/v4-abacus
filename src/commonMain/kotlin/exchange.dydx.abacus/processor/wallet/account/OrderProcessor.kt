package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.OrderTypeProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

/*

      {
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


@Suppress("UNCHECKED_CAST")
internal class OrderProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
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
        "SELL" to "APP.GENERAL.SELL"
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
    )
    private val timeInForceStringKeys = mapOf(
        "FOK" to "APP.TRADE.FILL_OR_KILL",
        "IOC" to "APP.TRADE.IMMEDIATE_OR_CANCEL",
        "GTT" to "APP.TRADE.GOOD_TIL_TIME"
    )
    private val cancelReasonStringKeys = mapOf(
        "COULD_NOT_FILL" to "APP.TRADE.COULD_NOT_FILL",
        "EXPIRED" to "APP.TRADE.EXPIRED",
        "FAILED" to "APP.TRADE.FAILED",
        "POST_ONLY_WOULD_CROSS" to "APP.TRADE.POST_ONLY_WOULD_CROSS",
        "SELF_TRADE" to "APP.TRADE.SELF_TRADE",
        "UNDERCOLLATERALIZED" to "APP.TRADE.UNDERCOLLATERALIZED",
        "USER_CANCELED" to "APP.TRADE.USER_CANCELED"
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
            "goodTilBlockTime" to "goodTilBlockTime"
        ),
        "bool" to mapOf(
            "postOnly" to "postOnly",
            "reduceOnly" to "reduceOnly"
        ),
        "int" to mapOf(
            "clobPairId" to "clobPairId",
            "orderFlags" to "orderFlags",
            "goodTilBlock" to "goodTilBlock",
            "clientMetadata" to "clientMetadata",
            "createdAtHeight" to "createdAtHeight"
        )
    )

    private fun shouldUpdate(existing: Map<String, Any>?, payload: Map<String, Any>): Boolean {
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
        return when (parser.asString(existing?.get("status"))) {
            "FILLED", "CANCELED" -> false
            "BEST_EFFORT_CANCELED" -> {
                val newStatus = parser.asString(payload["status"])
                (newStatus == "FILLED") || (newStatus == "CANCELED") || (newStatus == "BEST_EFFORT_CANCELED")
            }

            else -> true
        }
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        height: Int?,
    ): Map<String, Any>? {
        return if (shouldUpdate(existing, payload)) {
            val modified = transform(existing, payload, orderKeyMap)
            if (modified["marketId"] == null) {
                modified.safeSet("marketId", payload["ticker"])
            }
            if (modified["id"] == null) {
                modified.safeSet("id", payload["clientId"])
            }
            val size = parser.asDouble(payload["size"])
            if (size != null) {
                var totalFilled = parser.asDouble(payload["totalFilled"])
                var remainingSize = parser.asDouble(payload["remainingSize"])
                if (totalFilled != null && remainingSize == null) {
                    remainingSize = size - totalFilled
                } else if (totalFilled == null && remainingSize != null) {
                    totalFilled = size - remainingSize
                }
                if (totalFilled != null && remainingSize != null) {
                    modified.safeSet("totalFilled", totalFilled)
                    modified.safeSet("remainingSize", remainingSize)

                    if (totalFilled != Numeric.double.ZERO && modified["status"] == "OPEN") {
                        modified.safeSet("status", "PARTIALLY_FILLED")
                    }
                }
            }

            modified.safeSet("cancelReason", payload["removalReason"] ?: payload["cancelReason"])

            modified.safeSet(
                "type",
                OrderTypeProcessor.orderType(
                    parser.asString(modified["type"]),
                    parser.asInt(modified["clientMetadata"])
                )
            )

            updateResource(modified)
            val (returnValue, updated) = updateHeight(modified, height);
            return returnValue
        } else existing
    }

    private fun updateResource(modified: MutableMap<String, Any>) {
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

    internal fun updateHeight(
        existing: Map<String, Any>,
        height: Int?,
    ): Pair<Map<String, Any>, Boolean> {
        if (height != null) {
            when (val status = parser.asString(existing["status"])) {
                "BEST_EFFORT_CANCELED" -> {
                    val goodTilBlock = parser.asInt(existing["goodTilBlock"])
                    if (goodTilBlock != null && goodTilBlock != 0 && height >= goodTilBlock) {
                        val modified = existing.mutable();
                        modified["status"] = "CANCELED"
                        updateResource(modified)
                        return Pair(modified, true)
                    }
                }

                else -> {}
            }
        }
        return Pair(existing, false)
    }

    internal fun canceled(
        existing: Map<String, Any>,
    ): Map<String, Any> {
        val modified = existing.mutable()
        modified["status"] = "BEST_EFFORT_CANCELED"
        modified["cancelReason"] = "USER_CANCELED"
        updateResource(modified)
        return modified
    }
}
