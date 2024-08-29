package exchange.dydx.abacus.validation

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.transfer
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TransferRequiredInputTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTransferInputOnce()
    }

    private fun testTransferInputOnce() {
        var time = ServerTime.now()
        reset()
        testTransferInputDeposit()
        time = perp.log("Deposit", time)

        reset()
        testTransferInputWithdraw()
        time = perp.log("Withdrawal", time)

        reset()
        testTransferInputTransferOut()
        time = perp.log("Transfer", time)
    }

    override fun reset() {
        super.reset()
        test({
            perp.transfer(null, null, environment = mock.v4Environment)
        }, null)
    }

    private fun testTransferInputDeposit() {
        if (perp.staticTyping) {
            perp.transfer("DEPOSIT", TransferInputField.type, environment = mock.v4Environment)

            val transfer = perp.internalState.input.transfer
            assertEquals(TransferType.deposit, transfer.type)

            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals("REQUIRED_SIZE", error?.code)
            assertEquals(ErrorType.required, error?.type)
            assertEquals("size.usdcSize", error?.fields?.first())
            assertEquals("APP.TRADE.ENTER_AMOUNT", error?.resources?.action?.stringKey)
        } else {
            test(
                {
                    perp.transfer("DEPOSIT", TransferInputField.type, environment = mock.v4Environment)
                },
                """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "DEPOSIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                    "size.usdcSize"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                    }
                                }
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)

            val transfer = perp.internalState.input.transfer
            assertEquals(TransferType.deposit, transfer.type)

            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(null, error)
        } else {
            test(
                {
                    perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)
                },
                """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "DEPOSIT"
                        },
                        "errors": null
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testTransferInputWithdraw() {
        if (perp.staticTyping) {
            perp.transfer("WITHDRAWAL", TransferInputField.type, environment = mock.v4Environment)

            val transfer = perp.internalState.input.transfer
            assertEquals(TransferType.withdrawal, transfer.type)

            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals("REQUIRED_SIZE", error?.code)
            assertEquals(ErrorType.required, error?.type)
            assertEquals("size.usdcSize", error?.fields?.first())
            assertEquals("APP.TRADE.ENTER_AMOUNT", error?.resources?.action?.stringKey)
        } else {
            test(
                {
                    perp.transfer(
                        "WITHDRAWAL",
                        TransferInputField.type,
                        environment = mock.v4Environment,
                    )
                },
                """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "WITHDRAWAL"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                    "size.usdcSize"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                    }
                                }
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)

            val transfer = perp.internalState.input.transfer
            assertEquals(TransferType.withdrawal, transfer.type)

            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals("REQUIRED_ADDRESS", error?.code)
            assertEquals(ErrorType.required, error?.type)

            perp.transfer("dydx16zfx8g4jg9vels3rsvcym490tkn5la304c57e9", TransferInputField.address, environment = mock.v4Environment)
            assertNull(perp.internalState.input.errors?.firstOrNull())
        } else {
            test(
                {
                    perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)
                },
                """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "WITHDRAWAL"
                        },
                        "errors": null
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testTransferInputTransferOut() {
        if (perp.staticTyping) {
            perp.transfer("TRANSFER_OUT", TransferInputField.type, environment = mock.v4Environment)

            val transfer = perp.internalState.input.transfer
            assertEquals(TransferType.transferOut, transfer.type)

            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals("REQUIRED_ADDRESS", error?.code)
            assertEquals(ErrorType.required, error?.type)
            assertEquals("address", error?.fields?.first())
            assertEquals("APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS", error?.resources?.action?.stringKey)
        } else {
            test(
                {
                    perp.transfer("TRANSFER_OUT", TransferInputField.type, environment = mock.v4Environment)
                },
                """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "TRANSFER_OUT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_ADDRESS",
                                "type": "REQUIRED",
                                "fields": [
                                    "address"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS"
                                    }
                                }
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)

            val transfer = perp.internalState.input.transfer
            assertEquals(TransferType.transferOut, transfer.type)

            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals("REQUIRED_ADDRESS", error?.code)
        } else {
            test(
                {
                    perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)
                },
                """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "TRANSFER_OUT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_ADDRESS",
                                "type": "REQUIRED",
                                "fields": [
                                    "address"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS"
                                    }
                                }
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.transfer("dydx1111111", TransferInputField.address, environment = mock.v4Environment)

            val transfer = perp.internalState.input.transfer
            assertEquals(TransferType.transferOut, transfer.type)

            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals("INVALID_ADDRESS", error?.code)
            assertEquals(ErrorType.error, error?.type)
            assertEquals("address", error?.fields?.first())
            assertEquals("APP.DIRECT_TRANSFER_MODAL.ADDRESS_FIELD", error?.resources?.action?.stringKey)
        } else {
            test(
                {
                    perp.transfer("dydx1111111", TransferInputField.address, environment = mock.v4Environment)
                },
                """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "TRANSFER_OUT"
                        },
                        "errors": [ {
                                "code": "INVALID_ADDRESS",
                                "type": "ERROR",
                                "fields": [
                                    "address"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.DIRECT_TRANSFER_MODAL.ADDRESS_FIELD"
                                    }
                                }
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.transfer("dydx16zfx8g4jg9vels3rsvcym490tkn5la304c57e9", TransferInputField.address, environment = mock.v4Environment)

            val transfer = perp.internalState.input.transfer
            assertEquals(TransferType.transferOut, transfer.type)

            assertNull(perp.internalState.input.errors?.firstOrNull())
        } else {
            test(
                {
                    perp.transfer("dydx16zfx8g4jg9vels3rsvcym490tkn5la304c57e9", TransferInputField.address, environment = mock.v4Environment)
                },
                """
                {
                    "input": {
                        "current": "transfer",
                        "transfer": {
                            "type": "TRANSFER_OUT"
                        },
                        "errors": null
                    }
                }
                """.trimIndent(),
            )
        }
    }
}
