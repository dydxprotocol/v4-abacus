package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainUnbondingResponse(
    val unbondingResponses: List<OnChainUnbondingObject>? = null,
    val pagination: OnChainAccountPagination? = null,
)

@Serializable
data class OnChainUnbondingObject(
    val delegatorAddress: String? = null,
    val validatorAddress: String? = null,
    val entries: List<OnChainUnbondingEntry>? = null,
)

@Serializable
data class OnChainUnbondingEntry(
    val creationHeight: String? = null,
    val completionTime: String? = null,
    val initialBalance: String? = null,
    val balance: String? = null,
    val unbondingId: String? = null,
    val unbondingOnHoldRefCount: String? = null,
)
