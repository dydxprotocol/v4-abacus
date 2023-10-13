package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

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