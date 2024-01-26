package exchange.dydx.abacus.validation

import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class TradePositionTests: ValidationsTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTradeInputOnce()
    }

    private fun testTradeInputOnce() {
        reset()

        var time = ServerTime.now()
        testPositions()
        time = perp.log("Position Validation", time)
    }

    private fun testPositions() {
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

        test({
            perp.trade("SELL", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("LIMIT", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("IOC", TradeInputField.timeInForceType, 0)
        }, null)

        test({
            perp.trade("true", TradeInputField.reduceOnly, 0)
        }, null)

        test({
            perp.trade("1050.0", TradeInputField.limitPrice, 0)
        }, null)

        /*
        This test would throw an Flip Position error when reduceOnly is supported
         */
        test(
            {
                perp.trade("110.0", TradeInputField.size, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "SELL",
                            "marketId": "ETH-USD",
                            "timeInForce": "IOC"
                        },
                        "errors": null
                    }
                }
            """
                /*

            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "SELL",
                            "marketId": "ETH-USD",
                            "timeInForce": "IOC"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "ORDER_WOULD_FLIP_POSITION"
                            }
                        ]
                    }
                }
            """
                 */
                .trimIndent()
        )


        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("999.0", TradeInputField.limitPrice, 0)
        }, null)

        test(
            {
                perp.trade("210.0", TradeInputField.size, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "timeInForce": "IOC"
                        },
                        "errors": null
                    }
                }
            """.trimIndent()
        )
    }
}