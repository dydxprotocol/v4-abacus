package exchange.dydx.abacus.tests.mock.processor.wallet.account

import exchange.dydx.abacus.processor.wallet.account.AssetPositionProcessorProtocol
import exchange.dydx.abacus.state.internalstate.InternalAssetPositionState
import indexer.codegen.IndexerAssetPositionResponseObject

internal class AssetPositionProcessorMock : AssetPositionProcessorProtocol {
    var processCallCount = 0
    var processAction: ((payload: IndexerAssetPositionResponseObject?) -> InternalAssetPositionState?)? = null

    override fun process(payload: IndexerAssetPositionResponseObject?): InternalAssetPositionState? {
        processCallCount++
        return processAction?.invoke(payload)
    }
}
