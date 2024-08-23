package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.adjustIsolatedMargin
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import kotlin.test.BeforeTest
import kotlin.test.Test

class AdjustIsolatedMarginInputTests : V4BaseTests(useParentSubaccount = true) {

    internal override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    fun loadSubaccountsWithChildren(): StateResponse {
        return perp.socket(testWsUrl, mock.parentSubaccountsChannel.subscribed, 0, null)
    }

    private fun loadSubaccountsWithRealData(): StateResponse {
        return test({
            perp.rest(
                AbUrl.fromString("$testRestUrl/v4/addresses/dydxaddress"),
                mock.parentSubaccountsChannel.rest_response,
                0,
                null,
            )
        }, null)
    }

    @BeforeTest
    private fun prepareTest() {
        reset()
        loadMarketsConfigurations()
        loadMarkets()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadSubaccountsWithChildren()
    }

    @Test
    fun testInputs() {
        testChildSubaccountNumberInput()
        testMarketInput()
        testMarginAmountAddition()
        testMarginAmountRemoval()
        testZeroAmount()
        testMarginAmountPercent()
    }

    private fun testChildSubaccountNumberInput() {
        test(
            {
                perp.adjustIsolatedMargin("128", AdjustIsolatedMarginInputField.ChildSubaccountNumber, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "ChildSubaccountNumber": "128"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testMarketInput() {
        test(
            {
                perp.adjustIsolatedMargin("ETH-USD", AdjustIsolatedMarginInputField.Market, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Market": "ETH-USD"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testZeroAmount() {
        test(
            {
                perp.adjustIsolatedMargin("0", AdjustIsolatedMarginInputField.Amount, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Amount": "0"
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": "100000",
                                    "postOrder": null
                                }
                            },
                            "128": {
                                "quoteBalance": {
                                    "current": "500.000000",
                                    "postOrder": null
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testMarginAmountAddition() {
        test(
            {
                perp.adjustIsolatedMargin(IsolatedMarginAdjustmentType.Add.name, AdjustIsolatedMarginInputField.Type, 0)
            },
            """
        {
            "input": {
                "current": "adjustIsolatedMargin",
                "adjustIsolatedMargin": {
                    "Type": "Add",
                    "summary": {
                        "crossFreeCollateral": {
                            "current": 70675.46098618512
                        }
                    }
                }
            }
        }
            """.trimIndent(),
        )

        test(
            {
                perp.adjustIsolatedMargin("92.49", AdjustIsolatedMarginInputField.Amount, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Amount": "92.49",
                        "AmountInput": "Amount",
                        "AmountPercent": "0.0013"
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.adjustIsolatedMargin("-92.49", AdjustIsolatedMarginInputField.Amount, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Amount": "92.49",
                        "AmountInput": "Amount",
                        "AmountPercent": "0.0013"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testMarginAmountRemoval() {
        test(
            {
                perp.adjustIsolatedMargin(IsolatedMarginAdjustmentType.Remove.name, AdjustIsolatedMarginInputField.Type, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Remove",
                        "summary": {
                            "crossFreeCollateral": {
                                "current": 70675.46098618512
                            }
                        }
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": "100000"
                                }
                            },
                            "128": {
                                "quoteBalance": {
                                    "current": "500.000000"
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.adjustIsolatedMargin("20", AdjustIsolatedMarginInputField.Amount, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Remove",
                        "Amount": "20",
                        "AmountInput": "Amount",
                        "AmountPercent": "0.018"
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": "100000",
                                    "postOrder": "100020"
                                }
                            },
                            "128": {
                                "quoteBalance": {
                                    "current": "500.000000",
                                    "postOrder": "480"
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testMarginAmountPercent() {
        test(
            {
                perp.adjustIsolatedMargin(IsolatedMarginAdjustmentType.Add.name, AdjustIsolatedMarginInputField.Type, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Add",
                        "Amount": null,
                        "AmountPercent": null
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.adjustIsolatedMargin("0.1", AdjustIsolatedMarginInputField.AmountPercent, 0)
            },
            """
            {
                "wallet": {
                    "account": {
                        "groupedSubaccounts": {
                            "0": {
                                "freeCollateral": {
                                    "current": 70675.4609861851
                                }
                            }
                        }
                    }
                },
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Add",
                        "AmountPercent": "0.1",
                        "Amount": "7067.54609861851",
                        "AmountInput": "Percent"
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.adjustIsolatedMargin(
                    IsolatedMarginAdjustmentType.Remove.name,
                    AdjustIsolatedMarginInputField.Type,
                    0,
                )
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Remove",
                        "Amount": null,
                        "AmountPercent": null
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.adjustIsolatedMargin("1", AdjustIsolatedMarginInputField.AmountPercent, 0)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "128": {
                                "equity": {
                                    "current": 1132.02
                                },
                                "notionalTotal": {
                                    "current": 632.02
                                }
                            }
                        }
                    }
                },
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Remove",
                        "AmountPercent": "1",
                        "Amount": "1099.77",
                        "AmountInput": "Percent"
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.adjustIsolatedMargin("0.1", AdjustIsolatedMarginInputField.AmountPercent, 0)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "128": {
                                "equity": {
                                    "current": 1132.02
                                }
                            }
                        }
                    }
                },
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Remove",
                        "AmountPercent": "0.1",
                        "Amount": "109.98",
                        "AmountInput": "Percent"
                    }
                }
            }
            """.trimIndent(),
        )
    }
}
