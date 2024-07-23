package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.tests.mock.processor.wallet.account.HistoricalPNLProcessorMock
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoricalPNLsProcessorTests {
    private val pnlProcessor = HistoricalPNLProcessorMock()
    private val processor = HistoricalPNLsProcessor(
        parser = Parser(),
        pnlProcessor = pnlProcessor,
    )

    @Test
    fun testProcess_emptyPayload() {
        val output = processor.process(
            existing = null,
            payload = emptyList(),
        )
        assertEquals(0, output?.size)
    }

    @Test
    fun testProcess_nonEmptyPayload() {
        pnlProcessor.processAction = { _, input ->
            HistoricalPNLProcessorTests.pnlMock
        }

        val output = processor.process(
            existing = null,
            payload = listOf(
                HistoricalPNLProcessorTests.payload.copy(
                    createdAt = "2021-02-01T00:00:00Z",
                ),
                HistoricalPNLProcessorTests.payload,
            ),
        )
        assertEquals(2, output?.size)
        assertEquals(output?.get(0)?.equity, 1.0)
    }
}
