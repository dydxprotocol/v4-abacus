package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.tests.extensions.loadAccounts
import exchange.dydx.abacus.tests.extensions.loadFeeDiscounts
import exchange.dydx.abacus.tests.extensions.loadFeeTiers
import exchange.dydx.abacus.tests.extensions.loadMarkets
import exchange.dydx.abacus.tests.extensions.loadMarketsConfigurations
import exchange.dydx.abacus.tests.extensions.loadOrderbook
import exchange.dydx.abacus.tests.extensions.loadUser
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers

open class V3BaseTests : BaseTests(0, false) {
    internal fun loadMarkets(): StateResponse {
        return test({
            perp.loadMarkets(mock)
        }, null)
    }

    internal fun loadMarketsConfigurations(): StateResponse {
        return perp.loadMarketsConfigurations(mock, deploymentUri)
    }

    internal fun loadAccounts(): StateResponse {
        return test({
            perp.loadAccounts(mock)
        }, null)
    }

    internal fun loadUser(): StateResponse {
        return test({
            perp.loadUser(mock)
        }, null)
    }

    override fun setup() {
        loadMarkets()
        loadMarketsConfigurations()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadAccounts()
        loadUser()
    }

    internal fun loadOrderbook(): StateResponse {
        return test({
            perp.loadOrderbook(mock)
        }, null)
    }

    internal fun loadFeeTiers(): StateResponse {
        return test({
            perp.loadFeeTiers(mock)
        }, null)
    }

    internal fun loadFeeDiscounts(): StateResponse {
        return test({
            perp.loadFeeDiscounts(mock)
        }, null)
    }
}
