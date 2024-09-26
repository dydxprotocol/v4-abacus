package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainWithdrawalCapacityResponse(
    val limiterCapacityList: List<OnChainLimiterCapacity>? = null,
)

@Serializable
data class OnChainLimiterCapacity(
    val limiter: OnChainLimiter? = null,
    val capacity: String? = null,
)

@Serializable
data class OnChainLimiter(
    val period: OnChainLimiterPeriod? = null,
    val baselineMinimum: String? = null,
    val baselineTvlPpm: Double? = null,
)

@Serializable
data class OnChainLimiterPeriod(
    val seconds: String? = null,
    val nanos: Double? = null,
)
