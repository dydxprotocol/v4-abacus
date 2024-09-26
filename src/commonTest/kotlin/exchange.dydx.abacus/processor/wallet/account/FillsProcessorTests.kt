package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.tests.mock.processor.wallet.account.FillProcessorMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerFillResponseObject
import kotlin.test.Test
import kotlin.test.assertEquals

class FillsProcessorTests {
    private val fillProcessor = FillProcessorMock()
    private val fillsProcessor = FillsProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
        fillProcessor = fillProcessor,
    )

    @Test
    fun testProcess_emptyPayload() {
        val output = fillsProcessor.process(
            existing = null,
            payload = emptyList(),
            subaccountNumber = 0,
        )
        assertEquals(0, output.size)
    }

    @Test
    fun testProcess_nonEmptyPayload() {
        fillProcessor.processAction = { input, _ ->
            createSubaccountFill(input.id!!)
        }

        val output = fillsProcessor.process(
            existing = null,
            payload = listOf(
                IndexerFillResponseObject(
                    id = "1",
                ),
            ),
            subaccountNumber = 0,
        )
        assertEquals(1, output.size)
        assertEquals(output[0].id, "1")
    }

    @Test
    fun testProcess_withMerge() {
        fillProcessor.processAction = { input, _ ->
            createSubaccountFill(input.id!!)
        }

        val output = fillsProcessor.process(
            existing = listOf(
                createSubaccountFill("1"),
            ),
            payload = listOf(
                IndexerFillResponseObject(
                    id = "1",
                ),
                IndexerFillResponseObject(
                    id = "2",
                ),
            ),
            subaccountNumber = 0,
        )
        assertEquals(2, output.size)
        assertEquals(output[0].id, "1")
        assertEquals(output[1].id, "2")
    }

    private fun createSubaccountFill(id: String): SubaccountFill {
        return FillProcessorTests.fillMock.copy(id = id)
    }
}
