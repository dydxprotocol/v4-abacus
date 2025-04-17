package exchange.dydx.abacus.output.account

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountHistoricalPNL(
    val equity: Double,
    val totalPnl: Double,
    val netTransfers: Double,
    val createdAtMilliseconds: Double,
)
