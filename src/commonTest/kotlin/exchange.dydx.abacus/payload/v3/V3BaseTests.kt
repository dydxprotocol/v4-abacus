package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.tests.extensions.loadAccounts
import exchange.dydx.abacus.tests.extensions.loadFeeDiscounts
import exchange.dydx.abacus.tests.extensions.loadFeeTiers
import exchange.dydx.abacus.tests.extensions.loadMarkets
import exchange.dydx.abacus.tests.extensions.loadMarketsConfigurations
import exchange.dydx.abacus.tests.extensions.loadOrderbook
import exchange.dydx.abacus.tests.extensions.loadUser
import kotlin.test.Test
import kotlin.test.assertNotNull

open class V3BaseTests : BaseTests(0) {
    @Test
    fun testBadSocket() {
        test({
            perp.socket(mock.socketUrl, "blah", 0, null)
        }, null, {
            val error = it.errors?.firstOrNull()
            assertNotNull(error)
        })
    }

    @Test
    fun testBadRest() {
        test({
            perp.rest(
                AbUrl(host = "https://api.dydx.exchange", path = "/v2/fake_endpoint"),
                "test",
                0,
                null,
            )
        }, null, {
            val error = it.errors?.firstOrNull()
            assertNotNull(error)
        })
    }

    internal fun loadMarkets(): StateResponse {
        return test({
            perp.loadMarkets(mock)
        }, null)
    }

    internal fun loadMarketsConfigurations(): StateResponse {
        return test(
            {
                perp.loadMarketsConfigurations(mock)
            }, """
            {
                "markets": {
                    "markets": {
                        "BTC-USD": {
                            "configs": {
                                "displayStepSize":"0.0001",
                                "displayTickSize":"1"
                                }
                        },
                        "ETH-USD": {
                            "configs": {
                                "displayStepSize":"0.001",
                                "displayTickSize":"0.1"
                                }
                        }
                    }
                }
            }
        """.trimIndent()
        )
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