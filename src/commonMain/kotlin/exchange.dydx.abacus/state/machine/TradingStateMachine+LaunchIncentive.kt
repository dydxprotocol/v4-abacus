package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.models.configs.ConfigsLaunchIncentivePoints
import indexer.models.configs.ConfigsLaunchIncentiveResponse
import kollections.iListOf

internal fun TradingStateMachine.launchIncentiveSeasons(payload: String): StateChanges? {
    val launchIncentiveResponse = parser.asTypedObject<ConfigsLaunchIncentiveResponse>(payload)
    val oldState = internalState.launchIncentive.copy()
    launchIncentiveProcessor.processSeasons(
        internalState.launchIncentive,
        launchIncentiveResponse,
    )
    return if (oldState != internalState.launchIncentive) {
        StateChanges(iListOf(Changes.launchIncentive))
    } else {
        StateChanges.noChange
    }
}
internal fun TradingStateMachine.launchIncentivePoints(season: String, payload: String): StateChanges? {
    val points = parser.asTypedObject<ConfigsLaunchIncentivePoints>(payload)
    val oldValue = internalState.wallet.account.launchIncentivePoints[season]?.copy()
    walletProcessor.processLaunchIncentiveSeasons(
        existing = internalState.wallet,
        season = season,
        payload = points,
    )
    return if (internalState.wallet.account.launchIncentivePoints[season] != oldValue) {
        StateChanges(iListOf(Changes.accountBalances))
    } else {
        StateChanges.noChange
    }
}
