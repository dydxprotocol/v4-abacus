package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.models.configs.AssetJson
import kollections.iListOf
import kollections.toIList

internal fun TradingStateMachine.receivedMarkets(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    marketsSummary = marketsProcessor.subscribed(marketsSummary, payload)
    marketsSummary = marketsCalculator.calculate(parser.asMap(marketsSummary), assets, null)
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser,
        account,
        subaccountNumber,
        parser.asMap(input?.get("trade")),
    )

    return StateChanges(
        iListOf(
            Changes.assets,
            Changes.markets,
            Changes.subaccount,
            Changes.input,
            Changes.historicalPnl,
        ),
        null,
        subaccountNumbers,
    )
}

internal fun TradingStateMachine.receivedMarketsChanges(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val blankAssets = assets == null
    marketsSummary = marketsProcessor.channel_data(marketsSummary, payload)
    marketsSummary = marketsCalculator.calculate(marketsSummary, assets, payload.keys)
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser,
        account,
        subaccountNumber,
        parser.asMap(input?.get("trade")),
    )

    return StateChanges(
        if (blankAssets) {
            iListOf(
                Changes.markets,
                Changes.assets,
                Changes.subaccount,
                Changes.historicalPnl,
            )
        } else {
            iListOf(
                Changes.assets,
                Changes.markets,
                Changes.subaccount,
                Changes.input,
                Changes.historicalPnl,
            )
        },
        payload.keys.toIList(),
        subaccountNumbers,
    )
}

internal fun TradingStateMachine.receivedBatchedMarketsChanges(
    payload: List<Any>,
    subaccountNumber: Int,
): StateChanges {
    val blankAssets = assets == null
    marketsSummary = marketsProcessor.channel_batch_data(marketsSummary, payload)
    val keys = mutableSetOf<String>()
    for (partialPayload in payload) {
        parser.asMap(partialPayload)?.let { partialPayload ->
            val narrowedPayload =
                parser.asMap(partialPayload["trading"]) ?: parser.asMap(partialPayload["oraclePrices"])
                    ?: parser.asMap(partialPayload["markets"]) ?: partialPayload
            keys.addAll(narrowedPayload.keys)
        }
    }
    marketsSummary = marketsCalculator.calculate(marketsSummary, assets, keys)
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser,
        account,
        subaccountNumber,
        parser.asMap(input?.get("trade")),
    )

    return StateChanges(
        if (blankAssets) {
            iListOf(
                Changes.markets,
                Changes.assets,
                Changes.subaccount,
                Changes.historicalPnl,
            )
        } else {
            iListOf(
                Changes.assets,
                Changes.markets,
                Changes.subaccount,
                Changes.input,
                Changes.historicalPnl,
            )
        },
        keys.toIList(),
        subaccountNumbers,
    )
}

internal fun TradingStateMachine.processMarketsConfigurations(
    payload: Map<String, AssetJson>,
    subaccountNumber: Int?,
    deploymentUri: String,
): StateChanges {
    this.marketsSummary = marketsProcessor.receivedConfigurations(
        existing = this.marketsSummary,
        payload = payload,
    )

    internalState.assets = assetsProcessor.processConfigurations(
        existing = internalState.assets,
        payload = payload,
        deploymentUri = deploymentUri,
    )

    this.marketsSummary = marketsCalculator.calculate(
        marketsSummary = this.marketsSummary,
        assets = internalState.assets,
        keys = null,
    )
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser = parser,
        account = account,
        subaccountNumber = subaccountNumber ?: 0,
        tradeInput = parser.asMap(input?.get("trade")),
    )

    return if (subaccountNumber != null) {
        StateChanges(
            changes = iListOf(Changes.markets, Changes.assets, Changes.subaccount, Changes.input),
            markets = null,
            subaccountNumbers = subaccountNumbers,
        )
    } else {
        StateChanges(
            changes = iListOf(Changes.markets, Changes.assets),
            markets = null,
            subaccountNumbers = null,
        )
    }
}

internal fun TradingStateMachine.receivedMarketsConfigurationsDeprecated(
    payload: Map<String, Any>,
    subaccountNumber: Int?,
    deploymentUri: String,
): StateChanges {
    this.marketsSummary = marketsProcessor.receivedConfigurations(
        existing = this.marketsSummary,
        payload = payload,
    )
    assets = assetsProcessor.receivedConfigurations(
        existing = assets,
        payload = payload,
        deploymentUri = deploymentUri,
    )
    this.marketsSummary = marketsCalculator.calculate(
        marketsSummary = this.marketsSummary,
        assets = assets,
        keys = null,
    )
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser = parser,
        account = account,
        subaccountNumber = subaccountNumber ?: 0,
        tradeInput = parser.asMap(input?.get("trade")),
    )

    return if (subaccountNumber != null) {
        StateChanges(
            changes = iListOf(Changes.markets, Changes.assets, Changes.subaccount, Changes.input),
            markets = null,
            subaccountNumbers = subaccountNumbers,
        )
    } else {
        StateChanges(
            changes = iListOf(Changes.markets, Changes.assets),
            markets = null,
            subaccountNumbers = null,
        )
    }
}
