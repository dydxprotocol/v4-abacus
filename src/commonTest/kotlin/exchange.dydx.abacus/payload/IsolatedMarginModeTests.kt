package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.MarginModeCalculator
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import kotlin.test.BeforeTest
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

        val childSubaccountNumber = MarginModeCalculator.getChildSubaccountNumberForIsolatedMarginTrade(parser, account, 0, tradeInput)
        assertEquals(childSubaccountNumber, 256)
    }

//    @Test
//    fun testPendingPositions() {
//        testExistingPendingPositions()
//    }

    private fun testExistingPendingPositions() {
        testParentSubaccountSubscribedWithPendingPositions()
        test({
            perp.tradeInMarket("ARB-USD", 0)
        }, null)
        test({
            perp.trade("ISOLATED", TradeInputField.marginMode, 0)
        }, null)
        test({
            perp.trade("LIMIT", TradeInputField.type, 0)
        }, null)
        test({
            perp.trade("20", TradeInputField.usdcSize, 0)
        }, null)
        test(
            {
                perp.trade("2", TradeInputField.limitPrice, 0)
            },
            """
            {
                "wallet": {
                    "account": {
                        "groupedSubaccounts": {
                            "0": {
                                "openPositions": {
                                    "ARB-USD": {
                                        "size": {
                                            "current": 0.0,
                                            "postOrder": 16.0
                                        }
                                    }
                                },
                                "pendingPositions": [
                                    {
                                        "assetId": "ARB",
                                        "firstOrderId": "d1deed71-d743-5528-aff2-cf3daf8b6413",
                                        "quoteBalance": {
                                            "current": 20,
                                            "postOrder": 40
                                        },
                                        "freeCollateral": {
                                            "current": 20,
                                            "postOrder": 40
                                        },
                                        "equity": {
                                            "current": 20,
                                            "postOrder": 40
                                        }
                                    }
                                ]
                            }
                        }
                    }
                },
                "input": {
                    "current": "trade",
                    "trade": {
                        "marketId": "ARB-USD",
                        "marginMode": "ISOLATED",
                        "size": {
                            "usdcSize": 20.0
                        },
                        "price": {
                            "limitPrice": 2.0
                        },
                        "options": {
                            "needsMarginMode": false
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }
}
