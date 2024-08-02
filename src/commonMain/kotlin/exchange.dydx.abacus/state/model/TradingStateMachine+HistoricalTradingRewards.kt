package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import indexer.codegen.IndexerHistoricalTradingRewardAggregationsResponse
import kollections.iListOf

fun TradingStateMachine.historicalTradingRewards(
    payload: String,
    period: HistoricalTradingRewardsPeriod
): StateChanges {
    if (staticTyping) {
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
    } else {
        val json = parser.decodeJsonObject(payload)
        return if (json != null) {
            receivedHistoricalTradingRewards(json, period.rawValue)
        } else {
            StateChanges.noChange
        }
    }
}

private fun TradingStateMachine.receivedHistoricalTradingRewards(
    payload: Map<String, Any>,
    period: String
): StateChanges {
    val rewards = parser.asList(payload["rewards"])
    return if ((rewards?.size ?: 0) > 0) {
        wallet = walletProcessor.receivedHistoricalTradingRewards(wallet, rewards, period)
        StateChanges(iListOf(Changes.tradingRewards))
    } else {
        StateChanges(iListOf<Changes>())
    }
}
