package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.transfer
import kotlin.test.Test

class V4WithdrawalGatingTests: V4BaseTests() {
    @Test
    fun testDataFeed() {
        test(
            {
                perp.transfer(TransferType.withdrawal.rawValue, TransferInputField.type)
            },
            """
            {
                "withdrawalGating": {
                    "negativeTncSubaccountSeenAtBlock": 1,
                    "chainOutageSeenAtBlock": 1,
                    "withdrawalsAndTransfersUnblockedAtBlock": 1
                }
            }
            """.trimIndent(),
        )
    }
}