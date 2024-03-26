package exchange.dydx.abacus.payload.v4

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

class V4WithdrawalSafetyChecksTests: V4BaseTests() {

    override fun setup() {
        perp.loadAccounts(mock)
        perp.currentBlockAndHeight = mock.heightMock.currentBlockAndHeight
        perp.transfer("5", TransferInputField.usdcSize)
    }

    @Test
    fun testGating() {
        setup()
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
        perp.currentBlockAndHeight = mock.heightMock.beforeCurrentBlockAndHeight
        test(
            {
                perp.transfer(TransferType.withdrawal.rawValue, TransferInputField.type)
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
        perp.currentBlockAndHeight = mock.heightMock.afterCurrentBlockAndHeight
        test(
            {
                perp.transfer(TransferType.transferOut.rawValue, TransferInputField.type)
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

    @Test
    fun testCapacity() {
        setup()
        perp.transfer("WITHDRAWAL", TransferInputField.type)
        perp.transfer("1235.0", TransferInputField.usdcSize)
        test(
            {
                perp.parseOnChainWithdrawalCapacity(mock.v4WithdrawalSafetyChecksMock.withdrawal_capacity_by_denom_data_daily_less_than_weekly)
            },
            """
            {
            "input": {
                "transfer": {
                    "type": "WITHDRAWAL",
                    "size": {
                        "usdcSize": 1235.0
                    }
                },
                "errors": [
                    {
                        "type": "ERROR",
                        "code": "TEST2",
                        "fields": [
                            "address"
                        ],
                        "resources": {
                            "title": {
                                "stringKey": "TEST2"
                            },
                            "text": {
                                "stringKey": "TEST2",
                                "params": null
                            },
                            "action": {
                                "stringKey": "TEST2"
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

private fun TradingStateMachine.parseOnChainWithdrawalCapacity(payload: String): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    try {
        changes = onChainWithdrawalCapacity(payload)
    } catch (e: ParsingException) {
        error = e.toParsingError()
    }
    if (changes != null) {
        update(changes)
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
        update(changes)
    }

    val errors = if (error != null) iListOf(error) else null
    return StateResponse(state, changes, errors)
}