package exchange.dydx.abacus.validator.transfer

import com.ionspin.kotlin.bignum.decimal.BigDecimal
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

internal class WithdrawalCapacityValidator(
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
        val withdrawalCapacity = parser.asMap(parser.value(configs, "withdrawalCapacity"))
        val maxWithdrawalCapacity = if (staticTyping) {
            internalState.configs.withdrawalCapacity?.maxWithdrawalCapacity ?: BigDecimal.fromLong(Long.MAX_VALUE)
        } else {
            parser.asDecimal(parser.value(withdrawalCapacity, "maxWithdrawalCapacity")) ?: BigDecimal.fromLong(Long.MAX_VALUE)
        }
        val type = parser.asString(parser.value(transfer, "type"))
        val size = parser.asMap(parser.value(transfer, "size"))
        val usdcSize = parser.asDecimal(size?.get("usdcSize")) ?: BigDecimal.ZERO
        val usdcSizeInputIsGreaterThanCapacity = usdcSize > maxWithdrawalCapacity

        if (type == TransferType.withdrawal.rawValue && usdcSizeInputIsGreaterThanCapacity) {
            return listOf(
                error(
                    type = ErrorType.error.rawValue,
                    errorCode = "",
                    fields = null,
                    actionStringKey = "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_ACTION",
                    titleStringKey = "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_TITLE",
                    textStringKey = "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_DESCRIPTION",
                    textParams = mapOf(
                        "USDC_LIMIT" to mapOf(
                            "value" to maxWithdrawalCapacity.doubleValue(false),
                            "format" to "price",
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
