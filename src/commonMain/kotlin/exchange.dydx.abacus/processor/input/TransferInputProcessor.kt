package exchange.dydx.abacus.processor.input

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.state.model.TransferInputField
import kollections.iListOf

internal class TransferInputProcessor(

) {
    fun transfer(
        inputState: InternalInputState,
        data: String?,
        type: TransferInputField?,
        subaccountNumber: Int = 0
    ): StateChanges {

        //inputState.currentType = InputType.TRANSFER

        return StateChanges(
            changes = iListOf(Changes.subaccount, Changes.input),
            markets = null,
            subaccountNumbers = iListOf(),
        )
    }
}