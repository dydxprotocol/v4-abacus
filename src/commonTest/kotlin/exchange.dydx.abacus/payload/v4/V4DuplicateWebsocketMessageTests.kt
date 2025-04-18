package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.tests.extensions.loadv4TradesChanged
import exchange.dydx.abacus.tests.extensions.socket
import kotlin.test.Test
import kotlin.test.assertEquals

class V4DuplicateWebsocketMessageTests : V4BaseTests() {

    @Test
    fun testDuplicateFills() {
        setup()

        repeat(2) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.batchedSubaccountsChannel.channel_batch_data_order_filled_1,
                subaccountNumber = 0,
                height = null,
            )

            val account = perp.internalState.wallet.account
            assertEquals(2800.8, account.tradingRewards.total)

            val subaccount = account.subaccounts[0]
            val calculated = subaccount?.calculated?.get(CalculationPeriod.current)

            assertEquals(1599696.370275, calculated?.quoteBalance)
            val fills = subaccount?.fills

            assertEquals(2, fills?.size)
            assertEquals("a74830f8-d506-54b3-bf3b-1de791b8fe4e", fills?.get(0)?.id)
            assertEquals("0d473eec-93b0-5c49-94ca-b8017454d769", fills?.get(1)?.id)
        }
    }

    @Test
    fun testDuplicateTrades() {
        setup()

        repeat(2) {
            perp.loadv4TradesChanged(mock, testWsUrl)
            val market = perp.internalState.marketsSummary.markets.get("ETH-USD")
            assertEquals(1, market?.trades?.size)
            val firstItem = market?.trades?.get(0)
            assertEquals("8ee6d90d-272d-5edd-bf0f-2e4d6ae3d3b7", firstItem?.id)
            assertEquals("BUY", firstItem?.side?.rawValue)
            assertEquals(1.593707, firstItem?.size)
        }
    }
}
