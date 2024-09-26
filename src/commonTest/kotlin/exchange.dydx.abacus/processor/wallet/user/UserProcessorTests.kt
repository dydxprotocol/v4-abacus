package exchange.dydx.abacus.processor.wallet.user

import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainUserFeeTier
import indexer.models.chain.OnChainUserStatsResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class UserProcessorTests {
    companion object {
        val userFeeTierPayloadMock = OnChainUserFeeTier(
            name = "feeTierId",
            makerFeePpm = 100.0,
            takerFeePpm = 200.0,
        )

        val userStatsPayloadMock = OnChainUserStatsResponse(
            makerNotional = "100.0",
            takerNotional = "200.0",
        )
    }

    private val processor = UserProcessor(
        parser = Parser(),
    )

    @Test
    fun testProcessOnChainUserFeeTier() {
        val state = processor.processOnChainUserFeeTier(
            existing = null,
            payload = userFeeTierPayloadMock,
        )

        assertEquals("feeTierId", state.feeTierId)
        assertEquals(0.0001, state.makerFeeRate)
        assertEquals(0.0002, state.takerFeeRate)
    }

    @Test
    fun testProcessOnChainUserStats() {
        val state = processor.processOnChainUserStats(
            existing = null,
            payload = userStatsPayloadMock,
        )

        assertEquals(0.0001, state.makerVolume30D)
        assertEquals(0.0002, state.takerVolume30D)
    }
}
