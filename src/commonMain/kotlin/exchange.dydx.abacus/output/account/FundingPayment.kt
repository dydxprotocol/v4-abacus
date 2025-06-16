package exchange.dydx.abacus.output.account

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class FundingPayment(
    val createdAtInMilliseconds: Double,
    val ticker: String,
    val oraclePrice: Double,
    val size: Double,
    val side: PositionSide,
    val rate: Double,
    val payment: Double,
)
