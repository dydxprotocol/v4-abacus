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
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalTradingRewardsState
import exchange.dydx.abacus.state.model.PerpTradingStateMachine
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

open class V4BaseTests(useParentSubaccount: Boolean = false) : BaseTests(127, useParentSubaccount) {
    internal val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")
    internal val testRestUrl =
        "https://indexer.v4staging.dydx.exchange"
    override fun createState(useParentSubaccount: Boolean, staticTyping: Boolean): PerpTradingStateMachine {
        return PerpTradingStateMachine(
            environment = mock.v4Environment,
            localizer = null,
            formatter = null,
            maxSubaccountNumber = 127,
            useParentSubaccount = useParentSubaccount,
            staticTyping = staticTyping,
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
        staticTyping: Boolean,
        obj: Account?,
        trace: String
    ) {
        super.verifyAccountState(data, state, staticTyping, obj, trace)
        if (data != null) {
            verifyTradingRewardsState(
                data = parser.asNativeMap(data["tradingRewards"]),
                obj = obj!!.tradingRewards,
                staticTyping = staticTyping,
                state = state?.tradingRewards,
                trace = "$trace.tradingRewards",
            )
            verifyLaunchIncentivePointsState(
                parser.asNativeMap(data["launchIncentivePoints"]),
                obj.launchIncentivePoints,
                "$trace.launchIncentivePoints",
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
        data: Map<String, Any>?,
        obj: TradingRewards?,
        staticTyping: Boolean,
        state: InternalTradingRewardsState?,
        trace: String,
    ) {
        if (staticTyping) {
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
                            data = null,
                            obj = obj.blockRewards?.get(i),
                            staticTyping = staticTyping,
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
                                data = null,
                                obj = rewardsListObj[i],
                                staticTyping = staticTyping,
                                state = rewardsListData[i],
                                trace = " $trace.historical.$period.$i",
                            )
                        }
                    }
                }
            }
        } else {
            if (data != null) {
                val totalData = parser.asDouble(data["total"])
                val blockRewardsData = parser.asList(data["blockRewards"])
                val historicalData = parser.asNativeMap(data["historical"])

                if (totalData != null) {
                    assertNotNull(obj?.total)
                    assertEquals(totalData, obj?.total, "$trace.total")
                }

                if (blockRewardsData != null) {
                    val blockRewards = obj?.blockRewards
                    assertNotNull(blockRewards)
                    assertEquals(
                        blockRewardsData.size,
                        blockRewards.size,
                        "$trace.blockRewards.size $doesntMatchText",
                    )
                    for (i in blockRewards.indices) {
                        verifyBlockRewardState(
                            data = parser.asNativeMap(blockRewardsData.get(i)),
                            obj = blockRewards[i],
                            staticTyping = staticTyping,
                            state = null,
                            trace = "$trace.blockRewards.$i",
                        )
                    }
                }
                if (historicalData != null) {
                    assertNotNull(obj?.filledHistory)
                    for ((period, rewardsData) in historicalData) {
                        val rewardsListObjOrig = obj?.filledHistory?.get(period)
                        val rewardsListDataOrig = parser.asList(rewardsData)

                        assertTrue {
                            (rewardsListObjOrig?.size ?: 0) >= (rewardsListDataOrig?.size ?: 0)
                        }

                        val rewardsListObj = rewardsListObjOrig?.filter {
                            it.amount != 0.0
                        }
                        val rewardsListData = rewardsListDataOrig?.filter {
                            parser.asDouble(parser.value(it, "amount")) != 0.0
                        }

                        assertNotNull(rewardsListObj)
                        assertEquals(
                            (rewardsListData?.size ?: 0).toDouble(),
                            rewardsListObj.size.toDouble(),
                            0.0,
                            "$trace.historical.$period.size $doesntMatchText",
                        )

                        for (i in rewardsListObj.indices) {
                            verifyHistoricalTradingRewardState(
                                data = parser.asNativeMap(rewardsListData?.get(i)),
                                obj = rewardsListObj[i],
                                staticTyping = staticTyping,
                                state = null,
                                trace = "$trace.historical.$period.$i",
                            )
                        }
                    }
                }
            } else {
                assertNull(obj)
            }
        }
    }

    private fun verifyBlockRewardState(
        data: Map<String, Any>?,
        obj: BlockReward?,
        staticTyping: Boolean,
        state: IndexerHistoricalBlockTradingReward?,
        trace: String,
    ) {
        if (staticTyping) {
            assertNotNull(state)
            if (obj != null) {
                assertEquals(state.tradingReward, obj.tradingReward.toString(), "$trace.amount")
                assertEquals(parser.asDatetime(state.createdAt)?.toEpochMilliseconds()?.toDouble(), obj.createdAtMilliseconds, "$trace.createdAt")
                assertEquals(state.createdAtHeight, obj.createdAtHeight.toString(), "$trace.createdAtHeight")
            }
        } else {
            if (data != null) {
                assertNotNull(obj)
                assertEquals(
                    parser.asDouble(data["tradingReward"]),
                    obj.tradingReward,
                    "$trace.tradingReward",
                )
                assertEquals(
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                    obj.createdAtMilliseconds,
                    "$trace.createdAt",
                )
                assertEquals(
                    parser.asInt(data["createdAtHeight"]),
                    obj.createdAtHeight,
                    "$trace.createdAtHeight",
                )
            } else {
                assertNull(obj)
            }
        }
    }

    private fun verifyHistoricalTradingRewardState(
        data: Map<String, Any>?,
        obj: HistoricalTradingReward?,
        staticTyping: Boolean,
        state: IndexerHistoricalTradingRewardAggregation?,
        trace: String,
    ) {
        if (staticTyping) {
            assertNotNull(state)
            if (obj != null) {
                assertEquals(state.tradingReward, obj.amount.toString(), "$trace.amount")
                assertEquals(parser.asDatetime(state.startedAt), obj.startedAt, "$trace.startedAt")
                assertEquals(parser.asDatetime(state.endedAt), obj.endedAt, "$trace.endedAt")
            }
        } else {
            if (data != null) {
                assertNotNull(obj)
                assertEquals(
                    parser.asDouble(data["amount"]),
                    obj.amount,
                    "$trace.amount",
                )
                assertEquals(
                    parser.asDatetime(data["startedAt"]),
                    obj.startedAt,
                    "$trace.startedAt",
                )
                assertNotNull(obj.endedAt)
                if (parser.asDatetime(data["endedAt"]) != null) {
                    assertEquals(
                        parser.asDatetime(data["endedAt"]),
                        obj.endedAt,
                        "$trace.endedAt",
                    )
                }
            } else {
                assertNull(obj)
            }
        }
    }
}
