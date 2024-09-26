package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.tests.mock.processor.wallet.account.TransferProcessorMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerTransferResponseObject
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class TransfersProcessorTests {
    private val transferProcessor = TransferProcessorMock()
    private val transfersProcessor = TransfersProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
        itemProcessor = transferProcessor,
    )

    @Test
    fun testProcess_emptyPayload() {
        val output = transfersProcessor.process(
            existing = null,
            payload = emptyList(),
        )
        assertEquals(0, output.size)
    }

    @Test
    fun testProcess_nonEmptyPayload() {
        transferProcessor.processAction = { input ->
            TransferProcessorTests.transferMock
        }

        val output = transfersProcessor.process(
            existing = null,
            payload = listOf(
                IndexerTransferResponseObject(
                    id = "1",
                ),
            ),
        )
        assertEquals(1, output.size)
        assertEquals(output[0].id, TransferProcessorTests.transferMock.id)
    }

    @Test
    fun testProcess_nonEmptyMerged() {
        transferProcessor.processAction = { input ->
            TransferProcessorTests.transferMock
        }

        val output = transfersProcessor.process(
            existing = listOf(
                TransferProcessorTests.transferMock.copy(
                    id = "2",
                    updatedAtMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble(),
                ),
            ),
            payload = listOf(
                IndexerTransferResponseObject(
                    id = "1",
                ),
            ),
        )
        assertEquals(2, output.size)
        assertEquals(output[0].id, "2")
    }
}
