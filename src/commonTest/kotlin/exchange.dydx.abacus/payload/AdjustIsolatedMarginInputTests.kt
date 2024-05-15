package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.adjustIsolatedMargin
import kotlin.test.Test

class AdjustIsolatedMarginInputTests : V4BaseTests() {

    override fun loadSubaccounts(): StateResponse {
        return perp.socket(testWsUrl, "", 0, null)
    }

    fun loadSubaccountWithoutChildren(): StateResponse {
        return perp.socket(
            testWsUrl,
            mock.accountsChannel.v4_subscribed_for_calculation,
            0,
            null,
        )
    }

    fun loadSubaccountsWithChildren(): StateResponse {
        return perp.socket(testWsUrl, mock.parentSubaccountsChannel.subscribed, 0, null)
    }

//    @Test
//    fun testInputsWithoutChildren() {
//        setup()
//        loadSubaccountWithoutChildren()
//
//        testChildSubaccountNumberInput()
//        testMarginAddition()
//        testMarginRemoval()
//        testZeroAmount()
//    }

    @Test
    fun testInputs() {
        setup()
        loadSubaccountsWithChildren()

        testChildSubaccountNumberInput()
        testMarginAddition()
        testMarginRemoval()
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

    private fun testMarginAddition() {
        test(
            {
                perp.adjustIsolatedMargin(IsolatedMarginAdjustmentType.Add.name, AdjustIsolatedMarginInputField.Type, 0)
            },
            """
        {
            "input": {
                "current": "adjustIsolatedMargin",
                "adjustIsolatedMargin": {
                    "Type": "Add"
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
                        "Amount": "92.49"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testMarginRemoval() {
        test(
            {
                perp.adjustIsolatedMargin(IsolatedMarginAdjustmentType.Remove.name, AdjustIsolatedMarginInputField.Type, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Remove"
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
                        "Amount": "20"
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
        test({
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
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Add",
                        "AmountPercent": "0.1",
                        "Amount": "8882.656169898173"
                    }
                }
            }
            """.trimIndent(),
        )

        test({
            perp.adjustIsolatedMargin(
                IsolatedMarginAdjustmentType.Remove.name,
                AdjustIsolatedMarginInputField.Type,
                0
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

        test({
            perp.adjustIsolatedMargin("0.1", AdjustIsolatedMarginInputField.AmountPercent, 0)
        },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "Type": "Remove",
                        "AmountPercent": "0.1",
                        "Amount": "79.62439999999999"
                    }
                }
            }
            """.trimIndent(),
        )
    }
}
