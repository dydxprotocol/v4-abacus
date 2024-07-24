package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class BlockReward(
    val tradingReward: Double,
    val createdAtMilliseconds: Double,
    val createdAtHeight: Int,
) {
    companion object {
        internal fun create(
            existing: BlockReward?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): BlockReward? {
            data?.let {
                val tradingReward = parser.asDouble(data["tradingReward"])
                val createdAtMilliseconds =
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                val createdAtHeight = parser.asInt(data["createdAtHeight"])

                if (tradingReward != null && createdAtMilliseconds != null && createdAtHeight != null) {
                    return if (existing?.tradingReward != tradingReward ||
                        existing.createdAtMilliseconds != createdAtMilliseconds ||
                        existing.createdAtHeight != createdAtHeight
                    ) {
                        BlockReward(
                            tradingReward,
                            createdAtMilliseconds,
                            createdAtHeight,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "BlockReward not valid" }
            return null
        }
    }
}
