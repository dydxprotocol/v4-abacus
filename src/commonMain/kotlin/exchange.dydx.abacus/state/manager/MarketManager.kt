package exchange.dydx.abacus.state.manager

internal open class NetworkManager {
    internal var readyToConnect: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    onConnected()
                } else {
                    onDisconnected()
                }
            }
        }

    internal open fun onConnected() {
    }

    internal open fun onDisconnected() {
    }
}

internal class MarketManager(private val configs: MarketConfigs, private val marketId: String) :
    NetworkManager() {
    override fun onConnected() {
        super.onConnected()
        if (configs.retrieveCandles) {
            retrieveCandles()
        }
        if (configs.retrieveHistoricalFundings) {
            retrieveHistoricalFundings()
        }
        if (configs.subscribeToOrderbook) {
            subscribeToOrderbook()
        }
        if (configs.subscribeToTrades) {
            subscribeToTrades()
        }
        if (configs.subscribeToCandles) {
            subscribeToCandles()
        }
    }

    override fun onDisconnected() {
        super.onDisconnected()
        if (configs.subscribeToOrderbook) {
            unsubscribeFromOrderbook()
        }
        if (configs.subscribeToTrades) {
            unsubscribeFromTrades()
        }
        if (configs.subscribeToCandles) {
            unsubscribeFromCandles()
        }
    }

}