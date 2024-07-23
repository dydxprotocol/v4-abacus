package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAccountBalanceState
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.models.chain.OnChainAccountBalanceObject

internal interface AccountBalancesProcessorProtocol {
    fun process(
        existing: Map<String, InternalAccountBalanceState>?,
        payload: List<OnChainAccountBalanceObject>?,
    ): Map<String, InternalAccountBalanceState>?
}

internal class AccountBalancesProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), AccountBalancesProcessorProtocol {

    override fun process(
        existing: Map<String, InternalAccountBalanceState>?,
        payload: List<OnChainAccountBalanceObject>?,
    ): Map<String, InternalAccountBalanceState>? {
        if (payload != null) {
            val modified = mutableMapOf<String, InternalAccountBalanceState>()
            for (itemPayload in payload) {
                val denom = itemPayload.denom ?: continue
                val amount = parser.asDecimal(itemPayload.amount) ?: continue
                val value = InternalAccountBalanceState(
                    denom = denom,
                    amount = amount,
                )
                modified[denom] = value
            }
            return modified
        }
        return existing
    }

    fun receivedBalancesDeprecated(
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
