package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import kotlin.test.Test
import kotlin.test.assertEquals

class V4CalculationTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/weC--\n")

        setup()
        testAccountsOnce()
        testNextFundingRate()
    }

    override fun loadMarkets(): StateResponse {
        return perp.socket(
            url = mock.socketUrl,
            jsonString = mock.marketsChannel.v4_subscribed_for_calculation,
            subaccountNumber = 0,
            height = null,
        )
    }

    override fun loadSubaccounts(): StateResponse {
        return perp.rest(
            AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            mock.accountsChannel.v4_accounts_received_for_calculation,
            0,
            null,
        )
    }

    fun testAccountsOnce() {
        perp.rest(
            url = AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            payload = mock.accountsChannel.v4_accounts_received_for_calculation,
            subaccountNumber = 0,
            height = null,
        )

        val account = perp.internalState.wallet.account

        val tradingRewards = account.tradingRewards
        assertEquals(2800.8, tradingRewards.total)
        val blockRewards = tradingRewards.blockRewards
        assertEquals("0.02", blockRewards[0].tradingReward)
        assertEquals("2422", blockRewards[0].createdAtHeight)
        assertEquals("0.01", blockRewards[1].tradingReward)
        assertEquals("2500", blockRewards[1].createdAtHeight)

        var subaccount = account.subaccounts[0]
        var calculated = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(100.0, calculated?.equity)
        assertEquals(50.0, calculated?.freeCollateral)
        assertEquals(-900.0, calculated?.quoteBalance)
        assertEquals(1000.0, calculated?.notionalTotal)
        assertEquals(1000.0, calculated?.valueTotal)
        assertEquals(50.0, calculated?.initialRiskTotal)
        assertEquals(10.0, calculated?.leverage)
        assertEquals(0.5, calculated?.marginUsage)
        assertEquals(2500.0, calculated?.buyingPower)

        var ethPosition = subaccount?.openPositions?.get("ETH-USD")
        var positionCalculated = ethPosition?.calculated?.get(CalculationPeriod.current)
        assertEquals(1000.0, positionCalculated?.notionalTotal)
        assertEquals(1000.0, positionCalculated?.valueTotal)
        assertEquals(0.05, positionCalculated?.adjustedImf)
        assertEquals(50.0, positionCalculated?.initialRiskTotal)
        assertEquals(10.0, positionCalculated?.leverage)
        assertEquals(1000.0, positionCalculated?.buyingPower)
        assertEquals(927.8350515463918, positionCalculated?.liquidationPrice)

        perp.socket(
            url = mock.socketUrl,
            jsonString = mock.accountsChannel.v4_subscribed_for_calculation,
            subaccountNumber = 0,
            height = null,
        )

        subaccount = perp.internalState.wallet.account.subaccounts[0]
        calculated = subaccount?.calculated?.get(CalculationPeriod.current)!!
        assertEquals(100.0, calculated.equity)
        assertEquals(50.0, calculated.freeCollateral)
        assertEquals(1100.0, calculated.quoteBalance)
        assertEquals(1000.0, calculated.notionalTotal)
        assertEquals(-1000.0, calculated.valueTotal)
        assertEquals(50.0, calculated.initialRiskTotal)
        assertEquals(10.0, calculated.leverage)
        assertEquals(0.5, calculated.marginUsage)
        assertEquals(2500.0, calculated.buyingPower)

        ethPosition = subaccount.openPositions?.get("ETH-USD")!!
        positionCalculated = ethPosition.calculated[CalculationPeriod.current]!!
        assertEquals(1000.0, positionCalculated.notionalTotal)
        assertEquals(-1000.0, positionCalculated.valueTotal)
        assertEquals(0.05, positionCalculated.adjustedImf)
        assertEquals(50.0, positionCalculated.initialRiskTotal)
        assertEquals(-10.0, positionCalculated.leverage)
        assertEquals(1000.0, positionCalculated.buyingPower)
        assertEquals(1067.9611650485438, positionCalculated.liquidationPrice)
    }

    private fun testNextFundingRate(): StateResponse {
        val response = perp.socket(
            url = mock.socketUrl,
            jsonString = mock.marketsChannel.v4_next_funding_rate_update,
            subaccountNumber = 0,
            height = null,
        )

        val markets = perp.internalState.marketsSummary.markets
        val btcMarket = markets["BTC-USD"]!!
        assertEquals(-0.0085756875, btcMarket.perpetualMarket?.perpetual?.nextFundingRate)
        val ethMarket = markets["ETH-USD"]!!
        assertEquals(-0.0084455625, ethMarket.perpetualMarket?.perpetual?.nextFundingRate)

        return response
    }
}
