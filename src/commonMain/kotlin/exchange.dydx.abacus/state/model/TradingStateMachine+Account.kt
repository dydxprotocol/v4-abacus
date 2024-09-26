package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.Logger
import kollections.iListOf
import kollections.toIList
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal fun TradingStateMachine.account(payload: String): StateChanges {
    val json = try {
        parser.asMap(Json.parseToJsonElement(payload))
    } catch (exception: SerializationException) {
        Logger.e {
            "Failed to deserialize account: $payload \n" +
                "Exception: $exception"
        }
        null
    }
    return if (json != null) {
        receivedAccount(json)
    } else {
        StateChanges(iListOf<Changes>(), null, null)
    }
}

private fun TradingStateMachine.receivedAccount(
    payload: Map<String, Any>
): StateChanges {
    if (staticTyping) {
        walletProcessor.processAccount(
            internalState = internalState.wallet,
            payload = payload,
        )
    } else {
        this.wallet = walletProcessor.receivedAccount(wallet, payload)
    }
    return StateChanges(iListOf(Changes.subaccount, Changes.tradingRewards))
}

internal fun TradingStateMachine.updateHeight(
    height: BlockAndTime,
): StateResponse {
    this.currentBlockAndHeight = height
    if (staticTyping) {
        val (modifiedWallet, updated, subaccountIds) = walletProcessor.updateHeight(
            existing = internalState.wallet,
            height = height,
        )
        return if (updated) {
            val changes = StateChanges(iListOf(Changes.subaccount), null, subaccountIds?.toIList())
            val realChanges = updateStateChanges(changes)
            StateResponse(state, realChanges, null, null)
        } else {
            return StateResponse(state, null, null, null)
        }
    } else {
        val (modifiedWallet, updated, subaccountIds) = walletProcessor.updateHeightDeprecated(
            wallet,
            height,
        )
        return if (updated) {
            this.wallet = modifiedWallet
            val changes = StateChanges(iListOf(Changes.subaccount), null, subaccountIds?.toIList())
            val realChanges = updateStateChanges(changes)
            StateResponse(state, realChanges, null, null)
        } else {
            return StateResponse(state, null, null, null)
        }
    }
}

internal fun TradingStateMachine.findOrder(
    clientId: String,
    subaccountNumber: Int,
): SubaccountOrder? {
    val subaccount = state?.subaccount(subaccountNumber) ?: return null
    val orders = subaccount.orders ?: return null
    val order = orders.firstOrNull {
        it.clientId == clientId
    } ?: return null
    return order
}
