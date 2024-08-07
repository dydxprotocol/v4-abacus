package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.tests.extensions.loadv4TradesChanged
import kotlin.test.Test
import kotlin.test.assertEquals

class V4DuplicateWebsocketMessageTests : V4BaseTests() {

    @Test
    fun testDuplicateFills() {
        setup()

        repeat(2) {
            test(
                {
                    perp.socket(
                        testWsUrl,
                        mock.batchedSubaccountsChannel.channel_batch_data_order_filled_1,
                        0,
                        null,
                    )
                },
                """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "total": 2800.8
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                    },
                                    "freeCollateral": {
                                    },
                                    "quoteBalance": {
                                        "current": 1599696.37
                                    },
                                    "orders": {
                                    },
                                    "fills":[
                                         {
                                            "id":"a74830f8-d506-54b3-bf3b-1de791b8fe4e",
                                            "fee":"-0.067364",
                                            "side":"BUY",
                                            "size":"82",
                                            "type":"LIMIT",
                                            "price":"9.128",
                                            "orderId":"f7c9cd24-57cd-5240-a98d-3c9c3c11767d",
                                            "createdAt":"2024-05-06T18:41:20.606Z",
                                            "liquidity":"MAKER",
                                            "clientMetadata":"0",
                                            "marketId":"APT-USD"
                                         },
                                         {
                                            "id":"0d473eec-93b0-5c49-94ca-b8017454d769",
                                            "fee":"-0.001643",
                                            "side":"BUY",
                                            "size":"2",
                                            "type":"LIMIT",
                                            "price":"9.128",
                                            "orderId":"f7c9cd24-57cd-5240-a98d-3c9c3c11767d",
                                            "createdAt":"2024-05-06T18:41:20.606Z",
                                            "liquidity":"MAKER",
                                            "clientMetadata":"0",
                                            "marketId":"APT-USD"
                                         }
                                      ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    @Test
    fun testDuplicateTrades() {
        setup()

        repeat(2) {
            if (perp.staticTyping) {
                perp.loadv4TradesChanged(mock, testWsUrl)
                val market = perp.internalState.marketsSummary.markets.get("ETH-USD")
                assertEquals(1, market?.trades?.size)
                val firstItem = market?.trades?.get(0)
                assertEquals("8ee6d90d-272d-5edd-bf0f-2e4d6ae3d3b7", firstItem?.id)
                assertEquals("BUY", firstItem?.side?.rawValue)
                assertEquals(1.593707, firstItem?.size)
            } else {
                test(
                    {
                        perp.loadv4TradesChanged(mock, testWsUrl)
                    },
                    """
                {
                   "markets":{
                      "markets":{
                         "ETH-USD":{
                            "trades": [
                                {
                                    "id": "8ee6d90d-272d-5edd-bf0f-2e4d6ae3d3b7",
                                    "side": "BUY",
                                    "size": 1.593707,
                                    "price": 1255.949,
                                    "createdAt": "2022-12-12T02:28:14.859Z",
                                    "resources": {
                                    }
                                }
                            ]
                         }
                      }
                   }
                }
                    """.trimIndent(),
                )
            }
        }
    }
}
