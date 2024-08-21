package exchange.dydx.abacus.tests.payloads

import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.manager.EnvironmentEndpoints
import exchange.dydx.abacus.state.manager.EnvironmentFeatureFlags
import exchange.dydx.abacus.state.manager.TokenInfo
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.WalletConnect
import exchange.dydx.abacus.state.manager.WalletConnectClient
import exchange.dydx.abacus.state.manager.WalletConnectV1
import exchange.dydx.abacus.state.manager.WalletConnectV2
import exchange.dydx.abacus.state.manager.WalletConnection
import exchange.dydx.abacus.state.manager.WalletSegue
import kollections.JsExport
import kollections.toIMap

@JsExport
class AbacusMockData {
    internal val socketUrl = AbUrl("api.dydx.exchange", path = "/v3/ws", scheme = "wss")
    internal val environments = EnvironmentsMock()
    internal val accountsChannel = AccountsChannelMock()
    internal val batchedSubaccountsChannel = SubaccountsChannelMock()
    internal val parentSubaccountsChannel = ParentSubaccountsChannelMock()
    internal val historicalTradingRewards = HistoricalTradingRewardsMock()
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
    internal val squidV2AssetsMock = SquidV2AssetsMock()
    internal val squidV2RouteMock = SquidV2RouteMock()
    internal val localizationMock = LocalizationMock()
    internal val launchIncentiveMock = LaunchIncentiveMock()
    internal val v4OnChainMock = V4OnChainMock()
    internal val v4ParentSubaccountsMock = V4ParentSubaccountsMock()
    internal val v4WithdrawalSafetyChecksMock = V4WithdrawalSafetyChecksMock()
    internal val v4Environment = V4Environment(
        "test",
        "test",
        "test",
        "test",
        "dYdX-api",
        "dYdX Chain",
        "dYdX-logo.png",
        "1704844800000",
        false,
        EnvironmentEndpoints(
            indexers = null,
            validators = null,
            faucet = null,
            squid = null,
            skip = null,
            nobleValidator = null,
            geo = null,
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
                ),
            ),
            WalletSegue("callback"),
            "/images/",
            "test",
            "test",
        ),
        null,
        mapOf(
            "chain" to TokenInfo(
                "DYDX",
                "adv4tnt",
                18,
                null,
                "/currencies/dydx.png",
            ),
            "usdc" to TokenInfo(
                "USDC",
                "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                6,
                "uusdc",
                "/currencies/usdc.png",
            ),
        ).toIMap(),
        null,
        EnvironmentFeatureFlags(
            withdrawalSafetyEnabled = true,
            isSlTpLimitOrdersEnabled = true,
            isLimitCloseEnabled = true,
        ),
    )
}
