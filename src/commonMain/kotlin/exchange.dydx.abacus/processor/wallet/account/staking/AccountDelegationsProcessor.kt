package exchange.dydx.abacus.processor.wallet.account.staking

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalAccountBalanceState
import exchange.dydx.abacus.state.InternalStakingDelegationState
import indexer.models.chain.OnChainDelegationResponse

internal interface AccountDelegationsProcessorProtocol {
    fun process(
        existing: Map<String, InternalAccountBalanceState>?,
        payload: OnChainDelegationResponse?,
    ): Map<String, InternalAccountBalanceState>?

    fun processDelegations(
        existing: List<InternalStakingDelegationState>?,
        payload: OnChainDelegationResponse?,
    ): List<InternalStakingDelegationState>?
}

internal class AccountDelegationsProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), AccountDelegationsProcessorProtocol {

    override fun process(
        existing: Map<String, InternalAccountBalanceState>?,
        payload: OnChainDelegationResponse?,
    ): Map<String, InternalAccountBalanceState>? {
        return if (payload != null) {
            val modified = mutableMapOf<String, InternalAccountBalanceState>()
            for (item in payload.delegationResponses ?: emptyList()) {
                val denom = parser.asString(item.balance?.denom)
                val amount = parser.asDecimal(item.balance?.amount)
                if (denom != null && amount != null) {
                    modified[denom] = InternalAccountBalanceState(
                        denom = denom,
                        amount = amount + (modified[denom]?.amount ?: BigDecimal.ZERO),
                    )
                }
            }
            if (existing != modified) {
                modified
            } else {
                existing
            }
        } else {
            null
        }
    }

    override fun processDelegations(
        existing: List<InternalStakingDelegationState>?,
        payload: OnChainDelegationResponse?,
    ): List<InternalStakingDelegationState>? {
        return if (payload != null) {
            val modified = mutableListOf<InternalStakingDelegationState>()
            for (item in payload.delegationResponses ?: emptyList()) {
                val validator = parser.asString(item.delegation?.validatorAddress)
                val delegator = parser.asString(item.delegation?.delegatorAddress)
                val shares = parser.asDecimal(item.delegation?.shares)
                val amount = parser.asDecimal(item.balance?.amount)
                val denom = parser.asString(item.balance?.denom)
                if (amount != null && denom != null) {
                    modified.add(
                        InternalStakingDelegationState(
                            delegatorAddress = delegator,
                            validatorAddress = validator,
                            shares = shares,
                            balance = InternalAccountBalanceState(
                                denom = denom,
                                amount = amount,
                            ),
                        ),
                    )
                }
            }
            if (existing != modified) {
                modified
            } else {
                existing
            }
        } else {
            null
        }
    }
}
