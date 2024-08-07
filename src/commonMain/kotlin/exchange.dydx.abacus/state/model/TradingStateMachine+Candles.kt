package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedStringMapOfList
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kollections.toIList

internal fun TradingStateMachine.candles(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedCandles(json)
    } else {
        StateChanges(iListOf<Changes>())
    }
}

private fun TradingStateMachine.receivedCandles(payload: Map<String, Any>): StateChanges {
    val markets = parser.asMap(payload["candles"])
    val marketIds = if (markets != null) {
        markets.keys.toIList()
    } else {
        val marketId = parser.asString(parser.value(payload, "candles.0.market"))
            ?: parser.asString(parser.value(payload, "candles.0.ticker"))
        if (marketId != null) iListOf(marketId) else null
    }
    return if (marketIds != null) {
        val size = parser.asList(payload["candles"])?.size ?: 0
        if (size > 0) {
            marketsSummary = marketsProcessor.receivedCandles(marketsSummary, payload)
            StateChanges(iListOf(Changes.candles), marketIds)
        } else {
            val size = parser.asMap(payload["candles"])?.size ?: 0
            if (size > 0) {
                marketsSummary = marketsProcessor.receivedCandles(marketsSummary, payload)
                StateChanges(iListOf(Changes.candles), marketIds)
            } else {
                StateChanges(iListOf())
            }
        }
    } else {
        StateChanges(iListOf())
    }
}

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

internal fun TradingStateMachine.receivedCandles(
    market: String,
    resolution: String,
    payload: Map<String, Any>
): StateChanges {
    this.marketsSummary =
        marketsProcessor.receivedCandles(marketsSummary, market, resolution, payload)
    return StateChanges(iListOf(Changes.candles), iListOf(market))
}

internal fun TradingStateMachine.receivedCandlesChanges(
    market: String,
    resolution: String,
    payload: Map<String, Any>
): StateChanges {
    this.marketsSummary =
        marketsProcessor.receivedCandlesChanges(marketsSummary, market, resolution, payload)
    return StateChanges(iListOf(Changes.candles), iListOf(market))
}

internal fun TradingStateMachine.receivedBatchedCandlesChanges(
    market: String,
    resolution: String,
    payload: List<Any>
): StateChanges {
    this.marketsSummary =
        marketsProcessor.receivedBatchedCandlesChanges(marketsSummary, market, resolution, payload)
    return StateChanges(iListOf(Changes.candles), iListOf(market))
}
