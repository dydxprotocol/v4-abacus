package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.adjustIsolatedMargin
import kotlin.test.Test

class AdjustIsolatedMarginInputTests : V4BaseTests() {

    private fun loadSubaccountData() {
        perp.socket(testWsUrl, mock.parentSubaccountsChannel.subscribed, 0, null)
        perp.socket(testWsUrl, mock.parentSubaccountsChannel.channel_data, 0, null)
    }

    @Test
    fun testInputs() {
        setup()
        loadSubaccountData()

        testIsolatedMarginAdjustmentType()
        testChildSubaccountNumberInput()
        testMarginAmountInput()
    }

    private fun testIsolatedMarginAdjustmentType() {
        test(
            {
                perp.adjustIsolatedMargin(IsolatedMarginAdjustmentType.add.rawValue, AdjustIsolatedMarginInputField.type, 0)
            },
            """
        {
            "input": {
                "current": "adjustIsolatedMargin",
                "adjustIsolatedMargin": {
                    "type": "ADD"
                }
            }
        }
            """.trimIndent(),
        )
    }

    private fun testChildSubaccountNumberInput() {
        test(
            {
                perp.adjustIsolatedMargin("128", AdjustIsolatedMarginInputField.childSubaccountNumber, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "childSubaccountNumber": "128"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testMarginAmountInput() {
        test(
            {
                perp.adjustIsolatedMargin("92.49", AdjustIsolatedMarginInputField.amount, 0)
            },
            """
            {
                "input": {
                    "current": "adjustIsolatedMargin",
                    "adjustIsolatedMargin": {
                        "amount": "92.49"
                    }
                }
            }
            """.trimIndent(),
        )
    }
}
