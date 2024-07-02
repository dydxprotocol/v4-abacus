package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class AccountBalancesProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    fun receivedBalances(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val modified = mutableMapOf<String, Any>()
            for (itemPayload in payload) {
                val data = parser.asNativeMap(itemPayload)
                if (data != null) {
                    val denom = parser.asString(data["denom"])
                    if (denom != null) {
                        val key = "$denom"
                        val existing =
                            parser.asNativeMap(existing?.get(key))?.mutable() ?: mutableMapOf()
                        existing.safeSet("denom", denom)
                        existing.safeSet("amount", parser.asDecimal(data["amount"]))
                        modified.safeSet(key, existing)
                    }
                }
            }
            return modified
        } else {
            null
        }
    }
}
