package exchange.dydx.abacus.tests.payloads

@Suppress("PropertyName")
internal class V4WithdrawalSafetyChecksMock {
    val withdrawal_and_transfer_gating_status_data = """
    {
        "negativeTncSubaccountSeenAtBlock" : 8521777,
        "chainOutageSeenAtBlock" : 8489769,
        "withdrawalsAndTransfersUnblockedAtBlock" : 16750
    }
    """.trimIndent()

    val withdrawal_capacity_by_denom_data_weekly_less_than_daily = """
    {
        "limiterCapacityList": [
            {
                "limiter": {
                    "period": {
                        "seconds": "3600",
                        "nanos": 0
                    },
                    "baselineMinimum": "1000000000000",
                    "baselineTvlPpm": 10000
                },
                "capacity": "1234567891"
            },
            {
                "limiter": {
                    "period": {
                        "seconds": "86400",
                        "nanos": 0
                    },
                    "baselineMinimum": "10000000000000",
                    "baselineTvlPpm": 100000
                },
                "capacity": "1234567890"
            }
        ]
    }
    """.trimIndent()

    val withdrawal_capacity_by_denom_data_daily_less_than_weekly = """
    {
        "limiterCapacityList": [
            {
                "limiter": {
                    "period": {
                        "seconds": "3600",
                        "nanos": 0
                    },
                    "baselineMinimum": "1000000000000",
                    "baselineTvlPpm": 10000
                },
                "capacity": "1234567891"
            },
            {
                "limiter": {
                    "period": {
                        "seconds": "86400",
                        "nanos": 0
                    },
                    "baselineMinimum": "10000000000000",
                    "baselineTvlPpm": 100000
                },
                "capacity": "1234567892"
            }
        ]
    }
    """.trimIndent()
}
