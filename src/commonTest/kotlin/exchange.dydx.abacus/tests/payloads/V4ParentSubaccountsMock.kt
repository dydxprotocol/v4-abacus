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
                    "equity": "200",
                    "freeCollateral": "200",
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

    internal val subscribed_with_positions = """
        {
            "type": "subscribed",
            "connection_id": "6aa2642d-eda4-4483-9751-b70c5192b990",
            "message_id": 2,
            "channel": "v4_parent_subaccounts",
            "id": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
            "contents": {
                "subaccount": {
                    "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                    "parentSubaccountNumber": 0,
                    "equity": "592.86113348",
                    "freeCollateral": "590.874540184",
                    "childSubaccounts": [
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 0,
                            "equity": "535.435939",
                            "freeCollateral": "535.435939",
                            "openPerpetualPositions": {},
                            "assetPositions": {
                                "USDC": {
                                    "size": "535.435939",
                                    "symbol": "USDC",
                                    "side": "LONG",
                                    "assetId": "0",
                                    "subaccountNumber": 0
                                }
                            },
                            "marginEnabled": true,
                            "updatedAtHeight": "90617"
                        },
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 128,
                            "equity": "25",
                            "freeCollateral": "25",
                            "openPerpetualPositions": {},
                            "assetPositions": {
                                "USDC": {
                                    "size": "25",
                                    "symbol": "USDC",
                                    "side": "LONG",
                                    "assetId": "0",
                                    "subaccountNumber": 128
                                }
                            },
                            "marginEnabled": true,
                            "updatedAtHeight": "23992"
                        },
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 256,
                            "equity": "32.42519448",
                            "freeCollateral": "30.438601184",
                            "openPerpetualPositions": {
                                "APE-USD": {
                                    "market": "APE-USD",
                                    "status": "OPEN",
                                    "side": "LONG",
                                    "size": "8",
                                    "maxSize": "8",
                                    "entryPrice": "1.243",
                                    "exitPrice": null,
                                    "realizedPnl": "0",
                                    "unrealizedPnl": "-0.01103352",
                                    "createdAt": "2024-05-21T17:53:31.604Z",
                                    "createdAtHeight": "83211",
                                    "closedAt": null,
                                    "sumOpen": "8",
                                    "sumClose": "0",
                                    "netFunding": "0",
                                    "subaccountNumber": 256
                                }
                            },
                            "assetPositions": {
                                "USDC": {
                                    "size": "22.492228",
                                    "symbol": "USDC",
                                    "side": "LONG",
                                    "assetId": "0",
                                    "subaccountNumber": 256
                                }
                            },
                            "marginEnabled": true,
                            "updatedAtHeight": "83211"
                        },
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 384,
                            "equity": "0",
                            "freeCollateral": "0",
                            "openPerpetualPositions": {},
                            "assetPositions": {},
                            "marginEnabled": true,
                            "updatedAtHeight": "45152"
                        }
                    ]
                },
                "orders": [
                    {
                        "id": "7b781887-8a9e-5191-acfa-be58263d7cbf",
                        "subaccountId": "b342d48a-d9ce-5725-980f-eb5ef3d00951",
                        "clientId": "1996993052",
                        "clobPairId": "22",
                        "side": "BUY",
                        "size": "20",
                        "totalFilled": "0",
                        "price": "1.1",
                        "type": "LIMIT",
                        "status": "OPEN",
                        "timeInForce": "GTT",
                        "reduceOnly": false,
                        "orderFlags": "64",
                        "goodTilBlockTime": "2024-06-17T23:59:25.000Z",
                        "createdAtHeight": "24229",
                        "clientMetadata": "0",
                        "updatedAt": "2024-05-20T23:59:24.975Z",
                        "updatedAtHeight": "24229",
                        "postOnly": false,
                        "ticker": "APE-USD",
                        "subaccountNumber": 256
                    },
                    {
                        "id": "7338c86f-13c8-573e-91f5-a8d8e58f87a1",
                        "subaccountId": "b456e984-b4bc-5ad2-8662-6b8378c7e0ad",
                        "clientId": "1871509150",
                        "clobPairId": "24",
                        "side": "BUY",
                        "size": "20",
                        "totalFilled": "0",
                        "price": "1",
                        "type": "LIMIT",
                        "status": "OPEN",
                        "timeInForce": "GTT",
                        "reduceOnly": false,
                        "orderFlags": "64",
                        "goodTilBlockTime": "2024-06-17T23:55:11.000Z",
                        "createdAtHeight": "23994",
                        "clientMetadata": "0",
                        "updatedAt": "2024-05-20T23:55:11.560Z",
                        "updatedAtHeight": "23994",
                        "postOnly": false,
                        "ticker": "ARB-USD",
                        "subaccountNumber": 128
                    }
                ]
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
