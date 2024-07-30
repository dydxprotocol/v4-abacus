package exchange.dydx.abacus.tests.mock.processor.configs

import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.processor.configs.FeeTiersProcessorProtocol
import indexer.models.chain.OnChainFeeTier

internal class FeeTiersProcessorMock : FeeTiersProcessorProtocol {
    var processCallCount = 0
    var processAction: ((payload: List<OnChainFeeTier>?) -> List<FeeTier>?)? = null

    override fun process(payload: List<OnChainFeeTier>?): List<FeeTier>? {
        processCallCount++
        return processAction?.invoke(payload)
    }
}
