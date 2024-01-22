package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf

fun TradingStateMachine.historicalTradingRewards(payload: String, period: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedHistoricalTradingRewards(json, period)
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.receivedHistoricalTradingRewards(payload: Map<String, Any>, period: String): StateChanges {
    val rewards = parser.asList(payload["rewards"])
    return if ((rewards?.size?: 0) > 0) {
        wallet = walletProcessor.receivedHistoricalTradingRewards(wallet, rewards, period)
        StateChanges(iListOf(Changes.tradingRewards))
    } else StateChanges(iListOf<Changes>())
}
