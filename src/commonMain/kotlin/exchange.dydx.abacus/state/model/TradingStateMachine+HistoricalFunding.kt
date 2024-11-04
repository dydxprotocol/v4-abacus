package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.codegen.IndexerHistoricalFundingResponse
import kollections.iListOf
import kollections.toIList

internal fun TradingStateMachine.historicalFundings(payload: String): StateChanges {
    if (staticTyping) {
        val response = parser.asTypedObject<IndexerHistoricalFundingResponse>(payload)
        val marketIds: MutableList<String> = mutableListOf()
        for (item in response?.historicalFunding?.toList() ?: emptyList()) {
            val marketId = item.ticker
            if (marketId != null && marketIds.contains(marketId).not()) {
                marketIds.add(marketId)
            }
        }
        marketsProcessor.processHistoricalFundings(
            existing = internalState.marketsSummary,
            payload = response,
        )
        return if (marketIds.isNotEmpty()) {
            StateChanges(iListOf(Changes.historicalFundings), marketIds.toIList())
        } else {
            StateChanges(iListOf())
        }
    } else {
        val json = parser.decodeJsonObject(payload)
        return if (json != null) {
            receivedHistoricalFundings(json)
        } else {
            StateChanges.noChange
        }
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
            marketsSummary = marketsProcessor.receivedHistoricalFundingsDeprecated(marketsSummary, payload)
            StateChanges(iListOf(Changes.historicalFundings), iListOf(marketId))
        } else {
            StateChanges(iListOf())
        }
    } else {
        StateChanges(iListOf())
    }
}
