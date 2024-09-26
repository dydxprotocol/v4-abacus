package exchange.dydx.abacus.validator

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.transfer.DepositValidator
import exchange.dydx.abacus.validator.transfer.TransferFieldsValidator
import exchange.dydx.abacus.validator.transfer.TransferOutValidator
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

    override fun validateDeprecated(
        wallet: Map<String, Any>?,
        user: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        markets: Map<String, Any>?,
        configs: Map<String, Any>?,
        currentBlockAndHeight: BlockAndTime?,
        transaction: Map<String, Any>,
        transactionType: String,
        environment: V4Environment?,
    ): List<Any>? {
        if (transactionType == "transfer") {
            val errors = mutableListOf<Any>()
            val restricted = parser.asBool(user?.get("restricted")) ?: false
            for (validator in transferValidators) {
                val validatorErrors =
                    validator.validateTransferDeprecated(
                        wallet = wallet,
                        subaccount = subaccount,
                        transfer = transaction,
                        configs = configs,
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
        return null
    }
}
