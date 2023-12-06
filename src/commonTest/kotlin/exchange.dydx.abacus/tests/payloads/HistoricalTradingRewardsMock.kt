package exchange.dydx.abacus.tests.payloads

internal class HistoricalTradingRewardsMock {
    internal val weeklyCall = """
    {
        "rewards": [
          {
             "tradingRewards": "1.00",
             "startedAt": "2023-12-03T00:00:01.188Z",
             "startedAtHeight": "2725536",
             "period": "WEEKLY"
          },
          {
             "tradingRewards": "124.03",
             "startedAt": "2023-11-26T00:00:01.188Z",
             "startedAtHeight": "100000",
             "endedAt": "2023-12-02T23:59:58.888Z",
             "endedAtHeight": "2725535",
             "period": "WEEKLY"
          }
        ]
      }
    """.trimIndent()

    internal val monthlyCall = """
    {
        "rewards": [
          {
             "tradingRewards": "1.00",
             "startedAt": "2023-12-01T00:00:01.188Z",
             "startedAtHeight": "2725536",
             "period": "MONTHLY"
          },
          {
             "tradingRewards": "124.03",
             "startedAt": "2023-11-01T00:00:01.188Z",
             "startedAtHeight": "100000",
             "endedAt": "2023-11-30T23:59:58.888Z",
             "endedAtHeight": "2725535",
             "period": "MONTHLY"
          }
        ]
      }
    """.trimIndent()
    
    internal val empty = """
        {
        	"rewards": []
        }
    """.trimIndent()
}