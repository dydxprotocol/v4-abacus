package exchange.dydx.abacus.processor.wallet.account.staking

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.state.internalstate.InternalAccountBalanceState
import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainAccountBalanceObject
import indexer.models.chain.OnChainDelegationObject
import indexer.models.chain.OnChainDelegationResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountDelegationsProcessorTests {
    companion object {
        internal val payloadMock: OnChainDelegationResponse = OnChainDelegationResponse(
            delegationResponses = listOf(
                OnChainDelegationObject(
                    delegatorAddress = "delegatorAddress",
                    validatorAddress = "validatorAddress",
                    shares = null,
                    balance = OnChainAccountBalanceObject(
                        denom = "denom",
                        amount = "100.0",
                    ),
                ),
                OnChainDelegationObject(
                    delegatorAddress = "delegatorAddress",
                    validatorAddress = "validatorAddress",
                    shares = null,
                    balance = OnChainAccountBalanceObject(
                        denom = "denom",
                        amount = "300.0",
                    ),
                ),
                OnChainDelegationObject(
                    delegatorAddress = "delegatorAddress",
                    validatorAddress = "validatorAddress",
                    shares = null,
                    balance = OnChainAccountBalanceObject(
                        denom = "denom1",
                        amount = "200.0",
                    ),
                ),
            ),
        )

        internal val delegationsMock: Map<String, InternalAccountBalanceState> = mapOf(
            "denom" to InternalAccountBalanceState(
                denom = "denom",
                amount = 400.0.toBigDecimal(),
            ),
            "denom1" to InternalAccountBalanceState(
                denom = "denom1",
                amount = 200.0.toBigDecimal(),
            ),
        )
    }

    private val processor = AccountDelegationsProcessor(
        parser = Parser(),
    )

    @Test
    fun testProcess() {
        val result = processor.process(null, payloadMock)
        assertEquals(result, delegationsMock)
    }
}
