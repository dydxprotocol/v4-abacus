package exchange.dydx.abacus.state.internalstate

data class InternalMarketsSummary(
    val test: Int? = null,
) {
    companion object {
        fun fromMap(json: Map<String, Any>?): InternalMarketsSummary? {
            if (json == null) {
                return null
            }
            val test = json["test"] as? Int
            return InternalMarketsSummary(
                test = test,
            )
        }
    }
}