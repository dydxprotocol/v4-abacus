package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.TradingStates
import exchange.dydx.abacus.protocols.ParserProtocol
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class TradeStatesWithPositionSides(
    val current: PositionSide?,
    val postOrder: PositionSide?,
    val postAllOrders: PositionSide?,
) {
    companion object {
        internal fun create(
            existing: TradeStatesWithPositionSides?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeStatesWithPositionSides {
            val currentString = parser.asString(data?.get("current"))
            val postOrderString = parser.asString(data?.get("postOrder"))
            val postAllOrdersString = parser.asString(data?.get("postAllOrders"))
            val current = if (currentString != null) PositionSide.invoke(currentString) else null
            val postOrder =
                if (postOrderString != null) PositionSide.invoke(postOrderString) else null
            val postAllOrders =
                if (postAllOrdersString != null) PositionSide.invoke(postAllOrdersString) else null
            return if (existing == null ||
                existing.current !== current ||
                existing.postOrder !== postOrder ||
                existing.postAllOrders !== postAllOrders
            ) {
                TradeStatesWithPositionSides(current, postOrder, postAllOrders)
            } else {
                existing
            }
        }
    }

    internal fun asTradingStates(): TradingStates<PositionSide> {
        return object : TradingStates<PositionSide> {
            override val current: PositionSide?
                get() = this@TradeStatesWithPositionSides.current
            override val postOrder: PositionSide?
                get() = this@TradeStatesWithPositionSides.postOrder
            override val postAllOrders: PositionSide?
                get() = this@TradeStatesWithPositionSides.postAllOrders
        }
    }
}

@JsExport
@Serializable
enum class PositionSide(val rawValue: String) {
    LONG("LONG"),
    SHORT("SHORT"),
    NONE("NONE");

    companion object {
        operator fun invoke(rawValue: String?) =
            PositionSide.values().firstOrNull { it.rawValue == rawValue }
    }
}
