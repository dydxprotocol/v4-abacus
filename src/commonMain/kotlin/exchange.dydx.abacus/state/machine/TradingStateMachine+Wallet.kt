package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.Logger
import indexer.codegen.IndexerTransferResponse
import indexer.models.IndexerCompositeFillResponse
import indexer.models.chain.OnChainAccountBalanceObject
import indexer.models.chain.OnChainDelegationResponse
import indexer.models.chain.OnChainStakingRewardsResponse
import indexer.models.chain.OnChainUnbondingResponse
import indexer.models.chain.OnChainUserFeeTierResponse
import indexer.models.chain.OnChainUserStatsResponse
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIList
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

internal fun TradingStateMachine.receivedSubaccountSubscribed(
    payload: Map<String, Any>,
    height: BlockAndTime?,
): StateChanges {
    walletProcessor.processSubscribed(internalState.wallet, payload, height)

    val changes = iMutableListOf<Changes>()
    if (payload["account"] != null || payload["subaccount"] != null) {
        changes.add(Changes.subaccount)
        changes.add(Changes.input)
    }
    changes.add(Changes.fills)
    changes.add(Changes.transfers)
    changes.add(Changes.fundingPayments)
    changes.add(Changes.historicalPnl)
    changes.add(Changes.tradingRewards)
    val subaccountNumber = parser.asInt(payload["subaccountNumber"]) ?: 0
    val subaccountNumbers =
        MarginCalculator.getChangedSubaccountNumbers(
            parser = parser,
            subaccounts = internalState.wallet.account.subaccounts,
            subaccountNumber = subaccountNumber,
            tradeInput = internalState.input.trade,
        )

    return StateChanges(
        changes = changes,
        markets = null,
        subaccountNumbers = subaccountNumbers,
    )
}

internal fun TradingStateMachine.receivedSubaccountsChanges(
    payload: Map<String, Any>,
    info: SocketInfo,
    height: BlockAndTime?,
): StateChanges {
    walletProcessor.processChannelData(internalState.wallet, payload, info, height)

    val changes = iMutableListOf<Changes>()
    val idElements = info.id?.split("/")
    val subaccountNumber =
        if (idElements?.size == 2) parser.asInt(idElements.lastOrNull()) ?: 0 else 0
    val childSubaccountNumber = info.childSubaccountNumber
    val subaccountNumbers = iMutableListOf(subaccountNumber)

    if (childSubaccountNumber != null && !subaccountNumbers.contains(childSubaccountNumber)) {
        subaccountNumbers.add(childSubaccountNumber)
    }

    if (payload["accounts"] != null ||
        payload["subaccounts"] != null ||
        payload["positions"] != null ||
        payload["perpetualPositions"] != null ||
        payload["assetPositions"] != null ||
        payload["orders"] != null
    ) {
        changes.add(Changes.subaccount)
        changes.add(Changes.input)
        changes.add(Changes.historicalPnl)
    }
    if (payload["fills"] != null) {
        changes.add(Changes.fills)
    }
    if (payload["transfers"] != null) {
        changes.add(Changes.transfers)
    }
    if (payload["fundingPayments"] != null) {
        changes.add(Changes.fundingPayments)
    }
    if (payload["tradingReward"] != null) {
        changes.add(Changes.tradingRewards)
    }
    return StateChanges(
        changes,
        null,
        subaccountNumbers,
    )
}

internal fun TradingStateMachine.receivedBatchSubaccountsChanges(
    payload: List<Any>,
    info: SocketInfo,
    height: BlockAndTime?
): StateChanges {
    var changes = iListOf<Changes>()
    var subaccountNumbers = iListOf<Int>()
    for (item in payload) {
        parser.asMap(item)?.let {
            val itemChanges = receivedSubaccountsChanges(it, info, height)
            val changedSubaccountNumbers = itemChanges.subaccountNumbers?.toList()
            if (changedSubaccountNumbers != null) {
                subaccountNumbers = subaccountNumbers.union(changedSubaccountNumbers).toIList()
            }
            changes = changes.union(itemChanges.changes).toIList()
        }
    }
    return StateChanges(changes, null, subaccountNumbers)
}

internal fun TradingStateMachine.onChainUserFeeTier(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    val payload = parser.asTypedObject<OnChainUserFeeTierResponse>(json)
    val oldValue = internalState.wallet.user?.copy()
    walletProcessor.processOnChainUserFeeTier(internalState.wallet, payload)
    return if (oldValue != internalState.wallet.user) {
        StateChanges(iListOf(Changes.wallet), null)
    } else {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.onChainUserStats(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    val payload = parser.asTypedObject<OnChainUserStatsResponse>(json)
    val oldValue = internalState.wallet.user?.copy()
    walletProcessor.processOnChainUserStats(internalState.wallet, payload)
    return if (oldValue != internalState.wallet.user) {
        StateChanges(iListOf(Changes.wallet), null)
    } else {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.receivedFills(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val fills = parser.asList(payload["fills"])
    val size = fills?.size ?: 0
    return if (size > 0) {
        val payload = parser.asTypedObject<IndexerCompositeFillResponse>(payload)
        walletProcessor.processFills(
            existing = internalState.wallet,
            payload = payload?.fills?.toList(),
            subaccountNumber = subaccountNumber,
        )

        StateChanges(iListOf(Changes.fills), null, iListOf(subaccountNumber))
    } else {
        StateChanges(iListOf<Changes>())
    }
}

internal fun TradingStateMachine.receivedTransfers(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val size = parser.asList(payload["transfers"])?.size ?: 0
    return if (size > 0) {
        val payload = parser.asTypedObject<IndexerTransferResponse>(payload)
        walletProcessor.processTransfers(
            existing = internalState.wallet,
            payload = payload?.transfers?.toList(),
            subaccountNumber = subaccountNumber,
        )

        StateChanges(iListOf(Changes.transfers), null, iListOf(subaccountNumber))
    } else {
        StateChanges(iListOf<Changes>())
    }
}

internal fun TradingStateMachine.orderCanceled(
    orderId: String,
    subaccountNumber: Int
): StateChanges {
    val (modifiedWallet, updated) = walletProcessor.orderCanceled(
        existing = internalState.wallet,
        orderId = orderId,
        subaccountNumber = subaccountNumber,
    )
    return if (updated) {
        StateChanges(iListOf(Changes.subaccount), null, iListOf(subaccountNumber))
    } else {
        StateChanges(iListOf<Changes>())
    }
}

internal fun TradingStateMachine.onChainAccountBalances(payload: String): StateChanges {
    return try {
        val json = Json.parseToJsonElement(payload)
        val account = json.jsonArray.toList()
        val response = parser.asTypedList<OnChainAccountBalanceObject>(account)
        val oldValue = internalState.wallet.account.balances
        walletProcessor.processAccountBalances(internalState.wallet, response)
        if (oldValue != internalState.wallet.account.balances) {
            return StateChanges(iListOf(Changes.accountBalances), null)
        } else {
            return StateChanges(iListOf())
        }
    } catch (exception: SerializationException) { // JSON Deserialization exception
        Logger.e {
            "Failed to deserialize onChainAccountBalances: $payload \n" +
                "Exception: $exception"
        }
        StateChanges(iListOf())
    } catch (exception: IllegalArgumentException) { // .jsonArray exception
        Logger.e {
            "Failed to deserialize onChainAccountBalances: $payload \n" +
                "Exception: $exception"
        }
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.onChainDelegations(payload: String): StateChanges {
    val response = parser.asTypedObject<OnChainDelegationResponse>(payload)
    val oldValue = internalState.wallet.account.stakingBalances
    walletProcessor.processStakingDelegations(internalState.wallet, response)
    return if (oldValue != internalState.wallet.account.stakingBalances) {
        StateChanges(iListOf(Changes.accountBalances), null)
    } else {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.onChainUnbonding(payload: String): StateChanges {
    val response = parser.asTypedObject<OnChainUnbondingResponse>(payload)
    val oldValue = internalState.wallet.account.unbondingDelegation
    walletProcessor.processUnbonding(internalState.wallet, response)
    return if (oldValue != internalState.wallet.account.unbondingDelegation) {
        StateChanges(iListOf(Changes.accountBalances), null)
    } else {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.onChainStakingRewards(payload: String): StateChanges {
    val response = parser.asTypedObject<OnChainStakingRewardsResponse>(payload)
    val oldValue = internalState.wallet.account.stakingRewards
    walletProcessor.processStakingRewards(internalState.wallet, response)
    return if (oldValue != internalState.wallet.account.stakingRewards) {
        StateChanges(iListOf(Changes.accountBalances), null)
    } else {
        StateChanges(iListOf())
    }
}
