package exchange.dydx.abacus.processor.wallet.account

import abs
import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.account.SubaccountPositionResources
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPositionSide

/*
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
        }


      {
        "id": "70af36fe-f803-5185-b2bb-69eced5d73b5",
        "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
        "market": "ETH-USD",
        "side": "LONG",
        "status": "OPEN",
        "size": "192.096",
        "maxSize": "192.096",
        "entryPrice": "1314.480485",
        "exitPrice": "1265.594735",
        "openTransactionId": "45324205",
        "closeTransactionId": null,
        "lastTransactionId": "45929918",
        "closedAt": null,
        "updatedAt": "2022-06-30T01:01:10.234Z",
        "createdAt": "2022-06-30T01:01:10.234Z",
        "sumOpen": "377.245",
        "sumClose": "185.149",
        "netFunding": "-4155.221089",
        "realizedPnl": "-13206.368924"
      }

    "ETH-USD": {
      "id": "ETH-USD",
      "assetId": "ETH",
      "entryPrice": 1128.550466,
      "exitPrice": 1118.527317,
      "createdAtMilliseconds": 802452345245,
      "closedAtMilliseconds": null,
      "netFunding": 297.092137,
      "realizedPnl": -278.046176,
      "realizedPnlPercent": 0.34,
      "unrealizedPnl": 8180.039253,
      "unrealizedPnlPercent": -0.12,
      "size": {
        "current": 102.456,
        "postOrder": 105.34,
        "postAllOrders": 109.45
      }
    }
 */

internal interface PerpetualPositionProcessorProtocol {
    fun process(
        existing: InternalPerpetualPosition?,
        payload: IndexerPerpetualPositionResponseObject?,
    ): InternalPerpetualPosition?

    fun processChanges(
        existing: InternalPerpetualPosition?,
        payload: IndexerPerpetualPositionResponseObject?,
    ): InternalPerpetualPosition?
}

internal class PerpetualPositionProcessor(
    parser: ParserProtocol,
    private val localizer: LocalizerProtocol?,
) : BaseProcessor(parser), PerpetualPositionProcessorProtocol {
    private val sideStringKeys = mapOf(
        "LONG" to "APP.GENERAL.LONG_POSITION_SHORT",
        "SHORT" to "APP.GENERAL.SHORT_POSITION_SHORT",
        "NONE" to "APP.GENERAL.NONE",
    )
    private val indicators = mapOf(
        "LONG" to "long",
        "SHORT" to "short",
        "NONE" to "none",
    )
    private val positionKeyMap = mapOf(
        "string" to mapOf(
            "market" to "id",
            "status" to "status",
        ),
        "double" to mapOf(
            "maxSize" to "maxSize",
            "exitPrice" to "exitPrice",
            "netFunding" to "netFunding",
            "unrealizedPnl" to "unrealizedPnl",
        ),
        "datetime" to mapOf(
            "createdAt" to "createdAt",
            "closedAt" to "closedAt",
        ),
    )

    private val currentPositionKeyMap = mapOf(
        "double" to mapOf(
            "entryPrice" to "entryPrice",
            "realizedPnl" to "realizedPnl",
        ),
    )

    override fun process(
        existing: InternalPerpetualPosition?,
        payload: IndexerPerpetualPositionResponseObject?,
    ): InternalPerpetualPosition? {
        return if (payload != null) {
            val sideStringKey = sideStringKeys[payload.side?.value]
            val sideString = if (sideStringKey != null) {
                localizer?.localize(sideStringKey)
            } else {
                sideStringKey
            }
            val size = parser.asDouble(payload.size)
            val signedSize =
                if (size != null) {
                    if (payload.side == IndexerPositionSide.SHORT) (size.abs() * -1.0) else size
                } else {
                    null
                }

            InternalPerpetualPosition(
                market = payload.market,
                status = payload.status,
                side = payload.side,
                size = signedSize,
                maxSize = parser.asDouble(payload.maxSize),
                entryPrice = parser.asDouble(payload.entryPrice),
                realizedPnl = parser.asDouble(payload.realizedPnl),
                createdAt = parser.asDatetime(payload.createdAt),
                createdAtHeight = parser.asDouble(payload.createdAtHeight),
                sumOpen = parser.asDouble(payload.sumOpen),
                sumClose = parser.asDouble(payload.sumClose),
                netFunding = parser.asDouble(payload.netFunding),
                unrealizedPnl = parser.asDouble(payload.unrealizedPnl),
                closedAt = parser.asDatetime(payload.closedAt),
                exitPrice = parser.asDouble(payload.exitPrice),
                subaccountNumber = payload.subaccountNumber,
                resources = SubaccountPositionResources(
                    sideString = TradeStatesWithStringValues(
                        current = sideString,
                        postOrder = null,
                        postAllOrders = null,
                    ),
                    sideStringKey = TradeStatesWithStringValues(
                        current = sideStringKey,
                        postOrder = null,
                        postAllOrders = null,
                    ),
                    indicator = TradeStatesWithStringValues(
                        current = indicators[payload.side?.value],
                        postOrder = null,
                        postAllOrders = null,
                    ),
                ),
            )
        } else {
            existing
        }
    }

    override fun processChanges(
        existing: InternalPerpetualPosition?,
        payload: IndexerPerpetualPositionResponseObject?,
    ): InternalPerpetualPosition? {
        // Keep the position even if it is closed in the internal state.
        // Filter at output
        return if (payload != null) {
            process(existing, payload)
        } else {
            existing
        }
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        var modified = transform(existing, payload, positionKeyMap)
        modified = transform(modified, payload, "current", currentPositionKeyMap)

        val size = parser.asDouble(payload["size"])
        val sizeMap = size(parser.asNativeMap(payload["size"]), size, parser.asString(payload["side"]))
        modified.safeSet("size", sizeMap)

        parser.asInt(payload["subaccountNumber"])?.run {
            modified.safeSet("subaccountNumber", this)

            // the v4_parent_subaccount message has subaccountNumber available but v4_orders does not
            modified.safeSet("marginMode", if (this >= NUM_PARENT_SUBACCOUNTS) MarginMode.Isolated.rawValue else MarginMode.Cross.rawValue)
        }

        parser.asString(modified["id"])?.let { marketId ->
            MarketId.getDisplayId(marketId).let { displayId ->
                modified["displayId"] = displayId
            }

            MarketId.getAssetId(marketId)?.let { assetId ->
                modified["assetId"] = assetId
            }
        }

        val resources = parser.asNativeMap(modified["resources"])?.toMutableMap()
            ?: mutableMapOf<String, Any>()
        val side = parser.asString(payload["side"]) ?: "NONE"
        sideStringKeys[side]?.let {
            resources["sideStringKey"] = mapOf("current" to it)
        }
        indicators[side]?.let {
            resources["indicator"] = mapOf("current" to it)
        }
        modified["resources"] = resources
        return modified
    }

    private fun size(existing: Map<String, Any>?, size: Double?, side: String?): Map<String, Any>? {
        val sizeMap = existing?.toMutableMap() ?: mutableMapOf()
        val signedSize = if (size != null) if (side == "SHORT") (size.abs() * -1.0) else size else null
        sizeMap.safeSet("current", signedSize)
        return sizeMap
    }

    private fun side(size: Double?): String {
        if (size != null) {
            if (size > Numeric.double.ZERO) {
                return "LONG"
            } else if (size < Numeric.double.ZERO) {
                return "SHORT"
            }
        }
        return "NONE"
    }

    internal fun receivedChangesDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?
    ): Map<String, Any>? {
        // Keep the position even if it is closed in the internal state.
        // Filter at output
        return if (payload != null) {
            received(existing, payload)
        } else {
            existing
        }
    }
}
