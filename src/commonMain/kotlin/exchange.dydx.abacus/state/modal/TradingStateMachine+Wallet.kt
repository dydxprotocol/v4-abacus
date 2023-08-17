package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIList
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.receivedSubaccountSubscribed(
    payload: IMap<String, Any>,
    height: Int?,
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
    val subaccountNumber = parser.asInt(payload["subaccountNumber"]) ?: 0
    return StateChanges(changes, null, iListOf(subaccountNumber))
}

internal fun TradingStateMachine.receivedAccountsChanges(
    payload: IMap<String, Any>,
    info: SocketInfo,
    height: Int?,
): StateChanges {
    this.wallet = walletProcessor.channel_data(wallet, payload, info, height)
    val changes = iMutableListOf<Changes>()

    val idElements = info.id?.split("/")
    val subaccountNumber =
        if (idElements?.size == 2) parser.asInt(idElements.lastOrNull()) ?: 0 else 0
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
    return StateChanges(changes, null, iListOf(subaccountNumber))
}

internal fun TradingStateMachine.receivedBatchAccountsChanges(
    payload: IList<Any>,
    info: SocketInfo,
    height: Int?
): StateChanges {
    var changes = iListOf<Changes>()
    var subaccountNumbers = iListOf<Int>()
    for (item in payload) {
        parser.asMap(item)?.let {
            val itemChanges = receivedAccountsChanges(it, info, height)
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
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    return receivedUser(json)
}

internal fun TradingStateMachine.receivedUser(payload: IMap<String, Any>): StateChanges {
    this.wallet = walletProcessor.receivedUser(wallet, payload)
    return StateChanges(iListOf(Changes.wallet), null)
}

internal fun TradingStateMachine.onChainUserFeeTier(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    return receivedOnChainUserFeeTier(json)
}

internal fun TradingStateMachine.receivedOnChainUserFeeTier(payload: IMap<String, Any>): StateChanges {
    this.wallet = walletProcessor.receivedOnChainUserFeeTier(wallet, payload)
    return StateChanges(iListOf(Changes.wallet), null)
}

internal fun TradingStateMachine.onChainUserStats(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    return receivedOnChainUserStats(json)
}

internal fun TradingStateMachine.receivedOnChainUserStats(payload: IMap<String, Any>): StateChanges {
    this.wallet = walletProcessor.receivedOnChainUserStats(wallet, payload)
    return StateChanges(iListOf(Changes.wallet), null)
}

internal fun TradingStateMachine.fills(payload: String, subaccountNumber: Int): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    return receivedFills(json, subaccountNumber)
}

internal fun TradingStateMachine.receivedFills(
    payload: IMap<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val size = parser.asList(payload["fills"])?.size ?: 0
    return if (size > 0) {
        wallet = walletProcessor.receivedFills(wallet, payload, subaccountNumber)
        StateChanges(iListOf(Changes.fills), null, iListOf(subaccountNumber))
    } else StateChanges(iListOf<Changes>())
}

internal fun TradingStateMachine.transfers(payload: String, subaccountNumber: Int): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    return receivedTransfers(json, subaccountNumber)
}

internal fun TradingStateMachine.receivedTransfers(
    payload: IMap<String, Any>,
    subaccountNumber: Int,
): StateChanges {
    val size = parser.asList(payload["transfers"])?.size ?: 0
    return if (size > 0) {
        wallet = walletProcessor.receivedTransfers(wallet, payload, subaccountNumber)
        StateChanges(iListOf(Changes.transfers), null, iListOf(subaccountNumber))
    } else StateChanges(iListOf<Changes>())
}

internal fun TradingStateMachine.orderCanceled(orderId: String, subaccountNumber: Int): StateChanges {
    val wallet = wallet
    if (wallet != null) {
        val (modifiedWallet, updated) = walletProcessor.orderCanceled(
            wallet,
            orderId,
            subaccountNumber
        )
        if (updated) {
            this.wallet = modifiedWallet
            return StateChanges(iListOf(Changes.subaccount), null, iListOf(subaccountNumber))
        }
    }
    return StateChanges(iListOf<Changes>())
}