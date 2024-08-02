package exchange.dydx.abacus.tests.mock.processor.markets

import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.processor.markets.MarketProcessorProtocol
import exchange.dydx.abacus.state.manager.V4Environment
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketOraclePriceObject

class MarketProcessorMock : MarketProcessorProtocol {
    var processAction: ((String, IndexerCompositeMarketObject) -> PerpetualMarket?)? = null
    var processOraclePriceAction: ((String, IndexerWsMarketOraclePriceObject) -> PerpetualMarket?)? = null
    var clearCachedOraclePriceAction: ((String) -> Unit)? = null
    var processCallCount = 0
    var processOraclePriceCallCount = 0
    var clearCachedOraclePriceCallCount = 0

    override fun process(
        marketId: String,
        payload: IndexerCompositeMarketObject
    ): PerpetualMarket? {
        processCallCount++
        return processAction?.invoke(marketId, payload)
    }

    override fun processOraclePrice(
        marketId: String,
        payload: IndexerWsMarketOraclePriceObject
    ): PerpetualMarket? {
        processOraclePriceCallCount++
        return processOraclePriceAction?.invoke(marketId, payload)
    }

    override fun clearCachedOraclePrice(marketId: String) {
        clearCachedOraclePriceCallCount++
        clearCachedOraclePriceAction?.invoke(marketId)
    }

    override var accountAddress: String? = null
    override var environment: V4Environment? = null
}
