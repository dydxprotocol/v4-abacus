package exchange.dydx.abacus.tests.mock.processor.configs

import exchange.dydx.abacus.output.EquityTiers
import exchange.dydx.abacus.processor.configs.EquityTiersProcessorProtocol
import indexer.models.chain.OnChainEquityTiersResponse

internal class EquityTiersProcessorMock : EquityTiersProcessorProtocol {
    var processCallCount = 0
    var processAction: ((payload: OnChainEquityTiersResponse?) -> EquityTiers?)? = null

    override fun process(payload: OnChainEquityTiersResponse?): EquityTiers? {
        processCallCount++
        return processAction?.invoke(payload)
    }
}
