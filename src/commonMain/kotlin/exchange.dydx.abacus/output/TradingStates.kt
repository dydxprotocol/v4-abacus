package exchange.dydx.abacus.output

import exchange.dydx.abacus.protocols.ParserProtocol
import kollections.JsExport
import kotlinx.serialization.Serializable

interface TradingStates<T> {
    val current: T?
    val postOrder: T?
    val postAllOrders: T?
}

@JsExport
@Serializable
data class TradeStatesWithDoubleValues(
    val current: Double?,
    val postOrder: Double?,
    val postAllOrders: Double?
) {
    companion object {
        internal fun create(
            existing: TradeStatesWithDoubleValues?,
            parser: ParserProtocol,
            data: Map<String, Any>?
        ): TradeStatesWithDoubleValues {
            val current = parser.asDouble(data?.get("current"))
            val postOrder = parser.asDouble(data?.get("postOrder"))
            val postAllOrders = parser.asDouble(data?.get("postAllOrders"))
            return if (existing == null ||
                existing.current != current ||
                existing.postOrder != postOrder ||
                existing.postAllOrders != postAllOrders
            ) {
                TradeStatesWithDoubleValues(current, postOrder, postAllOrders)
            } else {
                existing
            }
        }
    }

    internal fun asTradingStates(): TradingStates<Double> {
        return object : TradingStates<Double> {
            override val current: Double?
                get() = this@TradeStatesWithDoubleValues.current
            override val postOrder: Double?
                get() = this@TradeStatesWithDoubleValues.postOrder
            override val postAllOrders: Double?
                get() = this@TradeStatesWithDoubleValues.postAllOrders
        }
    }
}

@JsExport
@Serializable
data class TradeStatesWithStringValues(
    val current: String?,
    val postOrder: String?,
    val postAllOrders: String?
) {
    companion object {
        internal fun create(
            existing: TradeStatesWithStringValues?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TradeStatesWithStringValues {
            val current = parser.asString(data?.get("current"))
            val postOrder = parser.asString(data?.get("postOrder"))
            val postAllOrders = parser.asString(data?.get("postAllOrders"))
            return if (existing == null ||
                existing.current != current ||
                existing.postOrder != postOrder ||
                existing.postAllOrders != postAllOrders
            ) {
                TradeStatesWithStringValues(current, postOrder, postAllOrders)
            } else {
                existing
            }
        }
    }
    internal fun asTradingStates(): TradingStates<String> {
        return object : TradingStates<String> {
            override val current: String?
                get() = this@TradeStatesWithStringValues.current
            override val postOrder: String?
                get() = this@TradeStatesWithStringValues.postOrder
            override val postAllOrders: String?
                get() = this@TradeStatesWithStringValues.postAllOrders
        }
    }
}
