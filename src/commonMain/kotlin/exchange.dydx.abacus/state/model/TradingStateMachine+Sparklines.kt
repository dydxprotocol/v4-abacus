package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedStringMapOfList
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf

internal fun TradingStateMachine.sparklines(payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload) as? Map<String, List<String>>
    if (staticTyping) {
        val sparklines = parser.asTypedStringMapOfList<String>(json)
        return if (sparklines != null) {
            marketsProcessor.processSparklines(internalState.marketsSummary, sparklines)
            StateChanges(iListOf(Changes.sparklines, Changes.markets), null)
        } else {
            StateChanges.noChange
        }
    } else {
        return if (json != null) {
            marketsSummary = marketsProcessor.receivedSparklinesDeprecated(marketsSummary, json)
            return StateChanges(iListOf(Changes.sparklines, Changes.markets), null)
        } else {
            StateChanges.noChange
        }
    }
}
