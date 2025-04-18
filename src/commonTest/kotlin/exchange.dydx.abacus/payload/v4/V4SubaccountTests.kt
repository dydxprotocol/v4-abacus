package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.ServerTime
import indexer.codegen.IndexerPerpetualPositionStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class V4SubaccountTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testAccountsOnce()
    }

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(mock.socketUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    private fun testAccountsOnce() {
        var time = ServerTime.now()
        testSubaccountsReceived()
        testSubaccountSubscribed()
        // This is a complete test with real payload.
        testSubaccountChanged1()
        testSubaccountChangedWithFills1()
    }

    private fun testSubaccountsReceived() {
        perp.rest(
            url = AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            payload = mock.batchedSubaccountsChannel.rest_response,
            subaccountNumber = 0,
            height = null,
        )

        val account = perp.internalState.wallet.account
        assertEquals(account.tradingRewards.total, 36059.40741180069)
        val subaccount = account.subaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(calculated?.equity, 623694.7306634021)
        assertEquals(calculated?.freeCollateral, 485562.68948613136)
        assertEquals(calculated?.quoteBalance, 1625586.093553)
    }

    private fun testSubaccountSubscribed() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.batchedSubaccountsChannel.subscribed,
            subaccountNumber = 0,
            height = null,
        )

        val account = perp.internalState.wallet.account
        assertEquals(account.tradingRewards.total, 36059.40741180069)
        val subaccount = account.subaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(calculated?.equity, 623694.7306634021)
        assertEquals(calculated?.freeCollateral, 485562.68948613136)
        assertEquals(calculated?.quoteBalance, 1625586.093553)
    }

    private fun testSubaccountChanged1() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.batchedSubaccountsChannel.channel_batch_data_1,
            subaccountNumber = 0,
            height = null,
        )

        val account = perp.internalState.wallet.account
        assertEquals(account.tradingRewards.total, 36059.40741180069)
        val subaccount = account.subaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(calculated?.equity, 623694.7306634021)
        assertEquals(calculated?.freeCollateral, 485562.68948613136)
        assertEquals(calculated?.quoteBalance, 1625586.093553)
    }

    private fun testSubaccountChangedWithFills1() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.batchedSubaccountsChannel.channel_batch_data_order_filled_1,
            subaccountNumber = 0,
            height = null,
        )

        val account = perp.internalState.wallet.account
        assertEquals(account.tradingRewards.total, 36059.40741180069)
        val subaccount = account.subaccounts[0]!!
        val calculated = subaccount.calculated[CalculationPeriod.current]
        assertEquals(calculated?.quoteBalance, 1599696.370275)
        val openPositions = subaccount.openPositions
        assertEquals(openPositions?.size, 42)
        val aptPosition = openPositions?.get("APT-USD")
        assertEquals(aptPosition?.status, IndexerPerpetualPositionStatus.OPEN)
        assertEquals(aptPosition?.calculated?.get(CalculationPeriod.current)?.size, -2776.0)
        assertEquals(aptPosition?.entryPrice, 9.129870549819497)
        assertEquals(aptPosition?.exitPrice, 9.132496184181717)
        assertEquals(aptPosition?.netFunding, 4.708773)
        assertEquals(aptPosition?.maxSize, -42.0)

        val fills = subaccount.fills
        assertEquals(fills?.size, 2)
    }
}
