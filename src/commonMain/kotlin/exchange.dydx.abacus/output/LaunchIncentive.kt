package exchange.dydx.abacus.output

import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class LaunchIncentiveSeason(
    val label: String,
    val startTimeInMilliseconds: Double
)

@JsExport
@Serializable
data class LaunchIncentiveSeasons(
    val seasons: IList<LaunchIncentiveSeason>
)

@JsExport
@Serializable
data class LaunchIncentive(
    val seasons: LaunchIncentiveSeasons
) {
    val currentSeason: String?
        get() = seasons.seasons.lastOrNull()?.label
}
