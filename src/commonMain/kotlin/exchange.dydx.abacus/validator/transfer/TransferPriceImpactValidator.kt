package exchange.dydx.abacus.validator.transfer

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.TransferValidatorProtocol

internal class TransferPriceImpactValidator(
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
        val transfer = internalState.input.transfer
        val type = transfer.type ?: return null
        val aggregatePriceImpact = transfer.summary?.aggregatePriceImpact ?: return null

        val maxPriceImpact = 0.02 // 2%

        when (type) {
            TransferType.deposit, TransferType.withdrawal -> {
                if (aggregatePriceImpact >= maxPriceImpact) {
                    return listOf(
                        error(
                            type = ErrorType.error,
                            errorCode = "PRICE_IMPACT_TOO_HIGH",
                            fields = null,
                            actionStringKey = null,
                            titleStringKey = "APP.TRADE.PRICE_IMPACT",
                            textStringKey = "ERRORS.ONBOARDING.PRICE_IMPACT_TOO_HIGH",
                        ),
                    )
                } else {
                    return null
                }
            }
            TransferType.transferOut -> return null
        }
    }
}
