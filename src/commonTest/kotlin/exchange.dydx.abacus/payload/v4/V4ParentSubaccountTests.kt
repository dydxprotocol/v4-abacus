package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import indexer.codegen.IndexerPerpetualPositionStatus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class V4ParentSubaccountTests : V4BaseTests(true) {
    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    @BeforeTest
    private fun prepareTest() {
        reset()
        loadMarketsConfigurations()
        loadMarkets()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadSubaccountsWithRealData()
    }

    private fun loadSubaccountsWithRealData(): StateResponse {
        return test({
            perp.rest(
                AbUrl.fromString("$testRestUrl/v4/addresses/dydxaddress"),
                mock.parentSubaccountsChannel.rest_response,
                0,
                null,
            )
        }, null)
    }

    @Test
    fun testParentSubaccountSubscribed() {
        reset()

        perp.socket(testWsUrl, mock.parentSubaccountsChannel.real_subscribed, 0, null)

        val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(2001.402434402, calculated?.equity)
        assertEquals(1711.959192, calculated?.freeCollateral)
        assertEquals(1711.959192, calculated?.quoteBalance)

        val position = subaccount.openPositions?.get("LDO-USD")!!
        assertEquals(IndexerPerpetualPositionStatus.OPEN, position.status)
        assertEquals(11.0, position.size)
        assertEquals("LDO-USD", position.market)
        assertEquals(128, position.childSubaccountNumber)

        val positionCalculated = position.calculated[CalculationPeriod.current]!!
        assertEquals(21.552185402, positionCalculated.valueTotal)
        assertEquals(21.552185402, positionCalculated.notionalTotal)
        assertEquals(0.07446083461180532, positionCalculated.leverage)
        assertEquals(1425.664026608, positionCalculated.buyingPower)
        assertEquals(285.689378002, positionCalculated.marginValue)
    }

    @Test
    fun testParentSubaccountChannelData() {
        testParentSubaccountSubscribed()

        perp.socket(
            url = testWsUrl,
            jsonString = mock.parentSubaccountsChannel.real_channel_batch_data,
            subaccountNumber = 0,
            height = null,
        )

        val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(2113.158171894, calculated?.equity)
        assertEquals(1711.959192, calculated?.freeCollateral)
        assertEquals(1711.959192, calculated?.quoteBalance)

        val position = subaccount.openPositions?.get("LDO-USD")!!
        assertEquals(IndexerPerpetualPositionStatus.OPEN, position.status)
        assertEquals(17.0, position.size)
        assertEquals("LDO-USD", position.market)
        assertEquals(128, position.childSubaccountNumber)

        val positionCalculated = position.calculated[CalculationPeriod.current]!!
        assertEquals(33.307922894, positionCalculated.valueTotal)
        assertEquals(33.307922894, positionCalculated.notionalTotal)
        assertEquals(0.0830209560921621, positionCalculated.leverage)
        assertEquals(1972.686976576, positionCalculated.buyingPower)
    }

    @Test
    fun testParentSubaccountSubscribedWithPendingPositions() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.parentSubaccountsChannel.real_subscribed_with_pending,
            subaccountNumber = 0,
            height = null,
        )

        val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(2021.402434402, calculated?.equity)
        assertEquals(1711.959192, calculated?.freeCollateral)
        assertEquals(1711.959192, calculated?.quoteBalance)

        val pendingPosition = subaccount.pendingPositions?.get(0)!!
        assertEquals("ARB", pendingPosition.assetId)
        assertEquals("d1deed71-d743-5528-aff2-cf3daf8b6413", pendingPosition.firstOrderId)
        val positionCalculated = pendingPosition.calculated[CalculationPeriod.current]!!
        assertEquals(20.0, positionCalculated.quoteBalance)
        assertEquals(20.0, positionCalculated.freeCollateral)
        assertEquals(20.0, positionCalculated.equity)
    }

    @Test
    fun testParentSubaccountSubscribedWithMultiplePendingPositions() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.parentSubaccountsChannel.real_subscribe_with_multiple_pending,
            subaccountNumber = 0,
            height = null,
        )

        val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(1108.672653491, calculated?.equity)
        assertEquals(1009.1322948555, calculated?.freeCollateral)
        assertEquals(1004.771214, calculated?.quoteBalance)

        val pendingPosition = subaccount.pendingPositions?.get(0)!!
        assertEquals("ETH", pendingPosition.assetId)
        assertEquals("5f7ad499-1d48-5ab1-acfd-d4664e07e7e3", pendingPosition.firstOrderId)
        val positionCalculated = pendingPosition.calculated[CalculationPeriod.current]!!
        assertEquals(20.0, positionCalculated.quoteBalance)
        assertEquals(20.0, positionCalculated.freeCollateral)
        assertEquals(20.0, positionCalculated.equity)

        val pendingPosition1 = subaccount.pendingPositions?.get(1)!!
        assertEquals("XLM", pendingPosition1.assetId)
        assertEquals("89d1fe83-5b0d-5c3e-aaf5-42d1b2537837", pendingPosition1.firstOrderId)
        val positionCalculated1 = pendingPosition1.calculated[CalculationPeriod.current]!!
        assertEquals(60.0, positionCalculated1.quoteBalance)
        assertEquals(60.0, positionCalculated1.freeCollateral)
        assertEquals(60.0, positionCalculated1.equity)
    }

    @Test
    fun testParentSubaccountSubscribedWithUnpopulatedChild() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.parentSubaccountsChannel.real_subscribed_with_unpopulated_child,
            subaccountNumber = 0,
            height = null,
        )

        val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(1979.850249, calculated?.equity)
        assertEquals(1711.959192, calculated?.freeCollateral)
        assertEquals(1711.959192, calculated?.quoteBalance)

        val pendingPosition = subaccount.pendingPositions?.get(0)!!
        assertEquals("ARB", pendingPosition.assetId)
        assertEquals("d1deed71-d743-5528-aff2-cf3daf8b6413", pendingPosition.firstOrderId)
        val positionCalculated = pendingPosition.calculated[CalculationPeriod.current]!!
        assertEquals(267.891057, positionCalculated.quoteBalance)
        assertEquals(267.891057, positionCalculated.freeCollateral)
        assertEquals(267.891057, positionCalculated.equity)
    }
}
