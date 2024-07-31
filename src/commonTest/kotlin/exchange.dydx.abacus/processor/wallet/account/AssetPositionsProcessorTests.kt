package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.tests.mock.processor.wallet.account.AssetPositionProcessorMock
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class AssetPositionsProcessorTests {
    private val itemProcessor = AssetPositionProcessorMock()
    private val processor = AssetPositionsProcessor(
        parser = Parser(),
        itemProcessor = itemProcessor,
    )

    @Test
    fun testProcess_emptyPayload() {
        val output = processor.process(
            payload = emptyMap(),
        )
        assertEquals(0, output?.size)
    }

    @Test
    fun testProcess_nonEmptyPayload() {
        itemProcessor.processAction = { input ->
            AssetPositionProcessTests.assetPositionMock
        }

        val output = processor.process(
            payload = mapOf(
                "WETH" to AssetPositionProcessTests.payloadMock,
            ),
        )
        assertEquals(1, output?.size)
        assertEquals(output?.get("WETH")?.symbol, "WETH")
    }

    @Test
    fun testProcessChanges() {
        itemProcessor.processAction = { input ->
            AssetPositionProcessTests.assetPositionMock
        }

        val output = processor.processChanges(
            existing = null,
            payload = listOf(AssetPositionProcessTests.payloadMock),
        )
        assertEquals(1, output?.size)
        assertEquals(output?.get("WETH")?.symbol, "WETH")
    }
}
