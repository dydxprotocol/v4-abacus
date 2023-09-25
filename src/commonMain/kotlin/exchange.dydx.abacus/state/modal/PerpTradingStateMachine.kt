package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.AppVersion
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.app.helper.Formatter

class PerpTradingStateMachine(
    environment: V4Environment?,
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    version: AppVersion,
    maxSubaccountNumber: Int,
) :
    TradingStateMachine(environment, localizer, formatter, version, maxSubaccountNumber) {
    /*
    Placeholder for now. Eventually, the code specifically for Perpetual will be in this class
     */
}