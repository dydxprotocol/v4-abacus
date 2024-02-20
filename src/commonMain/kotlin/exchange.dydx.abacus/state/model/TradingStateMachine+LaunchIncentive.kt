package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf


internal fun TradingStateMachine.launchIncentiveSeasons(payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        launchIncentive = launchIncentiveProcessor.receivedSeasons(launchIncentive, json)
        StateChanges(iListOf(Changes.launchIncentive))
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.launchIncentivePoints(season: String, payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    val wallet = this.wallet
    return if (wallet != null && json != null) {
        this.wallet = walletProcessor.receivedLaunchIncentivePoint(wallet, season, json)
        StateChanges(iListOf(Changes.accountBalances))
    } else StateChanges.noChange
}
