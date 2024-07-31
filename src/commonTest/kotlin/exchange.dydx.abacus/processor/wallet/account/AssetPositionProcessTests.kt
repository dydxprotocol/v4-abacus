package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.state.internalstate.InternalAssetPositionState
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerPositionSide
import kotlin.test.Test
import kotlin.test.assertEquals

class AssetPositionProcessTests {
    companion object {
        internal val payloadMock = IndexerAssetPositionResponseObject(
            symbol = "WETH",
            side = IndexerPositionSide.LONG,
            size = "1.0",
            assetId = "WETH",
            subaccountNumber = 1,
        )

        internal val assetPositionMock = InternalAssetPositionState(
            symbol = "WETH",
            side = PositionSide.LONG,
            size = 1.0,
            assetId = "WETH",
            subaccountNumber = 1,
        )
    }

    @Test
    fun testProcess_nullPayload() {
        val processor = AssetPositionProcessor(
            parser = Parser(),
        )
        val output = processor.process(payloadMock)
        assertEquals(assetPositionMock, output)
    }
}
