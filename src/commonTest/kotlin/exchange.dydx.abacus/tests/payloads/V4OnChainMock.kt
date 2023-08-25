package exchange.dydx.abacus.tests.payloads

internal class V4OnChainMock {
    internal val equity_tiers = """
        {
            "equityTierLimitConfig": {
                "shortTermOrderEquityTiers": [
                    {
                        "usdTncRequired": "0",
                        "limit": 0
                    },
                    {
                        "usdTncRequired": "20",
                        "limit": 1
                    },
                    {
                        "usdTncRequired": "100",
                        "limit": 5
                    },
                    {
                        "usdTncRequired": "1000",
                        "limit": 10
                    },
                    {
                        "usdTncRequired": "10000",
                        "limit": 100
                    },
                    {
                        "usdTncRequired": "100000",
                        "limit": 200
                    }
                ],
                "statefulOrderEquityTiers": [
                    {
                        "usdTncRequired": "0",
                        "limit": 0
                    },
                    {
                        "usdTncRequired": "20",
                        "limit": 1
                    },
                    {
                        "usdTncRequired": "100",
                        "limit": 5
                    },
                    {
                        "usdTncRequired": "1000",
                        "limit": 10
                    },
                    {
                        "usdTncRequired": "10000",
                        "limit": 100
                    },
                    {
                        "usdTncRequired": "100000",
                        "limit": 200
                    }
                ]
            }
        }
    """.trimIndent()

    internal val fee_tiers = """
        {
           "params":{
              "tiers":[
                 {
                    "name":"1",
                    "absoluteVolumeRequirement":"0",
                    "totalVolumeShareRequirementPpm":0,
                    "makerVolumeShareRequirementPpm":0,
                    "makerFeePpm":-110,
                    "takerFeePpm":500
                 },
                 {
                    "name":"2",
                    "absoluteVolumeRequirement":"1000000000000",
                    "totalVolumeShareRequirementPpm":0,
                    "makerVolumeShareRequirementPpm":0,
                    "makerFeePpm":-110,
                    "takerFeePpm":450
                 },
                 {
                    "name":"3",
                    "absoluteVolumeRequirement":"5000000000000",
                    "totalVolumeShareRequirementPpm":0,
                    "makerVolumeShareRequirementPpm":0,
                    "makerFeePpm":-110,
                    "takerFeePpm":400
                 },
                 {
                    "name":"4",
                    "absoluteVolumeRequirement":"25000000000000",
                    "totalVolumeShareRequirementPpm":0,
                    "makerVolumeShareRequirementPpm":0,
                    "makerFeePpm":-110,
                    "takerFeePpm":350
                 },
                 {
                    "name":"5",
                    "absoluteVolumeRequirement":"125000000000000",
                    "totalVolumeShareRequirementPpm":0,
                    "makerVolumeShareRequirementPpm":0,
                    "makerFeePpm":-110,
                    "takerFeePpm":300
                 },
                 {
                    "name":"6",
                    "absoluteVolumeRequirement":"125000000000000",
                    "totalVolumeShareRequirementPpm":5000,
                    "makerVolumeShareRequirementPpm":0,
                    "makerFeePpm":-110,
                    "takerFeePpm":250
                 },
                 {
                    "name":"7",
                    "absoluteVolumeRequirement":"125000000000000",
                    "totalVolumeShareRequirementPpm":5000,
                    "makerVolumeShareRequirementPpm":10000,
                    "makerFeePpm":-110,
                    "takerFeePpm":250
                 },
                 {
                    "name":"8",
                    "absoluteVolumeRequirement":"125000000000000",
                    "totalVolumeShareRequirementPpm":5000,
                    "makerVolumeShareRequirementPpm":20000,
                    "makerFeePpm":-110,
                    "takerFeePpm":250
                 },
                 {
                    "name":"9",
                    "absoluteVolumeRequirement":"125000000000000",
                    "totalVolumeShareRequirementPpm":5000,
                    "makerVolumeShareRequirementPpm":40000,
                    "makerFeePpm":-110,
                    "takerFeePpm":250
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