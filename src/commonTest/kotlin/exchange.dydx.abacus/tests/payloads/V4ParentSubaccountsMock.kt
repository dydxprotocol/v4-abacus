package exchange.dydx.abacus.tests.payloads

import kollections.JsExport
import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@JsExport
@Serializable
internal class V4ParentSubaccountsMock {
    internal val subscribed = """
        {
            "type": "subscribed",
            "connection_id": "83f3d084-f1bb-4506-8f56-96f2f2f69017",
            "message_id": 2,
            "channel": "v4_parent_subaccounts",
            "id": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
            "contents": {
                "subaccount": {
                    "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                    "parentSubaccountNumber": 0,
                    "equity": "225",
                    "freeCollateral": "225",
                    "childSubaccounts": [
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 0,
                            "equity": "50.5",
                            "freeCollateral": "50.5",
                            "openPerpetualPositions": {},
                            "assetPositions": {
                                "USDC": {
                                    "size": "50.5",
                                    "symbol": "USDC",
                                    "side": "LONG",
                                    "assetId": "0",
                                    "subaccountNumber": 0
                                }
                            },
                            "marginEnabled": true,
                            "updatedAtHeight": "17689"
                        },
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 128,
                            "equity": "149.5",
                            "freeCollateral": "149.5",
                            "openPerpetualPositions": {},
                            "assetPositions": {
                                "USDC": {
                                    "size": "149.5",
                                    "symbol": "USDC",
                                    "side": "LONG",
                                    "assetId": "0",
                                    "subaccountNumber": 128
                                }
                            },
                            "marginEnabled": true,
                            "updatedAtHeight": "17689"
                        },
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 256,
                            "equity": "25",
                            "freeCollateral": "25",
                            "openPerpetualPositions": {},
                            "assetPositions": {
                                "USDC": {
                                    "size": "25",
                                    "symbol": "USDC",
                                    "side": "LONG",
                                    "assetId": "0",
                                    "subaccountNumber": 256
                                }
                            },
                            "marginEnabled": true,
                            "updatedAtHeight": "17689"
                        }
                    ]
                },
                "orders": [
                    {
                        "id": "683bc8c4-ed8c-5c3c-86c0-4d0a2d74aa31",
                        "subaccountId": "b456e984-b4bc-5ad2-8662-6b8378c7e0ad",
                        "clientId": "6681712",
                        "clobPairId": "22",
                        "side": "BUY",
                        "size": "10",
                        "totalFilled": "0",
                        "price": "1",
                        "type": "LIMIT",
                        "status": "OPEN",
                        "timeInForce": "GTT",
                        "reduceOnly": false,
                        "orderFlags": "64",
                        "goodTilBlockTime": "2024-06-13T23:35:58.000Z",
                        "createdAtHeight": "17691",
                        "clientMetadata": "0",
                        "updatedAt": "2024-05-16T23:35:57.872Z",
                        "updatedAtHeight": "17691",
                        "postOnly": false,
                        "ticker": "APE-USD",
                        "subaccountNumber": 0
                    },
                    {
                        "id": "24b68694-d6ae-5df4-baf5-55b0716296e9",
                        "subaccountId": "b456e984-b4bc-5ad2-8662-6b8378c7e0ad",
                        "clientId": "1616027290",
                        "clobPairId": "6",
                        "side": "BUY",
                        "size": "40",
                        "totalFilled": "0",
                        "price": "0.44",
                        "type": "LIMIT",
                        "status": "OPEN",
                        "timeInForce": "GTT",
                        "reduceOnly": false,
                        "orderFlags": "64",
                        "goodTilBlockTime": "2024-06-13T20:20:15.000Z",
                        "createdAtHeight": "6603",
                        "clientMetadata": "0",
                        "updatedAt": "2024-05-16T20:20:15.399Z",
                        "updatedAtHeight": "6603",
                        "postOnly": false,
                        "ticker": "ADA-USD",
                        "subaccountNumber": 128
                    }
                ]
            }
        }
    """.trimIndent()

    internal val channel_batch_data_empty_childsubaccount = """
        {
            "type": "channel_batch_data",
            "connection_id": "57008204-8924-489c-89dc-c14397c1503e",
            "message_id": 59,
            "id": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
            "channel": "v4_parent_subaccounts",
            "version": "3.0.0",
            "contents": [
                {
                    "assetPositions": [
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 384,
                            "positionId": "a196314e-236e-54bb-9d4f-4792f5b57644",
                            "assetId": "0",
                            "symbol": "USDC",
                            "side": "LONG",
                            "size": "19.6"
                        }
                    ],
                    "blockHeight": "15494678"
                },
                {
                    "blockHeight": "15494678"
                }
            ],
            "subaccountNumber": 384
        }
    """.trimIndent()

    internal val subscribed_with_positions = """
        {
          "type": "subscribed",
          "connection_id": "3adc59d6-09cb-432e-b987-ced0da32bec9",
          "message_id": 2,
          "channel": "v4_parent_subaccounts",
          "id": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
          "contents": {
            "subaccount": {
              "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
              "parentSubaccountNumber": 0,
              "equity": "1004.026896843",
              "freeCollateral": "963.0237707731",
              "childSubaccounts": [
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 0,
                  "equity": "862.675984",
                  "freeCollateral": "862.675984",
                  "openPerpetualPositions": {},
                  "assetPositions": {
                    "USDC": {
                      "size": "862.675984",
                      "symbol": "USDC",
                      "side": "LONG",
                      "assetId": "0",
                      "subaccountNumber": 0
                    }
                  },
                  "marginEnabled": true,
                  "updatedAtHeight": "12906107"
                },
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 128,
                  "equity": "59.810279856",
                  "freeCollateral": "57.7685502848",
                  "openPerpetualPositions": {
                    "APE-USD": {
                      "market": "APE-USD",
                      "status": "OPEN",
                      "side": "LONG",
                      "size": "8",
                      "maxSize": "8",
                      "entryPrice": "1.292",
                      "exitPrice": null,
                      "realizedPnl": "0",
                      "unrealizedPnl": "-0.127352144",
                      "createdAt": "2024-05-22T20:18:18.080Z",
                      "createdAtHeight": "12905490",
                      "closedAt": null,
                      "sumOpen": "8",
                      "sumClose": "0",
                      "netFunding": "0",
                      "subaccountNumber": 128
                    }
                  },
                  "assetPositions": {
                    "USDC": {
                      "size": "49.601632",
                      "symbol": "USDC",
                      "side": "LONG",
                      "assetId": "0",
                      "subaccountNumber": 128
                    }
                  },
                  "marginEnabled": true,
                  "updatedAtHeight": "12905505"
                },
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 256,
                  "equity": "41.902552695",
                  "freeCollateral": "21.9040510255",
                  "openPerpetualPositions": {
                    "XLM-USD": {
                      "market": "XLM-USD",
                      "status": "OPEN",
                      "side": "LONG",
                      "size": "1810",
                      "maxSize": "1810",
                      "entryPrice": "0.11046795580110497238",
                      "exitPrice": null,
                      "realizedPnl": "0",
                      "unrealizedPnl": "0.0380166949999999922",
                      "createdAt": "2024-05-22T20:30:13.972Z",
                      "createdAtHeight": "12906057",
                      "closedAt": null,
                      "sumOpen": "1810",
                      "sumClose": "0",
                      "netFunding": "0",
                      "subaccountNumber": 256
                    }
                  },
                  "assetPositions": {
                    "USDC": {
                      "size": "158.082464",
                      "symbol": "USDC",
                      "side": "SHORT",
                      "assetId": "0",
                      "subaccountNumber": 256
                    }
                  },
                  "marginEnabled": true,
                  "updatedAtHeight": "12906057"
                },
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 384,
                  "equity": "39.638080292",
                  "freeCollateral": "20.6751854628",
                  "openPerpetualPositions": {
                    "ARB-USD": {
                      "market": "ARB-USD",
                      "status": "OPEN",
                      "side": "LONG",
                      "size": "166",
                      "maxSize": "166",
                      "entryPrice": "1.14298795180722891566",
                      "exitPrice": null,
                      "realizedPnl": "0",
                      "unrealizedPnl": "-0.10705170799999999956",
                      "createdAt": "2024-05-22T20:31:23.666Z",
                      "createdAtHeight": "12906110",
                      "closedAt": null,
                      "sumOpen": "166",
                      "sumClose": "0",
                      "netFunding": "0",
                      "subaccountNumber": 384
                    }
                  },
                  "assetPositions": {
                    "USDC": {
                      "size": "149.990868",
                      "symbol": "USDC",
                      "side": "SHORT",
                      "assetId": "0",
                      "subaccountNumber": 384
                    }
                  },
                  "marginEnabled": true,
                  "updatedAtHeight": "12906110"
                }
              ]
            },
            "orders": []
          }
        }
    """.trimIndent()

    internal val channel_batch_data = """
        {
          "type": "channel_batch_data",
          "connection_id": "83f3d084-f1bb-4506-8f56-96f2f2f69017",
          "message_id": 29,
          "id": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
          "channel": "v4_parent_subaccounts",
          "version": "2.4.0",
          "contents": [
            {
              "assetPositions": [
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 0,
                  "positionId": "2314ffd5-6723-54c7-ab87-7b292ae14ee1",
                  "assetId": "0",
                  "symbol": "USDC",
                  "side": "LONG",
                  "size": "46"
                }
              ]
            },
            {
              "transfers": {
                "sender": {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 0
                },
                "recipient": {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 128
                },
                "symbol": "USDC",
                "size": "4.5",
                "type": "TRANSFER_OUT",
                "createdAt": "2024-05-17T04:02:07.208Z",
                "createdAtHeight": "32866",
                "transactionHash": "72E80E605FAD46FF82C8C7AE2260519ACB73F5900A8F340ABBF190E84C2C4DFC"
              }
            }
          ],
          "subaccountNumber": 0
        }
    """.trimIndent()
}
