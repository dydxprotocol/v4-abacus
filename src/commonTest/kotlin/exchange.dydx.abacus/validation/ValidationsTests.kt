package exchange.dydx.abacus.validation

import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers

open class ValidationsTests : V4BaseTests() {
    override fun setup() {
        super.setup()

        test({
            loadValidationsMarkets()
        }, null)

        test({
            loadMarketsConfigurations()
        }, null)

        test({
            loadValidationsAccounts()
        }, null)

        test({
            loadValidationsOrderbook()
        }, null)

        test({
            perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        }, null)
    }

    private fun loadValidationsMarkets(): StateResponse {
        return perp.socket(mock.socketUrl, mock.validationsMock.marketsSubscribed, 0, null)
    }

    private fun loadValidationsAccounts(): StateResponse {
        return perp.socket(mock.socketUrl, mock.validationsMock.accountsSubscribed, 0, null)
    }

    private fun loadValidationsOrderbook(): StateResponse {
        return perp.socket(mock.socketUrl, mock.validationsMock.orderbookSubscribed, 0, null)
    }

    override fun reset() {
        super.reset()
        test({
            perp.trade(null, null, 0)
        }, null)
    }
}
