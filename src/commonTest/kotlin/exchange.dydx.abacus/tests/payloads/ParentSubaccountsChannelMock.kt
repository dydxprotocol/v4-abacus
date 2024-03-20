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
                             "entryPrice":"0.000000",
                             "exitPrice":null,
                             "realizedPnl":"0.000000",
                             "unrealizedPnl":"0.000000",
                             "createdAt":"2022-12-02T16:14:15.883Z",
                             "createdAtHeight":"862",
                             "closedAt":null,
                             "sumOpen":"0",
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
                             "exitPrice":"0.000000",
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
                    }
                 ]
              },
              "orders":[
                 {
                    "id":"b812bea8-29d3-5841-9549-caa072f6f8a8",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "subaccountNumber":0,
                    "clientId":"2194126268",
                    "clobPairId":"1",
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
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "subaccountNumber":128,
                    "clientId":"2194126269",
                    "clobPairId":"134",
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
              ]
           }
        }
    """.trimIndent()
    internal val channel_data = """
        {
          "type": "channel_data",
          "connection_id": "8ae0694d-d991-4afc-9999-a07006d0cfca",
          "message_id": 19,
          "id": "dace1648-c854-5aed-9879-88899bf647a3",
          "channel": "v4_parent_subaccounts",
          "contents": {
            "fills": [
              {
                "market": "ETH-USD",
                "transactionId": "45929918",
                "quoteAmount": "945.3928",
                "price": "1621.600000",
                "size": "0.583",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "0.472696",
                "type": "MARKET",
                "id": "c4f12741-dbe6-597c-8efc-0fd5801f99b3",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              },
              {
                "market": "ETH-USD",
                "transactionId": "45929917",
                "quoteAmount": "52929.003",
                "price": "1621.500000",
                "size": "32.642",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "26.464501",
                "type": "MARKET",
                "id": "33afca57-2512-5778-b8f8-78ff96cf3afe",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              },
              {
                "market": "ETH-USD",
                "transactionId": "45929916",
                "quoteAmount": "19496.916",
                "price": "1621.500000",
                "size": "12.024",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "9.748458",
                "type": "MARKET",
                "id": "7f39d458-5290-58db-8bb4-bceabbfa7d27",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              }
            ],
            "positions": [
              {
                "id": "70af36fe-f803-5185-b2bb-69eced5d73b5",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "market": "ETH-USD",
                "side": "LONG",
                "status": "OPEN",
                "size": "192.096",
                "maxSize": "192.096",
                "entryPrice": "1314.480485",
                "exitPrice": "1265.594735",
                "openTransactionId": "45324205",
                "closeTransactionId": null,
                "lastTransactionId": "45929918",
                "closedAt": null,
                "updatedAt": "2022-06-30T01:01:10.234Z",
                "createdAt": "2022-06-30T01:01:10.234Z",
                "sumOpen": "377.245",
                "sumClose": "185.149",
                "netFunding": "-4155.221089",
                "realizedPnl": "-13206.368924"
              }
            ],
            "orders": [
              {
                "id": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "clientId": "66507608042289887",
                "market": "ETH-USD",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "size": "45.249",
                "remainingSize": "0",
                "limitFee": "0.002",
                "price": "1702.7",
                "triggerPrice": null,
                "trailingPercent": null,
                "type": "MARKET",
                "status": "FILLED",
                "signature": "07c17d2804cbe11203a200c496e0fb72af4d4e824367f4a60227261b7f459ebe06f5fe893596b66a6bc7b23d05b83a99dc118fe7f967dee52792c01b8dab88bd",
                "timeInForce": "FOK",
                "postOnly": false,
                "cancelReason": null,
                "expiresAt": "2022-08-01T20:13:29.361Z",
                "unfillableAt": "2022-08-01T19:53:29.686Z",
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.653Z",
                "reduceOnly": false,
                "country": "JP",
                "client": "01",
                "reduceOnlySize": null
              }
            ],
            "accounts": [
              {
                "id": "dace1648-c854-5aed-9879-88899bf647a3",
                "userId": "11480ece-f71d-5012-bb7f-bf1f74b749a7",
                "accountNumber": 0,
                "starkKey": "01de24f8468f0590b80c12c87ae9e247ef3278d4683d875296051495b2ad0100",
                "quoteBalance": "-222333.2879",
                "lastTransactionId": "45929918",
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2021-04-20T18:27:38.698Z",
                "positionId": "30915",
                "starkKeyYCoordinate": "029b2777250c190b22d943780a884d248666030e1339544ab547b40bec0d2237"
              }
            ],
            "fundingPayments": []
          }
        }
    """.trimIndent()
}
