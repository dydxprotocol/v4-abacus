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
    val retrieveWithdrawSafetyChecks: Boolean,
) {
    companion object {
        val forApp = SystemConfigs(
            retrieveServerTime = true,
            retrieveMarketConfigs = true,
            retrieveEquityTiers = true,
            retrieveFeeTiers = true,
            retrieveRewardsParams = true,
            retrieveLaunchIncentiveSeasons = true,
            retrieveWithdrawSafetyChecks = true,
        )
        val forProgrammaticTraders = SystemConfigs(
            retrieveServerTime = true,
            retrieveMarketConfigs = true,
            retrieveEquityTiers = true,
            retrieveFeeTiers = true,
            retrieveRewardsParams = false,
            retrieveLaunchIncentiveSeasons = false,
            retrieveWithdrawSafetyChecks = false,
        )
    }
}

@JsExport
data class MarketsConfigs(
    val retrieveSparklines: Boolean,
    val retrieveCandles: Boolean,
    val retrieveHistoricalFundings: Boolean,
    val subscribeToMarkets: Boolean,
    val subscribeToOrderbook: Boolean,
    val subscribeToTrades: Boolean,
    val subscribeToCandles: Boolean,
) {
    companion object {
        val forApp = MarketsConfigs(
            retrieveSparklines = true,
            retrieveCandles = true,
            retrieveHistoricalFundings = true,
            subscribeToMarkets = true,
            subscribeToOrderbook = true,
            subscribeToTrades = true,
            subscribeToCandles = true,
        )
        val forWeb = MarketsConfigs(
            retrieveSparklines = false,
            retrieveCandles = false,
            retrieveHistoricalFundings = true,
            subscribeToMarkets = true,
            subscribeToOrderbook = true,
            subscribeToTrades = true,
            subscribeToCandles = false,
        )
        val forProgrammaticTraders = MarketsConfigs(
            retrieveSparklines = false,
            retrieveCandles = false,
            retrieveHistoricalFundings = false,
            subscribeToMarkets = true,
            subscribeToOrderbook = true,
            subscribeToTrades = false,
            subscribeToCandles = false,
        )
    }
}

enum class SubaccountSubscriptionType {
    SUBACCOUNT,
    PARENT_SUBACCOUNT,
    NONE,
}

@JsExport
data class SubaccountConfigs(
    val retrieveFills: Boolean,
    val retrieveTransfers: Boolean,
    val retrieveHistoricalPnls: Boolean,
    val subscribeToSubaccount: Boolean,
    val useParentSubaccount: Boolean,
) {
    companion object {
        val forApp = SubaccountConfigs(
            retrieveFills = true,
            retrieveTransfers = true,
            retrieveHistoricalPnls = true,
            subscribeToSubaccount = true,
            useParentSubaccount = false,
        )
        val forAppWithIsolatedMargins = SubaccountConfigs(
            retrieveFills = true,
            retrieveTransfers = true,
            retrieveHistoricalPnls = true,
            subscribeToSubaccount = true,
            useParentSubaccount = true,
        )
        val forProgrammaticTraders = SubaccountConfigs(
            retrieveFills = false,
            retrieveTransfers = false,
            retrieveHistoricalPnls = false,
            subscribeToSubaccount = true,
            useParentSubaccount = false,
        )
    }
}

@JsExport
data class AccountConfigs(
    val retrieveUserFeeTier: Boolean,
    val retrieveUserStats: Boolean,
    val retrieveBalances: Boolean,
    val retrieveSubaccounts: Boolean,
    val retrieveHistoricalTradingRewards: Boolean,
    val retrieveLaunchIncentivePoints: Boolean,
    val transferNobleBalances: Boolean,
    val subaccountConfigs: SubaccountConfigs,
) {
    companion object {
        val forApp = AccountConfigs(
            retrieveUserFeeTier = true,
            retrieveUserStats = true,
            retrieveBalances = true,
            retrieveSubaccounts = true,
            retrieveHistoricalTradingRewards = true,
            retrieveLaunchIncentivePoints = true,
            transferNobleBalances = true,
            subaccountConfigs = SubaccountConfigs.forApp,
        )
        val forAppWithIsolatedMargins = AccountConfigs(
            retrieveUserFeeTier = true,
            retrieveUserStats = true,
            retrieveBalances = true,
            retrieveSubaccounts = true,
            retrieveHistoricalTradingRewards = true,
            retrieveLaunchIncentivePoints = true,
            transferNobleBalances = true,
            subaccountConfigs = SubaccountConfigs.forAppWithIsolatedMargins,
        )
        val forProgrammaticTraders = AccountConfigs(
            retrieveUserFeeTier = true,
            retrieveUserStats = true,
            retrieveBalances = true,
            retrieveSubaccounts = true,
            retrieveHistoricalTradingRewards = true,
            retrieveLaunchIncentivePoints = true,
            transferNobleBalances = true,
            subaccountConfigs = SubaccountConfigs.forProgrammaticTraders,
        )
    }
}

@JsExport
data class OnboardingConfigs(
    val retrieveSquidRoutes: Boolean,
) {
    enum class SquidVersion {
        V2,
        V2DepositOnly,
        V2WithdrawalOnly,
    }

    var squidVersion: SquidVersion = SquidVersion.V2

    enum class RouterVendor {
        Squid,
        Skip
    }

    var routerVendor: RouterVendor = RouterVendor.Squid

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
data class AppConfigsV2(
    val systemConfigs: SystemConfigs,
    val marketConfigs: MarketsConfigs,
    val accountConfigs: AccountConfigs,
    var onboardingConfigs: OnboardingConfigs,
    var loadRemote: Boolean = true,
    var enableLogger: Boolean = false,
    var triggerOrderToast: Boolean = false,
) {
    companion object {
        val forApp = AppConfigsV2(
            systemConfigs = SystemConfigs.forApp,
            marketConfigs = MarketsConfigs.forApp,
            accountConfigs = AccountConfigs.forApp,
            onboardingConfigs = OnboardingConfigs.forApp,
            loadRemote = true,
            triggerOrderToast = true,
        )
        val forAppWithIsolatedMargins = AppConfigsV2(
            systemConfigs = SystemConfigs.forApp,
            marketConfigs = MarketsConfigs.forApp,
            accountConfigs = AccountConfigs.forAppWithIsolatedMargins,
            onboardingConfigs = OnboardingConfigs.forApp,
            loadRemote = true,
            triggerOrderToast = true,
        )
        val forAppDebug = AppConfigsV2(
            systemConfigs = SystemConfigs.forApp,
            marketConfigs = MarketsConfigs.forApp,
            accountConfigs = AccountConfigs.forApp,
            onboardingConfigs = OnboardingConfigs.forApp,
            loadRemote = false,
            enableLogger = true,
            triggerOrderToast = true,
        )
        val forWeb = AppConfigsV2(
            systemConfigs = SystemConfigs.forApp,
            marketConfigs = MarketsConfigs.forWeb,
            accountConfigs = AccountConfigs.forApp,
            onboardingConfigs = OnboardingConfigs.forApp,
            loadRemote = true,
            triggerOrderToast = false,
        )
        val forWebAppWithIsolatedMargins = AppConfigsV2(
            systemConfigs = SystemConfigs.forApp,
            marketConfigs = MarketsConfigs.forWeb,
            accountConfigs = AccountConfigs.forAppWithIsolatedMargins,
            onboardingConfigs = OnboardingConfigs.forApp,
            loadRemote = true,
            triggerOrderToast = false,
        )
        val forProgrammaticTraders = AppConfigsV2(
            systemConfigs = SystemConfigs.forProgrammaticTraders,
            marketConfigs = MarketsConfigs.forProgrammaticTraders,
            accountConfigs = AccountConfigs.forProgrammaticTraders,
            onboardingConfigs = OnboardingConfigs.forProgrammaticTraders,
            loadRemote = true,
            triggerOrderToast = false,
        )
    }
}
