package exchange.dydx.abacus.output

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class LaunchIncentiveSeason(
    val label: String,
    val startTimeInMilliseconds: Double
) {
    companion object {
        internal fun create(
            existing: LaunchIncentiveSeason?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
        ): LaunchIncentiveSeason? {
            data?.let {
                val label = parser.asString(data["label"]) ?: return null
                val startTimeInMilliseconds =
                    parser.asDouble(data["startTimestamp"])?.let { it * 1000.0 } ?: return null
                return if (existing?.label != label ||
                    existing.startTimeInMilliseconds != startTimeInMilliseconds
                ) {
                    LaunchIncentiveSeason(
                        label,
                        startTimeInMilliseconds,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "LaunchIncentiveSeason not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class LaunchIncentiveSeasons(
    val seasons: IList<LaunchIncentiveSeason>
) {
    companion object {
        internal fun create(
            existing: LaunchIncentiveSeasons?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
        ): LaunchIncentiveSeasons? {
            data?.let {
                val seasons = data.mapNotNull { LaunchIncentiveSeason.create(null, parser, it) }
                return if (existing?.seasons != seasons) {
                    LaunchIncentiveSeasons(seasons.toIList())
                } else {
                    existing
                }
            }
            Logger.d { "LaunchIncentiveSeasons not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class LaunchIncentive(
    val seasons: LaunchIncentiveSeasons
) {
    companion object {
        internal fun create(
            existing: LaunchIncentive?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
        ): LaunchIncentive? {
            data?.let {
                val seasonsData =
                    parser.asNativeList(data["seasons"]) as? List<Map<String, Any>> ?: return null
                val seasons =
                    LaunchIncentiveSeasons.create(null, parser, seasonsData) ?: return null
                return LaunchIncentive(seasons)
            }
            Logger.d { "LaunchIncentive not valid" }
            return null
        }
    }

    val currentSeason: String?
        get() = seasons.seasons.lastOrNull()?.label
}
