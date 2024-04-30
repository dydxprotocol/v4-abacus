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
    }

    private fun testChildSubaccountNumberInput() {
        test(
            {
                perp.adjustIsolatedMargin("128", AdjustIsolatedMarginInputField.childSubaccountNumber, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "childSubaccountNumber": "128"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testZeroAmount() {
        test(
            {
                perp.adjustIsolatedMargin("0", AdjustIsolatedMarginInputField.amount, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "amount": "0"
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

    private fun testMarginAddition() {
        test(
            {
                perp.adjustIsolatedMargin(IsolatedMarginAdjustmentType.add.rawValue, AdjustIsolatedMarginInputField.type, 0)
            },
            """
        {
            "input": {
                "current": "adjustIsolatedMargin",
                "adjustIsolatedMargin": {
                    "type": "ADD"
                }
            }
        }
            """.trimIndent(),
        )

        test(
            {
                perp.adjustIsolatedMargin("92.49", AdjustIsolatedMarginInputField.amount, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "amount": "92.49"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testMarginRemoval() {
        test(
            {
                perp.adjustIsolatedMargin(IsolatedMarginAdjustmentType.remove.rawValue, AdjustIsolatedMarginInputField.type, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "type": "REMOVE",
                        "amount": null
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
                perp.adjustIsolatedMargin("20", AdjustIsolatedMarginInputField.amount, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "type": "REMOVE",
                        "amount": "20"
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
