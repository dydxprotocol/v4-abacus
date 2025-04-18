package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment

class PerpTradingStateMachine(
    environment: V4Environment?,
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    maxSubaccountNumber: Int,
    useParentSubaccount: Boolean,
    skipGoFast: Boolean = false,
    trackingProtocol: TrackingProtocol?,
) :
    TradingStateMachine(
        environment = environment,
        localizer = localizer,
        formatter = formatter,
        maxSubaccountNumber = maxSubaccountNumber,
        useParentSubaccount = useParentSubaccount,
        skipGoFast = skipGoFast,
        trackingProtocol = trackingProtocol,
    ) {
    /*
    Placeholder for now. Eventually, the code specifically for Perpetual will be in this class
     */
}
