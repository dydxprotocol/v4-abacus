package exchange.dydx.abacus.tests.mock.processor.markets

import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.processor.markets.CandleProcessorProtocol
import indexer.codegen.IndexerCandleResponseObject

class CandleProcessorMock : CandleProcessorProtocol {
    var processCallCount = 0
    var processAction: ((IndexerCandleResponseObject?) -> MarketCandle?)? = null

    override fun process(payload: IndexerCandleResponseObject?): MarketCandle? {
        processCallCount++
        return processAction?.invoke(payload)
    }
}
