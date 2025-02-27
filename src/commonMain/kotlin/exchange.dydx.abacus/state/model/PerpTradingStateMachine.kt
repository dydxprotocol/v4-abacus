package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment

class PerpTradingStateMachine(
    environment: V4Environment?,
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    maxSubaccountNumber: Int,
    useParentSubaccount: Boolean,
    staticTyping: Boolean = false,
    skipGoFast: Boolean = false,
    trackingProtocol: TrackingProtocol?,
) :
    TradingStateMachine(
        environment = environment,
        localizer = localizer,
        formatter = formatter,
        maxSubaccountNumber = maxSubaccountNumber,
        useParentSubaccount = useParentSubaccount,
        staticTyping = staticTyping,
        skipGoFast = skipGoFast,
        trackingProtocol = trackingProtocol,
    ) {
    /*
    Placeholder for now. Eventually, the code specifically for Perpetual will be in this class
     */
}
