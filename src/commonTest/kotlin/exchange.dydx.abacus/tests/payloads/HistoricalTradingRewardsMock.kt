package exchange.dydx.abacus.tests.payloads

internal class HistoricalTradingRewardsMock {
    internal val weeklyCall = """
    {
        "rewards": [
          {
             "tradingRewards": "1.00",
             "startedAt": "2023-12-03T00:00:00.000Z",
             "period": "WEEKLY"
          },
          {
             "tradingRewards": "124.03",
             "startedAt": "2023-11-26T00:00:00.000Z",
             "endedAt": "2023-12-03T00:00:00.000Z",
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
             "startedAt": "2023-12-01T00:00:00.000Z",
             "period": "MONTHLY"
          },
          {
             "tradingRewards": "124.03",
             "startedAt": "2023-11-01T00:00:00.000Z",
             "endedAt": "2023-12-01T00:00:00.000Z",
             "period": "MONTHLY"
          }
        ]
      }
    """.trimIndent()

    internal val monthlySecondCall = """
    {
        "rewards": [
          {
             "tradingRewards": "100.0",
             "startedAt": "2023-09-01T00:00:00.000Z",
             "endedAt": "2023-10-01T00:00:00.000Z",
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