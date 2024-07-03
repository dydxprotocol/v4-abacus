package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.Logger
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
    this.wallet = walletProcessor.subscribed(wallet, payload, height)
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
    val subaccountNumbers = MarginCalculator.getChangedSubaccountNumbers(
        parser,
        account,
        subaccountNumber ?: 0,
        parser.asMap(input?.get("trade")),
    )

    return StateChanges(
        changes,
        null,
        subaccountNumbers,
    )
}

internal fun TradingStateMachine.receivedSubaccountsChanges(
    payload: Map<String, Any>,
    info: SocketInfo,
    height: BlockAndTime?,
): StateChanges {
    this.wallet = walletProcessor.channel_data(wallet, payload, info, height)
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

internal fun TradingStateMachine.user(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedUser(json)
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.receivedUser(payload: Map<String, Any>): StateChanges {
    this.wallet = walletProcessor.receivedUser(wallet, payload)
    return StateChanges(iListOf(Changes.wallet), null)
}

internal fun TradingStateMachine.onChainUserFeeTier(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedOnChainUserFeeTier(json)
    } else {
        StateChanges.noChange
    }
}

private fun TradingStateMachine.receivedOnChainUserFeeTier(payload: Map<String, Any>): StateChanges {
    this.wallet = walletProcessor.receivedOnChainUserFeeTier(wallet, payload)
    return StateChanges(iListOf(Changes.wallet), null)
}

internal fun TradingStateMachine.onChainUserStats(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        this.wallet = walletProcessor.receivedOnChainUserStats(wallet, json)
        StateChanges(iListOf(Changes.wallet), null)
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.fills(payload: String, subaccountNumber: Int): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedFills(json, subaccountNumber)
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.receivedFills(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val size = parser.asList(payload["fills"])?.size ?: 0
    return if (size > 0) {
        wallet = walletProcessor.receivedFills(wallet, payload, subaccountNumber)
        StateChanges(iListOf(Changes.fills), null, iListOf(subaccountNumber))
    } else {
        StateChanges(iListOf<Changes>())
    }
}

internal fun TradingStateMachine.transfers(payload: String, subaccountNumber: Int): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedTransfers(json, subaccountNumber)
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.receivedTransfers(
    payload: Map<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val size = parser.asList(payload["transfers"])?.size ?: 0
    return if (size > 0) {
        wallet = walletProcessor.receivedTransfers(wallet, payload, subaccountNumber)
        StateChanges(iListOf(Changes.transfers), null, iListOf(subaccountNumber))
    } else {
        StateChanges(iListOf<Changes>())
    }
}

internal fun TradingStateMachine.orderCanceled(
    orderId: String,
    subaccountNumber: Int
): StateChanges {
    val wallet = wallet
    if (wallet != null) {
        val (modifiedWallet, updated) = walletProcessor.orderCanceled(
            wallet,
            orderId,
            subaccountNumber,
        )
        if (updated) {
            this.wallet = modifiedWallet
            return StateChanges(iListOf(Changes.subaccount), null, iListOf(subaccountNumber))
        }
    }
    return StateChanges(iListOf<Changes>())
}

internal fun TradingStateMachine.onChainAccountBalances(payload: String): StateChanges {
    return try {
        val json = Json.parseToJsonElement(payload)
        val account = json.jsonArray.toList()
        this.wallet = walletProcessor.receivedAccountBalances(wallet, account)
        return StateChanges(iListOf(Changes.accountBalances), null)
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
    val response = parser.decodeJsonObject(payload)
    return try {
        val delegations = response?.get("delegationResponses")?.let {
            parser.asList(it)
        } ?: iListOf()
        this.wallet = walletProcessor.receivedDelegations(wallet, delegations)
        return StateChanges(iListOf(Changes.accountBalances), null)
    } catch (e: Exception) {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.onChainUnbonding(payload: String): StateChanges {
    val response = parser.decodeJsonObject(payload)
    return try {
        val unbonding = response?.get("unbondingResponses")?.let {
            parser.asList(it)
        } ?: iListOf()
        this.wallet = walletProcessor.receivedUnbonding(wallet, unbonding)
        return StateChanges(iListOf(Changes.accountBalances), null)
    } catch (e: Exception) {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.onChainStakingRewards(payload: String): StateChanges {
    val response = parser.decodeJsonObject(payload)
    return try {
        this.wallet = walletProcessor.receivedStakingRewards(wallet, response)
        return StateChanges(iListOf(Changes.accountBalances), null)
    } catch (e: Exception) {
        StateChanges(iListOf())
    }
}
