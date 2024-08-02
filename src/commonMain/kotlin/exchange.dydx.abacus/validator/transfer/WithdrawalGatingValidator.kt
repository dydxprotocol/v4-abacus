package exchange.dydx.abacus.validator.transfer

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.TransferValidatorProtocol

internal class WithdrawalGatingValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TransferValidatorProtocol {
    override fun validateTransfer(
        staticTyping: Boolean,
        internalState: InternalState,
        wallet: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        transfer: Map<String, Any>,
        configs: Map<String, Any>?,
        currentBlockAndHeight: BlockAndTime?,
        restricted: Boolean,
        environment: V4Environment?
    ): List<Any>? {
        val currentBlock = currentBlockAndHeight?.block ?: Int.MAX_VALUE // parser.asInt(parser.value(environment, "currentBlock"))
        val withdrawalGating = parser.asMap(parser.value(configs, "withdrawalGating"))
        val withdrawalsAndTransfersUnblockedAtBlock = if (staticTyping) {
            internalState.configs.withdrawalGating?.withdrawalsAndTransfersUnblockedAtBlock ?: 0
        } else {
            parser.asInt(withdrawalGating?.get("withdrawalsAndTransfersUnblockedAtBlock")) ?: 0
        }
        val blockDurationSeconds = if (environment?.isMainNet == true) 1.1 else 1.5
        val secondsUntilUnblock = ((withdrawalsAndTransfersUnblockedAtBlock - currentBlock) * blockDurationSeconds).toInt()

        val type = parser.asString(parser.value(transfer, "type"))

        if ((type == TransferType.withdrawal.rawValue || type == TransferType.transferOut.rawValue) &&
            secondsUntilUnblock > 0
        ) {
            return listOf(
                error(
                    type = ErrorType.error.rawValue,
                    errorCode = "",
                    fields = null,
                    actionStringKey = "WARNINGS.ACCOUNT_FUND_MANAGEMENT.${if (type == TransferType.withdrawal.rawValue) "WITHDRAWAL_PAUSED_ACTION" else "TRANSFERS_PAUSED_ACTION"}",
                    titleStringKey = "WARNINGS.ACCOUNT_FUND_MANAGEMENT.${if (type == TransferType.withdrawal.rawValue) "WITHDRAWAL_PAUSED_TITLE" else "TRANSFERS_PAUSED_TITLE"}",
                    textStringKey = "WARNINGS.ACCOUNT_FUND_MANAGEMENT.${if (type == TransferType.withdrawal.rawValue) "WITHDRAWAL_PAUSED_DESCRIPTION" else "TRANSFERS_PAUSED_DESCRIPTION"}",
                    textParams = mapOf(
                        "SECONDS" to mapOf(
                            "value" to secondsUntilUnblock,
                            "format" to "string",
                        ),
                    ),
                    action = null,
                    link = environment?.links?.withdrawalGateLearnMore,
                    linkText = "APP.GENERAL.LEARN_MORE_ARROW",
                ),
            )
        } else {
            return null
        }
    }
}
