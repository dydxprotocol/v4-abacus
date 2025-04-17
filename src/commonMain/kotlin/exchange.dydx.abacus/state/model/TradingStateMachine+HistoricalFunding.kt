package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.codegen.IndexerHistoricalFundingResponse
import kollections.iListOf
import kollections.toIList

internal fun TradingStateMachine.historicalFundings(payload: String): StateChanges {
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
}
