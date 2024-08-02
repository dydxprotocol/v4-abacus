package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.FeeTierResources
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainFeeTier
import indexer.models.chain.OnChainFeeTierParams
import indexer.models.chain.OnChainFeeTiersResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class FeeTiersProcessorTests {
    companion object {
        val payloadMock = OnChainFeeTiersResponse(
            params = OnChainFeeTierParams(
                tiers = listOf(
                    OnChainFeeTier(
                        name = "tier1",
                        absoluteVolumeRequirement = "1000000",
                        totalVolumeShareRequirementPpm = 1000000.0,
                        makerVolumeShareRequirementPpm = 2000000.0,
                        makerFeePpm = 3000000.0,
                        takerFeePpm = 4000000.0,
                    ),
                    OnChainFeeTier(
                        name = "tier2",
                        absoluteVolumeRequirement = "2000000",
                        totalVolumeShareRequirementPpm = 1000000.0,
                        makerVolumeShareRequirementPpm = 2000000.0,
                        makerFeePpm = 3000000.0,
                        takerFeePpm = 4000000.0,
                    ),
                ),
            ),
        )

        val feeTiersMock = listOf(
            FeeTier(
                id = "tier1",
                tier = "tier1",
                symbol = "≥",
                volume = 1,
                totalShare = 1.0,
                makerShare = 2.0,
                maker = 3.0,
                taker = 4.0,
                resources = FeeTierResources(
                    stringKey = "FEE_TIER.tier1",
                    string = "FEE_TIER.tier1",
                ),
            ),
            FeeTier(
                id = "tier2",
                tier = "tier2",
                symbol = "≥",
                volume = 2,
                totalShare = 1.0,
                makerShare = 2.0,
                maker = 3.0,
                taker = 4.0,
                resources = FeeTierResources(
                    stringKey = "FEE_TIER.tier2",
                    string = "FEE_TIER.tier2",
                ),
            ),
        )
    }

    private val processor = FeeTiersProcessor(parser = Parser(), localizer = LocalizerProtocolMock())

    @Test
    fun testProcess() {
        val feeTiers = processor.process(payloadMock.params?.tiers)
        assertEquals(feeTiersMock, feeTiers)
    }
}
