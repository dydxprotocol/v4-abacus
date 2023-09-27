package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.historicalFundings(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    return receivedHistoricalFundings(json)
}

internal fun TradingStateMachine.receivedHistoricalFundings(payload: Map<String, Any>): StateChanges {
    val marketId = parser.asString(
        parser.value(payload, "historicalFunding.0.market") ?: parser.value(
            payload,
            "historicalFunding.0.ticker"
        )
    )
    return if (marketId != null) {
        val size = parser.asList(payload["historicalFunding"])?.size ?: 0
        if (size > 0) {
            marketsSummary = marketsProcessor.receivedHistoricalFundings(marketsSummary, payload)
            StateChanges(iListOf(Changes.historicalFundings), iListOf(marketId))
        } else StateChanges(iListOf())
    } else StateChanges(iListOf())
}
