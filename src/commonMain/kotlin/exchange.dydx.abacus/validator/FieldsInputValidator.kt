package exchange.dydx.abacus.validator

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment

internal class FieldsInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    override fun validate(
        internalState: InternalState,
        subaccountNumber: Int?,
        currentBlockAndHeight: BlockAndTime?,
        inputType: InputType,
        environment: V4Environment?,
    ): List<ValidationError>? {
        return null
    }
}
