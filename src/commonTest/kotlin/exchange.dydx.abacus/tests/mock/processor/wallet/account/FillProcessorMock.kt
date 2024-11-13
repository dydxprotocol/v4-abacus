package exchange.dydx.abacus.tests.mock.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.processor.wallet.account.FillProcessorProtocol
import indexer.models.IndexerCompositeFillObject

internal class FillProcessorMock : FillProcessorProtocol {
    var processCallCount = 0
    var processAction: ((payload: IndexerCompositeFillObject, subaccountNumber: Int) -> SubaccountFill?)? = null

    override fun process(
        payload: IndexerCompositeFillObject,
        subaccountNumber: Int
    ): SubaccountFill? {
        processCallCount++
        return processAction?.invoke(payload, subaccountNumber)
    }
}
