package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.modify

internal class InputValidator(
    val localizer: LocalizerProtocol?,
    val formatter: Formatter?,
    val parser: ParserProtocol,
) {
    private val errorTypeLookup = mapOf<String, Int>(
        "ERROR" to 0,
        "REQUIRED" to 1,
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
        TradeInputValidator(localizer, formatter, parser),
    )

    private val closePositionValidators = listOf<ValidatorProtocol>(
        AccountInputValidator(localizer, formatter, parser),
        FieldsInputValidator(localizer, formatter, parser),
        TradeInputValidator(localizer, formatter, parser),
    )

    private val transferValidators = listOf<ValidatorProtocol>(
        FieldsInputValidator(localizer, formatter, parser),
        TransferInputValidator(localizer, formatter, parser),
    )

    private val triggerOrdersValidators = listOf<ValidatorProtocol>(
        TriggerOrdersInputValidator(localizer, formatter, parser),
    )

    fun validate(
        staticTyping: Boolean,
        internalState: InternalState,
        subaccountNumber: Int?,
        wallet: Map<String, Any>?,
        user: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        markets: Map<String, Any>?,
        input: Map<String, Any>?,
        configs: Map<String, Any>?,
        currentBlockAndHeight: BlockAndTime?,
        environment: V4Environment?,
    ): Map<String, Any>? {
        return if (input != null) {
            val transactionType = parser.asString(input["current"]) ?: return input
            val transaction = parser.asNativeMap(input[transactionType]) ?: return input
            val isChildSubaccount = subaccountNumber != null && subaccountNumber >= NUM_PARENT_SUBACCOUNTS

            val errors = sort(
                validateTransaction(
                    staticTyping = staticTyping,
                    internalState = internalState,
                    wallet = wallet,
                    user = user,
                    subaccount = subaccount,
                    markets = markets,
                    configs = configs,
                    currentBlockAndHeight = currentBlockAndHeight,
                    transaction = transaction,
                    transactionType = transactionType,
                    environment = environment,
                ),
            )

            if (isChildSubaccount) {
                if (errors != input["childSubaccountErrors"]) {
                    input.modify("childSubaccountErrors", errors)
                } else {
                    input
                }
            } else {
                if (errors != input["errors"]) {
                    input.modify("errors", errors)
                } else {
                    input
                }
            }
        } else {
            input?.modify("errors", null)
            input?.modify("childSubaccountErrors", null)
        }
    }

    private fun validateTransaction(
        staticTyping: Boolean,
        internalState: InternalState,
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
        val validators = validatorsFor(transactionType)
        return if (validators != null) {
            val result = mutableListOf<Any>()
            for (validator in validators) {
                val validatorErrors =
                    validator.validate(
                        staticTyping = staticTyping,
                        internalState = internalState,
                        wallet = wallet,
                        user = user,
                        subaccount = subaccount,
                        markets = markets,
                        configs = configs,
                        currentBlockAndHeight = currentBlockAndHeight,
                        transaction = transaction,
                        transactionType = transactionType,
                        environment = environment,
                    )
                if (validatorErrors != null) {
                    result.addAll(validatorErrors)
                }
            }
            if (result.size > 0) result else null
        } else {
            null
        }
    }

    private fun validatorsFor(transactionType: String): List<ValidatorProtocol>? {
        return when (transactionType) {
            "closePosition" -> closePositionValidators
            "transfer" -> transferValidators
            "trade" -> tradeValidators
            "triggerOrders" -> triggerOrdersValidators
            else -> null
        }
    }

    private fun sort(errors: List<Any>?): List<Any>? {
        return if (errors != null) {
            return errors.sortedWith { error1, error2 ->
                val typeString1 = parser.asString(parser.value(error1, "type"))
                val typeString2 = parser.asString(parser.value(error2, "type"))
                if (typeString1 == typeString2) {
                    val codeString1 = parser.asString(parser.value(error1, "code"))
                    val codeString2 = parser.asString(parser.value(error2, "code"))
                    val code1 = if (codeString1 != null) errorCodeLookup[codeString1] else null
                    val code2 = if (codeString2 != null) errorCodeLookup[codeString2] else null
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
                    val type1 = if (typeString1 != null) errorCodeLookup[typeString1] else null
                    val type2 = if (typeString2 != null) errorCodeLookup[typeString2] else null
                    if (type1 != null) {
                        if (type2 != null) {
                            type1 - type2
                        } else {
                            1
                        }
                    } else {
                        if (type2 != null) {
                            -1
                        } else {
                            0
                        }
                    }
                }
            }
        } else {
            null
        }
    }
}
