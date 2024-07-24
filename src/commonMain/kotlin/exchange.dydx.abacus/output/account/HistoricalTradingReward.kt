package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.nextMonth
import kollections.JsExport
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days

@JsExport
@Serializable
data class HistoricalTradingReward(
    val amount: Double,
    val cumulativeAmount: Double,
    val startedAtInMilliseconds: Double,
    val endedAtInMilliseconds: Double,
) {
    internal val startedAt: Instant
        get() = Instant.fromEpochMilliseconds(startedAtInMilliseconds.toLong())
    internal val endedAt: Instant
        get() = Instant.fromEpochMilliseconds(endedAtInMilliseconds.toLong())

    companion object {
        internal fun create(
            amount: Double,
            cumulativeAmount: Double,
            startedAt: Instant,
            endedAt: Instant,
        ): HistoricalTradingReward {
            return HistoricalTradingReward(
                amount,
                cumulativeAmount,
                startedAt.toEpochMilliseconds().toDouble(),
                endedAt.toEpochMilliseconds().toDouble(),
            )
        }

        internal fun create(
            existing: HistoricalTradingReward?,
            parser: ParserProtocol,
            data: Map<*, *>,
            period: String,
        ): HistoricalTradingReward? {
            data?.let {
                val amount = parser.asDouble(data["amount"])
                val cumulativeAmount = parser.asDouble(data["cumulativeAmount"])
                val startedAt = parser.asDatetime(data["startedAt"])
                val endedAt = parser.asDatetime(data["endedAt"])

                if (amount != null && cumulativeAmount != null && startedAt != null) {
                    return if (existing?.amount != amount ||
                        existing.cumulativeAmount != cumulativeAmount ||
                        existing.startedAt != startedAt ||
                        existing.endedAt != endedAt
                    ) {
                        create(
                            amount,
                            cumulativeAmount,
                            startedAt,
                            endedAt ?: getEndedAt(startedAt, period),
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "HistoricalTradingReward not valid" }
            return null
        }

        private fun getEndedAt(startedAt: Instant, period: String): Instant {
            return when (period) {
                "DAILY" -> startedAt.plus(1.days)
                "WEEKLY" -> startedAt.plus(7.days)
                "MONTHLY" -> startedAt.nextMonth()
                else -> startedAt.plus(1.days)
            }
        }
    }
}
