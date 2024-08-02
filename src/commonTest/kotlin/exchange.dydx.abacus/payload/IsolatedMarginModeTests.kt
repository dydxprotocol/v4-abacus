package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.closePosition
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class IsolatedMarginModeTests : V4BaseTests(true) {

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    override fun loadOrderbook(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.orderbookChannel.subscribed_ape, 0, null)
        }, null)
    }

    @BeforeTest
    private fun prepareToTest() {
        reset()
        loadMarketsConfigurations()
        loadMarkets()
        loadOrderbook()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
    }

    private fun testParentSubaccountSubscribedWithPendingPositions() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.real_subscribed_with_pending,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 2021.4
                                    },
                                    "freeCollateral": {
                                        "current": 1712.0
                                    },
                                    "quoteBalance": {
                                        "current": 1712.0
                                    },
                                    "openPositions": {
                                    },
                                    "pendingPositions": [
                                        {
                                            "assetId": "ARB",
                                            "firstOrderId": "d1deed71-d743-5528-aff2-cf3daf8b6413",
                                            "quoteBalance": {
                                                "current": 20
                                            },
                                            "freeCollateral": {
                                                "current": 20
                                            },
                                            "equity": {
                                                "current": 20
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testParentSubaccountSubscribedWithUnpopulatedChild() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.real_subscribed_with_unpopulated_child,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 1979.9
                                    },
                                    "freeCollateral": {
                                        "current": 1712.0
                                    },
                                    "quoteBalance": {
                                        "current": 1712.0
                                    },
                                    "openPositions": {
                                    },
                                    "pendingPositions": [
                                        {
                                            "assetId": "ARB",
                                            "firstOrderId": "d1deed71-d743-5528-aff2-cf3daf8b6413",
                                            "quoteBalance": {
                                                "current": 270
                                            },
                                            "freeCollateral": {
                                                "current": 270
                                            },
                                            "equity": {
                                                "current": 270
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    @Test
    fun testMarginMode() {
        testMarginModeOnMarketChange()
        testMarginAmountForSubaccountTransfer()
    }

    @Test
    fun testMarginModeWithExistingPosition() {
        testMarginAmountForSubaccountTransferWithExistingPosition()
        testMarginAmountForSubaccountTransferWithExistingPositionAndOpenOrders()
    }

    // MarginMode should automatically to match the current market based on a variety of factors
    private fun testMarginModeOnMarketChange() {
        testParentSubaccountSubscribedWithPendingPositions()

        // needsMarginMode should be false to prevent user from changing margin mode
        // Attaching to V4ParentSubaccountTests to test the tradeInMarket function with a subaccount that has a pending position
        test(
            {
                perp.tradeInMarket("LDO-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "LDO-USD",
                            "marginMode": "ISOLATED",
                            "options": {
                                "needsMarginMode": false,
                                "marginModeOptions": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // Test the placeholder openPosition's equity
        test(
            {
                perp.tradeInMarket("APE-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "CROSS",
                            "options": {
                                "needsMarginMode": true
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // Test dummy market with perpetualMarketType ISOLATED
        test(
            {
                perp.tradeInMarket("ISO-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "ISO-USD",
                            "marginMode": "ISOLATED",
                            "options": {
                                "needsMarginMode": false
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testMarginAmountForSubaccountTransferWithExistingPosition() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.real_subscribe_with_isolated_position,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "freeCollateral": {
                                        "current": 137.13
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20
                                            },
                                            "equity": {
                                                "current": 25.20
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // close all existing position should transfer out + subaccount more free collateral
        perp.closePosition("APE-USD", ClosePositionInputField.market, 0)
        test(
            {
                perp.closePosition("1", ClosePositionInputField.percent, 0)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "freeCollateral": {
                                        "current": 137.13,
                                        "postOrder": 157.12
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20,
                                                "postOrder": 0
                                            },
                                            "equity": {
                                                "current": 25.20
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "input": {
                        "current": "closePosition",
                        "closePosition": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "targetLeverage": 1.0,
                            "summary": {
                                "price": 1.0,
                                "size": 20.0
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // trade that will reduce existing position by 10 via trade
        perp.tradeInMarket("APE-USD", 0)
        perp.trade("SELL", TradeInputField.side, 0)
        perp.trade("1", TradeInputField.limitPrice, 0)
        perp.trade("10", TradeInputField.size, 0)
        test(
            {
                perp.trade("1", TradeInputField.targetLeverage, 0)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "freeCollateral": {
                                        "current": 137.13
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20,
                                                "postOrder": 10
                                            },
                                            "equity": {
                                                "current": 25.20,
                                                "postOrder": 25.20
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "targetLeverage": 1.0,
                            "summary": {
                                "price": 1.0,
                                "size": 10.0
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // input a trade that will flip position absolute net size +10
        perp.trade("50", TradeInputField.size, 0)
        test(
            {
                perp.trade("2", TradeInputField.targetLeverage, 0)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "freeCollateral": {
                                        "current": 137.13,
                                        "postOrder": 128.22
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20,
                                                "postOrder": -30
                                            },
                                            "equity": {
                                                "current": 25.20,
                                                "postOrder": 34.1
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "targetLeverage": 2.0,
                            "summary": {
                                "price": 1.0,
                                "size": 50.0
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testMarginAmountForSubaccountTransferWithExistingPositionAndOpenOrders() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.real_subscribe_with_isolated_position_and_open_orders,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "orders": {
                                        "bbc7cfe6-8837-5c46-94c4-36a4319231ac": {
                                            "side": "BUY",
                                            "type": "LIMIT",
                                            "status": "OPEN",
                                            "reduceOnly": false,
                                            "marketId": "APE-USD" 
                                        }
                                    },
                                    "freeCollateral": {
                                        "current": 137.13
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20
                                            },
                                            "equity": {
                                                "current": 25.20
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // close all existing position
        perp.closePosition("APE-USD", ClosePositionInputField.market, 0)
        test(
            {
                perp.closePosition("1", ClosePositionInputField.percent, 0)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "freeCollateral": {
                                        "current": 137.13
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20,
                                                "postOrder": 0
                                            },
                                            "equity": {
                                                "current": 25.20,
                                                "postOrder": 25.20
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "input": {
                        "current": "closePosition",
                        "closePosition": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "targetLeverage": 1.0,
                            "size": {
                                "size": 20.0
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // trade that will reduce existing position by 10 via trade
        perp.tradeInMarket("APE-USD", 0)
        perp.trade("SELL", TradeInputField.side, 0)
        perp.trade("1", TradeInputField.limitPrice, 0)
        perp.trade("10", TradeInputField.size, 0)
        test(
            {
                perp.trade("1", TradeInputField.targetLeverage, 0)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "freeCollateral": {
                                        "current": 137.13
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20,
                                                "postOrder": 10
                                            },
                                            "equity": {
                                                "current": 25.20,
                                                "postOrder": 25.20
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "targetLeverage": 1.0,
                            "summary": {
                                "price": 1.0,
                                "size": 10.0
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // input a trade that will flip position absolute net size +10
        perp.trade("50", TradeInputField.size, 0)
        test(
            {
                perp.trade("2", TradeInputField.targetLeverage, 0)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "freeCollateral": {
                                        "current": 137.13,
                                        "postOrder": 128.22
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20,
                                                "postOrder": -30
                                            },
                                            "equity": {
                                                "current": 25.20,
                                                "postOrder": 34.1
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "targetLeverage": 2.0,
                            "summary": {
                                "price": 1.0,
                                "size": 50.0
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    // Test the margin amount for subaccount transfer
    private fun testMarginAmountForSubaccountTransfer() {
        testParentSubaccountSubscribedWithPendingPositions()
        test(
            {
                perp.tradeInMarket("APE-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "CROSS",
                            "options": {
                                "needsMarginMode": true
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("ISOLATED", TradeInputField.marginMode, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "options": {
                                "needsMarginMode": true
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("20", TradeInputField.usdcSize, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "size": {
                                "usdcSize": 20.0
                            },
                            "options": {
                                "needsMarginMode": true
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("2", TradeInputField.limitPrice, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "size": {
                                "usdcSize": 20.0
                            },
                            "price": {
                                "limitPrice": 2.0
                            },
                            "options": {
                                "needsMarginMode": true
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("2", TradeInputField.targetLeverage, 0)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "openPositions": {
                                        "APE-USD": {
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "APE-USD",
                            "marginMode": "ISOLATED",
                            "size": {
                                "usdcSize": 20.0
                            },
                            "price": {
                                "limitPrice": 2.0
                            },
                            "targetLeverage": 2.0,
                            "options": {
                                "needsMarginMode": true
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        val postOrderEquity = parser.asDouble(parser.value(perp.data, "wallet.account.groupedSubaccounts.0.openPositions.APE-USD.equity.postOrder")) ?: 0.0
        assertEquals(postOrderEquity, 13.697401030000002)
    }

    @Test
    fun testGetChildSubaccountNumberForIsolatedMarginTrade() {
        testUnpopulatedSubaccount()
    }

    // Test getChildSubaccountNumberForIsolatedMarginTrade when subaccount 256 has a pending position but 128 does not
    private fun testUnpopulatedSubaccount() {
        testParentSubaccountSubscribedWithUnpopulatedChild()
        val account = perp.account

        val tradeInput = mapOf(
            "marginMode" to "ISOLATED",
            "marketId" to "ARB-USD",
        )

        val childSubaccountNumber = MarginCalculator.getChildSubaccountNumberForIsolatedMarginTrade(parser, account, 0, tradeInput)
        assertEquals(childSubaccountNumber, 256)
    }

    @Test
    fun testGetShouldTransferCollateral() {
        assertTrue(
            "Should result in a transfer",
            MarginCalculator.getShouldTransferInCollateral(
                parser,
                subaccount = mapOf(
                    "openPositions" to mapOf(
                        "ARB-USD" to mapOf(
                            "size" to mapOf(
                                "current" to 0.0,
                                "postOrder" to 16.0,
                            ),
                        ),
                    ),
                ),
                tradeInput = mapOf(
                    "marketId" to "ARB-USD",
                    "marginMode" to "ISOLATED",
                    "reduceOnly" to false,
                    "side" to "BUY",
                    "summary" to mapOf(
                        "filled" to true,
                        "size" to 16.0,
                    ),
                ),
            ),
        )

        // If reduce only is true, should not transfer in
        assertEquals(
            false,
            MarginCalculator.getShouldTransferInCollateral(
                parser,
                subaccount = mapOf(
                    "openPositions" to mapOf(
                        "ARB-USD" to mapOf(
                            "size" to mapOf(
                                "current" to 0.0,
                                "postOrder" to 16.0,
                            ),
                        ),
                    ),
                ),
                tradeInput = mapOf(
                    "marketId" to "ARB-USD",
                    "marginMode" to "ISOLATED",
                    "reduceOnly" to true,
                    "side" to "BUY",
                    "summary" to mapOf(
                        "filled" to true,
                        "size" to 16.0,
                    ),
                ),
            ),
        )

        // If full close + no open orders, should transfer out
        assertEquals(
            true,
            MarginCalculator.getShouldTransferOutRemainingCollateral(
                parser,
                subaccount = mapOf(
                    "openPositions" to mapOf(
                        "ARB-USD" to mapOf(
                            "size" to mapOf(
                                "current" to 16.0,
                                "postOrder" to 0.0,
                            ),
                        ),
                    ),
                ),
                tradeInput = mapOf(
                    "marketId" to "ARB-USD",
                    "marginMode" to "ISOLATED",
                    "reduceOnly" to true,
                    "side" to "SELL",
                    "summary" to mapOf(
                        "filled" to true,
                        "size" to 16.0,
                    ),
                ),
            ),
        )

        // If postOrder is less than current, should not transfer in
        assertEquals(
            false,
            MarginCalculator.getShouldTransferInCollateral(
                parser,
                subaccount = mapOf(
                    "openPositions" to mapOf(
                        "ARB-USD" to mapOf(
                            "size" to mapOf(
                                "current" to 22.0,
                                "postOrder" to 16.0,
                            ),
                        ),
                    ),
                ),
                tradeInput = mapOf(
                    "marketId" to "ARB-USD",
                    "marginMode" to "ISOLATED",
                    "reduceOnly" to false,
                    "side" to "SELL",
                    "summary" to mapOf(
                        "filled" to true,
                        "size" to 6.0,
                    ),
                ),
            ),
        )

        // If reducing position to full close but has open orders, should not transfer out
        assertEquals(
            false,
            MarginCalculator.getShouldTransferOutRemainingCollateral(
                parser,
                subaccount = mapOf(
                    "openPositions" to mapOf(
                        "ARB-USD" to mapOf(
                            "size" to mapOf(
                                "current" to 22.0,
                                "postOrder" to 0.0,
                            ),
                        ),
                    ),
                    "orders" to mapOf(
                        "order-id" to mapOf(
                            "marketId" to "ARB-USD",
                            "status" to "OPEN",
                        ),
                    ),
                ),
                tradeInput = mapOf(
                    "marketId" to "ARB-USD",
                    "marginMode" to "ISOLATED",
                    "reduceOnly" to false,
                    "side" to "SELL",
                    "summary" to mapOf(
                        "filled" to true,
                        "size" to 22.0,
                    ),
                ),
            ),
        )
    }

    @Test
    fun testGetTransferAmountFromTargetLeverage() {
        assertEquals(
            116.26514285714283,
            MarginCalculator.getTransferAmountFromTargetLeverage(
                price = 0.1465,
                oraclePrice = 0.1211,
                side = "BUY",
                size = 2320.0, // ~$400 usdcSize
                targetLeverage = 4.9,
            ),
            "Significant orderbook drift should result in $116.27 transfer amount instead of $80",
        )

        assertEquals(
            67.976,
            MarginCalculator.getTransferAmountFromTargetLeverage(
                price = 0.1465,
                oraclePrice = 0.1211,
                side = "SELL",
                size = 2320.0, // ~$400 usdcSize
                targetLeverage = 5.0,
            ),
            "A sell when there is significant orderbook drift should result in $67.976 transfer amount which is the naive (askPrice * size) / targetLeverage",
        )
    }
}
