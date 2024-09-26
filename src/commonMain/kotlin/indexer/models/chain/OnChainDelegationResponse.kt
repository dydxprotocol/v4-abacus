package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainDelegationResponse(
    val delegationResponses: List<OnChainDelegationObject>? = null,
    val pagination: OnChainAccountPagination? = null,
)

@Serializable
data class OnChainDelegationObject(
    val delegatorAddress: String? = null,
    val validatorAddress: String? = null,
    val shares: String? = null,
    val balance: OnChainAccountBalanceObject? = null,
)

@Serializable
data class OnChainAccountPagination(
    val nextKey: String? = null,
    val total: String? = null,
)
