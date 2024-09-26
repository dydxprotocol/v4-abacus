package exchange.dydx.abacus.processor.wallet.account.staking

import exchange.dydx.abacus.output.account.UnbondingDelegation
import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainUnbondingEntry
import indexer.models.chain.OnChainUnbondingObject
import indexer.models.chain.OnChainUnbondingResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class DelegationUnbondingProcessorTests {
    companion object {
        val payloadMock = OnChainUnbondingResponse(
            unbondingResponses = listOf(
                OnChainUnbondingObject(
                    validatorAddress = "validatorAddress1",
                    entries = listOf(
                        OnChainUnbondingEntry(
                            completionTime = "2021-01-01T00:00:00Z",
                            balance = "100.0",
                        ),
                        OnChainUnbondingEntry(
                            completionTime = "2021-01-01T00:00:01Z",
                            balance = "200.0",
                        ),
                    ),
                ),
                OnChainUnbondingObject(
                    validatorAddress = "validatorAddress2",
                    entries = listOf(
                        OnChainUnbondingEntry(
                            completionTime = "2021-01-01T00:00:02Z",
                            balance = "300.0",
                        ),
                    ),
                ),
            ),
        )

        val unbondingsMock = listOf(
            UnbondingDelegation(
                validator = "validatorAddress1",
                completionTime = "2021-01-01T00:00:00Z",
                balance = "100.0",
            ),
            UnbondingDelegation(
                validator = "validatorAddress1",
                completionTime = "2021-01-01T00:00:01Z",
                balance = "200.0",
            ),
            UnbondingDelegation(
                validator = "validatorAddress2",
                completionTime = "2021-01-01T00:00:02Z",
                balance = "300.0",
            ),
        )
    }

    private val processor = DelegationUnbondingProcessor(
        parser = Parser(),
    )

    @Test
    fun testProcess() {
        val unbondings = processor.process(
            existing = null,
            payload = payloadMock,
        )
        assertEquals(unbondingsMock, unbondings)
    }
}
