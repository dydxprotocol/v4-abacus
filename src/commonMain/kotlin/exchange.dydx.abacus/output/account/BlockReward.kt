package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class BlockReward(
    val tradingReward: Double,
    val createdAtMilliseconds: Double,
    val createdAtHeight: Int,
)