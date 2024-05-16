package exchange.dydx.abacus.state.internalstate

class InternalState(

) {
    private var _data: Map<String, Any>? = null

    internal var data: Map<String, Any>?
        get() = _data
        set(value) {
            _data = value
        }


    internal var marketsSummary: InternalMarketsSummary? = null


}