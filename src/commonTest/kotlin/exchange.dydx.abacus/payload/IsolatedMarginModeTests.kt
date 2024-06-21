package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class IsolatedMarginModeTests : V4BaseTests(true) {

    internal override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    @BeforeTest
    private fun prepareToTest() {
        reset()
        loadMarketsConfigurations()
        loadMarkets()
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
        testMarginAmountForSubaccountTransferWithExistingIsolatedPosition()
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

    private fun testMarginAmountForSubaccountTransferWithExistingIsolatedPosition() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.read_subscribe_with_isolated_position,
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
                                        "current": 162.33
                                    },
                                    "freeCollateral": {
                                        "current": 137.13
                                    },
                                    "quoteBalance": {
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

        // input a trade that will reduce existing position
        perp.tradeInMarket("APE-USD", 0)
        perp.trade("ISOLATED", TradeInputField.marginMode, 0)
        perp.trade("-10", TradeInputField.size, 0)
        perp.trade("2", TradeInputField.limitPrice, 0)
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
                            "size": {
                                "size": -10.0
                            },
                            "price": {
                                "limitPrice": 2.0
                            },
                            "targetLeverage": 1.0,
                            "summary": {
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        // input a trade that will flip position absolute net size +10
        perp.trade("-50", TradeInputField.size, 0)
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
                                        "current": 137.13,
                                        "postOrder": 117.13
                                    },
                                    "openPositions": {
                                        "APE-USD": {
                                            "size": {
                                                "current": 20,
                                                "postOrder": -30
                                            },
                                            "equity": {
                                                "current": 25.20,
                                                "postOrder": 45.20
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
                            "size": {
                                "size": -50.0
                            },
                            "price": {
                                "limitPrice": 2.0
                            },
                            "targetLeverage": 1.0,
                            "summary": {
                                "isolatedMarginTransferAmount": 20.0
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
                            },
                            "summary": {
                                "isolatedMarginTransferAmount": 13.697401030000002
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
            MarginCalculator.getShouldTransferCollateral(
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
                ),
            ),
        )

        // If reduce only is true, should not transfer
        assertEquals(
            false,
            MarginCalculator.getShouldTransferCollateral(
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
                ),
            ),
        )

        // If postOrder is less than current, should not transfer
        assertEquals(
            false,
            MarginCalculator.getShouldTransferCollateral(
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
