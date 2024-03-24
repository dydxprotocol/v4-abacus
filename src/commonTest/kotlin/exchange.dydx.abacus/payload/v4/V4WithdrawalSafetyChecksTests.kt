package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.onChainWithdrawalCapacity
import exchange.dydx.abacus.state.model.onChainWithdrawalGating
import kollections.iListOf
import kotlin.test.Test

class V4WithdrawalSafetyChecksTests: V4BaseTests() {
    @Test
    fun testGating() {
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
                        "withdrawalsAndTransfersUnblockedAtBlock" : 8521827
                    }
                }
            }
            """.trimIndent(),
        )
    }

    @Test
    fun testCapacity() {
        test(
            {
                perp.parseOnChainWithdrawalCapacity(mock.v4WithdrawalSafetyChecksMock.withdrawal_capacity_by_denom_data_daily_less_than_weekly)
            },
            """
            {
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

        test(
            {
                perp.parseOnChainWithdrawalCapacity(mock.v4WithdrawalSafetyChecksMock.withdrawal_capacity_by_denom_data_weekly_less_than_daily)
            },
            """
            {
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
                                "capacity": "1234567890",
                                "baselineMinimum": "10000000000000",
                                "nanos": 0.0,
                                "baselineTvlPpm": 100000.0
                            }
                        ],
                        "maxWithdrawalCapacity": "1234.567890"
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