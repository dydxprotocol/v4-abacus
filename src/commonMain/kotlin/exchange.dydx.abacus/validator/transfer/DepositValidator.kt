package exchange.dydx.abacus.validator.transfer

import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.TransferValidatorProtocol

internal class DepositValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TransferValidatorProtocol {
    override fun validateTransfer(
        internalState: InternalState,
        currentBlockAndHeight: BlockAndTime?,
        restricted: Boolean,
        environment: V4Environment?
    ): List<ValidationError>? {
        return null
    }
}
