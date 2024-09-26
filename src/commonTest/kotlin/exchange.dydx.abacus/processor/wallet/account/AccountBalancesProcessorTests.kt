package exchange.dydx.abacus.processor.wallet.account

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.state.internalstate.InternalAccountBalanceState
import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainAccountBalanceObject
import kotlin.test.Test
import kotlin.test.assertTrue

class AccountBalancesProcessorTests {
    companion object {
        internal val payloadMock = listOf(
            OnChainAccountBalanceObject(
                denom = "WETH",
                amount = "1.0",
            ),
            OnChainAccountBalanceObject(
                denom = "DAI",
                amount = "2.0",
            ),
        )

        internal val balancesMock = mapOf(
            "WETH" to InternalAccountBalanceState(
                denom = "WETH",
                amount = 1.0.toBigDecimal(),
            ),
            "DAI" to InternalAccountBalanceState(
                denom = "DAI",
                amount = 2.0.toBigDecimal(),
            ),
        )
    }

    @Test
    fun testProcess_emptyPayload() {
        val processor = AccountBalancesProcessor(
            parser = Parser(),
        )
        val output = processor.process(
            existing = null,
            payload = emptyList(),
        )
        assertTrue { output.isNullOrEmpty() }
    }

    @Test
    fun testProcess_nonEmptyPayload() {
        val processor = AccountBalancesProcessor(
            parser = Parser(),
        )
        val output = processor.process(
            existing = null,
            payload = payloadMock,
        )
        assertTrue { output == balancesMock }
    }
}
