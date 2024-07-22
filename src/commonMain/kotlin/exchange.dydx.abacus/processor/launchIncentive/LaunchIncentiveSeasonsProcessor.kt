package exchange.dydx.abacus.processor.launchIncentive

import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.models.configs.ConfigsLaunchIncentiveResponse

internal interface LaunchIncentiveSeasonsProcessorProtocol {
    fun process(
        existing: List<LaunchIncentiveSeason>?,
        payload: ConfigsLaunchIncentiveResponse?
    ): List<LaunchIncentiveSeason>?
}

internal class LaunchIncentiveSeasonsProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), LaunchIncentiveSeasonsProcessorProtocol {
    private val itemProcessor = LaunchIncentiveSeasonProcessor(parser = parser)

    private val millisecondsInSecond = 1000.0

    override fun process(
        existing: List<LaunchIncentiveSeason>?,
        payload: ConfigsLaunchIncentiveResponse?
    ): List<LaunchIncentiveSeason>? {
        val newValues = payload?.data?.tradingSeasons?.mapNotNull {
            LaunchIncentiveSeason(
                label = it.label ?: return@mapNotNull null,
                startTimeInMilliseconds = it.startTimestamp?.let { it * millisecondsInSecond } ?: return@mapNotNull null,
            )
        }
        return if (existing != newValues) {
            newValues
        } else {
            existing
        }
    }

    override fun received(
        existing: List<Any>?,
        payload: List<Any>
    ): List<Any>? {
        val modified = mutableListOf<Map<String, Any>>()
        for (item in payload) {
            parser.asNativeMap(item)?.let { it ->
                itemProcessor.received(null, it)?.let { received ->
                    modified.add(received)
                }
            }
        }
        return modified
    }
}
