package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class AccountDelegationsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    fun received(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val modified = mutableMapOf<String, Any>()
            for (itemPayload in payload) {
                val item = parser.asNativeMap(itemPayload)
                val balance = parser.asNativeMap(item?.get("balance"))

                if (balance != null) {
                    val denom = parser.asString(balance["denom"])
                    if (denom != null) {
                        val key = "$denom"
                        val current =
                            parser.asNativeMap(modified[key])?.mutable()
                        if (current == null) {
                            modified.safeSet(
                                key,
                                mapOf(
                                    "denom" to denom,
                                    "amount" to parser.asDecimal(
                                        balance["amount"],
                                    ),
                                ),
                            )
                        } else {
                            val amount = parser.asDecimal(balance["amount"]);
                            val existingAmount = parser.asDecimal(current["amount"]);
                            if (amount != null && existingAmount != null) {
                                current.safeSet("amount", amount + existingAmount)
                            }
                        }
                    }
                }
            }
            return modified
        } else {
            null
        }
    }

    fun receivedDelegations(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): List<Any>? {
        return if (payload != null) {
            val modified = mutableListOf<Any>()
            for (itemPayload in payload) {
                val item = parser.asNativeMap(itemPayload)
                val validator = parser.asString(parser.value(item, "delegation.validatorAddress"))
                val amount = parser.asDecimal(parser.value(item, "balance.amount"))
                val denom = parser.asString(parser.value(item, "balance.denom"))
                if (validator != null && amount != null) {
                    modified.add(
                        mapOf(
                            "validator" to validator,
                            "amount" to amount,
                            "denom" to denom,
                        ),
                    )
                }
            }
            return modified
        } else {
            null
        }
    }
}
