package exchange.dydx.abacus.validator.transfer

import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.isAddressValid
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.TransferValidatorProtocol

internal class WithdrawalValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TransferValidatorProtocol {
    override fun validateTransfer(
        wallet: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        transfer: Map<String, Any>,
        configs: Map<String, Any>?,
        restricted: Boolean,
        environment: V4Environment?
    ): List<Any>? {
        //TODO: replace with actual validation values
        val currentBlock = 15// parser.asInt(parser.value(environment, "currentBlock"))
        //TODO: how to get withdrawalCapacity?
        val withdrawalsAndTransfersUnblockedAtBlock = parser.asInt(parser.value(configs, "withdrawalCapacity"))
        val withdrawalsAndTransfersUnblockedAtBlockIsInTheFuture = (withdrawalsAndTransfersUnblockedAtBlock ?: 0) > currentBlock
        val type = parser.asString(parser.value(transfer, "type"))

        //TODO: how to get withdrawalCapacity?
        if ((type == TransferType.withdrawal.rawValue || type == TransferType.transferOut.rawValue) && withdrawalsAndTransfersUnblockedAtBlockIsInTheFuture) {
            return listOf(
                error(
                    "ERROR",
                    "TEST1",
                    listOf("address"),
                    "TEST1",
                    "TEST1",
                    "TEST1",
                ),
            )
        } else if (type == TransferType.withdrawal.rawValue && withdrawalsAndTransfersUnblockedAtBlockIsInTheFuture) {
            return listOf(
                error(
                    "ERROR",
                    "TEST2",
                    listOf("address"),
                    "TEST2",
                    "TEST2",
                    "TEST2",
                ),
            )
        } else {
            return null
        }
    }
}
