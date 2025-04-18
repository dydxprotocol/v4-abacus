package exchange.dydx.abacus.validator

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.TradeValidationTracker

internal class InputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    private val parser: ParserProtocol,
    tradeValidationTracker: TradeValidationTracker,
) {
    private val errorTypeLookup = mapOf<String, Int>(
        "REQUIRED" to 0,
        "ERROR" to 1,
        "WARNING" to 2,
    )
    private val errorCodeLookup = mapOf<String, Int>(
        "REQUIRED_WALLET" to 1000,
        "REQUIRED_ACCOUNT" to 1001,
        "NO_EQUITY_DEPOSIT_FIRST" to 1002,

        "USER_MAX_ORDERS" to 1010,
        "ORDER_SIZE_BELOW_MIN_SIZE" to 1020,
        "NEW_POSITION_SIZE_OVER_MAX" to 1022,

        "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY" to 1030,
        "MARKET_ORDER_ONE_SIDED_LIQUIDITY" to 1031,
        "MARKET_ORDER_ERROR_INDEX_PRICE_SLIPPAGE" to 1032,
        "MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE" to 1033,

        "ORDER_WOULD_FLIP_POSITION" to 1034,
        "TRIGGER_MUST_ABOVE_INDEX_PRICE" to 1040,
        "TRIGGER_MUST_BELOW_INDEX_PRICE" to 1041,
        "LIMIT_MUST_ABOVE_TRIGGER_PRICE" to 1042,
        "LIMIT_MUST_BELOW_TRIGGER_PRICE" to 1043,
        "PRICE_MUST_POSITIVE" to 1044,

        "INVALID_NEW_ACCOUNT_MARGIN_USAGE" to 1053,

        "BUY_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE" to 1060,
        "SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE" to 1061,

        "ORDER_CROSSES_OWN_ORDER" to 1070,
        "ORDER_WITH_CURRENT_ORDERS_INVALID" to 1071,

        "BRACKET_ORDER_TAKE_PROFIT_BELOW_EXPECTED_PRICE" to 1080,
        "BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE" to 1081,
        "BRACKET_ORDER_TAKE_PROFIT_BELOW_LIQUIDATION_PRICE" to 1082,
        "BRACKET_ORDER_TAKE_PROFIT_ABOVE_LIQUIDATION_PRICE" to 1083,
        "BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE" to 1090,
        "BRACKET_ORDER_STOP_LOSS_ABOVE_EXPECTED_PRICE" to 1091,
        "BRACKET_ORDER_STOP_LOSS_BELOW_LIQUIDATION_PRICE" to 1092,
        "BRACKET_ORDER_STOP_LOSS_ABOVE_LIQUIDATION_PRICE" to 1093,

        "WOULD_NOT_REDUCE_UNCHECK" to 1100,

        "MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE" to 1201,
        "MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE" to 1202,
        "LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_LOWER" to 1203,
        "LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_HIGHER" to 1204,
    )

    private val tradeValidators = listOf<ValidatorProtocol>(
        AccountInputValidator(localizer, formatter, parser),
        FieldsInputValidator(localizer, formatter, parser),
        TradeInputValidator(localizer, formatter, parser, tradeValidationTracker),
    )

    private val closePositionValidators = listOf<ValidatorProtocol>(
        AccountInputValidator(localizer, formatter, parser),
        FieldsInputValidator(localizer, formatter, parser),
        TradeInputValidator(localizer, formatter, parser, tradeValidationTracker),
    )

    private val transferValidators = listOf<ValidatorProtocol>(
        FieldsInputValidator(localizer, formatter, parser),
        TransferInputValidator(localizer, formatter, parser),
    )

    private val triggerOrdersValidators = listOf<ValidatorProtocol>(
        TriggerOrdersInputValidator(localizer, formatter, parser),
    )

    fun validate(
        internalState: InternalState,
        subaccountNumber: Int?,
        currentBlockAndHeight: BlockAndTime?,
        environment: V4Environment?,
    ) {
        val errors = sort(
            validateTransaction(
                internalState = internalState,
                subaccountNumber = subaccountNumber,
                currentBlockAndHeight = currentBlockAndHeight,
                environment = environment,
            ),
        )

        val isChildSubaccount = subaccountNumber != null && subaccountNumber >= NUM_PARENT_SUBACCOUNTS
        if (isChildSubaccount) {
            internalState.input.childSubaccountErrors = errors
        } else {
            internalState.input.errors = errors
        }
    }

    private fun validateTransaction(
        internalState: InternalState,
        subaccountNumber: Int?,
        currentBlockAndHeight: BlockAndTime?,
        environment: V4Environment?,
    ): List<ValidationError>? {
        val inputType = internalState.input.currentType ?: return null
        val validators = validatorsFor(inputType)
        if (validators.isNullOrEmpty()) {
            return null
        }

        val result: MutableList<ValidationError> = mutableListOf()
        for (validator in validators) {
            val validatorErrors = validator.validate(
                internalState = internalState,
                subaccountNumber = subaccountNumber,
                currentBlockAndHeight = currentBlockAndHeight,
                inputType = inputType,
                environment = environment,
            ) ?: emptyList()
            result.addAll(validatorErrors)
        }
        return result
    }

    private fun validatorsFor(inputType: InputType): List<ValidatorProtocol>? {
        return when (inputType) {
            InputType.TRADE -> tradeValidators
            InputType.TRANSFER -> transferValidators
            InputType.CLOSE_POSITION -> closePositionValidators
            InputType.ADJUST_ISOLATED_MARGIN -> null
            InputType.TRIGGER_ORDERS -> triggerOrdersValidators
        }
    }

    private fun sort(errors: List<ValidationError>?): List<ValidationError>? {
        if (errors == null) {
            return null
        }

        return errors.sortedWith { error1, error2 ->
            val type1 = error1.type
            val type2 = error2.type
            if (type1 == type2) {
                val code1 = errorCodeLookup[error1.code]
                val code2 = errorCodeLookup[error2.code]
                if (code1 != null) {
                    if (code2 != null) {
                        code1 - code2
                    } else {
                        1
                    }
                } else {
                    if (code2 != null) {
                        -1
                    } else {
                        0
                    }
                }
            } else {
                val typeCode1 = errorTypeLookup[type1.rawValue]
                val typeCode2 = errorTypeLookup[type2.rawValue]
                if (typeCode1 != null) {
                    if (typeCode2 != null) {
                        typeCode1 - typeCode2
                    } else {
                        1
                    }
                } else {
                    if (typeCode2 != null) {
                        -1
                    } else {
                        0
                    }
                }
            }
        }
    }
}
