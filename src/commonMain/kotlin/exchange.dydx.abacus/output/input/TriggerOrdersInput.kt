package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalTriggerOrderState
import exchange.dydx.abacus.state.InternalTriggerOrdersInputState
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class TriggerOrderInputSummary(
    val price: Double?,
    val size: Double?,
) {
    companion object {
        internal fun create(
            existing: TriggerOrderInputSummary?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TriggerOrderInputSummary? {
            Logger.d { "creating Trigger Order Input Summary\n" }

            data?.let {
                val price = parser.asDouble(data["price"])
                val size = parser.asDouble(data["size"])

                return if (existing?.price != price || existing?.size != size) {
                    TriggerOrderInputSummary(price, size)
                } else {
                    existing
                }
            }
            Logger.d { "Trigger Order Input not valid\n" }
            return null
        }
    }
}

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
            Logger.d { "creating Trigger Price\n" }

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
            Logger.d { "Trigger Price not valid\n" }
            return null
        }
    }
}

@JsExport
@Serializable
data class TriggerOrder(
    val orderId: String?,
    val size: Double?,
    val type: OrderType?,
    val side: OrderSide?,
    val price: TriggerPrice?,
    val summary: TriggerOrderInputSummary?,
) {
    companion object {
        internal fun create(
            state: InternalTriggerOrderState?
        ): TriggerOrder? {
            return if (state != null) {
                TriggerOrder(
                    orderId = state.orderId,
                    size = state.size,
                    type = state.type,
                    side = state.side,
                    price = state.price,
                    summary = state.summary,
                )
            } else {
                null
            }
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
            state: InternalTriggerOrdersInputState?
        ): TriggerOrdersInput? {
            return if (state != null) {
                TriggerOrdersInput(
                    marketId = state.marketId,
                    size = state.size,
                    stopLossOrder = TriggerOrder.create(state.stopLossOrder),
                    takeProfitOrder = TriggerOrder.create(state.takeProfitOrder),
                )
            } else {
                null
            }
        }
    }
}
