package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainAccountBalanceObject(
    val denom: String? = null,
    val amount: String? = null,
)
