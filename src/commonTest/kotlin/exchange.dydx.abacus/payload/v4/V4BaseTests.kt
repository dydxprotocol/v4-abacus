package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.LaunchIncentive
import exchange.dydx.abacus.output.LaunchIncentivePoint
import exchange.dydx.abacus.output.LaunchIncentivePoints
import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.output.LaunchIncentiveSeasons
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.account.Account
import exchange.dydx.abacus.output.account.BlockReward
import exchange.dydx.abacus.output.account.HistoricalTradingReward
import exchange.dydx.abacus.output.account.TradingRewards
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.InternalAccountState
import exchange.dydx.abacus.state.InternalTradingRewardsState
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.state.machine.PerpTradingStateMachine
import exchange.dydx.abacus.tests.extensions.loadMarkets
import exchange.dydx.abacus.tests.extensions.loadMarketsConfigurations
import exchange.dydx.abacus.tests.extensions.loadOrderbook
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import indexer.codegen.IndexerHistoricalBlockTradingReward
import indexer.codegen.IndexerHistoricalTradingRewardAggregation
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

open class V4BaseTests(
    useParentSubaccount: Boolean = false
) : BaseTests(127, useParentSubaccount) {
    internal val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")
    internal val testRestUrl =
        "https://indexer.v4staging.dydx.exchange"
    override fun createState(useParentSubaccount: Boolean): PerpTradingStateMachine {
        return PerpTradingStateMachine(
            environment = mock.v4Environment,
            localizer = null,
            formatter = null,
            maxSubaccountNumber = 127,
            useParentSubaccount = useParentSubaccount,
            trackingProtocol = trackingProtocol,
        )
    }

    internal open fun loadMarkets(): StateResponse {
        return test({
            perp.loadMarkets(mock)
        }, null)
    }

    internal open fun loadMarketsConfigurations(): StateResponse {
        return test({
            perp.loadMarketsConfigurations(mock, deploymentUri)
        }, null)
    }

    internal open fun loadSubaccounts(): StateResponse {
        return test({
            perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")
        }, null)
    }

    open fun loadOrderbook(): StateResponse {
        return test({
            perp.loadOrderbook(mock)
        }, null)
    }

    override fun setup() {
        loadMarketsConfigurations()
        loadMarkets()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadSubaccounts()
    }

    override fun verifyState(state: PerpetualState?) {
        super.verifyState(state)
        verifyLaunchIncentiveState(
            parser.asNativeMap(perp.launchIncentive),
            state?.launchIncentive,
            "account",
        )
    }

    private fun verifyLaunchIncentiveState(data: Map<String, Any>?, obj: LaunchIncentive?, trace: String) {
        if (data != null) {
            assertNotNull(obj)

            verifyLaunchIncentiveSeasonsState(
                parser.asNativeList(data["seasons"]),
                obj.seasons,
                "$trace.seasons",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyLaunchIncentiveSeasonsState(data: List<Any>?, obj: LaunchIncentiveSeasons?, trace: String) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.seasons.size, "$trace.size $doesntMatchText")

            for (i in data.indices) {
                verifyLaunchIncentiveSeasonState(
                    parser.asNativeMap(data[i]),
                    obj.seasons[i],
                    "$trace.$i",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyLaunchIncentiveSeasonState(data: Map<String, Any>?, obj: LaunchIncentiveSeason?, trace: String) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asString(data["label"]),
                obj.label,
                "$trace.label",
            )
            assertEquals(
                parser.asDouble(data["startTimestamp"])?.let { it * 1000.0 },
                obj.startTimeInMilliseconds,
                "$trace.startTimeInMilliseconds",
            )
        } else {
            assertNull(obj)
        }
    }

    override fun verifyAccountState(
        data: Map<String, Any>?,
        state: InternalAccountState?,
        obj: Account?,
        trace: String
    ) {
        super.verifyAccountState(data, state, obj, trace)
        if (state != null) {
            verifyTradingRewardsState(
                obj = obj!!.tradingRewards,
                state = state?.tradingRewards,
                trace = "$trace.tradingRewards",
            )
            verifyLaunchIncentivePointsState(
                data = parser.asNativeMap(data?.get("launchIncentivePoints")),
                obj = obj.launchIncentivePoints,
                trace = "$trace.launchIncentivePoints",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyLaunchIncentivePointsState(
        data: Map<String, Any>?,
        obj: LaunchIncentivePoints?,
        trace: String,
    ) {
        assertEquals(data?.keys, obj?.points?.keys, "$trace.keys")
        if (data != null) {
            assertNotNull(obj)
            for ((key, value) in data) {
                val pointData = parser.asNativeMap(value)
                val pointObj = obj.points[key]
                verifyLaunchIncentivePointState(
                    pointData,
                    pointObj,
                    "$trace.$key",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyLaunchIncentivePointState(
        data: Map<String, Any>?,
        obj: LaunchIncentivePoint?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["incentivePoints"]),
                obj.incentivePoints,
                "$trace.incentivePoints",
            )
            assertEquals(
                parser.asDouble(data["marketMakingIncentivePoints"]),
                obj.marketMakingIncentivePoints,
                "$trace.marketMakingIncentivePoints",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyTradingRewardsState(
        obj: TradingRewards?,
        state: InternalTradingRewardsState?,
        trace: String,
    ) {
        assertNotNull(state)

        if (obj != null) {
            if (state.total != null) {
                assertEquals(state.total, obj.total, "$trace.total")
            }

            if (state.blockRewards.isNotEmpty()) {
                assertEquals(
                    state.blockRewards.size,
                    obj.blockRewards?.size,
                    "$trace.blockRewards.size",
                )
                for (i in state.blockRewards.indices) {
                    verifyBlockRewardState(
                        obj = obj.blockRewards?.get(i),
                        state = state.blockRewards[i],
                        trace = "$trace.blockRewards.$i",
                    )
                }
            }

            if (state.historical.isNotEmpty()) {
                assertEquals(
                    state.historical.size,
                    obj.filledHistory?.size,
                    "$trace.historical.size",
                )

                for ((period, rewardsData) in state.historical) {
                    val rewardsListObjOrig = obj.filledHistory?.get(period.rawValue)
                    val rewardsListDataOrig = rewardsData

                    assertTrue {
                        (rewardsListObjOrig?.size ?: 0) >= rewardsListDataOrig.size
                    }

                    val rewardsListObj = rewardsListObjOrig?.filter {
                        it.amount != 0.0
                    }
                    val rewardsListData = rewardsListDataOrig.filter {
                        parser.asDouble(it.tradingReward) != 0.0
                    }

                    assertNotNull(rewardsListObj)
                    assertEquals(
                        rewardsListData.size.toDouble(),
                        rewardsListObj.size.toDouble(),
                        0.0,
                        "$trace.historical.$period.size $doesntMatchText",
                    )

                    for (i in rewardsListObj.indices) {
                        verifyHistoricalTradingRewardState(
                            obj = rewardsListObj[i],
                            state = rewardsListData[i],
                            trace = " $trace.historical.$period.$i",
                        )
                    }
                }
            }
        }
    }

    private fun verifyBlockRewardState(
        obj: BlockReward?,
        state: IndexerHistoricalBlockTradingReward?,
        trace: String,
    ) {
        assertNotNull(state)
        if (obj != null) {
            assertEquals(state.tradingReward, obj.tradingReward.toString(), "$trace.amount")
            assertEquals(parser.asDatetime(state.createdAt)?.toEpochMilliseconds()?.toDouble(), obj.createdAtMilliseconds, "$trace.createdAt")
            assertEquals(state.createdAtHeight, obj.createdAtHeight.toString(), "$trace.createdAtHeight")
        }
    }

    private fun verifyHistoricalTradingRewardState(
        obj: HistoricalTradingReward?,
        state: IndexerHistoricalTradingRewardAggregation?,
        trace: String,
    ) {
        assertNotNull(state)
        if (obj != null) {
            assertEquals(state.tradingReward, obj.amount.toString(), "$trace.amount")
            assertEquals(parser.asDatetime(state.startedAt), obj.startedAt, "$trace.startedAt")
            assertEquals(parser.asDatetime(state.endedAt), obj.endedAt, "$trace.endedAt")
        }
    }
}
