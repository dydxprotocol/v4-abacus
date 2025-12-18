package exchange.dydx.abacus.processor.leaderboards

import exchange.dydx.abacus.output.RebateLeaderboardEntry
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalRebateLeaderboardState
import exchange.dydx.abacus.state.machine.RebateLeaderboardResponse

internal class RebateLeaderboardProcessor(
    parser: ParserProtocol
): BaseProcessor(parser) {
    fun processLeaderboard(existing: InternalRebateLeaderboardState, payload: RebateLeaderboardResponse?): InternalRebateLeaderboardState? {
        existing.leaderboard = payload?.data?.map { RebateLeaderboardEntry(address = it.address, pnl = it.pnl, rank = it.position, dollarReward = it.dollarReward) }
        return existing
    }
}