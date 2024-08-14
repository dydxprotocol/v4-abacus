package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountPendingPosition(
    val assetId: String,
    val displayId: String,
    val marketId: String,
    val firstOrderId: String,
    val orderCount: Int,
    val freeCollateral: TradeStatesWithDoubleValues?,
    val quoteBalance: TradeStatesWithDoubleValues?, // available for isolated market position
    val equity: TradeStatesWithDoubleValues?, // available for isolated market position
) {
    companion object {
        internal fun create(
            existing: SubaccountPendingPosition?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
        ): SubaccountPendingPosition? {
            Logger.d { "creating Account Pending Position\n" }
            data?.let {
                val assetId = parser.asString(data["assetId"]) ?: return null
                val displayId = parser.asString(data["displayId"]) ?: return null
                val marketId = parser.asString(data["marketId"]) ?: return null
                val firstOrderId = parser.asString(data["firstOrderId"]) ?: return null
                val orderCount = parser.asInt(data["orderCount"]) ?: return null
                val freeCollateral = TradeStatesWithDoubleValues.create(
                    null,
                    parser,
                    parser.asMap(data["freeCollateral"]),
                )
                val quoteBalance = TradeStatesWithDoubleValues.create(
                    null,
                    parser,
                    parser.asMap(data["quoteBalance"]),
                )
                val equity = TradeStatesWithDoubleValues.create(
                    null,
                    parser,
                    parser.asMap(data["equity"]),
                )

                return if (existing?.assetId != assetId ||
                    existing?.displayId != displayId ||
                    existing.marketId != marketId ||
                    existing.firstOrderId != firstOrderId ||
                    existing.orderCount != orderCount ||
                    existing.freeCollateral !== freeCollateral ||
                    existing.quoteBalance !== quoteBalance ||
                    existing.equity !== equity
                ) {
                    SubaccountPendingPosition(
                        assetId = assetId,
                        displayId = displayId,
                        marketId = marketId,
                        firstOrderId = firstOrderId,
                        orderCount = orderCount,
                        freeCollateral = freeCollateral,
                        quoteBalance = quoteBalance,
                        equity = equity,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Account Pending Position not valid" }
            return null
        }

        private fun positionSide(size: TradeStatesWithDoubleValues): TradeStatesWithPositionSides {
            val current = positionSide(size.current)
            val postOrder = positionSide(size.postOrder)
            val postAllOrders = positionSide(size.postAllOrders)
            return TradeStatesWithPositionSides(current, postOrder, postAllOrders)
        }

        private fun positionSide(size: Double?): PositionSide? {
            return if (size != null) {
                if (size > 0) {
                    PositionSide.LONG
                } else if (size < 0) {
                    PositionSide.SHORT
                } else {
                    PositionSide.NONE
                }
            } else {
                null
            }
        }
    }
}
