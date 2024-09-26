package exchange.dydx.abacus.tests.mock.calculator

import exchange.dydx.abacus.calculator.OrderbookCalculatorProtocol
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.state.internalstate.InternalOrderbook

internal class OrderbookCalculatorMock : OrderbookCalculatorProtocol {
    var calculateCallCount = 0
    var consolidateCallCount = 0
    var calculateAction: ((InternalOrderbook?, Double, Int) -> MarketOrderbook?)? = null
    var consolidateAction: ((InternalOrderbook?) -> InternalOrderbook?)? = null

    override fun calculate(
        rawOrderbook: InternalOrderbook?,
        tickSize: Double,
        groupingMultiplier: Int
    ): MarketOrderbook? {
        calculateCallCount++
        return calculateAction?.invoke(rawOrderbook, tickSize, groupingMultiplier)
    }

    override fun consolidate(rawOrderbook: InternalOrderbook?): InternalOrderbook? {
        consolidateCallCount++
        return consolidateAction?.invoke(rawOrderbook)
    }
}
