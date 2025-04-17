package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_DURATION
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V4AccountOrdersSortingTest : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testAccountsOnce()
    }

    private fun testAccountsOnce() {
        var time = ServerTime.now()
        testSubaccountsReceived()
        time = perp.log("Accounts Received", time)

        testSubaccountSubscribed()
        time = perp.log("Accounts Subscribed", time)
    }

    private fun testSubaccountsReceived() {
        perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")

        val subaccounts = perp.internalState?.wallet?.account?.subaccounts
        val subaccount = subaccounts?.get(0)
        val calculated = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(108116.7318528828, calculated?.equity)
        assertEquals(106640.3767269893, calculated?.freeCollateral)
        assertEquals(99872.368956, calculated?.quoteBalance)
    }

    private fun testSubaccountSubscribed() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.accountsChannel.v4_subscribed_for_orders_sorting,
            subaccountNumber = 0,
            height = null,
        )

        val subaccount = perp.state?.subaccount(0)
        val orders = subaccount?.orders

        assertNotNull(orders)
        val sortedOrders = orders.sortedBy {
            it.createdAtHeight
                ?: (if (it.goodTilBlock != null) it.goodTilBlock!! - SHORT_TERM_ORDER_DURATION else 0)
        }.reversed()
        for (i in 0 until orders.size) {
            assertEquals(orders[i].id, sortedOrders[i].id)
        }
    }
}
