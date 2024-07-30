package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment

internal class FieldsInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    override fun validate(
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
        parser.asNativeList(transaction["fields"])?.let { fields ->
            val errors = mutableListOf<Any>()
            for (item in fields) {
                parser.asNativeMap(item)?.let {
                    parser.asString(it["field"])?.let { field ->
                        parser.asString(it["type"])?.let { type ->
                            missingData(
                                parser,
                                transaction,
                                transactionType,
                                field,
                                type,
                            )?.let {
                                errors.add(it)
                            }
                        }
                    }
                }
            }
            return if (errors.size > 0) errors else null
        }
        return null
    }

    private fun missingData(
        parser: ParserProtocol,
        transaction: Map<String, Any>,
        transactionType: String,
        field: String,
        type: String,
    ): Map<String, Any>? {
        return if (hasData(parser, transaction, field, type)) {
            null
        } else {
            val errorCode = errorCode(field)
            val errorStringKey = errorStringKey(transaction, transactionType, field)
            if (errorCode != null && errorStringKey != null) {
                required(errorCode, field, errorStringKey)
            } else {
                null
            }
        }
    }

    private fun errorCode(field: String): String? {
        return when (field) {
            "size.size", "size.usdcSize" -> "REQUIRED_SIZE"
            "price.triggerPrice" -> "REQUIRED_TRIGGER_PRICE"
            "price.limitPrice" -> "REQUIRED_LIMIT_PRICE"
            "price.trailingPercent" -> "REQUIRED_TRAILING_PERCENT"
            "timeInForce" -> "REQUIRED_TIME_IN_FORCE"
            "goodTil" -> "REQUIRED_GOOD_UNTIL"
            "execution" -> "REQUIRED_EXECUTION"
            "asset" -> "REQUIRED_ASSET"
            "address" -> "REQUIRED_ADDRESS"
            else -> null
        }
    }

    private fun errorStringKey(
        transaction: Map<String, Any>,
        transactionType: String,
        field: String
    ): String? {
        return when (transactionType) {
            "trade", "closePosition" -> when (field) {
                "size.size" -> "APP.TRADE.ENTER_AMOUNT"
                "price.triggerPrice" -> "APP.TRADE.ENTER_TRIGGER_PRICE"
                "price.limitPrice" -> "APP.TRADE.ENTER_LIMIT_PRICE"
                "price.trailingPercent" -> "APP.TRADE.ENTER_TRAILING_PERCENT"
                "timeInForce" -> "APP.TRADE.ENTER_TIME_IN_FORCE"
                "goodTil" -> "APP.TRADE.ENTER_GOOD_UNTIL"
                "execution" -> "APP.TRADE.ENTER_EXECUTION"
                else -> null
            }

            "transfer" -> when (transaction["type"]) {
                "WITHDRAWAL", "DEPOSIT" -> when (field) {
                    "size.usdcSize" -> "APP.TRADE.ENTER_AMOUNT"
                    "address" -> "APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS"
                    else -> null
                }

                "TRANSFER_OUT" -> when (field) {
                    "address" -> "APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS"
                    else -> null
                }

                else -> null
            }

            else -> null
        }
    }

    private fun hasData(
        parser: ParserProtocol,
        transaction: Map<String, Any>,
        field: String,
        type: String,
    ): Boolean {
        val value = when (field) {
            "size.size" -> {
                val inputField = parser.asString(parser.value(transaction, "size.input"))
                if (inputField != null) {
                    parser.value(transaction, inputField)
                } else {
                    null
                }
            }

            else -> parser.value(transaction, field)
        }
        return if (value != null) {
            validData(parser, value, type)
        } else {
            false
        }
    }

    private fun validData(parser: ParserProtocol, data: Any, type: String): Boolean {
        return when (type) {
            "double" -> (parser.asDouble(data) ?: 0.0) != 0.0
            "int" -> (parser.asInt(data) ?: 0) != 0
            "string" -> (parser.asString(data) ?: "") != ""
            else -> true
        }
    }
}
