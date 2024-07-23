package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainRewardsParamsResponse(
    val params: OnChainRewardsParams? = null,
)

@Serializable
data class OnChainRewardsParams(
    val treasuryAccount: String? = null,
    val denom: String? = null,
    val denomExponent: Double? = null,
    val marketId: Double? = null,
    val feeMultiplierPpm: Double? = null,
)
