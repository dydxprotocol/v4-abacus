package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.trade
import exchange.dydx.abacus.state.modal.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadAccounts
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlinx.datetime.Clock
import kotlin.test.Test

class V3TradeInputWithoutAccountTests: V3BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        var time = ServerTime.now()
        testMarketTradeInput()
        time = perp.log("Market Order", time)

        testLoadAccounts()
        time = perp.log("Loaded Account", time)
    }

    override fun setup() {
        loadMarkets()
        loadMarketsConfigurations()
        // do not load account
        loadOrderbook()
    }

    private fun testMarketTradeInput() {
        /*
        Initial setup
         */
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        test(
            {
                perp.trade("1.", TradeInputField.size, 0)
            },
            """
                {
                    "input": {
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "timeInForce": "GTT",
                            "goodTil": {
                                "unit": "D",
                                "duration": 28
                            },
                            "fields": [
                            ],
                            "options": {
                            },
                            "summary": {
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            }
                        },
                        "current": "trade"
                    }
                }
            """.trimIndent()
        )
    }

    private fun testLoadAccounts() {
        test(
            {
                perp.loadAccounts(mock)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                }
                            }
                        }
                    },
                    "input": {
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "timeInForce": "GTT",
                            "goodTil": {
                                "unit": "D",
                                "duration": 28
                            },
                            "fields": [
                            ],
                            "options": {
                            },
                            "summary": {
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            }
                        },
                        "current": "trade"
                    }
                }
            """.trimIndent()
        )
    }
}