package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.Account
import exchange.dydx.abacus.output.BlockReward
import exchange.dydx.abacus.output.HistoricalTradingReward
import exchange.dydx.abacus.output.TradingRewards
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.PerpTradingStateMachine
import exchange.dydx.abacus.tests.extensions.loadMarkets
import exchange.dydx.abacus.tests.extensions.loadMarketsConfigurations
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


open class V4BaseTests : BaseTests(127) {
    internal val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")
    internal val testRestUrl =
        "https://indexer.v4staging.dydx.exchange"
    override fun createState(): PerpTradingStateMachine {
        return PerpTradingStateMachine(mock.v4Environment, null, null, 127)
    }

    internal open fun loadMarkets(): StateResponse {
        return test({
            perp.loadMarkets(mock)
        }, null)
    }

    internal fun loadMarketsConfigurations(): StateResponse {
        return test({
            perp.loadMarketsConfigurations(mock, deploymentUri)
        }, null)
    }

    internal open fun loadSubaccounts(): StateResponse {
        return test({
            perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")
        }, null)
    }

    override fun setup() {
        loadMarketsConfigurations()
        loadMarkets()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadSubaccounts()
    }

    override fun verifyAccountState(data: Map<String, Any>?, obj: Account?, trace: String) {
        super.verifyAccountState(data, obj, trace)
        if (data != null) {
            verifyTradingRewardsState(
                parser.asNativeMap(data["tradingRewards"]),
                obj!!.tradingRewards,
                "$trace.tradingRewards"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyTradingRewardsState(
        data: Map<String, Any>?,
        obj: TradingRewards?,
        trace: String,
    ) {
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
                    "$trace.blockRewards.size $doesntMatchText"
                )
                for (i in blockRewards.indices) {
                    verifyBlockRewardState(
                        parser.asNativeMap(blockRewardsData?.get(i)),
                        blockRewards[i],
                        "$trace.blockRewards.$i"
                    )
                }
            }
            if (historicalData != null) {
                assertNotNull(obj?.historical)
                for ((period, rewardsData) in historicalData) {
                    val rewardsListObjOrig = obj?.historical?.get(period)
                    val rewardsListDataOrig = parser.asList(rewardsData)

                    assert(rewardsListObjOrig?.size ?: 0 >= rewardsListDataOrig?.size ?: 0)

                    val rewardsListObj = rewardsListObjOrig?.filter {
                        it.amount != 0.0
                    }
                    val rewardsListData = rewardsListDataOrig?.filter {
                        parser.asDouble(parser.value(it, "amount")) != 0.0
                    }

                    assertNotNull(rewardsListObj)
                    assertEquals(
                        (rewardsListData?.size ?: 0).toDouble(),
                        (rewardsListObj?.size ?: 0).toDouble(),
                        0.0,
                        "$trace.historical.$period.size $doesntMatchText"
                    )

                    for (i in rewardsListObj.indices) {
                        verifyHistoricalTradingRewardState(
                            parser.asNativeMap(rewardsListData?.get(i)),
                            rewardsListObj[i],
                            "$trace.historical.$period.$i"
                        )
                    }

                }
            }
        } else {
            assertNull(obj)
        }
    }


    private fun verifyBlockRewardState(
        data: Map<String, Any>?,
        obj: BlockReward?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["tradingReward"]),
                obj.tradingReward,
                "$trace.tradingReward"
            )
            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt"
            )
            assertEquals(
                parser.asInt(data["createdAtHeight"]),
                obj.createdAtHeight,
                "$trace.createdAtHeight"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyHistoricalTradingRewardState(
        data: Map<String, Any>?,
        obj: HistoricalTradingReward?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["amount"]),
                obj.amount,
                "$trace.amount"
            )
            assertEquals(
                parser.asDatetime(data["startedAt"]),
                obj.startedAt,
                "$trace.startedAt"
            )
            assertNotNull(obj.endedAt)
            if (parser.asDatetime(data["endedAt"]) != null) {
                assertEquals(
                    parser.asDatetime(data["endedAt"]),
                    obj.endedAt,
                    "$trace.endedAt"
                )
            }

        } else {
            assertNull(obj)
        }
    }

}