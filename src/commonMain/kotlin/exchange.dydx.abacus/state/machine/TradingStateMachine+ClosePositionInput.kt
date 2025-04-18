package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.responses.StateResponse
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class ClosePositionInputField(val rawValue: String) {
    market("market"),
    size("size.size"),
    percent("size.percent"),

    useLimit("useLimit"),
    limitPrice("price.limitPrice");

    companion object {
        operator fun invoke(rawValue: String?) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.closePosition(
    data: String?,
    type: ClosePositionInputField,
    subaccountNumber: Int
): StateResponse {
    val result = closePositionInputProcessor.closePosition(
        inputState = internalState.input,
        walletState = internalState.wallet,
        marketSummaryState = internalState.marketsSummary,
        configs = internalState.configs,
        rewardsParams = internalState.rewardsParams,
        data = data,
        type = type,
        subaccountNumber = subaccountNumber,
    )
    result.changes?.let {
        updateStateChanges(it)
    }
    return StateResponse(
        state,
        result.changes,
        if (result.error != null) iListOf(result.error) else null,
    )
}
