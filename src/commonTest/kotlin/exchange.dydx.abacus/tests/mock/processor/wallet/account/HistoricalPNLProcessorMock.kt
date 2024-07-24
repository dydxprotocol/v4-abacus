package exchange.dydx.abacus.tests.mock.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.processor.wallet.account.HistoricalPNLProcessorProtocol
import indexer.codegen.IndexerPnlTicksResponseObject

internal class HistoricalPNLProcessorMock : HistoricalPNLProcessorProtocol {
    var processCount = 0
    var processAction: ((existing: SubaccountHistoricalPNL?, payload: IndexerPnlTicksResponseObject) -> SubaccountHistoricalPNL)? = null

    override fun process(
        existing: SubaccountHistoricalPNL?,
        payload: IndexerPnlTicksResponseObject
    ): SubaccountHistoricalPNL? {
        processCount++
        return processAction?.invoke(existing, payload)
    }
}
