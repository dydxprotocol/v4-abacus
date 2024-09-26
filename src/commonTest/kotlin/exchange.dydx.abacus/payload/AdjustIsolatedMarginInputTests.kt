package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.IsolatedMarginInputType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.adjustIsolatedMargin
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AdjustIsolatedMarginInputTests : V4BaseTests(useParentSubaccount = true) {

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    private fun loadSubaccountsWithChildren(): StateResponse {
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
        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "128",
                type = AdjustIsolatedMarginInputField.ChildSubaccountNumber,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals(128, perp.internalState.input.adjustIsolatedMargin.childSubaccountNumber)
        } else {
            test(
                {
                    perp.adjustIsolatedMargin(
                        "128",
                        AdjustIsolatedMarginInputField.ChildSubaccountNumber,
                        0,
                    )
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
    }

    private fun testMarketInput() {
        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "ETH-USD",
                type = AdjustIsolatedMarginInputField.Market,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals("ETH-USD", perp.internalState.input.adjustIsolatedMargin.market)
        } else {
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
    }

    private fun testZeroAmount() {
        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "0",
                type = AdjustIsolatedMarginInputField.Amount,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals(0.0, perp.internalState.input.adjustIsolatedMargin.amount)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]
            assertEquals(100000.0, subaccount?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
            val subaccount1 = perp.internalState.wallet.account.subaccounts[128]
            assertEquals(500.0, subaccount1?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
        } else {
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
    }

    private fun testMarginAmountAddition() {
        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = IsolatedMarginAdjustmentType.Add.name,
                type = AdjustIsolatedMarginInputField.Type,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals(IsolatedMarginAdjustmentType.Add, perp.internalState.input.adjustIsolatedMargin.type)
            assertEquals(70675.46098618512, perp.internalState.input.adjustIsolatedMargin.summary?.crossFreeCollateral)
        } else {
            test(
                {
                    perp.adjustIsolatedMargin(
                        IsolatedMarginAdjustmentType.Add.name,
                        AdjustIsolatedMarginInputField.Type,
                        0,
                    )
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
        }

        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "92.49",
                type = AdjustIsolatedMarginInputField.Amount,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals(92.49, perp.internalState.input.adjustIsolatedMargin.amount)
            assertEquals(IsolatedMarginInputType.Amount, perp.internalState.input.adjustIsolatedMargin.amountInput)
            assertEquals(0.0013086578949669525, perp.internalState.input.adjustIsolatedMargin.amountPercent)
        } else {
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
        }

        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "-92.49",
                type = AdjustIsolatedMarginInputField.Amount,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals(92.49, perp.internalState.input.adjustIsolatedMargin.amount)
            assertEquals(IsolatedMarginInputType.Amount, perp.internalState.input.adjustIsolatedMargin.amountInput)
            assertEquals(0.0013086578949669525, perp.internalState.input.adjustIsolatedMargin.amountPercent)
        } else {
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
    }

    private fun testMarginAmountRemoval() {
        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = IsolatedMarginAdjustmentType.Remove.name,
                type = AdjustIsolatedMarginInputField.Type,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals(IsolatedMarginAdjustmentType.Remove, perp.internalState.input.adjustIsolatedMargin.type)
            assertEquals(70675.46098618512, perp.internalState.input.adjustIsolatedMargin.summary?.crossFreeCollateral)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]
            assertEquals(100000.0, subaccount?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
            val subaccount1 = perp.internalState.wallet.account.subaccounts[128]
            assertEquals(500.0, subaccount1?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
        } else {
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
        }

        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "20",
                type = AdjustIsolatedMarginInputField.Amount,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals(20.0, perp.internalState.input.adjustIsolatedMargin.amount)
            assertEquals(IsolatedMarginInputType.Amount, perp.internalState.input.adjustIsolatedMargin.amountInput)
            assertEquals(0.018185629293809846, perp.internalState.input.adjustIsolatedMargin.amountPercent)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]
            assertEquals(100000.0, subaccount?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
            assertEquals(100020.0, subaccount?.calculated?.get(CalculationPeriod.post)?.quoteBalance)
            val subaccount1 = perp.internalState.wallet.account.subaccounts[128]
            assertEquals(500.0, subaccount1?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
            assertEquals(480.0, subaccount1?.calculated?.get(CalculationPeriod.post)?.quoteBalance)
        } else {
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
    }

    private fun testMarginAmountPercent() {
        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = IsolatedMarginAdjustmentType.Add.name,
                type = AdjustIsolatedMarginInputField.Type,
                parentSubaccountNumber = 0,
            )

            assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
            assertEquals(IsolatedMarginAdjustmentType.Add, perp.internalState.input.adjustIsolatedMargin.type)
            assertEquals(null, perp.internalState.input.adjustIsolatedMargin.amount)
            assertEquals(null, perp.internalState.input.adjustIsolatedMargin.amountPercent)
        } else {
            test(
                {
                    perp.adjustIsolatedMargin(
                        IsolatedMarginAdjustmentType.Add.name,
                        AdjustIsolatedMarginInputField.Type,
                        0,
                    )
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
        }

        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "0.1",
                type = AdjustIsolatedMarginInputField.AmountPercent,
                parentSubaccountNumber = 0,
            )

            val input = perp.internalState.input.adjustIsolatedMargin
            assertEquals(IsolatedMarginAdjustmentType.Add, input.type)
            assertEquals(0.1, input.amountPercent)
            assertEquals(IsolatedMarginInputType.Percent, input.amountInput)
            assertEquals(7067.546098618513, input.amount)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]
            assertEquals(70675.46098618512, subaccount?.calculated?.get(CalculationPeriod.current)?.freeCollateral)
        } else {
            test(
                {
                    perp.adjustIsolatedMargin(
                        "0.1",
                        AdjustIsolatedMarginInputField.AmountPercent,
                        0,
                    )
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
        }

        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = IsolatedMarginAdjustmentType.Remove.name,
                type = AdjustIsolatedMarginInputField.Type,
                parentSubaccountNumber = 0,
            )

            val input = perp.internalState.input.adjustIsolatedMargin
            assertEquals(IsolatedMarginAdjustmentType.Remove, input.type)
            assertEquals(null, input.amount)
            assertEquals(null, input.amountPercent)
        } else {
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
        }

        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "1",
                type = AdjustIsolatedMarginInputField.AmountPercent,
                parentSubaccountNumber = 0,
            )

            val input = perp.internalState.input.adjustIsolatedMargin
            assertEquals(IsolatedMarginAdjustmentType.Remove, input.type)
            assertEquals(1.0, input.amountPercent)
            assertEquals(IsolatedMarginInputType.Percent, input.amountInput)
            assertEquals(1099.7694760448978, input.amount)

            val subaccount = perp.internalState.wallet.account.subaccounts[128]
            assertEquals(1132.0151468, subaccount?.calculated?.get(CalculationPeriod.current)?.equity)
            assertEquals(632.0151467999999, subaccount?.calculated?.get(CalculationPeriod.current)?.notionalTotal)
        } else {
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
        }

        if (perp.staticTyping) {
            perp.adjustIsolatedMargin(
                data = "0.1",
                type = AdjustIsolatedMarginInputField.AmountPercent,
                parentSubaccountNumber = 0,
            )

            val input = perp.internalState.input.adjustIsolatedMargin
            assertEquals(IsolatedMarginAdjustmentType.Remove, input.type)
            assertEquals(0.1, input.amountPercent)
            assertEquals(IsolatedMarginInputType.Percent, input.amountInput)
            assertEquals(109.9769476044898, input.amount)

            val subaccount = perp.internalState.wallet.account.subaccounts[128]
            assertEquals(1132.0151468, subaccount?.calculated?.get(CalculationPeriod.current)?.equity)
        } else {
            test(
                {
                    perp.adjustIsolatedMargin(
                        "0.1",
                        AdjustIsolatedMarginInputField.AmountPercent,
                        0,
                    )
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
}
