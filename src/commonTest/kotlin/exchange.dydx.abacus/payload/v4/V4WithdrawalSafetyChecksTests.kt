package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.ErrorFormat
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.onChainWithdrawalCapacity
import exchange.dydx.abacus.state.model.onChainWithdrawalGating
import exchange.dydx.abacus.state.model.transfer
import exchange.dydx.abacus.tests.extensions.loadAccounts
import kollections.iListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class V4WithdrawalSafetyChecksTests : V4BaseTests() {

    override fun setup() {
        perp.loadAccounts(mock)
        perp.currentBlockAndHeight = mock.heightMock.currentBlockAndHeight
        perp.transfer(TransferType.deposit.rawValue, TransferInputField.type, environment = mock.v4Environment)
    }

    @Test
    fun testGating() {
        setup()

        if (perp.staticTyping) {
            perp.parseOnChainWithdrawalGating(mock.v4WithdrawalSafetyChecksMock.withdrawal_and_transfer_gating_status_data)
            val withdrwalGating = perp.internalState.configs.withdrawalGating
            assertEquals(16750, withdrwalGating?.withdrawalsAndTransfersUnblockedAtBlock)
        } else {
            test(
                {
                    perp.parseOnChainWithdrawalGating(mock.v4WithdrawalSafetyChecksMock.withdrawal_and_transfer_gating_status_data)
                },
                """
            {
                "configs": {
                    "withdrawalGating": {
                        "negativeTncSubaccountSeenAtBlock" : 8521777,
                        "chainOutageSeenAtBlock" : 8489769,
                        "withdrawalsAndTransfersUnblockedAtBlock" : 16750
                    }
                }
            }
                """.trimIndent(),
            )
        }

        perp.currentBlockAndHeight = mock.heightMock.beforeCurrentBlockAndHeight
        perp.transfer(TransferType.withdrawal.rawValue, TransferInputField.type, environment = mock.v4Environment)

        if (perp.staticTyping) {
            perp.transfer("1235.0", TransferInputField.usdcSize, environment = mock.v4Environment)
            val error = perp.internalState.input.errors?.firstOrNull {
                it.type == ErrorType.error
            }
            assertEquals(ErrorType.error, error?.type)
            assertEquals("", error?.code)
            assertEquals("APP.GENERAL.LEARN_MORE_ARROW", error?.linkText)
            val resources = error?.resources
            assertEquals("WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_TITLE", resources?.title?.stringKey)
            assertEquals("WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_DESCRIPTION", resources?.text?.stringKey)
            assertEquals("WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_ACTION", resources?.action?.stringKey)
            assertEquals(1, resources?.text?.params?.size)
            val param = resources?.text?.params?.get(0)
            assertEquals("1", param?.value)
            assertEquals(ErrorFormat.StringVal, param?.format)
            assertEquals("SECONDS", param?.key)
        } else {
            test(
                {
                    perp.transfer("1235.0", TransferInputField.usdcSize, environment = mock.v4Environment)
                },
                """
            {
                "configs": {
                    "withdrawalGating": {
                        "negativeTncSubaccountSeenAtBlock" : 8521777,
                        "chainOutageSeenAtBlock" : 8489769,
                        "withdrawalsAndTransfersUnblockedAtBlock" : 16750
                    }
                },
                "input": {
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "",
                            "linkText": "APP.GENERAL.LEARN_MORE_ARROW",
                            "resources": {
                                "title": {
                                    "stringKey": "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_TITLE"
                                },
                                "text": {
                                    "stringKey": "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_DESCRIPTION",
                                    "params": [
                                        {
                                            "value": 1.0,
                                            "format": "string",
                                            "key": "SECONDS"
                                        }
                                    ]
                                },
                                "action": {
                                    "stringKey": "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_ACTION"
                                }
                            }
                        }
                    ]
                }
            }
                """.trimIndent(),
            )
        }

        perp.currentBlockAndHeight = mock.heightMock.afterCurrentBlockAndHeight
        perp.transfer(TransferType.transferOut.rawValue, TransferInputField.type, environment = mock.v4Environment)

        if (perp.staticTyping) {
            perp.transfer("1235.0", TransferInputField.usdcSize, environment = mock.v4Environment)

            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(ErrorType.required, error?.type)
            assertEquals("REQUIRED_ADDRESS", error?.code)
            assertEquals("address", error?.fields?.get(0))
            assertEquals("APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS", error?.resources?.action?.stringKey)
        } else {
            test(
                {
                    perp.transfer("1235.0", TransferInputField.usdcSize, environment = mock.v4Environment)
                },
                """
            {
                "configs": {
                    "withdrawalGating": {
                        "negativeTncSubaccountSeenAtBlock" : 8521777,
                        "chainOutageSeenAtBlock" : 8489769,
                        "withdrawalsAndTransfersUnblockedAtBlock" : 16750
                    }
                },
                "input": {
                    "errors": [
                        {
                            "type": "REQUIRED",
                            "code": "REQUIRED_ADDRESS",
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
    }

    @Test
    fun testCapacity() {
        setup()

        perp.transfer("WITHDRAWAL", TransferInputField.type, environment = mock.v4Environment)
        perp.transfer("1235.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        if (perp.staticTyping) {
            perp.parseOnChainWithdrawalCapacity(mock.v4WithdrawalSafetyChecksMock.withdrawal_capacity_by_denom_data_daily_less_than_weekly)
            val errors = perp.internalState.input.errors
            val error = errors?.firstOrNull { it.type == ErrorType.error }
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "")
            assertEquals(error?.linkText, "APP.GENERAL.LEARN_MORE_ARROW")
            val resources = error?.resources
            assertEquals(
                resources?.title?.stringKey,
                "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_TITLE",
            )
            assertEquals(
                resources?.text?.stringKey,
                "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_DESCRIPTION",
            )
            assertEquals(resources?.text?.params?.size, 1)
            val param = resources?.text?.params?.get(0)
            assertEquals(param?.value, "1234.567891")
            assertEquals(param?.format, ErrorFormat.Price)
            assertEquals(param?.key, "USDC_LIMIT")

            assertEquals(
                parser.asDouble(perp.internalState.configs.withdrawalCapacity?.maxWithdrawalCapacity),
                1234.567891,
            )
            assertEquals(perp.internalState.configs.withdrawalCapacity?.capacity, "1234567891")
        } else {
            test(
                {
                    perp.parseOnChainWithdrawalCapacity(mock.v4WithdrawalSafetyChecksMock.withdrawal_capacity_by_denom_data_daily_less_than_weekly)
                },
                """
            {
                "input": {
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "",
                            "linkText": "APP.GENERAL.LEARN_MORE_ARROW",
                            "resources": {
                                "title": {
                                    "stringKey": "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_TITLE"
                                },
                                "text": {
                                    "stringKey": "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_DESCRIPTION",
                                    "params": [
                                        {
                                            "value": 1234.567891,
                                            "format": "price",
                                            "key": "USDC_LIMIT"
                                        }
                                    ]
                                },
                                "action": {
                                    "stringKey": "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_ACTION"
                                }
                            }
                        }
                    ]
                },
                "configs": {
                    "withdrawalCapacity": {
                        "limiterCapacityList": [
                            {
                                "seconds": "3600",
                                "capacity": "1234567891",
                                "baselineMinimum": "1000000000000",
                                "nanos": 0.0,
                                "baselineTvlPpm": 10000.0
                            },
                            {
                                "seconds": "86400",
                                "capacity": "1234567892",
                                "baselineMinimum": "10000000000000",
                                "nanos": 0.0,
                                "baselineTvlPpm": 100000.0
                            }
                        ],
                        "maxWithdrawalCapacity": "1234.567891"
                    }
                }
            }
                """.trimIndent(),
            )
        }
    }
}

private fun TradingStateMachine.parseOnChainWithdrawalCapacity(payload: String): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    try {
        changes = onChainWithdrawalCapacity(payload)
    } catch (e: ParsingException) {
        error = e.toParsingError()
    }
    if (changes != null) {
        updateStateChanges(changes)
    }

    val errors = if (error != null) iListOf(error) else null
    return StateResponse(state, changes, errors)
}

private fun TradingStateMachine.parseOnChainWithdrawalGating(payload: String): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    try {
        changes = onChainWithdrawalGating(payload)
    } catch (e: ParsingException) {
        error = e.toParsingError()
    }
    if (changes != null) {
        updateStateChanges(changes)
    }

    val errors = if (error != null) iListOf(error) else null
    return StateResponse(state, changes, errors)
}
