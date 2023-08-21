package exchange.dydx.abacus.tests.payloads

internal class V4OnChainMock {
    internal val fee_tiers = """
        {
           "params":{
              "tiers":[
                 {
                    "name":"1",
                    "absoluteVolumeRequirement":{
                       "low":0,
                       "high":0,
                       "unsigned":true
                    },
                    "totalVolumeShareRequirementPpm":0,
                    "makerVolumeShareRequirementPpm":0,
                    "makerFeePpm":0,
                    "takerFeePpm":0
                 }
              ]
           }
        }
    """.trimIndent()

    internal val user_fee_tier = """
        {
           "index":0,
           "tier":{
              "name":"1",
              "absoluteVolumeRequirement":{
                 "low":0,
                 "high":0,
                 "unsigned":true
              },
              "totalVolumeShareRequirementPpm":0,
              "makerVolumeShareRequirementPpm":0,
              "makerFeePpm":0,
              "takerFeePpm":0
           }
        }
    """.trimIndent()

    internal val user_stats = """
        {
           "makerNotional": 1000000,
           "takerNotional": 1000000
        }
    """.trimIndent()

    internal val account_balances = """
       [
        {"denom":"ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5","amount":"110"},
        {"denom":"dv4tnt","amount":"1220"}
       ]
    """.trimIndent()
}