package indexer.models.chain

import kotlinx.serialization.Serializable

//
// getAccountBalance Response
//
@Serializable
data class OnChainAccountBalanceObject(
    val denom: String? = null,
    val amount: String? = null,
)
