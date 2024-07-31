package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.processor.base.ComparisonOrder
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.nextMonth
import exchange.dydx.abacus.utils.previousMonth
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.typedSafeSet
import kollections.JsExport
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIList
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days

@JsExport
@Serializable
data class TradingRewards(
    val total: Double?,
    val blockRewards: IList<BlockReward>?,
    val filledHistory: IMap<String, IList<HistoricalTradingReward>>?,
    val rawHistory: IMap<String, IList<HistoricalTradingReward>>?
) {
    companion object {
        internal fun create(
            existing: TradingRewards?,
            parser: ParserProtocol,
            data: Map<String, Any>?
        ): TradingRewards? {
            Logger.d { "creating TradingRewards\n" }
            data?.let {
                val total = parser.asDouble(data["total"])
                val filledHistory = total?.let {
                    createHistoricalTradingRewards(
                        it,
                        existing?.filledHistory,
                        parser.asMap(data["historical"]),
                        fillZeros = true,
                        parser,
                    )
                }
                val rawHistory = total?.let {
                    createHistoricalTradingRewards(it, existing?.rawHistory, parser.asMap(data["historical"]), fillZeros = false, parser)
                }
                val blockRewards = parser.asList(data["blockRewards"])?.map {
                    BlockReward.create(null, parser, parser.asMap(it))
                }?.filterNotNull()?.toIList()

                return if (existing?.total != total ||
                    existing?.blockRewards != blockRewards ||
                    existing?.filledHistory != filledHistory ||
                    existing?.rawHistory != rawHistory
                ) {
                    TradingRewards(
                        total,
                        blockRewards,
                        filledHistory,
                        rawHistory,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "TradingRewards not valid" }
            return null
        }

        private fun createHistoricalTradingRewards(
            total: Double,
            existing: IMap<String, IList<HistoricalTradingReward>>?,
            data: Map<String, Any>?,
            fillZeros: Boolean,
            parser: ParserProtocol,
        ): IMap<String, IList<HistoricalTradingReward>> {
            val objs = iMutableMapOf<String, IList<HistoricalTradingReward>>()
            val periods = setOf("WEEKLY", "DAILY", "MONTHLY")
            for (period in periods) {
                val periodObjs = existing?.get(period)
                val periodData = parser.asList(data?.get(period))
                val rewards =
                    createHistoricalTradingRewardsPerPeriod(periodObjs, periodData, parser, period, total, fillZeros)
                objs.typedSafeSet(period, rewards)
            }
            return objs
        }

        private fun createHistoricalTradingRewardsPerPeriod(
            objs: IList<HistoricalTradingReward>?,
            data: List<Any>?,
            parser: ParserProtocol,
            period: String,
            total: Double,
            fillZeros: Boolean
        ): IList<HistoricalTradingReward> {
            val result = iMutableListOf<HistoricalTradingReward>()
            if (data != null) {
                var objIndex = 0
                var dataIndex = 0
                var lastStart: Double? = null
                var cumulativeAmount: Double = total

                while (objIndex < (objs?.size ?: 0) && dataIndex < data.size) {
                    val obj = objs!![objIndex]
                    val item = parser.asMap(data[dataIndex])
                    val itemStart =
                        parser.asDatetime(item?.get("startedAt"))?.toEpochMilliseconds()?.toDouble()
                    if (item != null && itemStart != null) {
                        val objStart = obj.startedAtInMilliseconds
                        val comparison = ParsingHelper.compare(objStart, itemStart, true)
                        when {
                            (comparison == ComparisonOrder.ascending) -> {
                                // item is newer than obj
                                val modified = item.mutable()
                                modified.safeSet("cumulativeAmount", cumulativeAmount)

                                val synced =
                                    HistoricalTradingReward.create(null, parser, modified, period)
                                if (fillZeros) {
                                    addHistoricalTradingRewards(result, synced!!, period, lastStart)
                                }
                                result.add(synced!!)
                                dataIndex++
                                lastStart = synced.startedAtInMilliseconds
                                cumulativeAmount = cumulativeAmount - parser.asDouble(item["amount"])!!
                            }

                            (comparison == ComparisonOrder.descending) -> {
                                // item is older than obj
                                val modified = mapOf(
                                    "amount" to obj.amount,
                                    "cumulativeAmount" to cumulativeAmount,
                                    "startedAt" to obj.startedAt,
                                    "endedAt" to obj.endedAt,
                                )

                                val synced = HistoricalTradingReward.create(obj, parser, modified, period)
                                if (fillZeros) {
                                    addHistoricalTradingRewards(result, synced!!, period, lastStart)
                                }
                                result.add(synced!!)
                                objIndex++
                                lastStart = synced.startedAtInMilliseconds
                                cumulativeAmount = cumulativeAmount - obj.amount
                            }

                            else -> {
                                // same thing
                                val modified = item.mutable()
                                modified.safeSet("cumulativeAmount", cumulativeAmount)

                                val synced =
                                    HistoricalTradingReward.create(obj, parser, modified, period)

                                if (fillZeros) {
                                    addHistoricalTradingRewards(result, synced!!, period, lastStart)
                                }
                                result.add(synced!!)
                                objIndex++
                                dataIndex++
                                lastStart = synced.startedAtInMilliseconds
                                cumulativeAmount = cumulativeAmount - obj.amount
                            }
                        }
                    } else {
                        dataIndex++
                    }
                }
                if (objs != null) {
                    while (objIndex < objs.size) {
                        val obj = objs[objIndex]
                        val modified = mapOf(
                            "amount" to obj.amount,
                            "cumulativeAmount" to cumulativeAmount,
                            "startedAt" to obj.startedAt,
                            "endedAt" to obj.endedAt,
                        )

                        val synced = HistoricalTradingReward.create(obj, parser, modified, period)
                        if (fillZeros) {
                            addHistoricalTradingRewards(result, synced!!, period, lastStart)
                        }
                        result.add(synced!!)
                        objIndex++
                        lastStart = obj.startedAtInMilliseconds
                        cumulativeAmount = cumulativeAmount - obj.amount
                    }
                }
                while (dataIndex < data.size) {
                    val item = parser.asMap(data[dataIndex])
                    val itemStart =
                        parser.asDatetime(item?.get("startedAt"))?.toEpochMilliseconds()?.toDouble()

                    if (item != null && itemStart != null) {
                        val modified = item.mutable()
                        modified.safeSet("cumulativeAmount", cumulativeAmount)

                        val synced = HistoricalTradingReward.create(null, parser, modified, period)
                        if (fillZeros) {
                            addHistoricalTradingRewards(result, synced!!, period, lastStart)
                        }
                        result.add(synced!!)
                        dataIndex++
                        lastStart = synced.startedAtInMilliseconds
                        cumulativeAmount = cumulativeAmount - synced.amount
                    } else {
                        dataIndex++
                    }
                }
            } else {
                if (fillZeros) {
                    result.add(currentPeriodPlaceHolder(period, total))
                }
            }
            return result
        }

        private fun currentPeriodPlaceHolder(period: String, total: Double): HistoricalTradingReward {
            val now = Clock.System.now()
            val thisPeriod = when (period) {
                "DAILY" -> today(now)
                "WEEKLY" -> thisWeek(now)
                "MONTHLY" -> thisMonth(now)
                else -> today(now)
            }
            return HistoricalTradingReward(
                0.0,
                total,
                thisPeriod.start.toEpochMilliseconds().toDouble(),
                thisPeriod.end.toEpochMilliseconds().toDouble(),
            )
        }

        private fun previousPlaceHolder(
            period: String,
            lastStartTime: Instant,
            total: Double,
        ): HistoricalTradingReward {
            return when (period) {
                "DAILY" -> HistoricalTradingReward(
                    0.0,
                    total,
                    lastStartTime.minus(1.days).toEpochMilliseconds().toDouble(),
                    lastStartTime.toEpochMilliseconds().toDouble(),
                )

                "WEEKLY" -> HistoricalTradingReward(
                    0.0,
                    total,
                    lastStartTime.minus(7.days).toEpochMilliseconds().toDouble(),
                    lastStartTime.toEpochMilliseconds().toDouble(),
                )

                "MONTHLY" -> HistoricalTradingReward(
                    0.0,
                    total,
                    lastStartTime.previousMonth().toEpochMilliseconds().toDouble(),
                    lastStartTime.toEpochMilliseconds().toDouble(),
                )

                else -> HistoricalTradingReward(
                    0.0,
                    total,
                    lastStartTime.minus(1.days).toEpochMilliseconds().toDouble(),
                    lastStartTime.toEpochMilliseconds().toDouble(),
                )
            }
        }

        private fun addHistoricalTradingRewards(
            result: IMutableList<HistoricalTradingReward>,
            obj: HistoricalTradingReward,
            period: String,
            lastStart: Double?,
        ): Double {
            var lastStartTime: Instant = if (lastStart == null) {
                val thisPeriod = currentPeriodPlaceHolder(period, obj.cumulativeAmount)
                if (obj.startedAtInMilliseconds < thisPeriod.startedAtInMilliseconds) {
                    result.add(thisPeriod)
                }
                Instant.fromEpochMilliseconds(thisPeriod.startedAtInMilliseconds.toLong())
            } else {
                Instant.fromEpochMilliseconds(lastStart.toLong())
            }
            while (obj.startedAtInMilliseconds < lastStartTime.toEpochMilliseconds().toDouble()) {
                val previous = previousPlaceHolder(period, lastStartTime, obj.cumulativeAmount)
                if (obj.startedAtInMilliseconds < previous.startedAtInMilliseconds) {
                    result.add(previous)
                    lastStartTime =
                        Instant.fromEpochMilliseconds(previous.startedAtInMilliseconds.toLong())
                } else {
                    break
                }
            }
            return lastStartTime.toEpochMilliseconds().toDouble()
        }

        private fun today(now: Instant): DatePeriod {
            val zoned: LocalDateTime = now.toLocalDateTime(TimeZone.UTC)
            val utc = LocalDateTime(zoned.year, zoned.month, zoned.dayOfMonth, 0, 0, 0)
            val start = utc.toInstant(TimeZone.UTC)
            val end = start.plus(1.days)
            return DatePeriod(start, end)
        }

        private fun thisWeek(now: Instant): DatePeriod {
            val zoned: LocalDateTime = now.toLocalDateTime(TimeZone.UTC)
            val utc = LocalDateTime(zoned.year, zoned.month, zoned.dayOfMonth, 0, 0, 0)
            val today = utc.toInstant(TimeZone.UTC)
            val dayOfWeek = utc.dayOfWeek
            val start = when (dayOfWeek) {
                DayOfWeek.MONDAY -> today
                DayOfWeek.TUESDAY -> today.minus(1.days)
                DayOfWeek.WEDNESDAY -> today.minus(2.days)
                DayOfWeek.THURSDAY -> today.minus(3.days)
                DayOfWeek.FRIDAY -> today.minus(4.days)
                DayOfWeek.SATURDAY -> today.minus(5.days)
                DayOfWeek.SUNDAY -> today.minus(6.days)
                else -> {
                    Logger.d { "Invalid day of week" }
                    today
                }
            }
            val end = start.plus(7.days)
            return DatePeriod(start, end)
        }

        private fun thisMonth(now: Instant): DatePeriod {
            val zoned: LocalDateTime = now.toLocalDateTime(TimeZone.UTC)
            val utc = LocalDateTime(zoned.year, zoned.month, 1, 0, 0, 0)
            val start = utc.toInstant(TimeZone.UTC)
            val end = start.nextMonth()
            return DatePeriod(start, end)
        }
    }
}

@JsExport
@Serializable
data class DatePeriod(
    val start: Instant,
    val end: Instant,
)
