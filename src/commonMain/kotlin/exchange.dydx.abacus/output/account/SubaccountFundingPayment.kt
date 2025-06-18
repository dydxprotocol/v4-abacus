package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.ParsingHelper
import kollections.JsExport
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountFundingPayment(
    val marketId: String,
    val payment: Double,
    val rate: Double,
    val positionSize: Double,
    val price: Double?,
    val createdAtMilliseconds: Double,
    val side: PositionSide,
) {
    companion object {
        internal fun merge(
            existing: IList<SubaccountFundingPayment>?,
            new: IList<SubaccountFundingPayment>?,
        ): IList<SubaccountFundingPayment> {
            return ParsingHelper.merge(
                existing = existing,
                new = new,
                comparison = { obj, newItem ->
                    ParsingHelper.compare(obj.createdAtMilliseconds, newItem.createdAtMilliseconds, true)
                },
                syncItems = true,
            ).toIList()
        }
    }
}
