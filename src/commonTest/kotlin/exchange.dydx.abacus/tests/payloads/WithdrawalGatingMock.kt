package exchange.dydx.abacus.tests.payloads

@Suppress("PropertyName")
internal class WithdrawalGatingMock {
    val withdrawal_and_transfer_gating_status_data = """
        {       
            "negativeTncSubaccountSeenAtBlock" = 1
            "chainOutageSeenAtBlock" = 1
            "withdrawalsAndTransfersUnblockedAtBlock" = 13232978
        }
    """.trimIndent()

    val withdrawal_capacity_by_denom_data = """
        {
          "limiterCapacityList": [
            {
              "limiter": {
                "period": {
                  "seconds": "3600",
                  "nanos": 0
                },
                "baselineMinimum": "02e8d4a51000",
                "baselineTvlPpm": 10000
              },
              "capacity": "0204ecba67668f"
            },
            {
              "limiter": {
                "period": {
                  "seconds": "86400",
                  "nanos": 0
                },
                "baselineMinimum": "0209184e72a000",
                "baselineTvlPpm": 100000
              },
              "capacity": "02313f480a019a"
            }
          ]
        }
    """.trimIndent()

}
