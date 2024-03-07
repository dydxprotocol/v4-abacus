package exchange.dydx.abacus.state.v2.supervisor

import kollections.JsExport


@JsExport
data class SystemConfigs(
    val retrieveServerTime: Boolean,
    val retrieveMarketConfigs: Boolean,
    val retrieveEquityTiers: Boolean,
    val retrieveFeeTiers: Boolean,
    val retrieveRewardsParams: Boolean,
    val retrieveLaunchIncentiveSeasons: Boolean,
    val subscribeToMarkets: Boolean,
) {
    companion object {
        val forApp = SystemConfigs(
            retrieveServerTime = true,
            retrieveMarketConfigs = true,
            retrieveEquityTiers = true,
            retrieveFeeTiers = true,
            retrieveRewardsParams = true,
            retrieveLaunchIncentiveSeasons = true,
            subscribeToMarkets = true,
        )
        val forProgrammaticTraders = SystemConfigs(
            retrieveServerTime = true,
            retrieveMarketConfigs = true,
            retrieveEquityTiers = true,
            retrieveFeeTiers = true,
            retrieveRewardsParams = false,
            retrieveLaunchIncentiveSeasons = false,
            subscribeToMarkets = true,
        )
    }
}

@JsExport
data class MarketConfigs(
    val retrieveSparklines: Boolean,
    val retrieveCandles: Boolean,
    val retrieveHistoricalFundings: Boolean,
    val subscribeToOrderbook: Boolean,
    val subscribeToTrades: Boolean,
    val subscribeToCandles: Boolean,
) {
    companion object {
        val forApp = MarketConfigs(
            retrieveSparklines = true,
            retrieveCandles = true,
            retrieveHistoricalFundings = true,
            subscribeToOrderbook = true,
            subscribeToTrades = true,
            subscribeToCandles = true,
        )
        val forWeb = MarketConfigs(
            retrieveSparklines = true,
            retrieveCandles = true,
            retrieveHistoricalFundings = true,
            subscribeToOrderbook = true,
            subscribeToTrades = true,
            subscribeToCandles = false,
        )
        val forProgrammaticTraders = MarketConfigs(
            retrieveSparklines = false,
            retrieveCandles = false,
            retrieveHistoricalFundings = false,
            subscribeToOrderbook = true,
            subscribeToTrades = false,
            subscribeToCandles = false,
        )
    }
}

@JsExport
data class SubaccountConfigs(
    val retrieveFills: Boolean,
    val retrieveTransfers: Boolean,
    val retrieveHistoricalPnls: Boolean,
    val subscribeToSubaccount: Boolean,
) {
    companion object {
        val forApp = SubaccountConfigs(
            retrieveFills = true,
            retrieveTransfers = true,
            retrieveHistoricalPnls = true,
            subscribeToSubaccount = true,
        )
        val forProgrammaticTraders = SubaccountConfigs(
            retrieveFills = false,
            retrieveTransfers = false,
            retrieveHistoricalPnls = false,
            subscribeToSubaccount = true,
        )
    }
}

@JsExport
data class AccountConfigs(
    val retrieveBalances: Boolean,
    val retrieveSubaccounts: Boolean,
    val retrieveHistoricalTradingRewards: Boolean,
    val retrieveLaunchIncentivePoints: Boolean,
    val transferNobleBalances: Boolean,
    val onboardingConfigs: OnboardingConfigs,
    val subaccountConfigs: SubaccountConfigs,
) {
    companion object {
        val forApp = AccountConfigs(
            retrieveBalances = true,
            retrieveSubaccounts = true,
            retrieveHistoricalTradingRewards = true,
            retrieveLaunchIncentivePoints = true,
            transferNobleBalances = true,
            onboardingConfigs = OnboardingConfigs.forApp,
            subaccountConfigs = SubaccountConfigs.forApp,
        )
        val forProgrammaticTraders = AccountConfigs(
            retrieveBalances = true,
            retrieveSubaccounts = true,
            retrieveHistoricalTradingRewards = true,
            retrieveLaunchIncentivePoints = true,
            transferNobleBalances = true,
            onboardingConfigs = OnboardingConfigs.forProgrammaticTraders,
            subaccountConfigs = SubaccountConfigs.forProgrammaticTraders,
        )
    }
}

@JsExport
data class OnboardingConfigs(
    val retrieveSquidRoutes: Boolean,
) {
    enum class SquidVersion {
        V1, V2, V2DepositOnly, V2WithdrawalOnly,
    }

    var squidVersion: SquidVersion = SquidVersion.V1

    companion object {
        val forApp = OnboardingConfigs(
            retrieveSquidRoutes = true,
        )
        val forProgrammaticTraders = OnboardingConfigs(
            retrieveSquidRoutes = false,
        )
    }
}

@JsExport
class AppConfigsV2(
    val systemConfigs: SystemConfigs,
    val marketConfigs: MarketConfigs,
    val accountConfigs: AccountConfigs,
    val loadRemote: Boolean = true,
    val enableLogger: Boolean = false,
) {
    companion object {
        val forApp = AppConfigsV2(
            systemConfigs = SystemConfigs.forApp,
            marketConfigs = MarketConfigs.forApp,
            accountConfigs = AccountConfigs.forApp,
            loadRemote = true
        )
        val forAppDebug = AppConfigsV2(
            systemConfigs = SystemConfigs.forApp,
            marketConfigs = MarketConfigs.forApp,
            accountConfigs = AccountConfigs.forApp,
            loadRemote = false,
            enableLogger = true
        )
        val forWeb = AppConfigsV2(
            systemConfigs = SystemConfigs.forApp,
            marketConfigs = MarketConfigs.forWeb,
            accountConfigs = AccountConfigs.forApp,
            loadRemote = true
        )
        val forProgrammaticTraders = AppConfigsV2(
            systemConfigs = SystemConfigs.forProgrammaticTraders,
            marketConfigs = MarketConfigs.forProgrammaticTraders,
            accountConfigs = AccountConfigs.forProgrammaticTraders,
            loadRemote = true
        )
    }
}
