package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import kollections.iListOf

@Suppress("ConstructorParameterNaming")
data class RebateLeaderboardEntryResponse(
    val address: String,
    val pnl: Double,
    val position: Int,
    val dollarReward: Double
)

data class RebateLeaderboardResponse(
    val data: List<RebateLeaderboardEntryResponse>
)

internal fun TradingStateMachine.rebateLeaderboard(payload: String): StateChanges {
    val leaderboard = parser.asTypedObject<RebateLeaderboardResponse>(payload)
    val oldState = internalState.rebateLeaderboard.copy()
    rebateLeaderboardProcessor.processLeaderboard(
        internalState.rebateLeaderboard,
        leaderboard
    )
    return if (oldState != internalState.rebateLeaderboard) {
        StateChanges(iListOf(Changes.rebateLeaderboard))
    } else {
        StateChanges.noChange
    }
}
