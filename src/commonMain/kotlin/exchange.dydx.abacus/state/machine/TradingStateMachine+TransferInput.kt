package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.processor.input.TransferInputProcessor
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.manager.V4Environment
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class TransferInputField(val rawValue: String) {
    type("type"),

    usdcSize("size.usdcSize"),
    size("size.size"),
    usdcFee("fee"),

    exchange("exchange"),
    chain("chain"),
    token("token"),
    address("address"),
    MEMO("memo"),
    decimals("decimals"),
    fastSpeed("fastSpeed");

    companion object {
        operator fun invoke(rawValue: String) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.transfer(
    data: String?,
    type: TransferInputField?,
    subaccountNumber: Int = 0,
    environment: V4Environment,
): StateResponse {
    val processor = TransferInputProcessor(
        parser = parser,
        routerProcessor = routerProcessor,
        environment = environment,
    )
    val result = processor.transfer(
        inputState = internalState.input,
        walletState = internalState.wallet,
        data = data,
        inputField = type,
        subaccountNumber = subaccountNumber,
    )

    if (result.changes != null) {
        updateStateChanges(result.changes)
    }

    return StateResponse(
        state = state,
        changes = result.changes,
        errors = if (result.error != null) iListOf(result.error) else null,
    )
}
