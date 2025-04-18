package exchange.dydx.abacus.validator

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class PositionChange(val rawValue: String) {
    NONE("None"),
    NEW("New"),
    INCREASING("Increasing"),
    DECREASING("Decreasing"),
    CROSSING("Crossing"),
    CLOSING("Closing");

    companion object {
        operator fun invoke(rawValue: String) =
            PositionChange.values().firstOrNull { it.rawValue == rawValue }
    }
}

internal interface ValidatorProtocol {
    fun validate(
        internalState: InternalState,
        subaccountNumber: Int?,
        currentBlockAndHeight: BlockAndTime?,
        inputType: InputType,
        environment: V4Environment?,
    ): List<ValidationError>?
}

internal interface TradeValidatorProtocol {
    fun validateTrade(
        internalState: InternalState,
        subaccountNumber: Int?,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<ValidationError>?
}

internal interface TransferValidatorProtocol {
    fun validateTransfer(
        internalState: InternalState,
        currentBlockAndHeight: BlockAndTime?,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<ValidationError>?
}
