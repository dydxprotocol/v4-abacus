package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.protocols.asTypedStringMap
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketUpdateResponse
import indexer.models.configs.ConfigsAssetMetadata
import indexer.models.configs.ConfigsAssetMetadataPrice
import kollections.iListOf
import kollections.toIList

internal fun TradingStateMachine.receivedMarkets(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val markets = parser.asNativeMap(payload["markets"])
    val marketsPayload = parser.asTypedStringMap<IndexerCompositeMarketObject>(markets)
    marketsProcessor.processSubscribed(internalState.marketsSummary, marketsPayload)
    marketsCalculator.calculate(internalState.marketsSummary)
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser = parser,
        subaccounts = internalState.wallet.account.subaccounts,
        subaccountNumber = subaccountNumber,
        tradeInput = internalState.input.trade,
    )
    return StateChanges(
        changes = iListOf(
            Changes.assets,
            Changes.markets,
            Changes.subaccount,
            Changes.input,
            Changes.historicalPnl,
        ),
        markets = null,
        subaccountNumbers = subaccountNumbers,
    )
}

internal fun TradingStateMachine.receivedMarketsChanges(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val response = parser.asTypedObject<IndexerWsMarketUpdateResponse>(payload)
    marketsProcessor.processChannelData(internalState.marketsSummary, response)
    marketsCalculator.calculate(internalState.marketsSummary)
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser = parser,
        subaccounts = internalState.wallet.account.subaccounts,
        subaccountNumber = subaccountNumber,
        tradeInput = internalState.input.trade,
    )
    val blankAssets = internalState.assets.isEmpty()
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
        markets = internalState.marketsSummary.markets.keys.toIList(),
        subaccountNumbers = subaccountNumbers,
    )
}

internal fun TradingStateMachine.receivedBatchedMarketsChanges(
    payload: List<Any>,
    subaccountNumber: Int,
): StateChanges {
    val response = parser.asTypedList<IndexerWsMarketUpdateResponse>(payload)
    marketsProcessor.processChannelBatchData(internalState.marketsSummary, response)
    marketsCalculator.calculate(internalState.marketsSummary)
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser = parser,
        subaccounts = internalState.wallet.account.subaccounts,
        subaccountNumber = subaccountNumber,
        tradeInput = internalState.input.trade,
    )
    val blankAssets = internalState.assets.isEmpty()
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
        markets = internalState.marketsSummary.markets.keys.toIList(),
        subaccountNumbers = subaccountNumbers,
    )
}

internal fun TradingStateMachine.processMarketsConfigurationsWithMetadataService(
    infoPayload: Map<String, ConfigsAssetMetadata>,
    pricesPayload: Map<String, ConfigsAssetMetadataPrice>?,
    subaccountNumber: Int?,
): StateChanges {
    internalState.assets = assetsProcessor.processMetadataConfigurations(
        existing = internalState.assets,
        payload = infoPayload,
    )
    internalState.marketsSummary.launchableMarketPrices = pricesPayload ?: mapOf()

    marketsCalculator.calculate(internalState.marketsSummary)
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser = parser,
        subaccounts = internalState.wallet.account.subaccounts,
        subaccountNumber = subaccountNumber ?: 0,
        tradeInput = internalState.input.trade,
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
