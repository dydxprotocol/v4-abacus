package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadAccounts
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class V3TradeInputWithoutAccountTests : V3BaseTests() {
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

        if (perp.staticTyping) {
            perp.trade("1.", TradeInputField.size, 0)
            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Market)
            assertEquals(trade.side, OrderSide.Buy)
            assertEquals(trade.marketId, "ETH-USD")
        } else {
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
                """.trimIndent(),
            )
        }
    }

    private fun testLoadAccounts() {
        if (perp.staticTyping) {
            perp.loadAccounts(mock)
            val account = perp.internalState.wallet.account
            assertEquals(account.subaccounts.size, 1)
        } else {
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
                """.trimIndent(),
            )
        }
    }
}
