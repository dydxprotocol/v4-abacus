package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class TriggerOrdersInputPriceDiff(
    val percent: Double?,
    val usdc: Double?,
    val input: String?,
) {
    companion object {
        internal fun create(
            existing: TriggerOrdersInputPriceDiff?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TriggerOrdersInputPriceDiff? {
            DebugLogger.log("creating Trigger Orders Input Price Diff\n")

            data?.let {
                val percent = parser.asDouble(data["percent"])
                val usdc = parser.asDouble(data["usdc"])
                val input = parser.asString(data["input"])

                return if (existing?.percent != percent ||
                    existing?.usdc != usdc ||
                    existing?.input != input
                ) {
                    TriggerOrdersInputPriceDiff(percent, usdc, input)
                } else {
                    existing
                }
            }

            DebugLogger.log("Trigger Orders Input Price Diff not valid\n")
            return null
        }
    }
}

@JsExport
@Serializable
data class TriggerOrdersInputPrice(
    val limitPrice: Double?,
    val triggerPrice: Double?,
    val triggerPriceDiff: TriggerOrdersInputPriceDiff?,
    // val triggerPercent: Double?,
    // val triggerInput: String?,
) {
    companion object {
        internal fun create(
            existing: TriggerOrdersInputPrice?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TriggerOrdersInputPrice? {
            DebugLogger.log("creating Trigger Orders Input Price\n")

            data?.let {
                val limitPrice = parser.asDouble(data["limitPrice"])
                val triggerPrice = parser.asDouble(data["triggerPrice"])
                val triggerPriceDiff = TriggerOrdersInputPriceDiff.create(
                    existing?.triggerPriceDiff,
                    parser,
                    parser.asMap(data["triggerPriceDiff"]),
                )
                // val triggerPercent = parser.asDouble(data["triggerPercent"])
                // val triggerInput = parser.asString(data["triggerInput"])

                return if (existing?.limitPrice != limitPrice ||
                    existing?.triggerPrice != triggerPrice ||
                    existing?.triggerPriceDiff != triggerPriceDiff
                ) {
                    TriggerOrdersInputPrice(limitPrice, triggerPrice, triggerPriceDiff)
                } else {
                    existing
                }
            }
            DebugLogger.log("Trigger Orders Input Price not valid\n")
            return null
        }
    }
}

@JsExport
@Serializable
data class TriggerOrder(
    val type: OrderType?,
    val price: TriggerOrdersInputPrice?,
) {
    companion object {
        internal fun create(
            existing: TriggerOrder?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TriggerOrder? {
            DebugLogger.log("creating Trigger Order\n")

            data?.let {
                val type = parser.asString(data["type"])?.let {
                    OrderType.invoke(it)
                }
                val price = TriggerOrdersInputPrice.create(
                    existing?.price,
                    parser,
                    parser.asMap(data["price"]),
                )

                return if (
                    existing?.type != type ||
                    existing?.price != price
                ) {
                    TriggerOrder(type, price)
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
    // val stopLossPrice: TriggerOrdersInputPrice?,
    // val takeProfitPrice: TriggerOrdersInputPrice?,
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

                return if (existing?.marketId != marketId ||
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
