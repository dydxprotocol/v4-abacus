package exchange.dydx.abacus.validator

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.transfer.DepositValidator
import exchange.dydx.abacus.validator.transfer.TransferFieldsValidator
import exchange.dydx.abacus.validator.transfer.TransferOutValidator
import exchange.dydx.abacus.validator.transfer.TransferPriceImpactValidator
import exchange.dydx.abacus.validator.transfer.WithdrawalCapacityValidator
import exchange.dydx.abacus.validator.transfer.WithdrawalGatingValidator

internal class TransferInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    private val transferValidators = listOf<TransferValidatorProtocol>(
        TransferFieldsValidator(localizer, formatter, parser),
        DepositValidator(localizer, formatter, parser),
        TransferOutValidator(localizer, formatter, parser),
        WithdrawalGatingValidator(localizer, formatter, parser),
        WithdrawalCapacityValidator(localizer, formatter, parser),
        TransferPriceImpactValidator(localizer, formatter, parser),
    )

    override fun validate(
        internalState: InternalState,
        subaccountNumber: Int?,
        currentBlockAndHeight: BlockAndTime?,
        inputType: InputType,
        environment: V4Environment?,
    ): List<ValidationError>? {
        if (inputType != InputType.TRANSFER) {
            return null
        }

        val errors = mutableListOf<ValidationError>()
        val restricted = internalState.wallet.user?.restricted ?: false
        for (validator in transferValidators) {
            val validatorErrors =
                validator.validateTransfer(
                    internalState = internalState,
                    currentBlockAndHeight = currentBlockAndHeight,
                    restricted = restricted,
                    environment = environment,
                )
            if (validatorErrors != null) {
                errors.addAll(validatorErrors)
            }
        }

        return errors
    }
}
