package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.output.EquityTier
import exchange.dydx.abacus.output.EquityTiers
import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainEquityTier
import indexer.models.chain.OnChainEquityTiers
import indexer.models.chain.OnChainEquityTiersResponse
import kollections.toIList
import kotlin.test.Test
import kotlin.test.assertEquals

class EquityTiersProcessorTests {
    companion object {
        val payloadMock = OnChainEquityTiersResponse(
            equityTierLimitConfig = OnChainEquityTiers(
                shortTermOrderEquityTiers = listOf(
                    OnChainEquityTier(
                        usdTncRequired = "0",
                        limit = 1.0,
                    ),
                    OnChainEquityTier(
                        usdTncRequired = "2000000",
                        limit = 2.0,
                    ),
                ),
                statefulOrderEquityTiers = listOf(
                    OnChainEquityTier(
                        usdTncRequired = "0",
                        limit = 1.0,
                    ),
                    OnChainEquityTier(
                        usdTncRequired = "3000000",
                        limit = 2.0,
                    ),
                ),
            ),
        )

        val shortTermEquityTiersMock = listOf(
            EquityTier(
                requiredTotalNetCollateralUSD = 0.0,
                nextLevelRequiredTotalNetCollateralUSD = 2.0,
                maxOrders = 1,
            ),
            EquityTier(
                requiredTotalNetCollateralUSD = 2.0,
                nextLevelRequiredTotalNetCollateralUSD = null,
                maxOrders = 2,
            ),
        )

        val statefulOrderEquityTiersMock = listOf(
            EquityTier(
                requiredTotalNetCollateralUSD = 0.0,
                nextLevelRequiredTotalNetCollateralUSD = 3.0,
                maxOrders = 1,
            ),
            EquityTier(
                requiredTotalNetCollateralUSD = 3.0,
                nextLevelRequiredTotalNetCollateralUSD = null,
                maxOrders = 2,
            ),
        )

        val equityTiersMock = EquityTiers(
            shortTermOrderEquityTiers = shortTermEquityTiersMock.toIList(),
            statefulOrderEquityTiers = statefulOrderEquityTiersMock.toIList(),
        )
    }

    private val processor = EquityTiersProcessor(parser = Parser())

    @Test
    fun testProcess() {
        val equityTiers = processor.process(payloadMock)
        assertEquals(equityTiers, equityTiersMock)
    }
}
