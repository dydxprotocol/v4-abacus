package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import kollections.iListOf

@Suppress("ConstructorParameterNaming")
data class FeeLeaderboardEntryResponse(
    val address: String,
    val rank: Int,
    val total_fees: Double
)

data class FeeLeaderboardResponse(
    val data: List<FeeLeaderboardEntryResponse>
)

internal fun TradingStateMachine.feeLeaderboard(payload: String): StateChanges {
    val leaderboard = parser.asTypedObject<FeeLeaderboardResponse>(payload)
    val oldState = internalState.feeLeaderboard.copy()
    feeLeaderboardProcessor.processLeaderboard(
        internalState.feeLeaderboard,
        leaderboard
    )
    return if (oldState != internalState.feeLeaderboard) {
        StateChanges(iListOf(Changes.feeLeaderboard))
    } else {
        StateChanges.noChange
    }
}
