package exchange.dydx.abacus.tests.payloads

import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.manager.EnvironmentEndpoints
import exchange.dydx.abacus.state.manager.TokenInfo
import exchange.dydx.abacus.state.manager.WalletConnect
import exchange.dydx.abacus.state.manager.WalletConnectClient
import exchange.dydx.abacus.state.manager.WalletConnectV1
import exchange.dydx.abacus.state.manager.WalletConnectV2
import exchange.dydx.abacus.state.manager.WalletConnection
import exchange.dydx.abacus.state.manager.WalletSegue
import kollections.JsExport
import kollections.iMapOf
import kollections.toIMap

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
        "dYdX Chain",
        "dYdX-logo.png",
        false,
        EnvironmentEndpoints(
            null,
            null,
            null,
            null,
        ),
        null,
        WalletConnection(
            WalletConnect(
                WalletConnectClient(
                    "test",
                    "test",
                    "test",
                ),
                WalletConnectV1(
                    "test",
                ),
                WalletConnectV2(
                    "test",
                )
            ),
            WalletSegue("callback"),
            "/images/",
            "test",
            "test",
        ),
        mapOf(
            "chain" to TokenInfo("DYDX", "dv4tnt",  null,"/currencies/dydx.png"),
            "usdc" to TokenInfo("USDC", "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5", null, "/currencies/usdc.png"),
        ).toIMap()
    )
}