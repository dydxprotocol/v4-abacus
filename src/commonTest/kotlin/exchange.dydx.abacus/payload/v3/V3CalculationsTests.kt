package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class V3CalculationsTests : V3BaseTests() {
    @Test
    fun testCalculations() {
        setup()

        print("--------First round----------\n")

        testCalculationsOnce()
    }

    fun testCalculationsOnce() {
        var time = ServerTime.now()
        testOneCalculation()
        time = perp.log("Candles All Markets", time)
    }

    fun testOneCalculation() {
//        val response = perp.loadCandlesFirst(mock)
//
//        if (doAsserts) {
//            verifyCandlesDataAfterFirstCall(size)
//            verifyCandlesStateAfterFirstCall(response.state, size)
//        }
    }
}
