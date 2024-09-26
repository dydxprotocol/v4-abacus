package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.tests.mock.processor.wallet.account.PerpetualPositionProcessorMock
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertTrue

class PerpetualPositionsProcessorTests {
    private val itemProcessor = PerpetualPositionProcessorMock()
    private val processor = PerpetualPositionsProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
        itemProcessor = itemProcessor,
    )

    @Test
    fun testProcess_emptyPayload() {
        val output = processor.process(
            payload = emptyMap(),
        )
        assertTrue { output.isNullOrEmpty() }
    }

    @Test
    fun testProcess_nonEmptyPayload() {
        itemProcessor.processAction = { _, _ ->
            PerpetualPositionProcessorTests.positionMock
        }

        val output = processor.process(
            payload = mapOf(
                "ETH-USD" to PerpetualPositionProcessorTests.payloadMock,
            ),
        )
        assertTrue { output?.size == 1 }
        assertTrue { output?.get("ETH-USD") == PerpetualPositionProcessorTests.positionMock }
    }

    @Test
    fun testProcessChanges_emptyPayload() {
        val output = processor.processChanges(
            existing = null,
            payload = emptyList(),
        )
        assertTrue { output.isNullOrEmpty() }
    }

    @Test
    fun testProcessChanges_nonEmptyPayload() {
        itemProcessor.processChangesAction = { _, _ ->
            PerpetualPositionProcessorTests.positionMock
        }

        val output = processor.processChanges(
            existing = mapOf(
                "ETH-USD" to PerpetualPositionProcessorTests.positionMock.copy(size = 111.0),
            ),
            payload = listOf(PerpetualPositionProcessorTests.payloadMock),
        )
        assertTrue { output?.size == 1 }
        assertTrue { output?.get("ETH-USD") == PerpetualPositionProcessorTests.positionMock }
    }
}
