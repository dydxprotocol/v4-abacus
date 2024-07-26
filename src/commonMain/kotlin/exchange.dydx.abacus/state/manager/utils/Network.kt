package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.utils.ServerTime
import kollections.JsExport
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class NetworkStatus(val rawValue: String) {
    UNKNOWN("UNKNOWN"),
    UNREACHABLE("UNREACHABLE"),
    HALTED("HALTED"),
    NORMAL("NORMAL");

    companion object {
        operator fun invoke(rawValue: String) =
            NetworkStatus.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
class BlockAndTime(
    val block: Int,
    val time: Instant,
    val localTime: Instant = ServerTime.now()
)

@JsExport
internal class NetworkState() {
    var status: NetworkStatus = NetworkStatus.UNKNOWN
        private set
    var blockAndTime: BlockAndTime? = null
        private set
    private var sameBlockCount: Int = 0
    private var failCount: Int = 0

    internal var time: Instant? = null

    internal var previousRequestTime: Instant? = null
    internal var requestTime: Instant? = null

    internal fun updateHeight(height: Int?, heightTime: Instant?) {
        time = ServerTime.now()
        if (height != null && heightTime != null) {
            failCount = 0
            if (blockAndTime?.block != height) {
                blockAndTime = BlockAndTime(height, heightTime)
                sameBlockCount = 0
            } else {
                sameBlockCount += 1
            }
        } else {
            failCount += 1
        }
        updateStatus()
    }

    private fun updateStatus() {
        val time = time
        status = if (time != null) {
            if (failCount >= 3) {
                NetworkStatus.UNREACHABLE
            } else if (sameBlockCount >= 6) {
                NetworkStatus.HALTED
            } else if (blockAndTime != null) {
                NetworkStatus.NORMAL
            } else {
                NetworkStatus.UNKNOWN
            }
        } else {
            NetworkStatus.UNKNOWN
        }
    }
}

@JsExport
@Serializable
enum class ApiStatus(val rawValue: String) {
    UNKNOWN("UNKNOWN"),
    VALIDATOR_DOWN("VALIDATOR_DOWN"),
    VALIDATOR_HALTED("VALIDATOR_HALTED"),
    INDEXER_DOWN("INDEXER_DOWN"),
    INDEXER_HALTED("INDEXER_HALTED"),
    INDEXER_TRAILING("INDEXER_TRAILING"),
    NORMAL("NORMAL");

    companion object {
        operator fun invoke(rawValue: String) =
            ApiStatus.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class ApiState(
    val status: ApiStatus?,
    val height: Int?,
    val haltedBlock: Int?,
    val trailingBlocks: Int?
) {
    fun abnormalState(): Boolean {
        return status == ApiStatus.INDEXER_DOWN || status == ApiStatus.INDEXER_HALTED || status == ApiStatus.VALIDATOR_DOWN || status == ApiStatus.VALIDATOR_HALTED
    }
}
