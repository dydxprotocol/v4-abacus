package exchange.dydx.abacus.state.manager

data class ExchangeInfo(
    val name: String,
    val label: String,
    val icon: String,
    val depositType: String,
)

object ExchangeConfig {
    var exchangeList: List<ExchangeInfo>? = null
}
