package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.processor.input.AdjustIsolatedMarginInputProcessor
import exchange.dydx.abacus.responses.StateResponse
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class AdjustIsolatedMarginInputField {
    Market,
    Type,
    Amount,
    AmountPercent,
    ChildSubaccountNumber,
}

fun TradingStateMachine.adjustIsolatedMargin(
    data: String?,
    type: AdjustIsolatedMarginInputField?,
    parentSubaccountNumber: Int,
): StateResponse {
    val adjustIsolatedMarginInputProcessor = AdjustIsolatedMarginInputProcessor(parser)
    val result = adjustIsolatedMarginInputProcessor.adjustIsolatedMargin(
        inputState = internalState.input,
        walletState = internalState.wallet,
        markets = internalState.marketsSummary.markets,
        data = data,
        type = type,
        parentSubaccountNumber = parentSubaccountNumber,
    )

    result.changes?.let { updateStateChanges(it) }

    return StateResponse(
        state = state,
        changes = result.changes,
        errors = if (result.error != null) iListOf(result.error) else null,
    )
}
