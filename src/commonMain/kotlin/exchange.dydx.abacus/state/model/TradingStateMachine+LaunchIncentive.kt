package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.models.configs.ConfigsLaunchIncentivePoints
import indexer.models.configs.ConfigsLaunchIncentiveResponse
import kollections.iListOf

internal fun TradingStateMachine.launchIncentiveSeasons(payload: String): StateChanges? {
    if (staticTyping) {
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
    } else {
        val json = parser.decodeJsonObject(payload)
        return if (json != null) {
            launchIncentive = launchIncentiveProcessor.receivedSeasons(launchIncentive, json)
            StateChanges(iListOf(Changes.launchIncentive))
        } else {
            StateChanges.noChange
        }
    }
}

internal fun TradingStateMachine.launchIncentivePoints(season: String, payload: String): StateChanges? {
    if (staticTyping) {
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
    } else {
        val json = parser.decodeJsonObject(payload)
        val wallet = this.wallet
        return if (wallet != null && json != null) {
            this.wallet = walletProcessor.receivedLaunchIncentivePointDeprecated(wallet, season, json)
            StateChanges(iListOf(Changes.accountBalances))
        } else {
            StateChanges.noChange
        }
    }
}
