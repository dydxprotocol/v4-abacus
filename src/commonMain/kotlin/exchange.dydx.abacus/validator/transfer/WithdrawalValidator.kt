package exchange.dydx.abacus.validator.transfer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
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
        //TODO: mmm replace with actual validation values
        val currentBlock = 50000000000// parser.asInt(parser.value(environment, "currentBlock"))
        val withdrawalGating = parser.asMap(parser.value(configs, "withdrawalGating"))
        val withdrawalsAndTransfersUnblockedAtBlock = parser.asInt(withdrawalGating?.get("withdrawalsAndTransfersUnblockedAtBlock"))
        val withdrawalsAndTransfersUnblockedAtBlockIsInTheFuture = (withdrawalsAndTransfersUnblockedAtBlock ?: 0) > currentBlock

        val withdrawalCapacity = parser.asMap(parser.value(configs, "withdrawalCapacity"))
        val maxWithdrawalCapacity = parser.asDecimal(parser.value(withdrawalCapacity, "maxWithdrawalCapacity")) ?: BigDecimal.ZERO
        val type = parser.asString(parser.value(transfer, "type"))
        val size = parser.asMap(parser.value(transfer, "size"))
        val usdcSize = parser.asDecimal(size?.get("usdcSize")) ?: BigDecimal.ZERO
        val usdcSizeInputIsGreaterThanCapacity = usdcSize > maxWithdrawalCapacity

        if ((type == TransferType.withdrawal.rawValue || type == TransferType.transferOut.rawValue)
            && withdrawalsAndTransfersUnblockedAtBlockIsInTheFuture) {
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
        } else if (type == TransferType.withdrawal.rawValue && usdcSizeInputIsGreaterThanCapacity) {
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
