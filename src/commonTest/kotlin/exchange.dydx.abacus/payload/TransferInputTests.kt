package exchange.dydx.abacus.payload

import exchange.dydx.abacus.payload.v3.V3BaseTests
import exchange.dydx.abacus.state.modal.TransferInputField
import exchange.dydx.abacus.state.modal.transfer
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class TransferInputTests : V3BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTransferInputOnce()
    }

    private fun testTransferInputOnce() {
        var time = ServerTime.now()
        testDepositTransferInput()
        time = perp.log("Deposit", time)

        testSlowWithdrawalTransferInput()
        time = perp.log("Slow Withdrawal", time)

        testTransferOutTransferInput()
        perp.log("Transfer Out", time)


    }

    private fun testDepositTransferInput() {
        /*
        Designed workflow
        transfer("DEPOSIT", TransferInputField.type)
        transfer("1000", TransferInputField.usdcSize)
        transfer("0", TransferInputField.usdcFee)   // if fee is charged outside usdcSize
        transfer("1.3", TransferInputField.usdcFee) // if fee is deducted from usdcSize
         */

        test(
            {
                perp.transfer("DEPOSIT", TransferInputField.type)
            },
            """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "DEPOSIT"
                        }
                    }
                }
            """.trimIndent()
        )

        test(
            {
                perp.transfer("1", TransferInputField.usdcSize)
            },
            """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "DEPOSIT",
                            "size": {
                                "usdcSize": 1.0
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 206724.55,
                                        "postOrder": 206725.55
                                    },
                                    "freeCollateral": {
                                        "current": 187967.34,
                                        "postOrder": 187968.34
                                    },
                                    "quoteBalance": {
                                        "current": -62697.28,
                                        "postOrder": -62696.28
                                    },
                                    "leverage": {
                                        "current": 1.30,
                                        "postOrder": 1.30
                                    },
                                    "marginUsage": {
                                        "current": 0.0907,
                                        "postOrder": 0.0907
                                    },
                                    "buyingPower": {
                                        "current": 3759346.73,
                                        "postOrder": 3759366.73
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )


        test(
            {
                perp.transfer("5000.0", TransferInputField.usdcSize)
            },
            """
                {
                    "input": {
                        "transfer": {
                            "type": "DEPOSIT",
                            "size": {
                                "usdcSize": 5000.0
                            },
                            "summary": {
                                "usdcSize": 5000.0
                            },
                            "options": {
                                "needsSize": true,
                                "needsGasless": true
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 211724.55
                                    },
                                    "freeCollateral": {
                                        "postOrder": 192967.34
                                    },
                                    "quoteBalance": {
                                        "postOrder": -57697.28
                                    },
                                    "leverage": {
                                        "postOrder": 1.27
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0886
                                    },
                                    "buyingPower": {
                                        "postOrder": 3859346.73
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )


        /*
        size = 1000.0
         */

        test(
            {
                perp.transfer("1000.0", TransferInputField.usdcSize)
            },
            """
                {
                    "input": {
                        "transfer": {
                            "type": "DEPOSIT",
                            "size": {
                                "usdcSize": 1000.0
                            },
                            "summary": {
                                "usdcSize": 1000.0
                            },
                            "options": {
                                "needsSize": true,
                                "needsGasless": true
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 207724.55
                                    },
                                    "freeCollateral": {
                                        "postOrder": 188967.34
                                    },
                                    "quoteBalance": {
                                        "postOrder": -61697.28
                                    },
                                    "leverage": {
                                        "postOrder": 1.30
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0903
                                    },
                                    "buyingPower": {
                                        "postOrder": 3779346.73
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        test(
            {
                perp.transfer("10.0", TransferInputField.usdcFee)
            },
            """
                {
                    "input": {
                        "transfer": {
                            "type": "DEPOSIT",
                            "size": {
                                "usdcSize": 1000.0
                            },
                            "summary": {
                                "usdcSize": 1000.0,
                                "filled": true,
                                "fee": 10.0
                            },
                            "options": {
                                "needsSize": true,
                                "needsFastSpeed": false
                            },
                            "fee": 10.0
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 206724.55,
                                        "postOrder": 207714.55
                                    },
                                    "freeCollateral": {
                                        "current": 187967.34,
                                        "postOrder": 188957.34
                                    },
                                    "quoteBalance": {
                                        "current": -62697.28,
                                        "postOrder": -61707.28
                                    },
                                    "leverage": {
                                        "current": 1.30,
                                        "postOrder": 1.30
                                    },
                                    "marginUsage": {
                                        "current": 0.0907,
                                        "postOrder": 0.0903
                                    },
                                    "buyingPower": {
                                        "current": 3759346.73,
                                        "postOrder": 3779146.73
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testSlowWithdrawalTransferInput() {
        test({
            perp.transfer("WITHDRAWAL", TransferInputField.type)
        }, null)

        test({
            perp.transfer("false", TransferInputField.fastSpeed)
        }, null)

        test({
            perp.transfer("0", TransferInputField.usdcFee)
        }, null)

        /*
        size = 1000.0
         */
        test({
            perp.transfer("5000.0", TransferInputField.usdcSize)
        }, null)

        test(
            {
                perp.transfer("1000.0", TransferInputField.usdcSize)
            },
            """
                {
                    "input": {
                        "transfer": {
                            "type": "WITHDRAWAL",
                            "size": {
                                "usdcSize": 1000.0
                            },
                            "summary": {
                                "usdcSize": 1000.0,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsFastSpeed": true
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 205724.55
                                    },
                                    "freeCollateral": {
                                        "postOrder": 186967.34
                                    },
                                    "quoteBalance": {
                                        "postOrder": -63697.28
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0912
                                    },
                                    "buyingPower": {
                                        "postOrder": 3739346.73
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )


        test(
            {
                perp.transfer("10.0", TransferInputField.usdcFee)
            },
            """
                {
                    "input": {
                        "transfer": {
                            "type": "WITHDRAWAL",
                            "size": {
                                "usdcSize": 1000.0
                            },
                            "summary": {
                                "usdcSize": 1000.0,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsFastSpeed": true
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 205714.55
                                    },
                                    "freeCollateral": {
                                        "postOrder": 186957.34
                                    },
                                    "quoteBalance": {
                                        "postOrder": -63707.28
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0912
                                    },
                                    "buyingPower": {
                                        "postOrder": 3739146.73
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testTransferOutTransferInput() {
        test({
            perp.transfer("TRANSFER_OUT", TransferInputField.type)
        }, null)


        test({
            perp.transfer("0.0", TransferInputField.usdcFee)
        }, null)

        /*
        size = 1000.0
         */
        test({
            perp.transfer("5000.0", TransferInputField.usdcSize)
        }, null)

        test(
            {
                perp.transfer("1000.0", TransferInputField.usdcSize)
            },
            """
                {
                    "input": {
                        "transfer": {
                            "type": "TRANSFER_OUT",
                            "size": {
                                "usdcSize": 1000.0
                            },
                            "summary": {
                                "usdcSize": 1000.0,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 205724.55
                                    },
                                    "freeCollateral": {
                                        "postOrder": 186967.34
                                    },
                                    "quoteBalance": {
                                        "postOrder": -63697.28
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0912
                                    },
                                    "buyingPower": {
                                        "postOrder": 3739346.73
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }
}