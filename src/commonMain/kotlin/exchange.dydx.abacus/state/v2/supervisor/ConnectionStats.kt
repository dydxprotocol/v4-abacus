package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.state.manager.ApiState
import exchange.dydx.abacus.state.manager.ApiStatus
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.IndexerURIs
import exchange.dydx.abacus.state.manager.NetworkState
import exchange.dydx.abacus.state.manager.NetworkStatus
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.updateHeight
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.iMapOf
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

    @Suppress("PropertyName", "VariableNaming", "MagicNumber")
    private val MAX_NUM_BLOCK_DELAY = 25

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
            } else {
                validatorBlockAndTime.block
            }
        } else {
            indexerBlockAndTime?.block
        }
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
                } else {
                    false
                }
            }
        } else {
            false
        }
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

    private fun apiStateParams(): IMap<String, Any>? {
        val indexerTime = lastIndexerCallTime?.toEpochMilliseconds()
        val validatorTime = lastValidatorCallTime?.toEpochMilliseconds()
        val interval = indexerTime?.let { Clock.System.now().toEpochMilliseconds() - it }
        return iMapOf(
            "lastSuccessfulIndexerRPC" to indexerTime?.toDouble(),
            "lastSuccessfulFullNodeRPC" to validatorTime?.toDouble(),
            "elapsedTime" to interval?.toDouble(),
            "blockHeight" to indexerState.blockAndTime?.block,
            "nodeHeight" to validatorState.blockAndTime?.block,
            "validatorUrl" to helper.validatorUrl,
        ) as IMap<String, Any>?
    }

    private fun trackApiStateIfNeeded(apiState: ApiState?, oldValue: ApiState?) {
        if (apiState?.abnormalState() == true || oldValue?.abnormalState() == true) {
            tracking(AnalyticsEvent.NetworkStatus.rawValue)
        }
    }

    private fun tracking(eventName: String, params: IMap<String, Any>? = null) {
        val additionalParams = apiStateParams()
        val paramsAsString = helper.jsonEncoder.encode(params?.let { ParsingHelper.merge(it, additionalParams) } ?: additionalParams)
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.ioImplementations.tracking?.log(eventName, paramsAsString)
        }
    }
}
