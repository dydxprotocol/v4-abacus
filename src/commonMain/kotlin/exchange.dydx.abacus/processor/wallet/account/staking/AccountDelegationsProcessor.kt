package exchange.dydx.abacus.processor.wallet.account.staking

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAccountBalanceState
import exchange.dydx.abacus.state.internalstate.InternalStakingDelegationState
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
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
                val validator = parser.asString(item.validatorAddress)
                val delegator = parser.asString(item.delegatorAddress)
                val shares = parser.asDecimal(item.shares)
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

    fun receivedDeprecated(
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

    fun receivedDelegationsDeprecated(
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
