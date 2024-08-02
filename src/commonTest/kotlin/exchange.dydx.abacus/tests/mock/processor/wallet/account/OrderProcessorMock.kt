package exchange.dydx.abacus.tests.mock.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.processor.wallet.account.OrderProcessorProtocol
import exchange.dydx.abacus.state.manager.BlockAndTime
import indexer.models.IndexerCompositeOrderObject

internal class OrderProcessorMock : OrderProcessorProtocol {
    var processCallCount = 0
    var processAction: ((existing: SubaccountOrder?, payload: IndexerCompositeOrderObject, subaccountNumber: Int, height: BlockAndTime?) -> SubaccountOrder?)? = null
    var updateHeightCallCount = 0
    var updateHeightAction: ((existing: SubaccountOrder, height: BlockAndTime?) -> Pair<SubaccountOrder, Boolean>)? = null
    var canceledCallCount = 0
    var canceledAction: ((existing: SubaccountOrder) -> SubaccountOrder)? = null

    override fun process(
        existing: SubaccountOrder?,
        payload: IndexerCompositeOrderObject,
        subaccountNumber: Int,
        height: BlockAndTime?
    ): SubaccountOrder? {
        processCallCount++
        return processAction?.invoke(existing, payload, subaccountNumber, height)
    }

    override fun updateHeight(
        existing: SubaccountOrder,
        height: BlockAndTime?
    ): Pair<SubaccountOrder, Boolean> {
        updateHeightCallCount++
        return updateHeightAction?.invoke(existing, height) ?: Pair(existing, false)
    }

    override fun canceled(existing: SubaccountOrder): SubaccountOrder {
        canceledCallCount++
        return canceledAction?.invoke(existing) ?: existing
    }
}
