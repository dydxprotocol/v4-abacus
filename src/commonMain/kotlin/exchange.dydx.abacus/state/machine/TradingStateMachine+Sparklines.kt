package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedStringMapOfList
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.codegen.IndexerSparklineTimePeriod
import kollections.iListOf

internal fun TradingStateMachine.sparklines(
    payload: String,
    period: IndexerSparklineTimePeriod
): StateChanges? {
    val json = parser.decodeJsonObject(payload) as? Map<String, List<String>>
    val sparklines = parser.asTypedStringMapOfList<String>(json)
    return if (sparklines != null) {
        marketsProcessor.processSparklines(internalState.marketsSummary, sparklines, period)
        when (period) {
            IndexerSparklineTimePeriod.ONE_DAY -> {
                StateChanges(iListOf(Changes.sparklines, Changes.markets), null)
            }

            IndexerSparklineTimePeriod.SEVEN_DAYS -> {
                StateChanges(iListOf(Changes.markets), null)
            }
        }
    } else {
        StateChanges.noChange
    }
}
