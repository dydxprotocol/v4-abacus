package exchange.dydx.abacus.tests.payloads

internal class LaunchIncentiveMock {
    internal val seasons = """
        {
           "data":{
              "tradingSeasons":[
                 {
                    "startTimestamp":1701177710,
                    "label":"1",
                    "__typename":"TradingSeason"
                 },
                 {
                    "startTimestamp":1704384000,
                    "label":"2",
                    "__typename":"TradingSeason"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val points = """
        {
           "incentivePoints":0.01,
           "marketMakingIncentivePoints":0
        }
    """.trimIndent()
}
