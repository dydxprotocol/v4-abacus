package exchange.dydx.abacus.state.v2.supervisor

import ApiState
import BlockAndTime
import NetworkState
import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.state.manager.IndexerURIs
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.updateHeight
import exchange.dydx.abacus.utils.IMap
import kollections.toIMap
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

internal interface ConnectionStatsDelegate {
    var validatorUrl: String?
    var indexerConfig: IndexerURIs?
}

internal class ConnectionStats(
    private val stateMachine: TradingStateMachine,
    private val helper: NetworkHelper,
    private val delegate: ConnectionStatsDelegate,
) {
    internal val heightPollingDuration = 10.0
    internal var firstBlockAndTime: BlockAndTime? = null
    internal var lastIndexerCallTime: Instant? = null
    private var lastValidatorCallTime: Instant? = null

    private val MAX_NUM_BLOCK_DELAY = 15

    internal var indexerState = NetworkState()
    internal var validatorState = NetworkState()
    private var apiState: ApiState? = null
        set(value) {
            val oldValue = field
            if (field !== value) {
                field = value
                didSetApiState(field, oldValue)
            }
        }

    private var indexerRestriction: UsageRestriction? = null
        set(value) {
            if (field !== value) {
                field = value
                didSetIndexerRestriction(field)
            }
        }

    private fun didSetApiState(apiState: ApiState?, oldValue: ApiState?) {
        helper.stateNotification?.apiStateChanged(apiState)
        helper.dataNotification?.apiStateChanged(apiState)
        trackApiStateIfNeeded(apiState, oldValue)
        when (apiState?.status) {
            ApiStatus.VALIDATOR_DOWN, ApiStatus.VALIDATOR_HALTED -> {
                delegate.validatorUrl = null
            }

            ApiStatus.INDEXER_DOWN, ApiStatus.INDEXER_HALTED -> {
                delegate.indexerConfig = null
            }

            else -> {}
        }
    }

    private fun didSetIndexerRestriction(indexerRestriction: UsageRestriction?) {
        updateRestriction()
    }

    private fun updateRestriction() {
//        restriction = indexerRestriction ?: addressRestriction ?: UsageRestriction.noRestriction
    }


    private fun updateApiState() {
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            apiState = apiState(apiState, indexerState, validatorState)
        }
    }

    private fun apiState(
        apiState: ApiState?,
        indexerState: NetworkState,
        validatorState: NetworkState,
    ): ApiState {
        var status = apiState?.status ?: ApiStatus.UNKNOWN
        var haltedBlock = apiState?.haltedBlock
        var blockDiff: Int? = null

        val delayedStatus = delayedStatus(indexerState, validatorState)
        when (validatorState.status) {
            NetworkStatus.NORMAL -> {
                when (indexerState.status) {
                    NetworkStatus.NORMAL, NetworkStatus.UNKNOWN -> {
                        status = ApiStatus.NORMAL
                        haltedBlock = null
                    }

                    NetworkStatus.UNREACHABLE -> {
                        status = ApiStatus.INDEXER_DOWN
                        haltedBlock = null
                    }

                    NetworkStatus.HALTED -> {
                        status = ApiStatus.INDEXER_HALTED
                        haltedBlock = indexerState.blockAndTime?.block
                    }
                }
            }

            NetworkStatus.UNKNOWN -> {
                when (indexerState.status) {
                    NetworkStatus.NORMAL -> {
                        status = ApiStatus.NORMAL
                        haltedBlock = null
                    }

                    NetworkStatus.UNKNOWN -> {
                        status = ApiStatus.UNKNOWN
                        haltedBlock = null
                    }

                    NetworkStatus.UNREACHABLE -> {
                        status = ApiStatus.INDEXER_DOWN
                        haltedBlock = null
                    }

                    NetworkStatus.HALTED -> {
                        status = ApiStatus.INDEXER_HALTED
                        haltedBlock = indexerState.blockAndTime?.block
                    }
                }
            }

            NetworkStatus.UNREACHABLE -> {
                status = ApiStatus.VALIDATOR_DOWN
                haltedBlock = null
            }

            NetworkStatus.HALTED -> {
                status = ApiStatus.VALIDATOR_HALTED
                haltedBlock = validatorState.blockAndTime?.block
            }
        }
        if (status == ApiStatus.NORMAL) {
            if (!delayedStatus) {
                val indexerBlock = indexerState.blockAndTime?.block
                val validatorBlock = validatorState.blockAndTime?.block
                if (indexerBlock != null && validatorBlock != null) {
                    val diff = validatorBlock - indexerBlock
                    if (diff > MAX_NUM_BLOCK_DELAY) {
                        status = ApiStatus.INDEXER_TRAILING
                        blockDiff = diff
                        haltedBlock = null
                    }
                }
            }
        }
        val validatorBlockAndTime = validatorState.blockAndTime
        val indexerBlockAndTime = indexerState.blockAndTime
        val block = if (validatorBlockAndTime != null) {
            if (indexerBlockAndTime != null) {
                max(validatorBlockAndTime.block, indexerBlockAndTime.block)
            } else validatorBlockAndTime.block
        } else indexerBlockAndTime?.block
        if (apiState?.status != status ||
            apiState.height != block ||
            apiState.haltedBlock != haltedBlock ||
            apiState.trailingBlocks != blockDiff
        ) {
            return ApiState(status, block, haltedBlock, blockDiff)
        }
        return apiState
    }

    private fun delayedStatus(
        indexerState: NetworkState,
        validatorState: NetworkState,
    ): Boolean {
        return delayedInRequestTime(indexerState) || delayedInRequestTime(validatorState)
    }

    private fun delayedInRequestTime(networkState: NetworkState): Boolean {
        val now = Clock.System.now()
        val time = networkState.requestTime
        return if (time != null) {
            val gap = now - time
            if (gap > 4.seconds) {
                // If request (time) was sent more than 4 seconds ago
                // The app was probably in background
                true
            } else {
                val previousTime = networkState.previousRequestTime
                if (previousTime != null) {
                    val gap = time - previousTime
                    // If request (time) was sent more than 15 seconds after
                    // previous request (previousTime)
                    // The app was probably in background
                    gap > (heightPollingDuration * 1.5).seconds
                } else false
            }
        } else false
    }

    internal fun parseHeight(response: String) {
        val json = helper.parser.decodeJsonObject(response)
        if (json != null && json["error"] != null) {
            validatorState.updateHeight(null, null)
            firstBlockAndTime = null
        } else {
            val header = helper.parser.asMap(helper.parser.value(json, "header"))
            val height = helper.parser.asInt(header?.get("height"))
            val time = helper.parser.asDatetime(header?.get("time"))
            validatorState.updateHeight(height, time)
            // Always use validator blockAndHeight as source of truth
            if (firstBlockAndTime == null) {
                firstBlockAndTime = validatorState.blockAndTime
            }
            if (height != null && time != null) {
                val stateResponse = stateMachine.updateHeight(BlockAndTime(height, time))
                helper.ioImplementations.threading?.async(ThreadingType.main) {
                    helper.stateNotification?.stateChanged(
                        stateResponse.state,
                        stateResponse.changes,
                    )
                }
            }
        }
        updateApiState()
    }

    private fun trackApiStateIfNeeded(apiState: ApiState?, oldValue: ApiState?) {
        if (apiState?.abnormalState() == true || oldValue?.abnormalState() == true) {
            val indexerTime = lastIndexerCallTime?.toEpochMilliseconds()?.toDouble()
            val validatorTime = lastValidatorCallTime?.toEpochMilliseconds()?.toDouble()
            val interval = if (indexerTime != null) (Clock.System.now().toEpochMilliseconds()
                .toDouble() - indexerTime) else null
            val params = mapOf(
                "lastSuccessfulIndexerRPC" to indexerTime,
                "lastSuccessfulFullNodeRPC" to validatorTime,
                "elapsedTime" to interval,
                "blockHeight" to indexerState.blockAndTime?.block,
                "nodeHeight" to validatorState.blockAndTime?.block,
            ).filterValues { it != null } as Map<String, Any>

            tracking(AnalyticsEvent.NetworkStatus.rawValue, params.toIMap())
        }
    }

    private fun tracking(eventName: String, params: IMap<String, Any>?) {
        val paramsAsString = helper.jsonEncoder.encode(params)
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.ioImplementations.tracking?.log(eventName, paramsAsString)
        }
    }

}