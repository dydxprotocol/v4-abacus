package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf

fun TradingStateMachine.onChainWithdrawalGating(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return json?.let {
        configs = configsProcessor.receivedWithdrawalGating(configs, it)
        //TODO: input changes processing
        return StateChanges(iListOf(Changes.configs, Changes.input))
    } ?: StateChanges.noChange
}

fun TradingStateMachine.onChainWithdrawalCapacity(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return json?.let {
        configs = configsProcessor.receivedWithdrawalCapacity(configs, it)
        //TODO: input changes processing
        return StateChanges(iListOf(Changes.configs, Changes.input))
    } ?: StateChanges.noChange
}