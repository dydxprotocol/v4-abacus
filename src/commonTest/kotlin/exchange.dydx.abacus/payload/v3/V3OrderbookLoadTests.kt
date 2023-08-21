package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.utils.DebugLogger
import kotlin.test.Test
import kotlin.test.assertNotNull

class V3OrderbookLoadTests : V3BaseTests() {
    @Test
    fun testOverlappedOrderbook() {
        loadMarkets()
        loadMarketsConfigurations()

        test(
            {
                perp.socket(mock.socketUrl, mock.orderbookChannel.load_test_2_subscribed, 0, null)
            }, """
            {
                "markets": {
                    "markets": {
                        "ETH-USD": {
                            "orderbook": {
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        )

        for (i in 0 until mock.orderbookChannel.load_test_2_channel_batch_data_list.count()) {
            testChannelBatchData(mock.orderbookChannel.load_test_2_channel_batch_data_list[i], i)
        }
    }

    fun testChannelBatchData(text: String, index: Int) {
        test(
            {
                perp.socket(mock.socketUrl, text, 0, null)
            }, """
            {
                "markets": {
                    "markets": {
                        "ETH-USD": {
                            "orderbook": {
                            }
                        }
                    }
                }
            }
        """.trimIndent(), { stateResponse ->
                val orderbook = stateResponse.state?.marketOrderbook("ETH-USD")
                assertNotNull(orderbook)
                val asks = orderbook.asks
                assertNotNull(asks)
                val bids = orderbook.bids
                assertNotNull(bids)
                val asksCount = asks.count()
                val bidsCount = bids.count()
                DebugLogger.log("Asks: $asksCount, Bids: $bidsCount")
            })
    }
}