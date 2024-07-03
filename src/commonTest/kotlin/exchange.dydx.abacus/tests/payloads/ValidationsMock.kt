package exchange.dydx.abacus.tests.payloads

internal class ValidationsMock {
    internal val marketsSubscribed = """
        {
          "type": "subscribed",
          "connection_id": "9b8111e7-a502-4171-af74-5bd0844b102c",
          "message_id": 1,
          "channel": "v3_markets",
          "contents": {
            "markets": {
              "BTC-USD": {
                "market": "BTC-USD",
                "status": "ONLINE",
                "stepSize": "0.0001",
                "tickSize": "1",
                "indexPrice": "20000.0000",
                "oraclePrice": "20000.0000",
                "priceChange24H": "138.180620",
                "nextFundingRate": "0.0000102743",
                "nextFundingAt": "2022-07-30T00:00:00.000Z",
                "minOrderSize": "0.001",
                "type": "PERPETUAL",
                "initialMarginFraction": "0.05",
                "maintenanceMarginFraction": "0.03",
                "volume24H": "424522782.317100",
                "trades24H": "54584",
                "openInterest": "5061.9983",
                "incrementalInitialMarginFraction": "0.01",
                "incrementalPositionSize": "5",
                "maxPositionSize": "500",
                "baselinePositionSize": "25",
                "assetResolution": "10000000000",
                "syntheticAssetId": "0x4254432d3130000000000000000000"
              },
              "ETH-USD": {
                "market": "ETH-USD",
                "status": "ONLINE",
                "stepSize": "0.001",
                "tickSize": "0.1",
                "indexPrice": "1000.0000",
                "oraclePrice": "1000.0000",
                "priceChange24H": "14.475020",
                "nextFundingRate": "-0.0000178049",
                "nextFundingAt": "2022-07-30T00:00:00.000Z",
                "minOrderSize": "0.01",
                "type": "PERPETUAL",
                "initialMarginFraction": "0.05",
                "maintenanceMarginFraction": "0.03",
                "volume24H": "774356829.130300",
                "trades24H": "86995",
                "openInterest": "81484.747",
                "incrementalInitialMarginFraction": "0.01",
                "incrementalPositionSize": "100",
                "maxPositionSize": "10000",
                "baselinePositionSize": "500",
                "assetResolution": "1000000000",
                "syntheticAssetId": "0x4554482d3900000000000000000000"
              }
            }
          }
        }
    """.trimIndent()

    internal val accountsSubscribed = """
        {
          "type": "subscribed",
          "connection_id": "c49e6d6b-d489-4140-bbaf-dc11b2fab13a",
          "message_id": 1,
          "channel": "v3_accounts",
          "id": "dace1648-c854-5aed-9879-88899bf647a3",
          "contents": {
            "orders": [
            ],
            "account": {
              "starkKey": "01de24f8468f0590b80c12c87ae9e247ef3278d4683d875296051495b2ad0100",
              "positionId": "30915",
              "equity": "205935.352966",
              "freeCollateral": "187233.155294",
              "pendingDeposits": "0.000000",
              "pendingWithdrawals": "0.000000",
              "openPositions": {
                "ETH-USD": {
                  "market": "ETH-USD",
                  "status": "OPEN",
                  "side": "LONG",
                  "size": "100.00",
                  "maxSize": "100",
                  "entryPrice": "1000.000000",
                  "exitPrice": "0.000000",
                  "unrealizedPnl": "1000.000000",
                  "realizedPnl": "-4173.521266",
                  "createdAt": "2022-06-30T01:01:10.234Z",
                  "closedAt": null,
                  "sumOpen": "100000.000000",
                  "netFunding": "-4101.337527"
                },
                "LINK-USD": {
                  "market": "LINK-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-10",
                  "maxSize": "-10",
                  "entryPrice": "20000.0",
                  "exitPrice": "0.000000",
                  "unrealizedPnl": "-9.916131",
                  "realizedPnl": "2.022104",
                  "createdAt": "2022-07-20T18:24:29.570Z",
                  "closedAt": null,
                  "sumOpen": "11",
                  "sumClose": "0",
                  "netFunding": "2.022104"
                }
              },
              "accountNumber": "0",
              "id": "dace1648-c854-5aed-9879-88899bf647a3",
              "quoteBalance": "-62697.279528",
              "createdAt": "2021-04-20T18:27:38.698Z"
            },
            "transfers": [
            ],
            "fundingPayments": [
            ]
          }
        }
    """.trimIndent()

    internal val accountsSubscribed2 = """
        {
          "type": "subscribed",
          "connection_id": "c49e6d6b-d489-4140-bbaf-dc11b2fab13a",
          "message_id": 1,
          "channel": "v4_subaccounts",
          "id": "dace1648-c854-5aed-9879-88899bf647a3",
          "contents": {
            "orders": [
            ],
            "account": {
              "starkKey": "01de24f8468f0590b80c12c87ae9e247ef3278d4683d875296051495b2ad0100",
              "positionId": "30915",
              "equity": "205935.352966",
              "freeCollateral": "187233.155294",
              "pendingDeposits": "0.000000",
              "pendingWithdrawals": "0.000000",
              "openPositions": {
                "ETH-USD": {
                  "market": "ETH-USD",
                  "status": "OPEN",
                  "side": "LONG",
                  "size": "1.0",
                  "maxSize": "1.0",
                  "entryPrice": "1000.000000",
                  "exitPrice": "0.000000",
                  "unrealizedPnl": "1000.000000",
                  "realizedPnl": "-4173.521266",
                  "createdAt": "2022-06-30T01:01:10.234Z",
                  "closedAt": null,
                  "sumOpen": "100000.000000",
                  "netFunding": "-4101.337527"
                },
                "LINK-USD": {
                  "market": "LINK-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-10",
                  "maxSize": "-10",
                  "entryPrice": "20000.0",
                  "exitPrice": "0.000000",
                  "unrealizedPnl": "-9.916131",
                  "realizedPnl": "2.022104",
                  "createdAt": "2022-07-20T18:24:29.570Z",
                  "closedAt": null,
                  "sumOpen": "11",
                  "sumClose": "0",
                  "netFunding": "2.022104"
                }
              },
              "accountNumber": "0",
              "id": "dace1648-c854-5aed-9879-88899bf647a3",
              "quoteBalance": "-62697.279528",
              "createdAt": "2021-04-20T18:27:38.698Z"
            },
            "transfers": [
            ],
            "fundingPayments": [
            ]
          }
        }
    """.trimIndent()

    internal val orderbookSubscribed = """
        {
          "type": "subscribed",
          "connection_id": "3936dbcc-fe3f-4598-ba07-fa8656c455b1",
          "message_id": 9,
          "channel": "v3_orderbook",
          "id": "ETH-USD",
          "contents": {
            "asks": [
              {
                "size": "0.1",
                "price": "1000.1"
              },
              {
                "size": "0.5",
                "price": "1000.5"
              },
              {
                "size": "5.0",
                "price": "1060.0"
              },
              {
                "size": "10.0",
                "price": "1800.0"
              }
            ],
            "bids": [
              {
                "size": "0.1",
                "price": "999.9"
              },
              {
                "size": "0.5",
                "price": "999.5"
              },
              {
                "size": "5.0",
                "price": "990.0"
              },
              {
                "size": "10.0",
                "price": "900.0"
              }
            ]
          }
        }
    """.trimIndent()
}
