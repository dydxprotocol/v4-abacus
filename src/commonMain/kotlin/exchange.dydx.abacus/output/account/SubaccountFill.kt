package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.ParsingHelper
import kollections.JsExport
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountFill(
    val id: String,
    val marketId: String,
    val displayId: String,
    val orderId: String?,
    val subaccountNumber: Int?,
    val marginMode: MarginMode?,
    val side: OrderSide,
    val type: OrderType,
    val liquidity: FillLiquidity,
    val price: Double,
    val size: Double,
    val fee: Double?,
    val createdAtMilliseconds: Double,
    val resources: SubaccountFillResources,
) {
    companion object {
        internal fun merge(
            existing: IList<SubaccountFill>?,
            new: IList<SubaccountFill>?,
        ): IList<SubaccountFill> {
            return ParsingHelper.merge(
                existing = existing,
                new = new,
                comparison = { obj, newItem ->
                    if (obj.id == newItem.id) {
                        ParsingHelper.compare(obj.createdAtMilliseconds, newItem.createdAtMilliseconds, true)
                    } else {
                        ParsingHelper.compare(obj.id, newItem.id, true)
                    }
                },
                syncItems = true,
            ).toIList()
        }
    }
}

/*
typeStringKey, statusStringKey, iconLocal and indicator are set to optional, in case
BE returns new transfer type enum values or status enum values which Abacus doesn't recognize
*/
@JsExport
@Serializable
data class SubaccountFillResources(
    val sideString: String?,
    val liquidityString: String?,
    val typeString: String?,
    val sideStringKey: String?,
    val liquidityStringKey: String?,
    val typeStringKey: String?,
    val iconLocal: String?,
)

@JsExport
@Serializable
enum class FillLiquidity(val rawValue: String) {
    maker("MAKER"),
    taker("TAKER");

    companion object {
        operator fun invoke(rawValue: String) =
            FillLiquidity.entries.firstOrNull { it.rawValue == rawValue }
    }
}
