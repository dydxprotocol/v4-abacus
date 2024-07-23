package exchange.dydx.abacus.tests.mock.processor.wallet.account

import exchange.dydx.abacus.processor.wallet.account.PerpetualPositionProcessorProtocol
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import indexer.codegen.IndexerPerpetualPositionResponseObject

internal class PerpetualPositionProcessorMock : PerpetualPositionProcessorProtocol {
    var processCallCount: Int = 0
    var processAction: ((InternalPerpetualPosition?, IndexerPerpetualPositionResponseObject?) -> InternalPerpetualPosition?)? = null

    override fun process(
        existing: InternalPerpetualPosition?,
        payload: IndexerPerpetualPositionResponseObject?
    ): InternalPerpetualPosition? {
        processCallCount += 1
        return processAction?.invoke(existing, payload)
    }
}
