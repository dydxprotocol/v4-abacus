package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.account.SubaccountPositionResources
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionStatus
import indexer.codegen.IndexerPositionSide
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class PerpetualPositionProcessorTests {
    companion object {
        private val createdAt = Instant.parse("2021-01-01T00:00:00Z")

        internal val payloadMock = IndexerPerpetualPositionResponseObject(
            market = "ETH-USD",
            status = IndexerPerpetualPositionStatus.OPEN,
            side = IndexerPositionSide.LONG,
            size = "93.57",
            maxSize = "100",
            entryPrice = "1091.812076",
            exitPrice = "1091.236219",
            unrealizedPnl = "61455.547636",
            realizedPnl = "-4173.521266",
            createdAt = createdAt.toString(),
            closedAt = null,
            sumOpen = "218.92",
            sumClose = "125.35",
            netFunding = "-4101.337527",
            subaccountNumber = 11,
        )

        internal val positionMock = InternalPerpetualPosition(
            market = "ETH-USD",
            status = IndexerPerpetualPositionStatus.OPEN,
            side = IndexerPositionSide.LONG,
            size = 93.57,
            maxSize = 100.0,
            entryPrice = 1091.812076,
            exitPrice = 1091.236219,
            unrealizedPnl = 61455.547636,
            realizedPnl = -4173.521266,
            createdAt = createdAt,
            closedAt = null,
            sumOpen = 218.92,
            sumClose = 125.35,
            netFunding = -4101.337527,
            subaccountNumber = 11,
            resources = SubaccountPositionResources(
                sideString = TradeStatesWithStringValues(
                    current = "APP.GENERAL.LONG_POSITION_SHORT",
                    postOrder = null,
                    postAllOrders = null,
                ),
                sideStringKey = TradeStatesWithStringValues(
                    current = "APP.GENERAL.LONG_POSITION_SHORT",
                    postOrder = null,
                    postAllOrders = null,
                ),
                indicator = TradeStatesWithStringValues(
                    current = "long",
                    postOrder = null,
                    postAllOrders = null,
                ),
            ),
        )
    }

    private val positionProcessor = PerpetualPositionProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
    )

    @Test
    fun testProcess() {
        val position = positionProcessor.process(
            existing = null,
            payload = payloadMock,
        )
        assertEquals(positionMock, position)
    }

    @Test
    fun testProcessChanges() {
        val position = positionProcessor.processChanges(
            existing = null,
            payload = payloadMock,
        )
        assertEquals(positionMock, position)

        val existing = positionProcessor.process(
            existing = positionMock,
            payload = null,
        )
        assertEquals(positionMock, existing)

        val position2 = positionProcessor.processChanges(
            existing = positionMock.copy(size = 111.0),
            payload = payloadMock,
        )
        assertEquals(positionMock, position2)
    }
}
