package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.triggerOrders
import kotlin.test.Test

class TriggerOrderInputTests : V4BaseTests() {
    @Test
    fun testInputs() {
        setup()

        test(
            {
                perp.triggerOrders("1.0", TriggerOrdersInputField.size, 0)
            }, 
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "size": "1.0"
                    }
                }
            }
            """
        )

        testStopLossInput()
        testTakeProfitInput()
    }

    private fun testStopLossInput() {
        test({
            perp.triggerOrders("LIMIT", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        test({
            perp.triggerOrders("3.0", TriggerOrdersInputField.stopLossLimitPrice, 0)
        }, null)

        test({
            perp.triggerOrders("3.0", TriggerOrdersInputField.stopLossPrice, 0)
        }, null)

        test({
            perp.triggerOrders("40", TriggerOrdersInputField.stopLossPercentDiff, 0)
        }, null)

        test(
            {
                perp.triggerOrders("20", TriggerOrdersInputField.stopLossUsdcDiff, 0)
            }, 
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "type": "LIMIT",
                            "price": {
                                "limitPrice": "3.0",
                                "triggerPrice": "3.0",
                                "percentDiff": "40",
                                "usdcDiff": "20",
                                "input": "stopLossOrder.price.usdcDiff"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testTakeProfitInput() {
        test({
            perp.triggerOrders("LIMIT", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        test({
            perp.triggerOrders("3.0", TriggerOrdersInputField.takeProfitLimitPrice, 0)
        }, null)

        test({
            perp.triggerOrders("3.0", TriggerOrdersInputField.takeProfitPrice, 0)
        }, null)

        test({
            perp.triggerOrders("40", TriggerOrdersInputField.takeProfitPercentDiff, 0)
        }, null)

        test(
            {
                perp.triggerOrders("20", TriggerOrdersInputField.takeProfitUsdcDiff, 0)
            }, 
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "type": "LIMIT",
                            "price": {
                                "limitPrice": "3.0",
                                "triggerPrice": "3.0",
                                "percentDiff": "40",
                                "usdcDiff": "20",
                                "input": "takeProfitOrder.price.usdcDiff"
                            }
                        }
                    }
                }g
            }
            """.trimIndent(),
        )
    }
}
