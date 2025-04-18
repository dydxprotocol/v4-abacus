package exchange.dydx.abacus.processor.wallet.account

import abs
import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.account.SubaccountPositionResources
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalPerpetualPosition
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
}
