package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.manager.NotificationsProvider
import exchange.dydx.abacus.state.modal.onChainAccountBalances
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountSubscribed
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountWithOrdersAndFillsChanged
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V4WalletTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testWalletOnce()
    }

    private fun testWalletOnce() {
        var time = ServerTime.now()
        testSetWallet()
        testResetWallet()
    }

    private fun testSetWallet() {
        test(
            {
                perp.resetWallet("1234")
            },
            """
            {
                "wallet": {
                    "walletAddress": "1234"
                }
            }
        """.trimIndent()
        )
    }


    private fun testResetWallet() {
        test(
            {
                perp.resetWallet(null)
            },
            """
                {
                    "wallet": null
                }
            """.trimIndent()
        )
    }
}