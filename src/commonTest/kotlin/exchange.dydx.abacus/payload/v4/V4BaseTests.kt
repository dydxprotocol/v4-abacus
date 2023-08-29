package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.AppVersion
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.modal.PerpTradingStateMachine
import exchange.dydx.abacus.tests.extensions.loadMarkets
import exchange.dydx.abacus.tests.extensions.loadMarketsConfigurations
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions


open class V4BaseTests : BaseTests(127) {

    internal val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")
    internal val testRestUrl =
        "https://indexer.v4staging.dydx.exchange"
    override fun createState(): PerpTradingStateMachine {
        return PerpTradingStateMachine(mock.v4Environment, null, null, AppVersion.v4, 127)
    }

    internal open fun loadMarkets(): StateResponse {
        return test({
            perp.loadMarkets(mock)
        }, null)
    }

    internal fun loadMarketsConfigurations(): StateResponse {
        return test({
            perp.loadMarketsConfigurations(mock)
        }, null)
    }

    internal open fun loadSubaccounts(): StateResponse {
        return test({
            perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")
        }, null)
    }

    override fun setup() {
        loadMarketsConfigurations()
        loadMarkets()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadSubaccounts()
    }
}