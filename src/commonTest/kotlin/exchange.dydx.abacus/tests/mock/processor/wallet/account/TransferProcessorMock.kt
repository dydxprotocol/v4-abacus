package exchange.dydx.abacus.tests.mock.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.processor.wallet.account.TransferProcessorProtocol
import exchange.dydx.abacus.state.manager.V4Environment
import indexer.codegen.IndexerTransferResponseObject

internal class TransferProcessorMock : TransferProcessorProtocol {
    var processCallCount = 0
    var processAction: ((payload: IndexerTransferResponseObject?) -> SubaccountTransfer?)? = null

    override fun process(payload: IndexerTransferResponseObject?): SubaccountTransfer? {
        processCallCount++
        return processAction?.invoke(payload)
    }

    override var accountAddress: String? = null
    override var environment: V4Environment? = null
}
