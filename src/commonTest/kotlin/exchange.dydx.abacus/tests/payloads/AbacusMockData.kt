package exchange.dydx.abacus.tests.payloads

import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.state.manager.EnvironmentEndpoints
import exchange.dydx.abacus.state.manager.EnvironmentFeatureFlags
import exchange.dydx.abacus.state.manager.EnvironmentLinks
import exchange.dydx.abacus.state.manager.Phantom
import exchange.dydx.abacus.state.manager.TokenInfo
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.WalletConnect
import exchange.dydx.abacus.state.manager.WalletConnectClient
import exchange.dydx.abacus.state.manager.WalletConnectV1
import exchange.dydx.abacus.state.manager.WalletConnectV2
import exchange.dydx.abacus.state.manager.WalletConnection
import exchange.dydx.abacus.state.manager.WalletSegue
import kollections.JsExport
import kollections.iListOf
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
    internal val localizationMock = LocalizationMock()
    internal val launchIncentiveMock = LaunchIncentiveMock()
    internal val v4OnChainMock = V4OnChainMock()
    internal val v4ParentSubaccountsMock = V4ParentSubaccountsMock()
    internal val v4WithdrawalSafetyChecksMock = V4WithdrawalSafetyChecksMock()
    internal val vaultMocks = VaultMocks()
    internal val v4Environment = V4Environment(
        id = "test",
        name = "test",
        ethereumChainId = "test",
        dydxChainId = "test",
        chainName = "dYdX Chain",
        chainLogo = "dYdX-logo.png",
        rewardsHistoryStartDateMs = "1704844800000",
        megavaultHistoryStartDateMs = 1704844800000.0,
        megavaultOperatorName = "test",
        isMainNet = false,
        endpoints = EnvironmentEndpoints(
            indexers = null,
            validators = null,
            faucet = null,
            skip = null,
            metadataService = null,
            nobleValidator = null,
            geo = null,
        ),
        links = EnvironmentLinks(
            tos = "https://dydx.exchange/v4-terms",
            privacy = "https://dydx.exchange/privacy",
            mintscan = "https://testnet.mintscan.io/dydx-testnet/txs/{tx_hash}",
            mintscanBase = "https://testnet.mintscan.io/dydx-testnet",
            documentation = "https://v4-teacher.vercel.app/",
            community = "https://discord.com/invite/dydx",
            feedback = "https://docs.google.com/forms/d/e/1FAIpQLSezLsWCKvAYDEb7L-2O4wOON1T56xxro9A2Azvl6IxXHP_15Q/viewform",
            blogs = "https://www.dydx.foundation/blog",
            help = "https://help.dydx.exchange/",
            vaultLearnMore = "https://help.dydx.exchange/",
            vaultTos = "https://help.dydx.exchange/",
            vaultOperatorLearnMore = "https://help.dydx.exchange/",
            simpleTradeLearnMore = "https://help.dydx.exchange/",
            launchIncentive = "https://dydx.exchange/v4-launch-incentive",
            statusPage = "https://status.v4testnet.dydx.exchange/",
            withdrawalGateLearnMore = "https://help.dydx.exchange/en/articles/8981384-withdrawals-on-dydx-chain#h_23e97bc665",
            equityTiersLearnMore = "https://help.dydx.trade/en/articles/171918-equity-tiers-and-rate-limits",
            tradingRewardsLearnMore = "https://docs.dydx.exchange/concepts-trading/rewards_fees_and_parameters",
            incentiveProgram = null,
            incentiveProgramLeaderboard = null,
        ),
        walletConnection = WalletConnection(
            walletConnect = WalletConnect(
                WalletConnectClient(
                    name = "test",
                    description = "test",
                    iconUrl = "test",
                ),
                WalletConnectV1(
                    bridgeUrl = "test",
                ),
                WalletConnectV2(
                    projectId = "test",
                    wallets = null,
                ),
            ),
            walletSegue = WalletSegue("callback"),
            phantom = Phantom("callback"),
            images = "/images/",
            signTypedDataAction = "test",
            signTypedDataDomainName = "test",
        ),
        apps = null,
        tokens = mapOf(
            "chain" to TokenInfo(
                name = "DYDX",
                denom = "adv4tnt",
                decimals = 18,
                gasDenom = null,
                imageUrl = "/currencies/dydx.png",
            ),
            "usdc" to TokenInfo(
                name = "USDC",
                denom = "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                decimals = 6,
                gasDenom = "uusdc",
                imageUrl = "/currencies/usdc.png",
            ),
        ).toIMap(),
        governance = null,
        featureFlags = EnvironmentFeatureFlags(
            withdrawalSafetyEnabled = true,
            isSlTpLimitOrdersEnabled = true,
            cctpWithdrawalOnly = true,
        ),
        restrictedLocales = iListOf(),
    )
}
