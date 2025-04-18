package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.processor.input.TriggerOrdersInputProcessor
import exchange.dydx.abacus.responses.StateResponse
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class TriggerOrdersInputField(val rawValue: String) {
    marketId("marketId"),
    size("size"),

    stopLossOrderId("stopLossOrder.orderId"),
    stopLossOrderSize("stopLossOrder.size"),
    stopLossOrderType("stopLossOrder.type"),
    stopLossLimitPrice("stopLossOrder.price.limitPrice"),
    stopLossPrice("stopLossOrder.price.triggerPrice"),
    stopLossPercentDiff("stopLossOrder.price.percentDiff"),
    stopLossUsdcDiff("stopLossOrder.price.usdcDiff"),

    takeProfitOrderId("takeProfitOrder.orderId"),
    takeProfitOrderSize("takeProfitOrder.size"),
    takeProfitOrderType("takeProfitOrder.type"),
    takeProfitLimitPrice("takeProfitOrder.price.limitPrice"),
    takeProfitPrice("takeProfitOrder.price.triggerPrice"),
    takeProfitPercentDiff("takeProfitOrder.price.percentDiff"),
    takeProfitUsdcDiff("takeProfitOrder.price.usdcDiff");

    companion object {
        operator fun invoke(rawValue: String) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.triggerOrders(
    data: String?,
    type: TriggerOrdersInputField?,
    subaccountNumber: Int,
): StateResponse {
    val triggerOrdersInputProcessor = TriggerOrdersInputProcessor(parser)
    val changes = triggerOrdersInputProcessor.triggerOrderInput(
        inputState = internalState.input,
        account = internalState.wallet.account,
        data = data,
        type = type,
        subaccountNumber = subaccountNumber,
    )
    updateStateChanges(changes)
    return StateResponse(
        state = state,
        changes = changes,
        errors = null,
    )
}
