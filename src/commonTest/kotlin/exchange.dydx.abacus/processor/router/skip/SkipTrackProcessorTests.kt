package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.tests.payloads.SkipTrackMock
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class SkipTrackProcessorTests {
    val parser = Parser()
    internal val processor = SkipTrackProcessor(parser = parser)
    val mock = SkipTrackMock

    /**
     * Tests that receiving an error payload from the track endpoint
     * with no existing track state results in a no-op.
     * No state is saved
     */
    @Test
    fun testReceivedErrorNoExistingTransfer() {
        val expected = mapOf<String, Any>()
        val payload = templateToMap(template = mock.error)
        val result = processor.received(existing = mapOf(), payload = payload)
        assertEquals(expected, result)
    }

    /**
     * Tests that receiving a success response payload from the track endpoint
     * with no existing track state adds a key (tx hash) with the value true.
     */
    @Test
    fun testReceivedSuccessNoExistingTransfer() {
        val expected = mapOf<String, Any>(
            "0x897a7464fe7736def48f5eb77ffe06f11beacadc9805d3f9237c17767567c00f" to true,
        )
        val payload = templateToMap(template = mock.success)
        val result = processor.received(existing = mapOf(), payload = payload)
        assertEquals(expected, result)
    }
}
