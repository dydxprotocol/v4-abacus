package exchange.dydx.abacus.tests.mock.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.processor.wallet.account.FillProcessorProtocol
import indexer.codegen.IndexerFillResponseObject

internal class FillProcessorMock : FillProcessorProtocol {
    var processCallCount = 0
    var processAction: ((payload: IndexerFillResponseObject, subaccountNumber: Int) -> SubaccountFill?)? = null

    override fun process(
        payload: IndexerFillResponseObject,
        subaccountNumber: Int
    ): SubaccountFill? {
        processCallCount++
        return processAction?.invoke(payload, subaccountNumber)
    }
}
