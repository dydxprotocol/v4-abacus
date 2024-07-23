package indexer.models.configs

import kotlinx.serialization.Serializable

@Serializable
data class ConfigsLaunchIncentiveResponse(
    val data: ConfigsLaunchIncentiveData? = null
)

@Serializable
data class ConfigsLaunchIncentiveData(
    val tradingSeasons: List<ConfigsLaunchIncentiveSeason>? = null,
)

@Serializable
data class ConfigsLaunchIncentiveSeason(
    val label: String? = null,
    val startTimestamp: Double? = null,
)

@Serializable
data class ConfigsLaunchIncentivePoints(
    val incentivePoints: Double? = null,
    val marketMakingIncentivePoints: Double? = null,
    val dydxRewards: Double? = null,
)
