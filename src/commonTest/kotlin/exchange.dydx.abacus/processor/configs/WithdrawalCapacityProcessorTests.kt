package exchange.dydx.abacus.processor.configs

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.state.internalstate.InternalWithdrawalCapacityState
import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainLimiterCapacity
import indexer.models.chain.OnChainWithdrawalCapacityResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class WithdrawalCapacityProcessorTests {
    companion object {
        internal val payloadMock = OnChainWithdrawalCapacityResponse(
            limiterCapacityList = listOf(
                OnChainLimiterCapacity(
                    limiter = null,
                    capacity = "100",
                ),
                OnChainLimiterCapacity(
                    limiter = null,
                    capacity = "200",
                ),
            ),
        )

        internal val capacityPayload = InternalWithdrawalCapacityState(
            capacity = "100",
            maxWithdrawalCapacity = 0.0001.toBigDecimal(),
        )
    }

    private val processor = WithdrawalCapacityProcessor(parser = Parser())

    @Test
    fun testProcess() {
        val result = processor.process(payloadMock)
        assertEquals(capacityPayload, result)
    }
}
