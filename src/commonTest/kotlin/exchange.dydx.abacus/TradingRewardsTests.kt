import exchange.dydx.abacus.output.BlockReward
import exchange.dydx.abacus.output.HistoricalTradingReward
import exchange.dydx.abacus.output.TradingRewards
import exchange.dydx.abacus.utils.Parser
import kollections.iListOf
import kollections.toIMap
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class TradingRewardsTests {
    val parser = Parser()
    val now = Clock.System.now()
    val zoned: LocalDateTime = now.toLocalDateTime(TimeZone.UTC)
    val utc = LocalDateTime(zoned.year, zoned.month, zoned.dayOfMonth, 0, 0, 0)
    val today = utc.toInstant(TimeZone.UTC)
    val tomorrow = today.plus(1.days)
    val yesterday = today.minus(1.days)
    val dayBeforeYesterday = today.minus(2.days)

    @Test
    fun testHistoricalDailyTradingRewardsWithoutExisting() {
        val tradingRewards = TradingRewards.create(
            null,
            parser,
            mapOf(
                "total" to 200.0,
                "historical" to mapOf(
                    "DAILY" to iListOf(
                        mapOf(
                            "amount" to 3.0,
                            "startedAt" to today,
                        ),
                        mapOf(
                            "amount" to 2.0,
                            "startedAt" to yesterday,
                            "endedAt" to today,
                        ),
                        mapOf(
                            "amount" to 1.0,
                            "startedAt" to dayBeforeYesterday,
                            "endedAt" to yesterday,
                        ),
                    ),
                ),
                "blockRewards" to iListOf(
                    BlockReward(1.0, yesterday.toEpochMilliseconds().toDouble(), 1),
                ),
            ).toIMap(),
        )

        assertEquals(200.0, tradingRewards?.total)

        // DAILY
        // day before yesterday -> yesterday, yesterday -> today, today -> tomorrow
        assertEquals(3, tradingRewards?.historical?.get("DAILY")?.size)
        // Ordered newest -> oldest
        assertEquals(
            iListOf(
                HistoricalTradingReward(
                    3.0,
                    200.0,
                    today.toEpochMilliseconds().toDouble(),
                    tomorrow.toEpochMilliseconds().toDouble(),
                ),
                HistoricalTradingReward(
                    2.0,
                    197.0,
                    yesterday.toEpochMilliseconds().toDouble(),
                    today.toEpochMilliseconds().toDouble(),
                ),
                HistoricalTradingReward(
                    1.0,
                    195.0,
                    dayBeforeYesterday.toEpochMilliseconds().toDouble(),
                    yesterday.toEpochMilliseconds().toDouble(),
                ),
            ),
            tradingRewards?.historical?.get("DAILY"),
        )
    }

    @Test
    fun testHistoricalDailyTradingRewardsWithExisting() {
        val total = 200.0
        val tradingRewards = TradingRewards.create(
            TradingRewards(
                total,
                iListOf(
                    BlockReward(1.0, yesterday.toEpochMilliseconds().toDouble(), 1),
                ),
                mapOf(
                    "DAILY" to iListOf(
                        HistoricalTradingReward(
                            2.0,
                            total,
                            yesterday.toEpochMilliseconds().toDouble(),
                            today.toEpochMilliseconds().toDouble(),
                        ),
                    ),
                ).toIMap(),
            ),
            parser,
            mapOf(
                "total" to total,
                "historical" to mapOf(
                    "DAILY" to iListOf(
                        mapOf(
                            "amount" to 3.0,
                            "startedAt" to today,
                            "endedAt" to tomorrow,
                        ),
                        mapOf(
                            "amount" to 1.0,
                            "startedAt" to dayBeforeYesterday,
                            "endedAt" to yesterday,
                        ),
                    ),
                ),
                "blockRewards" to iListOf(
                    BlockReward(1.0, yesterday.toEpochMilliseconds().toDouble(), 1),
                ),
            ).toIMap(),
        )

        assertEquals(total, tradingRewards?.total)

        // DAILY
        // day before yesterday -> yesterday, yesterday -> today, today -> tomorrow
        assertEquals(3, tradingRewards?.historical?.get("DAILY")?.size)
        // Ordered newest -> oldest
        assertEquals(
            iListOf(
                HistoricalTradingReward(
                    3.0,
                    total,
                    today.toEpochMilliseconds().toDouble(),
                    tomorrow.toEpochMilliseconds().toDouble(),
                ),
                HistoricalTradingReward(
                    2.0,
                    197.0,
                    yesterday.toEpochMilliseconds().toDouble(),
                    today.toEpochMilliseconds().toDouble(),
                ),
                HistoricalTradingReward(
                    1.0,
                    195.0,
                    dayBeforeYesterday.toEpochMilliseconds().toDouble(),
                    yesterday.toEpochMilliseconds().toDouble(),
                ),
            ),
            tradingRewards?.historical?.get("DAILY"),
        )
    }
}
