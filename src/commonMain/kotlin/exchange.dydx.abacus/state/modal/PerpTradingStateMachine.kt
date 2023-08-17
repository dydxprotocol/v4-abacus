package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.app.AppVersion
import exchange.dydx.abacus.state.app.helper.Formatter

class PerpTradingStateMachine(
    private val localizer: LocalizerProtocol?,
    private val formatter: Formatter?,
    version: AppVersion,
    maxSubaccountNumber: Int,
) :
    TradingStateMachine(localizer, formatter, version, maxSubaccountNumber) {
    /*
    Placeholder for now. Eventually, the code specifically for Perpetual will be in this class
     */
}