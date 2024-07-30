package exchange.dydx.abacus.tests.mock.processor.configs

import exchange.dydx.abacus.processor.configs.WithdrawalCapacityProcessorProtocol
import exchange.dydx.abacus.state.internalstate.InternalWithdrawalCapacityState
import exchange.dydx.abacus.state.manager.V4Environment
import indexer.models.chain.OnChainWithdrawalCapacityResponse

internal class WithdrawalCapacityProcessorMock(
    override var accountAddress: String?,
    override var environment: V4Environment?
) : WithdrawalCapacityProcessorProtocol {
    var processCallCount = 0
    var processAction: ((payload: OnChainWithdrawalCapacityResponse?) -> InternalWithdrawalCapacityState?)? = null

    override fun process(payload: OnChainWithdrawalCapacityResponse?): InternalWithdrawalCapacityState? {
        processCallCount++
        return processAction?.invoke(payload)
    }
}
