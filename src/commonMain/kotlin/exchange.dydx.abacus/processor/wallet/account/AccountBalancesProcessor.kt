package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalAccountBalanceState
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
}
