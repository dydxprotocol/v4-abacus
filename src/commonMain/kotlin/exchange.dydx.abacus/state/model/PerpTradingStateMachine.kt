package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.app.helper.Formatter

class PerpTradingStateMachine(
    environment: V4Environment?,
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    maxSubaccountNumber: Int,
) :
    TradingStateMachine(environment, localizer, formatter, maxSubaccountNumber) {
    /*
    Placeholder for now. Eventually, the code specifically for Perpetual will be in this class
     */
}