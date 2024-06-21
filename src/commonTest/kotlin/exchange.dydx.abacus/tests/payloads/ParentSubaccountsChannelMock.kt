package exchange.dydx.abacus.tests.payloads

import kollections.JsExport
import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@JsExport
@Serializable
internal class ParentSubaccountsChannelMock {
    internal val subscribed = """
        {
           "type":"subscribed",
           "connection_id":"c5a28fa5-c257-4fb5-b68e-fe084c2768e5",
           "message_id":1,
           "channel":"v4_parent_subaccounts",
           "id":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4/0",
           "contents":{
              "subaccount":{
                 "address":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4",
                 "parentSubaccountNumber":0,
                 "equity":"100000000000.000000",
                 "freeCollateral":"100000000000.000000",
                 "childSubaccounts":[
                    {
                       "address":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4",
                       "subaccountNumber":0,
                       "equity":"100000000000.000000",
                       "freeCollateral":"100000000000.000000",
                       "openPerpetualPositions":{
                          "BTC-USD":{
                             "market":"BTC-USD",
                             "status":"OPEN",
                             "side":"SHORT",
                             "size":"-0.442371112",
                             "maxSize":"0.442388027",
                             "entryPrice":"33000.2",
                             "exitPrice":null,
                             "realizedPnl":"0.000000",
                             "unrealizedPnl":"0.000000",
                             "createdAt":"2022-12-02T16:14:15.883Z",
                             "createdAtHeight":"862",
                             "closedAt":null,
                             "sumOpen":"0.442388027",
                             "sumClose":"0",
                             "netFunding":"0"
                          }
                       },
                       "assetPositions":{
                          "USDC":{
                             "symbol":"USDC",
                             "side":"LONG",
                             "size":"100000",
                             "assetId":"0"
                          }
                       },
                       "marginEnabled":true
                    },
                    {
                       "address":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4",
                       "subaccountNumber":128,
                       "equity":"500.000000",
                       "freeCollateral":"500.000000",
                       "openPerpetualPositions":{
                          "RUNE-USD":{
                             "market":"RUNE-USD",
                             "status":"OPEN",
                             "side":"LONG",
                             "size":"120",
                             "maxSize":"12",
                             "entryPrice":"1.464000",
                             "exitPrice":"null",
                             "unrealizedPnl":"0.729203",
                             "realizedPnl":"0.271316",
                             "createdAt":"2022-07-18T20:36:17.165Z",
                             "closedAt":null,
                             "sumOpen":"12",
                             "sumClose":"0",
                             "netFunding":"0.271316"
                          }
                       },
                       "assetPositions":{
                          "USDC":{
                             "symbol":"USDC",
                             "side":"LONG",
                             "size":"500",
                             "assetId":"0"
                          }
                       },
                       "marginEnabled":true
                    },
                    {
                       "address":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4",
                       "subaccountNumber":256,
                       "equity":"500.000000",
                       "freeCollateral":"500.000000",
                       "assetPositions":{
                          "USDC":{
                             "symbol":"USDC",
                             "side":"LONG",
                             "size":"500",
                             "assetId":"0"
                          }
                       },
                       "marginEnabled":true
                    }
                 ]
              },
              "orders":[
                 {
                    "id":"b812bea8-29d3-5841-9549-caa072f6f8a9",
                    "market":"RUNE-USD",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "subaccountNumber":128,
                    "clientId":"194126269",
                    "clobPairId":"134",
                    "orderFlags": "0",
                    "side":"SELL",
                    "size":"1.653451",
                    "totalFilled":"0.682633",
                    "price":"1255.927",
                    "type":"LIMIT",
                    "status":"BEST_EFFORT_CANCELED",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"5837"
                 },
                 {
                    "id":"b812bea8-29d3-5841-0549-caa072f6f8b9",
                    "market":"APE-USD",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "subaccountNumber":256,
                    "clientId":"194126270",
                    "clobPairId":"134",
                    "orderFlags": "0",
                    "side":"BUY",
                    "size":"0.01",
                    "totalFilled":"0.682633",
                    "price":"1255.927",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"5837"
                 },
                 {
                    "id":"b812bea8-29d3-5841-1549-caa072f6f8b9",
                    "market":"ETH-USD",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "subaccountNumber":0,
                    "clientId":"194126270",
                    "clobPairId":"134",
                    "orderFlags": "0",
                    "side":"SELL",
                    "size":"1.653451",
                    "totalFilled":"0.682633",
                    "price":"6255.927",
                    "type":"LIMIT",
                    "status":"BEST_EFFORT_CANCELED",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"5837"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val channel_data = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":120,
           "id":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4/1",
           "channel":"v4_parent_subaccounts",
           "subaccountNumber":129,
           "contents":{
              "subaccounts":{
                 "address":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4",
                 "subaccountNumber":129
              },
              "perpetualPositions":[
                 {
                    "address":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4",
                    "subaccountNumber":129,
                    "positionId":"1bb14a35-db8b-57c0-a39c-dc6b80b995e0",
                    "market":"RUNE-USD",
                    "side":"LONG",
                    "status":"OPEN",
                    "size":"300",
                    "maxSize":"300"
                 }
              ],
              "assetPositions":[
                 {
                    "address":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4",
                    "subaccountNumber":129,
                    "positionId":"24a26508-9d45-5b4c-a13b-31f6e9780ecc",
                    "assetId":"0",
                    "denom":"USDC",
                    "side":"LONG",
                    "size":"9000"
                 }
              ],
              "orders":[
                 {
                    "id":"b812bea8-29d3-5841-9549-caa072f6f8a8",
                    "market":"RUNE-USD",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "subaccountNumber":257,
                    "clientId":"94126268",
                    "clobPairId":"1",
                    "orderFlags": "0",
                    "side":"SELL",
                    "size":"1.653451",
                    "totalFilled":"0.682633",
                    "price":"1255.927",
                    "type":"LIMIT",
                    "status":"BEST_EFFORT_OPENED",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"5837"
                 },
                 {
                    "id":"b812bea8-29d3-5841-9549-caa072f6f8a9",
                    "market":"RUNE-USD",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "subaccountNumber":513,
                    "clientId":"94126269",
                    "clobPairId":"134",
                    "orderFlags": "0",
                    "side":"SELL",
                    "size":"1.653451",
                    "totalFilled":"0.682633",
                    "price":"1255.927",
                    "type":"LIMIT",
                    "status":"BEST_EFFORT_CANCELED",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"5837"
                 }
              ],
              "fills":[
                {
                   "id":"180c2462-eb3b-5985-a702-32c503462a37",
                   "subaccountNumber": 129,
                   "side":"BUY",
                   "size":"0.01",
                   "type":"LIMIT",
                   "price":"1878",
                   "eventId":"0001781f0000000200000011",
                   "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                   "createdAt":"2023-07-07T17:20:10.369Z",
                   "liquidity":"TAKER",
                   "clobPairId":"1",
                   "quoteAmount":"18.78",
                   "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                   "createdAtHeight":"96287",
                   "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                   "ticker":"ETH-USD"
                }
             ],
             "transfers": {
                "id":"89586775-0646-582e-9b36-4f131715644d",
                "sender": {
                    "address":"dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                    "subaccountNumber":129
                },
                "recipient":{
                    "address":"dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8"
                 },
                "size":"419.98472",
                "createdAt":"2023-08-21T21:37:53.373Z",
                "createdAtHeight":"404014",
                "symbol":"USDC",
                "type":"WITHDRAWAL",
                "transactionHash": "MOCKHASH1"
            }
           }
        }
    """.trimIndent()

    internal val channel_data_with_fill_only = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":120,
           "id":"dydx199tqg4wdlnu4qjlxchpd7seg454937hjrknju4/1",
           "channel":"v4_parent_subaccounts",
           "subaccountNumber":1,
           "contents":{
              "fills":[
                {
                   "id":"180c2462-eb3b-5985-a702-32c503462a37",
                   "subaccountNumber": 1,
                   "side":"BUY",
                   "size":"0.01",
                   "type":"LIMIT",
                   "price":"1878",
                   "eventId":"0001781f0000000200000011",
                   "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                   "createdAt":"2023-07-07T17:11:10.369Z",
                   "liquidity":"TAKER",
                   "clobPairId":"1",
                   "quoteAmount":"18.78",
                   "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                   "createdAtHeight":"96287",
                   "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                   "ticker":"ETH-USD"
                }
             ],
             "transfers": {
                "id":"89586775-0646-582e-9b36-4f133445644d",
                "sender": {
                    "address":"dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                    "subaccountNumber":1
                },
                "recipient":{
                    "address":"dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8"
                 },
                "size":"419.98472",
                "createdAt":"2023-08-22T21:37:53.373Z",
                "createdAtHeight":"404014",
                "symbol":"USDC",
                "type":"WITHDRAWAL",
                "transactionHash": "MOCKHASH2"
            }
           }
        }
    """.trimIndent()

    internal val rest_response = """
        {
           "subaccounts":[
              {
                 "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                 "subaccountNumber":0,
                 "equity":"1711.959192",
                 "freeCollateral":"1711.959192",
                 "openPerpetualPositions":{
                    
                 },
                 "assetPositions":{
                    "USDC":{
                       "size":"1711.959192",
                       "symbol":"USDC",
                       "side":"LONG",
                       "assetId":"0",
                       "subaccountNumber":0
                    }
                 },
                 "marginEnabled":true
              },
              {
                 "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                 "subaccountNumber":128,
                 "equity":"285.689378002",
                 "freeCollateral":"282.1297138016",
                 "openPerpetualPositions":{
                    "LDO-USD":{
                       "market":"LDO-USD",
                       "status":"OPEN",
                       "side":"LONG",
                       "size":"11",
                       "maxSize":"11",
                       "entryPrice":"1.7",
                       "exitPrice":null,
                       "realizedPnl":"0",
                       "unrealizedPnl":"-0.901678998",
                       "createdAt":"2024-05-14T18:03:47.615Z",
                       "createdAtHeight":"304483",
                       "closedAt":null,
                       "sumOpen":"11",
                       "sumClose":"0",
                       "netFunding":"0",
                       "subaccountNumber":128
                    }
                 },
                 "assetPositions":{
                    "USDC":{
                       "size":"267.891057",
                       "symbol":"USDC",
                       "side":"LONG",
                       "assetId":"0",
                       "subaccountNumber":128
                    }
                 },
                 "marginEnabled":true
              }
           ]
        }
    """.trimIndent()

    internal val real_subscribed = """
        {
           "type":"subscribed",
           "connection_id":"51b652ba-3264-4c46-a410-3439c8ea4020",
           "message_id":2,
           "channel":"v4_parent_subaccounts",
           "id":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
           "contents":{
              "subaccount":{
                 "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                 "parentSubaccountNumber":0,
                 "equity":"1997.648570002",
                 "freeCollateral":"1994.0889058016",
                 "childSubaccounts":[
                    {
                       "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                       "subaccountNumber":0,
                       "equity":"1711.959192",
                       "freeCollateral":"1711.959192",
                       "openPerpetualPositions":{
                          
                       },
                       "assetPositions":{
                          "USDC":{
                             "size":"1711.959192",
                             "symbol":"USDC",
                             "side":"LONG",
                             "assetId":"0",
                             "subaccountNumber":0
                          }
                       },
                       "marginEnabled":true
                    },
                    {
                       "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                       "subaccountNumber":128,
                       "equity":"285.689378002",
                       "freeCollateral":"282.1297138016",
                       "openPerpetualPositions":{
                          "LDO-USD":{
                             "market":"LDO-USD",
                             "status":"OPEN",
                             "side":"LONG",
                             "size":"11",
                             "maxSize":"11",
                             "entryPrice":"1.7",
                             "exitPrice":null,
                             "realizedPnl":"0",
                             "unrealizedPnl":"-0.901678998",
                             "createdAt":"2024-05-14T18:03:47.615Z",
                             "createdAtHeight":"304483",
                             "closedAt":null,
                             "sumOpen":"11",
                             "sumClose":"0",
                             "netFunding":"0",
                             "subaccountNumber":128
                          }
                       },
                       "assetPositions":{
                          "USDC":{
                             "size":"267.891057",
                             "symbol":"USDC",
                             "side":"LONG",
                             "assetId":"0",
                             "subaccountNumber":128
                          }
                       },
                       "marginEnabled":true
                    }
                 ]
              },
              "orders":[
                 {
                    "id":"d1deed71-d743-5528-aff2-cf3daf8b6413",
                    "subaccountId":"b456e984-b4bc-5ad2-8662-6b8378c7e0ad",
                    "clientId":"1840243499",
                    "clobPairId":"26",
                    "side":"BUY",
                    "size":"6",
                    "totalFilled":"0",
                    "price":"1.5",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"GTT",
                    "reduceOnly":false,
                    "orderFlags":"64",
                    "goodTilBlockTime":"2024-06-11T18:11:11.000Z",
                    "createdAtHeight":"304912",
                    "clientMetadata":"0",
                    "updatedAt":"2024-05-14T18:11:10.494Z",
                    "updatedAtHeight":"304912",
                    "postOnly":false,
                    "ticker":"LDO-USD",
                    "subaccountNumber":128
                 }
              ]
           }
        }
    """.trimIndent()

    /**
     * Mock subscribed message to test dealing with a Parent subaccount that has a gap in populated childSubaccounts
     * i.e. subaccount 128 has had their positions closed and equity returned to the parent subaccount
     */
    internal val real_subscribed_with_unpopulated_child = """
        {
           "type":"subscribed",
           "connection_id":"51b652ba-3264-4c46-a410-3439c8ea4020",
           "message_id":2,
           "channel":"v4_parent_subaccounts",
           "id":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
           "contents":{
              "subaccount":{
                 "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                 "parentSubaccountNumber":0,
                 "equity":"1997.648570002",
                 "freeCollateral":"1994.0889058016",
                 "childSubaccounts":[
                    {
                       "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                       "subaccountNumber":0,
                       "equity":"1711.959192",
                       "freeCollateral":"1711.959192",
                       "openPerpetualPositions":{
                       },
                       "assetPositions":{
                          "USDC":{
                             "size":"1711.959192",
                             "symbol":"USDC",
                             "side":"LONG",
                             "assetId":"0",
                             "subaccountNumber":0
                          }
                       },
                       "marginEnabled":true
                    },
                    
                    {
                       "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                       "subaccountNumber":128,
                       "equity":"0",
                       "freeCollateral":"0",
                       "openPerpetualPositions":{
                       },
                       "assetPositions":{
                          "USDC":{
                             "size":"0",
                             "symbol":"USDC",
                             "side":"LONG",
                             "assetId":"0",
                             "subaccountNumber":0
                          }
                       },
                       "marginEnabled":true
                    },
                    {
                       "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                       "subaccountNumber":256,
                       "equity":"285.689378002",
                       "freeCollateral":"282.1297138016",
                       "assetPositions":{
                          "USDC":{
                             "size":"267.891057",
                             "symbol":"USDC",
                             "side":"LONG",
                             "assetId":"0",
                             "subaccountNumber":256
                          }
                       },
                       "marginEnabled":true
                    }
                 ]
              },
              "orders":[
                 {
                    "id":"d1deed71-d743-5528-aff2-cf3daf8b6413",
                    "subaccountId":"b456e984-b4bc-5ad2-8662-6b8378c7e0ad",
                    "clientId":"1840243499",
                    "clobPairId":"26",
                    "side":"BUY",
                    "size":"6",
                    "totalFilled":"0",
                    "price":"1.5",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"GTT",
                    "reduceOnly":false,
                    "orderFlags":"64",
                    "goodTilBlockTime":"2024-06-11T18:11:11.000Z",
                    "createdAtHeight":"304912",
                    "clientMetadata":"0",
                    "updatedAt":"2024-05-14T18:11:10.494Z",
                    "updatedAtHeight":"304912",
                    "postOnly":false,
                    "ticker":"ARB-USD",
                    "subaccountNumber":256
                 }
              ]
           }
        }
    """.trimIndent()

    internal val real_subscribed_with_pending = """
        {
           "type":"subscribed",
           "connection_id":"51b652ba-3264-4c46-a410-3439c8ea4020",
           "message_id":2,
           "channel":"v4_parent_subaccounts",
           "id":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
           "contents":{
              "subaccount":{
                 "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                 "parentSubaccountNumber":0,
                 "equity":"1997.648570002",
                 "freeCollateral":"1994.0889058016",
                 "childSubaccounts":[
                    {
                       "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                       "subaccountNumber":0,
                       "equity":"1711.959192",
                       "freeCollateral":"1711.959192",
                       "openPerpetualPositions":{
                       },
                       "assetPositions":{
                          "USDC":{
                             "size":"1711.959192",
                             "symbol":"USDC",
                             "side":"LONG",
                             "assetId":"0",
                             "subaccountNumber":0
                          }
                       },
                       "marginEnabled":true
                    },
                    {
                       "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                       "subaccountNumber":128,
                       "equity":"285.689378002",
                       "freeCollateral":"282.1297138016",
                       "openPerpetualPositions":{
                          "LDO-USD":{
                             "market":"LDO-USD",
                             "status":"OPEN",
                             "side":"LONG",
                             "size":"11",
                             "maxSize":"11",
                             "entryPrice":"1.7",
                             "exitPrice":null,
                             "realizedPnl":"0",
                             "unrealizedPnl":"-0.901678998",
                             "createdAt":"2024-05-14T18:03:47.615Z",
                             "createdAtHeight":"304483",
                             "closedAt":null,
                             "sumOpen":"11",
                             "sumClose":"0",
                             "netFunding":"0",
                             "subaccountNumber":128
                          }
                       },
                       "assetPositions":{
                          "USDC":{
                             "size":"267.891057",
                             "symbol":"USDC",
                             "side":"LONG",
                             "assetId":"0",
                             "subaccountNumber":128
                          }
                       },
                       "marginEnabled":true
                    },
                    {
                       "address":"dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                       "subaccountNumber":256,
                       "equity":"20",
                       "freeCollateral":"20",
                       "openPerpetualPositions":{
                       },
                       "assetPositions":{
                          "USDC":{
                             "size":"20",
                             "symbol":"USDC",
                             "side":"LONG",
                             "assetId":"0",
                             "subaccountNumber":0
                          }
                       },
                       "marginEnabled":true
                    }
                 ]
              },
              "orders":[
                 {
                    "id":"d1deed71-d743-5528-aff2-cf3daf8b6413",
                    "subaccountId":"b456e984-b4bc-5ad2-8662-6b8378c7e0ad",
                    "clientId":"1840243499",
                    "clobPairId":"26",
                    "side":"BUY",
                    "size":"6",
                    "totalFilled":"0",
                    "price":"1.5",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"GTT",
                    "reduceOnly":false,
                    "orderFlags":"64",
                    "goodTilBlockTime":"2024-06-11T18:11:11.000Z",
                    "createdAtHeight":"304912",
                    "clientMetadata":"0",
                    "updatedAt":"2024-05-14T18:11:10.494Z",
                    "updatedAtHeight":"304912",
                    "postOnly":false,
                    "ticker":"ARB-USD",
                    "subaccountNumber":256
                 }
              ]
           }
        }
    """.trimIndent()

    internal val real_subscribe_with_multiple_pending = """
        {
          "type": "subscribed",
          "connection_id": "12fb075c-885d-4095-8810-ab3bfd41cfd1",
          "message_id": 2,
          "channel": "v4_parent_subaccounts",
          "id": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
          "contents": {
            "subaccount": {
              "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
              "parentSubaccountNumber": 0,
              "equity": "1109.619011799",
              "freeCollateral": "1107.3573440191",
              "childSubaccounts": [
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 0,
                  "equity": "1010.081475655",
                  "freeCollateral": "1009.5504494895",
                  "openPerpetualPositions": {
                    "TIA-USD": {
                      "market": "TIA-USD",
                      "status": "OPEN",
                      "side": "LONG",
                      "size": "0.5",
                      "maxSize": "0.5",
                      "entryPrice": "10.51",
                      "exitPrice": null,
                      "realizedPnl": "0",
                      "unrealizedPnl": "0.055261655",
                      "createdAt": "2024-06-07T06:38:33.129Z",
                      "createdAtHeight": "13959584",
                      "closedAt": null,
                      "sumOpen": "0.5",
                      "sumClose": "0",
                      "netFunding": "0",
                      "subaccountNumber": 0
                    }
                  },
                  "assetPositions": {
                    "USDC": {
                      "size": "1004.771214",
                      "symbol": "USDC",
                      "side": "LONG",
                      "assetId": "0",
                      "subaccountNumber": 0
                    }
                  },
                  "marginEnabled": true
                },
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 128,
                  "equity": "19.537536144",
                  "freeCollateral": "17.8068945296",
                  "openPerpetualPositions": {
                    "ARB-USD": {
                      "market": "ARB-USD",
                      "status": "OPEN",
                      "side": "LONG",
                      "size": "16",
                      "maxSize": "16",
                      "entryPrice": "1.11",
                      "exitPrice": null,
                      "realizedPnl": "0",
                      "unrealizedPnl": "-0.453583856",
                      "createdAt": "2024-06-06T15:50:59.007Z",
                      "createdAtHeight": "13917102",
                      "closedAt": null,
                      "sumOpen": "16",
                      "sumClose": "0",
                      "netFunding": "0",
                      "subaccountNumber": 128
                    }
                  },
                  "assetPositions": {
                    "USDC": {
                      "size": "2.23112",
                      "symbol": "USDC",
                      "side": "LONG",
                      "assetId": "0",
                      "subaccountNumber": 128
                    }
                  },
                  "marginEnabled": true
                },
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 256,
                  "equity": "60",
                  "freeCollateral": "60",
                  "openPerpetualPositions": {},
                  "assetPositions": {
                    "USDC": {
                      "size": "60",
                      "symbol": "USDC",
                      "side": "LONG",
                      "assetId": "0",
                      "subaccountNumber": 256
                    }
                  },
                  "marginEnabled": true
                },
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 384,
                  "equity": "20",
                  "freeCollateral": "20",
                  "openPerpetualPositions": {},
                  "assetPositions": {
                    "USDC": {
                      "size": "20",
                      "symbol": "USDC",
                      "side": "LONG",
                      "assetId": "0",
                      "subaccountNumber": 384
                    }
                  },
                  "marginEnabled": true
                },
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 512,
                  "equity": "0",
                  "freeCollateral": "0",
                  "openPerpetualPositions": {},
                  "assetPositions": {},
                  "marginEnabled": true
                },
                {
                  "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                  "subaccountNumber": 640,
                  "equity": "0",
                  "freeCollateral": "0",
                  "openPerpetualPositions": {},
                  "assetPositions": {},
                  "marginEnabled": true
                }
              ]
            },
            "orders": [
              {
                "id": "89d1fe83-5b0d-5c3e-aaf5-42d1b2537837",
                "subaccountId": "b342d48a-d9ce-5725-980f-eb5ef3d00951",
                "clientId": "1537457515",
                "clobPairId": "18",
                "side": "SELL",
                "size": "10",
                "totalFilled": "0",
                "price": "2",
                "type": "LIMIT",
                "status": "OPEN",
                "timeInForce": "GTT",
                "reduceOnly": false,
                "orderFlags": "64",
                "goodTilBlockTime": "2024-07-05T06:46:53.000Z",
                "createdAtHeight": "13959987",
                "clientMetadata": "0",
                "updatedAt": "2024-06-07T06:46:52.986Z",
                "updatedAtHeight": "13959987",
                "postOnly": false,
                "ticker": "XLM-USD",
                "subaccountNumber": 256
              },
              {
                "id": "413db126-d62e-50f5-8d88-c7440ab975a3",
                "subaccountId": "b342d48a-d9ce-5725-980f-eb5ef3d00951",
                "clientId": "124525944",
                "clobPairId": "18",
                "side": "SELL",
                "size": "30",
                "totalFilled": "0",
                "price": "2",
                "type": "LIMIT",
                "status": "OPEN",
                "timeInForce": "GTT",
                "reduceOnly": false,
                "orderFlags": "64",
                "goodTilBlockTime": "2024-07-05T06:43:48.000Z",
                "createdAtHeight": "13959838",
                "clientMetadata": "0",
                "updatedAt": "2024-06-07T06:43:48.624Z",
                "updatedAtHeight": "13959838",
                "postOnly": false,
                "ticker": "XLM-USD",
                "subaccountNumber": 256
              },
              {
                "id": "a6a43472-ea2f-57d2-89d1-563babc1c489",
                "subaccountId": "b342d48a-d9ce-5725-980f-eb5ef3d00951",
                "clientId": "1234966169",
                "clobPairId": "18",
                "side": "SELL",
                "size": "40",
                "totalFilled": "0",
                "price": "1",
                "type": "LIMIT",
                "status": "OPEN",
                "timeInForce": "GTT",
                "reduceOnly": false,
                "orderFlags": "64",
                "goodTilBlockTime": "2024-07-05T06:42:18.000Z",
                "createdAtHeight": "13959763",
                "clientMetadata": "0",
                "updatedAt": "2024-06-07T06:42:17.886Z",
                "updatedAtHeight": "13959763",
                "postOnly": false,
                "ticker": "XLM-USD",
                "subaccountNumber": 256
              },
              {
                "id": "d65b720d-fd8f-5897-aa6a-343960d64b31",
                "subaccountId": "2391dd63-6759-5ff8-adb2-d2a5022e0728",
                "clientId": "386114797",
                "clobPairId": "33",
                "side": "BUY",
                "size": "4",
                "totalFilled": "0",
                "price": "5",
                "type": "LIMIT",
                "status": "OPEN",
                "timeInForce": "GTT",
                "reduceOnly": false,
                "orderFlags": "64",
                "goodTilBlockTime": "2024-07-05T06:03:51.000Z",
                "createdAtHeight": "13957923",
                "clientMetadata": "0",
                "updatedAt": "2024-06-07T06:03:51.710Z",
                "updatedAtHeight": "13957923",
                "postOnly": false,
                "ticker": "TIA-USD",
                "subaccountNumber": 0
              },
              {
                "id": "5f7ad499-1d48-5ab1-acfd-d4664e07e7e3",
                "subaccountId": "4f35148c-d290-52a4-a394-41b4d853eab0",
                "clientId": "1345093492",
                "clobPairId": "1",
                "side": "SELL",
                "size": "0.01",
                "totalFilled": "0",
                "price": "4000",
                "type": "LIMIT",
                "status": "OPEN",
                "timeInForce": "GTT",
                "reduceOnly": false,
                "orderFlags": "64",
                "goodTilBlockTime": "2024-07-05T04:53:48.000Z",
                "createdAtHeight": "13954608",
                "clientMetadata": "0",
                "updatedAt": "2024-06-07T04:53:47.890Z",
                "updatedAtHeight": "13954608",
                "postOnly": false,
                "ticker": "ETH-USD",
                "subaccountNumber": 384
              }
            ],
            "blockHeight": "13989984"
          }
        }
    """.trimIndent()

    internal val read_subscribe_with_isolated_position = """
        {
          "type": "subscribed",
          "connection_id": "54dfe8a3-db43-4d07-b37f-66b37d00296c",
          "message_id": 2,
          "channel": "v4_parent_subaccounts",
          "id": "dydx1jd8uuuwcfr2xg6ek0g3mes2kppzm54qd65npwe/0",
          "contents": {
            "subaccount": {
              "address": "dydx1jd8uuuwcfr2xg6ek0g3mes2kppzm54qd65npwe",
              "parentSubaccountNumber": 0,
              "equity": "155.66683808",
              "freeCollateral": "151.957356664",
              "childSubaccounts": [
                {
                  "address": "dydx1jd8uuuwcfr2xg6ek0g3mes2kppzm54qd65npwe",
                  "subaccountNumber": 0,
                  "equity": "137.128721",
                  "freeCollateral": "137.128721",
                  "openPerpetualPositions": {},
                  "assetPositions": {
                    "USDC": {
                      "size": "137.128721",
                      "symbol": "USDC",
                      "side": "LONG",
                      "assetId": "0",
                      "subaccountNumber": 0
                    }
                  },
                  "marginEnabled": true
                },
                {
                  "address": "dydx1jd8uuuwcfr2xg6ek0g3mes2kppzm54qd65npwe",
                  "subaccountNumber": 128,
                  "equity": "18.53811708",
                  "freeCollateral": "14.828635664",
                  "openPerpetualPositions": {
                    "APE-USD": {
                      "market": "APE-USD",
                      "status": "OPEN",
                      "side": "LONG",
                      "size": "20",
                      "maxSize": "20",
                      "entryPrice": "0.929",
                      "exitPrice": null,
                      "realizedPnl": "0",
                      "unrealizedPnl": "-0.03259292",
                      "createdAt": "2024-06-21T19:30:50.890Z",
                      "createdAtHeight": "18641960",
                      "closedAt": null,
                      "sumOpen": "20",
                      "sumClose": "0",
                      "netFunding": "0",
                      "subaccountNumber": 128
                    }
                  },
                  "assetPositions": {
                    "USDC": {
                      "size": "0.00929",
                      "symbol": "USDC",
                      "side": "SHORT",
                      "assetId": "0",
                      "subaccountNumber": 128
                    }
                  },
                  "marginEnabled": true
                }
              ]
            },
            "orders": [],
            "blockHeight": "18642087"
          }
        }
    """.trimIndent()

    internal val real_channel_batch_data = """
        {
            "type": "channel_batch_data",
            "connection_id": "9ef7b25a-1c25-48f6-93e9-b4664232aa21",
            "message_id": 14,
            "id": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5/0",
            "channel": "v4_parent_subaccounts",
            "version": "2.4.0",
            "contents": [
                {
                    "perpetualPositions": [
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 128,
                            "positionId": "0bd60156-d281-501b-9c2c-8ea3b27c260a",
                            "market": "LDO-USD",
                            "side": "LONG",
                            "status": "OPEN",
                            "size": "17",
                            "maxSize": "17",
                            "netFunding": "0",
                            "entryPrice": "0",
                            "exitPrice": null,
                            "sumOpen": "0",
                            "sumClose": "0",
                            "realizedPnl": "0",
                            "unrealizedPnl": "17.798321002"
                        }
                    ],
                    "assetPositions": [
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 128,
                            "positionId": "443f239c-51c8-5cbb-b3b8-5d86718b88c2",
                            "assetId": "0",
                            "symbol": "USDC",
                            "side": "LONG",
                            "size": "367.891057"
                        }
                    ]
                },
                {
                    "fills": [
                        {
                            "id": "9051d9e5-b9b9-5431-9f8c-dd96e86760cf",
                            "fee": "-0.002057",
                            "side": "BUY",
                            "size": "11",
                            "type": "LIMIT",
                            "price": "1.7",
                            "eventId": "0004a5630000000200000002",
                            "orderId": "3a098370-6fa9-5791-b921-fdf4174978c9",
                            "createdAt": "2024-05-14T18:03:47.615Z",
                            "liquidity": "MAKER",
                            "clobPairId": "26",
                            "quoteAmount": "18.7",
                            "subaccountId": "b456e984-b4bc-5ad2-8662-6b8378c7e0ad",
                            "clientMetadata": "0",
                            "createdAtHeight": "304483",
                            "transactionHash": "33F24BCE8CFDAF1CD8DAC0ED2C2A749929DC03B2B10590595A652DDC60F906DD",
                            "ticker": "LDO-USD"
                        }
                    ],
                    "perpetualPositions": [
                        {
                            "address": "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5",
                            "subaccountNumber": 128,
                            "positionId": "0bd60156-d281-501b-9c2c-8ea3b27c260a",
                            "market": "LDO-USD",
                            "side": "LONG",
                            "status": "OPEN",
                            "size": "17",
                            "maxSize": "17",
                            "netFunding": "0",
                            "entryPrice": "1.7",
                            "exitPrice": null,
                            "sumOpen": "11",
                            "sumClose": "0"
                        }
                    ],
                    "orders": [
                        {
                            "id": "3a098370-6fa9-5791-b921-fdf4174978c9",
                            "side": "BUY",
                            "size": "11",
                            "type": "LIMIT",
                            "price": "1.7",
                            "status": "FILLED",
                            "clientId": "326692184",
                            "updatedAt": "2024-05-14T18:03:47.615Z",
                            "clobPairId": "26",
                            "orderFlags": "64",
                            "reduceOnly": false,
                            "timeInForce": "GTT",
                            "totalFilled": "11",
                            "goodTilBlock": null,
                            "subaccountId": "b456e984-b4bc-5ad2-8662-6b8378c7e0ad",
                            "triggerPrice": null,
                            "clientMetadata": "0",
                            "createdAtHeight": "303758",
                            "updatedAtHeight": "304483",
                            "goodTilBlockTime": "2024-06-11T17:51:31.000Z",
                            "postOnly": false,
                            "ticker": "LDO-USD"
                        }
                    ]
                }
            ],
            "subaccountNumber": 128
        }
    """.trimIndent()
}
