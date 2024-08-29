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

    private fun validateClosingOnly(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): Map<String, Any>? {
        val marketId = parser.asNativeMap(market?.get("assetId")) ?: ""
        val canTrade = parser.asBool(parser.value(market, "status.canTrade")) ?: true
        val canReduce = parser.asBool(parser.value(market, "status.canTrade")) ?: true
        return if (canTrade) {
            if (restricted) {
                when (change) {
                    PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING ->
                        errorDeprecated(
                            type = "ERROR",
                            errorCode = "RESTRICTED_USER",
                            fields = listOf("size.size"),
                            actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                            titleStringKey = "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_CLOSE_POSITION_ONLY",
                            textStringKey = "ERRORS.TRADE_BOX.MARKET_ORDER_CLOSE_POSITION_ONLY",
                        )

                    else -> null
                }
            } else {
                return null
            }
        } else if (canReduce) {
            when (change) {
                PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING ->
                    errorDeprecated(
                        type = "ERROR",
                        errorCode = "CLOSE_ONLY_MARKET",
                        fields = listOf("size.size"),
                        actionStringKey = "APP.TRADE.MODIFY_SIZE_FIELD",
                        titleStringKey = "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                        textStringKey = "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                        textParams = mapOf(
                            "MARKET" to mapOf(
                                "value" to marketId,
                                "format" to "string",
                            ),
                        ),
                    )

                else -> null
            }
        } else {
            errorDeprecated(
                type = "ERROR",
                errorCode = "CLOSED_MARKET",
                fields = null,
                actionStringKey = null,
                titleStringKey = "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                textStringKey = "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                textParams = mapOf(
                    "MARKET" to mapOf(
                        "value" to marketId,
                        "format" to "string",
                    ),
                ),
            )
        }
    }
}
