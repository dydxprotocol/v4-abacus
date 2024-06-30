package exchange.dydx.abacus.calculator

import kotlin.test.Test
import kotlin.test.assertEquals

class MarginModeCalculatorTest {

    @Test fun testExistingIsolatedPosition() {
        val markets = mutableMapOf<String, Any>()
        val account = mutableMapOf<String, Any>(
            "groupedSubaccounts" to mutableMapOf<String, Any>(
                "0" to mutableMapOf<String, Any>(
                    "openPositions" to mutableMapOf<String, Any>(
                        "ETH-USD" to mutableMapOf<String, Any>(
                            "size" to mutableMapOf<String, Any>(
                                "current" to "10.0",
                            ),
                            "equity" to mutableMapOf<String, Any>(
                                "current" to "36000.0",
                            ),
                            "leverage" to mutableMapOf<String, Any>(
                                "current" to "15.0",
                            ),
                        ),
                    ),
                ),
            ),
        )
        val tradeInput = mutableMapOf<String, Any>(
            "marketId" to "ETH-USD",
        )

        val modified = MarginModeCalculator.updateTradeInputMarginMode(
            markets,
            account,
            tradeInput,
            0,
        )

        requireNotNull(modified)

        assertEquals("ISOLATED", modified["marginMode"])
        assertEquals(15.0, modified["targetLeverage"])
    }
}
