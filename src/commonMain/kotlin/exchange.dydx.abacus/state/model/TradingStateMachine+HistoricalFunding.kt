package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf

internal fun TradingStateMachine.historicalFundings(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedHistoricalFundings(json)
    } else {
        StateChanges.noChange
    }
}

private fun TradingStateMachine.receivedHistoricalFundings(payload: Map<String, Any>): StateChanges {
    val marketId = parser.asString(
        parser.value(payload, "historicalFunding.0.market") ?: parser.value(
            payload,
            "historicalFunding.0.ticker",
        ),
    )
    return if (marketId != null) {
        val size = parser.asList(payload["historicalFunding"])?.size ?: 0
        if (size > 0) {
            marketsSummary = marketsProcessor.receivedHistoricalFundings(marketsSummary, payload)
            StateChanges(iListOf(Changes.historicalFundings), iListOf(marketId))
        } else {
            StateChanges(iListOf())
        }
    } else {
        StateChanges(iListOf())
    }
}
