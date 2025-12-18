package exchange.dydx.abacus.output

import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class FeeLeaderboardEntry(
    val address: String,
    val rank: Int,
    val totalFees: Double
)

@JsExport
@Serializable
data class FeeLeaderboard(
    val leaderboard: IList<FeeLeaderboardEntry>
)

@JsExport
@Serializable
data class RebateLeaderboardEntry(
    val address: String,
    val pnl: Double,
    val rank: Int,
    val dollarReward: Double
)

@JsExport
@Serializable
data class RebateLeaderboard(
    val leaderboard: IList<RebateLeaderboardEntry>
)