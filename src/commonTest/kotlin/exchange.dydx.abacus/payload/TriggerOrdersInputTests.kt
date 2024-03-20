package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.triggerOrders
import kotlin.test.Test

class TriggerOrderInputTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()
        print("--------First round----------\n")

        test(
                { perp.triggerOrders("0", TriggerOrdersInputField.size, 0) },
                """
    {
        "input": {
            "current": "triggerOrders",
            "triggerOrders": {
                "size": "0"
            },
            "errors": null
        }
    }
    """.trimIndent()
        )
    }
}
