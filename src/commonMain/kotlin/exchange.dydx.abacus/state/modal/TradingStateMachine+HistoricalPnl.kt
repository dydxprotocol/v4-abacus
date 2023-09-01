package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IMap
import kollections.iListOf
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.historicalPnl(payload: String, subaccountNumber: Int): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    return receivedHistoricalPnls(json, subaccountNumber)
}

internal fun TradingStateMachine.receivedHistoricalPnls(payload: IMap<String, Any>, subaccountNumber: Int): StateChanges {
    val size = parser.asList(payload["historicalPnl"])?.size ?: 0
    return if (size > 0) {
        wallet = walletProcessor.receivedHistoricalPnls(wallet, payload, subaccountNumber)
        StateChanges(iListOf(Changes.historicalPnl), null, iListOf(subaccountNumber))
    } else StateChanges(iListOf<Changes>())
}
