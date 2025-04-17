package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.TradingStates
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class TradeStatesWithPositionSides(
    val current: PositionSide?,
    val postOrder: PositionSide?,
    val postAllOrders: PositionSide?,
) {
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
            PositionSide.entries.firstOrNull { it.rawValue == rawValue }
    }
}
