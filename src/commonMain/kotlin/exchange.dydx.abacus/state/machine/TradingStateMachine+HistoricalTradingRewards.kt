package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import indexer.codegen.IndexerHistoricalTradingRewardAggregationsResponse
import kollections.iListOf

fun TradingStateMachine.historicalTradingRewards(
    payload: String,
    period: HistoricalTradingRewardsPeriod
): StateChanges {
    val response = parser.asTypedObject<IndexerHistoricalTradingRewardAggregationsResponse>(payload)
    if (response != null && response.rewards.isNullOrEmpty().not()) {
        walletProcessor.processHistoricalTradingRewards(
            existing = internalState.wallet,
            payload = response.rewards?.toList(),
            period = period,
        )
        return StateChanges(iListOf(Changes.tradingRewards))
    } else {
        return StateChanges(iListOf<Changes>())
    }
}
