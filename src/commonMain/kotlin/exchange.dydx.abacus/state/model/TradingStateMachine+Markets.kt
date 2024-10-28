package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.protocols.asTypedStringMap
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketUpdateResponse
import indexer.models.configs.ConfigsAssetMetadata
import indexer.models.configs.ConfigsMarketAsset
import kollections.iListOf
import kollections.toIList

internal fun TradingStateMachine.receivedMarkets(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    if (staticTyping) {
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
    } else {
        marketsSummary = marketsProcessor.subscribedDeprecated(marketsSummary, payload)
        marketsSummary = marketsCalculator.calculateDeprecated(parser.asMap(marketsSummary), assets, null)
        val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbersDeprecated(
            parser = parser,
            account = account,
            subaccountNumber = subaccountNumber,
            tradeInput = parser.asMap(input?.get("trade")),
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
}

internal fun TradingStateMachine.receivedMarketsChanges(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    if (staticTyping) {
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
    } else {
        val markets = parser.asNativeMap(payload.get("trading"))
        marketsSummary = marketsProcessor.channel_dataDeprecated(marketsSummary, payload)
        marketsSummary =
            marketsCalculator.calculateDeprecated(marketsSummary, assets, markets?.keys)
        val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbersDeprecated(
            parser,
            account,
            subaccountNumber,
            parser.asMap(input?.get("trade")),
        )

        val blankAssets = assets == null
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
            markets?.keys?.toIList(),
            subaccountNumbers,
        )
    }
}

internal fun TradingStateMachine.receivedBatchedMarketsChanges(
    payload: List<Any>,
    subaccountNumber: Int,
): StateChanges {
    if (staticTyping) {
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
    } else {
        marketsSummary = marketsProcessor.channel_batch_dataDeprecated(marketsSummary, payload)
        val keys = mutableSetOf<String>()
        for (partialPayload in payload) {
            parser.asMap(partialPayload)?.let { partialPayload ->
                val narrowedPayload =
                    parser.asMap(partialPayload["trading"])
                        ?: parser.asMap(partialPayload["oraclePrices"])
                        ?: parser.asMap(partialPayload["markets"]) ?: partialPayload
                keys.addAll(narrowedPayload.keys)
            }
        }
        marketsSummary = marketsCalculator.calculateDeprecated(marketsSummary, assets, keys)
        val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbersDeprecated(
            parser = parser,
            account = account,
            subaccountNumber = subaccountNumber,
            tradeInput = parser.asMap(input?.get("trade")),
        )

        val blankAssets = assets == null
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
}

internal fun TradingStateMachine.processMarketsConfigurationsWithMetadataService(
    payload: Map<String, ConfigsAssetMetadata>,
    subaccountNumber: Int?,
    deploymentUri: String,
): StateChanges {
    internalState.assets = assetsProcessor.processMetadataConfigurations(
        existing = internalState.assets,
        payload = payload,
    )

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

internal fun TradingStateMachine.processMarketsConfigurations(
    payload: Map<String, ConfigsMarketAsset>,
    subaccountNumber: Int?,
    deploymentUri: String,
): StateChanges {
    internalState.assets = assetsProcessor.processConfigurations(
        existing = internalState.assets,
        payload = payload,
        deploymentUri = deploymentUri,
    )
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

internal fun TradingStateMachine.receivedMarketsConfigurationsDeprecated(
    payload: Map<String, Any>,
    subaccountNumber: Int?,
    deploymentUri: String,
): StateChanges {
    this.marketsSummary = marketsProcessor.receivedConfigurationsDeprecated(
        existing = this.marketsSummary,
        payload = payload,
    )
    assets = assetsProcessor.receivedConfigurations(
        existing = assets,
        payload = payload,
        deploymentUri = deploymentUri,
    )
    this.marketsSummary = marketsCalculator.calculateDeprecated(
        marketsSummary = this.marketsSummary,
        assets = assets,
        keys = null,
    )
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbersDeprecated(
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
