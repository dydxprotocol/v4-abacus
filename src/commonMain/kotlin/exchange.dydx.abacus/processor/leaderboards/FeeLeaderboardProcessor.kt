package exchange.dydx.abacus.processor.leaderboards

import exchange.dydx.abacus.output.FeeLeaderboardEntry
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalFeeLeaderboardState
import exchange.dydx.abacus.state.machine.FeeLeaderboardResponse

internal class FeeLeaderboardProcessor(
    parser: ParserProtocol
): BaseProcessor(parser) {
    fun processLeaderboard(existing: InternalFeeLeaderboardState, payload: FeeLeaderboardResponse?): InternalFeeLeaderboardState {
        existing.leaderboard = payload?.data?.map { FeeLeaderboardEntry(it.address, it.rank, it.total_fees) }
        return existing
    }
}