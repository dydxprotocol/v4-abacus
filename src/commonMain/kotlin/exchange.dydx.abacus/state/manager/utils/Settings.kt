package exchange.dydx.abacus.state.manager

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
class AppConfigs(
    var subscribeToCandles: Boolean,
    var loadRemote: Boolean = true,
    var enableLogger: Boolean = false,
) {
    enum class SquidVersion {
        V2,
        V2DepositOnly,
        V2WithdrawalOnly,
    }
    var squidVersion: SquidVersion = SquidVersion.V2

    enum class RouterVendor {
        Skip,
        Squid
    }
    var routerVendor: RouterVendor = RouterVendor.Squid

    companion object {
        val forApp = AppConfigs(subscribeToCandles = true, loadRemote = true)
        val forAppDebug = AppConfigs(subscribeToCandles = true, loadRemote = false, enableLogger = true)
        val forWeb = AppConfigs(subscribeToCandles = false, loadRemote = true)
    }
}

@JsExport
@Serializable
enum class HistoricalPnlPeriod(val rawValue: String) {
    Period1d("1d"),
    Period7d("7d"),
    Period30d("30d"),
    Period90d("90d");

    companion object {
        operator fun invoke(rawValue: String) =
            HistoricalPnlPeriod.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class HistoricalTradingRewardsPeriod(val rawValue: String) {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY");

    companion object {
        operator fun invoke(rawValue: String) =
            HistoricalTradingRewardsPeriod.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class CandlesPeriod(val rawValue: String) {
    Period1m("1m"),
    Period5m("5m"),
    Period15m("15m"),
    Period30m("30m"),
    Period1h("1h"),
    Period4h("4h"),
    Period1d("1d");

    companion object {
        operator fun invoke(rawValue: String) =
            CandlesPeriod.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class OrderbookGrouping(val rawValue: Int) {
    none(1),
    x10(10),
    x100(100),
    x1000(1000);

    companion object {
        operator fun invoke(rawValue: Int) =
            OrderbookGrouping.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
enum class ApiData {
    HISTORICAL_PNLS,
    HISTORICAL_TRADING_REWARDS,
}

@JsExport
enum class ConfigFile(val rawValue: String) {
    DOCUMENTATION("DOCUMENTATION") {
        override val path: String
            get() = "/configs/documentation.json"
    },
    ENV("ENV") {
        override val path: String
            get() = "/configs/v1/env.json"
    };

    abstract val path: String
}
