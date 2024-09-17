package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.processor.router.skip.SkipProcessor
import kotlin.test.Test
import kotlin.test.assertTrue

class TradingStateMachineTests {
    @Test
    fun testConstructRouterProcessor() {
        val tradingStateMachine = TradingStateMachine(
            environment = null,
            localizer = null,
            formatter = null,
            maxSubaccountNumber = 1,
            useParentSubaccount = false,
            trackingProtocol = null,
        )
        assertTrue(tradingStateMachine.routerProcessor is SkipProcessor)
    }
}
