package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kollections.toIList

internal fun TradingStateMachine.receivedMarkets(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    internalState = internalState.copy(
        transfer = internalState.transfer,
        perpetualMarkets = marketsProcessor.testSubscribed(payload)
    )
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

internal fun TradingStateMachine.receivedMarketsConfigurations(
    payload: Map<String, Any>,
    subaccountNumber: Int?,
    deploymentUri: String,
): StateChanges {
    this.marketsSummary = marketsProcessor.receivedConfigurations(this.marketsSummary, payload)
    assets = assetsProcessor.receivedConfigurations(assets, payload, deploymentUri)
    this.marketsSummary = marketsCalculator.calculate(this.marketsSummary, assets, null)
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser,
        account,
        subaccountNumber ?: 0,
        parser.asMap(input?.get("trade")),
    )

    return if (subaccountNumber != null) {
        StateChanges(
            iListOf(Changes.markets, Changes.assets, Changes.subaccount, Changes.input),
            null,
            subaccountNumbers,
        )
    } else {
        StateChanges(
            iListOf(Changes.markets, Changes.assets),
            null,
            null,
        )
    }
}
