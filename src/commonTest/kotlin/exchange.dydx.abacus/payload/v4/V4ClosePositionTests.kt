package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.modal.ClosePositionInputField
import exchange.dydx.abacus.state.modal.closePosition
import exchange.dydx.abacus.state.modal.closePositionPayload
import exchange.dydx.abacus.state.modal.trade
import exchange.dydx.abacus.state.modal.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadOrderbook
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import kotlin.test.Test
import kotlin.test.assertEquals

open class V4ClosePositionTests : V4BaseTests() {
    @Test
    fun testLimit() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testOnce()

        reset()

        print("--------Second round----------\n")

        testOnce()
    }

    override fun setup() {
        super.setup()
        loadOrderbook()
    }

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.v4_subscribed_r1, 0, null)
        }, null)
    }

    override fun loadSubaccounts(): StateResponse {
        return test({
            perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")
        }, null)
    }

    internal fun loadOrderbook(): StateResponse {
        return test({
            perp.loadOrderbook(mock)
        }, null)
    }

    private fun testOnce() {
        testLimitTradeInputOnce()
    }

    private fun testLimitTradeInputOnce() {
        perp.tradeInMarket("BTC-USD", 0)
        perp.trade(null, null, 0)
        test({
            perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)
        }, null)


        test({
            perp.closePosition("0.5", ClosePositionInputField.percent, 0)
        }, null, {
            val placeOrder = perp.closePositionPayload(20)
            assertEquals(1, placeOrder?.clobPairId)
            assertEquals("SELL", placeOrder?.side)
            assertEquals(5.385788E9, placeOrder?.quantums)
            assertEquals(1.571584E9, placeOrder?.subticks)
            assertEquals(23, placeOrder?.goodUntilBlock)
            assertEquals(false, placeOrder?.reduceOnly)
            assertEquals("TIME_IN_FORCE_IOC", placeOrder?.timeInForce)
            assertEquals("SHORT_TERM", placeOrder?.orderFlags)
        })
    }
}
