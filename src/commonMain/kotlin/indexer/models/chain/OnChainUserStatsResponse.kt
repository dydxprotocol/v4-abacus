package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainUserStatsResponse(
    val takerNotional: String? = null,
    val makerNotional: String? = null,
)
