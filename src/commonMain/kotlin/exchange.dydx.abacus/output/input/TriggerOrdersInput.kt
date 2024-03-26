package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class TriggerPrice(
    val limitPrice: Double?,
    val triggerPrice: Double?,
    val percentDiff: Double?,
    val usdcDiff: Double?,
    val input: String?,
) {
    companion object {
        internal fun create(
            existing: TriggerPrice?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TriggerPrice? {
            DebugLogger.log("creating Trigger Price\n")

            data?.let {
                val limitPrice = parser.asDouble(data["limitPrice"])
                val triggerPrice = parser.asDouble(data["triggerPrice"])
                val percentDiff = parser.asDouble(data["percentDiff"])
                val usdcDiff = parser.asDouble(data["usdcDiff"])
                val input = parser.asString(data["input"])

                return if (existing?.limitPrice != limitPrice ||
                    existing?.triggerPrice != triggerPrice ||
                    existing?.percentDiff != percentDiff ||
                    existing?.usdcDiff != usdcDiff ||
                    existing?.input != input
                ) {
                    TriggerPrice(limitPrice, triggerPrice, percentDiff, usdcDiff, input)
                } else {
                    existing
                }
            }
            DebugLogger.log("Trigger Price not valid\n")
            return null
        }
    }
}

@JsExport
@Serializable
data class TriggerOrder(
    val orderId: String?,
    val type: OrderType?,
    val side: OrderSide?,
    val price: TriggerPrice?,
) {
    companion object {
        internal fun create(
            existing: TriggerOrder?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TriggerOrder? {
            DebugLogger.log("creating Trigger Order\n")

            data?.let {
                val orderId = parser.asString(data["orderId"])
                val type = parser.asString(data["type"])?.let {
                    OrderType.invoke(it)
                }
                val side = parser.asString(data["side"])?.let {
                    OrderSide.invoke(it)
                }
                val price = TriggerPrice.create(
                    existing?.price,
                    parser,
                    parser.asMap(data["price"]),
                )

                return if (
                    existing?.orderId != orderId ||
                    existing?.type != type ||
                    existing?.side != side ||
                    existing?.price != price
                ) {
                    TriggerOrder(orderId, type, side, price)
                } else {
                    existing
                }
            }
            DebugLogger.log("Trigger Order not valid\n")
            return null
        }
    }
}

@JsExport
@Serializable
data class TriggerOrdersInput(
    val marketId: String?,
    val size: Double?,
    val stopLossOrder: TriggerOrder?,
    val takeProfitOrder: TriggerOrder?,
) {
    companion object {
        internal fun create(
            existing: TriggerOrdersInput?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TriggerOrdersInput? {
            DebugLogger.log("creating Trigger Orders Input\n")

            data?.let {
                val marketId = parser.asString(data["marketId"])
                val size = parser.asDouble(data["size"])

                val stopLossOrder =
                    TriggerOrder.create(
                        existing?.stopLossOrder,
                        parser,
                        parser.asMap(data["stopLossOrder"]),
                    )
                val takeProfitOrder =
                    TriggerOrder.create(
                        existing?.takeProfitOrder,
                        parser,
                        parser.asMap(data["takeProfitOrder"]),
                    )

                return if (
                    existing?.marketId != marketId ||
                    existing?.size != size ||
                    existing?.stopLossOrder != stopLossOrder ||
                    existing?.takeProfitOrder != takeProfitOrder
                ) {
                    TriggerOrdersInput(marketId, size, stopLossOrder, takeProfitOrder)
                } else {
                    existing
                }
            }
            DebugLogger.log("Trigger Orders Input not valid\n")
            return null
        }
    }
}
