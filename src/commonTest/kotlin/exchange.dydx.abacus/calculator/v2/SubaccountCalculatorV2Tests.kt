package exchange.dydx.abacus.calculator.v2

import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.state.internalstate.InternalAssetPositionState
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class SubaccountCalculatorV2Tests {
    private val calculator = SubaccountCalculatorV2(
        parser = Parser(),
    )

    @Test
    fun testCalculateQuoteBalance_nullAssetPositions() {
        val output = calculator.calculateQuoteBalance(
            assetPositions = null,
        )
        assertEquals(null, output)
    }

    @Test
    fun testCalculateQuoteBalance_long() {
        val output = calculator.calculateQuoteBalance(
            assetPositions = mapOf(
                "USDC" to InternalAssetPositionState(
                    symbol = "USDC",
                    size = 1.0,
                    side = PositionSide.LONG,
                ),
            ),
        )
        assertEquals(1.0, output)
    }

    @Test
    fun testCalculateQuoteBalance_short() {
        val output = calculator.calculateQuoteBalance(
            assetPositions = mapOf(
                "USDC" to InternalAssetPositionState(
                    symbol = "USDC",
                    size = 1.0,
                    side = PositionSide.SHORT,
                ),
            ),
        )
        assertEquals(-1.0, output)
    }
}
