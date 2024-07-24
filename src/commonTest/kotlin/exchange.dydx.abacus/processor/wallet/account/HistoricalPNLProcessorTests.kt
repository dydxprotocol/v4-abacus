package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerPnlTicksResponseObject
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoricalPNLProcessorTests {
    companion object {
        private val createdAt = Instant.parse("2021-01-01T00:00:00Z")

        val payload = IndexerPnlTicksResponseObject(
            equity = "1.0",
            totalPnl = "2.0",
            netTransfers = "3.0",
            createdAt = createdAt.toString(),
        )

        val pnlMock = SubaccountHistoricalPNL(
            equity = 1.0,
            totalPnl = 2.0,
            netTransfers = 3.0,
            createdAtMilliseconds = createdAt.toEpochMilliseconds().toDouble(),
        )
    }
    private val processor = HistoricalPNLProcessor(
        parser = Parser(),
    )

    @Test
    fun testProcess() {
        val pnl = processor.process(
            existing = null,
            payload = payload,
        )

        assertEquals(
            expected = pnlMock,
            actual = pnl,
        )
    }
}
