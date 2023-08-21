package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IList
import kollections.iListOf
import kotlinx.serialization.json.Json


internal fun TradingStateMachine.subaccounts(payload: String): StateChanges {
    val json = parser.asList(parser.asMap(Json.parseToJsonElement(payload))?.get("subaccounts"))
    return if (json != null) {
        receivedSubaccounts(json)
    } else StateChanges(iListOf<Changes>(), null, null)
}

internal fun TradingStateMachine.receivedSubaccounts(
    payload: IList<Any>
): StateChanges {
    this.wallet = walletProcessor.receivedSubaccounts(wallet, payload)
    return StateChanges(iListOf(Changes.subaccount))
}

internal fun TradingStateMachine.findOrder(
    orderId: String,
    subaccountNumber: Int,
): SubaccountOrder? {
    val subaccount = state?.subaccount(subaccountNumber) ?: return null
    val orders = subaccount.orders ?: return null
    val order = orders.firstOrNull {
        it.id == orderId
    } ?: return null
    return when (order.status) {
        OrderStatus.open, OrderStatus.pending, OrderStatus.untriggered -> order
        else -> null
    }
}

internal fun TradingStateMachine.findOrder(
    clientId: Int,
    subaccountNumber: Int,
): SubaccountOrder? {
    val subaccount = state?.subaccount(subaccountNumber) ?: return null
    val orders = subaccount.orders ?: return null
    val order = orders.firstOrNull {
        it.clientId == clientId
    } ?: return null
    return order
}

internal fun TradingStateMachine.findOrderInData(
    orderId: String,
    subaccountNumber: Int,
): SubaccountOrder? {
    val subaccount = state?.subaccount(subaccountNumber) ?: return null
    val orders = subaccount.orders ?: return null
    val order = orders.firstOrNull {
        it.id == orderId
    } ?: return null
    return when (order.status) {
        OrderStatus.open, OrderStatus.pending, OrderStatus.untriggered -> order
        else -> null
    }
}
