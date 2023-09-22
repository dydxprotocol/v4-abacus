package exchange.dydx.abacus.tests.payloads

import exchange.dydx.abacus.state.manager.AppVersion
import exchange.dydx.abacus.state.manager.EnvironmentURIs
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import kollections.JsExport

@JsExport
class AbacusMockData {
    internal val socketUrl = AbUrl("api.dydx.exchange", path = "/v3/ws", scheme = "wss")
    internal val environments = EnvironmentsMock()
    internal val accountsChannel = AccountsChannelMock()
    internal val fillsChannel = FillsMock()
    internal val transfersMock = TransfersMock()
    internal val user = UserMock()
    internal val marketsChannel = MarketsChannelMock()
    internal val marketsConfigurations = MarketConfigurationsMock()
    internal val tradesChannel = TradesChannelMock()
    internal val orderbookChannel = OrderbookChannelMock()
    internal val historicalPNL = HistoricalPNLMock()
    internal val candles = CandlesMock()
    internal val feeTiers = FeeTiersMock()
    internal val feeDiscounts = FeeDiscountsMock()
    internal val calculations = Calculations()
    internal val connectionMock = ConnectionMock()
    internal val validationsMock = ValidationsMock()
    internal val historicalFundingsMock = HistoricalFundingsMock()
    internal val heightMock = HeightMock()
    internal val transactionsMock = TransactionsMock()
    internal val squidChainsMock = SquidChainsMock()
    internal val squidTokensMock = SquidTokensMock()
    internal val squidRouteMock = SquidRouteMock()
    internal val squidStatusMock = SquidStatusMock()
    internal val localizationMock = LocalizationMock()
    internal val v4OnChainMock = V4OnChainMock()
    internal val v4Environment = V4Environment(
        "test",
        "test",
        "test",
        "test",
        "test",
        false,
        AppVersion.v4,
        127,
        EnvironmentURIs(
            null,
            null,
            null,
            null,
            null,
            null,
            "https://v4.testnet.dydx.exchange/currencies/{asset}.svg",
            null,
            null,
            null,
        ),


        )
}