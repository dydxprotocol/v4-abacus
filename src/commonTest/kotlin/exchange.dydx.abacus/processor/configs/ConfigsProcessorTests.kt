package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.state.internalstate.InternalConfigsState
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.tests.mock.processor.configs.EquityTiersProcessorMock
import exchange.dydx.abacus.tests.mock.processor.configs.FeeTiersProcessorMock
import exchange.dydx.abacus.tests.mock.processor.configs.WithdrawalCapacityProcessorMock
import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainWithdrawalAndTransferGatingStatusResponse
import kotlin.test.Test
import kotlin.test.assertTrue

class ConfigsProcessorTests {
    private val withdrawalCapacityProcessorMock = WithdrawalCapacityProcessorMock(
        accountAddress = null,
        environment = null,
    )
    private val feeTiersProcessorMock = FeeTiersProcessorMock()
    private val equityTiersProcessorMock = EquityTiersProcessorMock()

    private val configsProcessor = ConfigsProcessor(
        withdrawalCapacityProcessor = withdrawalCapacityProcessorMock,
        feeTiersProcessor = feeTiersProcessorMock,
        equityTiersProcessor = equityTiersProcessorMock,
        localier = LocalizerProtocolMock(),
        parser = Parser(),
    )

    @Test
    fun testProcessOnChainEquityTiers() {
        val existing = InternalConfigsState()
        val payload = null
        val result = configsProcessor.processOnChainEquityTiers(existing, payload)
        assertTrue { (equityTiersProcessorMock.processCallCount == 1) }
    }

    @Test
    fun testProcessOnChainFeeTiers() {
        val existing = InternalConfigsState()
        val payload = null
        val result = configsProcessor.processOnChainFeeTiers(existing, payload)
        assertTrue { (feeTiersProcessorMock.processCallCount == 1) }
    }

    @Test
    fun testProcessWithdrawalCapacity() {
        val existing = InternalConfigsState()
        val payload = null
        val result = configsProcessor.processWithdrawalCapacity(existing, payload)
        assertTrue { (withdrawalCapacityProcessorMock.processCallCount == 1) }
    }

    @Test
    fun testProcessWithdrawalGating() {
        val existing = InternalConfigsState()
        val payload = OnChainWithdrawalAndTransferGatingStatusResponse(
            withdrawalsAndTransfersUnblockedAtBlock = 1000.0,
        )
        val result = configsProcessor.processWithdrawalGating(existing, payload)
        assertTrue { (result.withdrawalGating?.withdrawalsAndTransfersUnblockedAtBlock == 1000) }
    }
}
