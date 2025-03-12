package exchange.dydx.abacus.state.manager.configs

import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IMap
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class V4StateManagerConfigs(
    deploymentUrl: String,
    environment: V4Environment,
) : StateManagerConfigs(deploymentUrl, environment, configs) {
    companion object {
        internal val configs: IMap<String, Any> =
            Json.parseToJsonElement(
                """
                {
                   "paths":{
                      "public":{
                         "fills":"/fills",
                         "candles":"/candles/perpetualMarkets",
                         "config":"/config",
                         "historical-funding":"/historicalFunding",
                         "historical-pnl":"/historical-pnl",
                         "sparklines":"/sparklines",
                         "account":"/addresses",
                         "time":"/time",
                         "screen":"/screen",
                         "complianceScreen":"/compliance/screen",
                         "complianceGeoblock":"/compliance/geoblock",
                         "complianceGeoblockKeplr":"/compliance/geoblock-keplr",
                         "height":"/height",
                         "vaultPositions":"/vault/v1/megavault/positions",
                         "vaultHistoricalPnl":"/vault/v1/megavault/historicalPnl",
                         "vaultMarketPnls":"/vault/v1/vaults/historicalPnl",
                         "transfers":"/transfers/between"
                      },
                      "private":{
                         "account":"/addresses",
                         "fills":"/fills",
                         "historical-pnl":"/historical-pnl",
                         "parent-historical-pnl":"/historical-pnl/parentSubaccountNumber",
                         "transfers":"/transfers",
                         "historicalTradingRewardAggregations":"/historicalTradingRewardAggregations",
                         "parent-fills":"/fills/parentSubaccountNumber",
                         "parent-transfers": "/transfers/parentSubaccountNumber"
                      },
                      "faucet":{
                         "faucet":"/faucet/tokens"
                      },
                      "0xsquid":{
                         "chains":"/v1/chains",
                         "tokens":"/v1/tokens",
                         "route":"/v1/route",
                         "status":"/v1/status"
                      },
                      "configs":{
                         "markets":"/configs/markets.json"
                      },
                      "launchIncentive":{
                         "graphql":"/query/ccar-perpetuals",
                         "points":"/query/api/dydx/points"
                      }
                   },
                   "socket":"/v4/ws",
                   "channels":{
                      "markets":"v4_markets",
                      "trades":"v4_trades",
                      "candles":"v4_candles",
                      "orderbook":"v4_orderbook",
                      "subaccount":"v4_subaccounts",
                      "parent_subaccount":"v4_parent_subaccounts"
                   }
                }
                """.trimIndent(),
            ).jsonObject.toIMap()
    }

    fun candlesChannel(): String? {
        return parser.asString(parser.value(configs, "channels.candles"))
    }

    fun squidStatus(): String? {
        val squid = environment.endpoints.squid ?: return null
        val path = parser.asString(parser.value(configs, "paths.0xsquid.status"))
        return "$squid$path"
    }

    fun squidV2Status(): String? {
        val path = parser.asString(parser.value(configs, "paths.0xsquid.status"))
        return "$squidV2Host$path"
    }

    fun squidRoute(): String? {
        val squid = environment.endpoints.squid ?: return null
        val path = parser.asString(parser.value(configs, "paths.0xsquid.route"))
        return "$squid$path"
    }

    fun squidV2Assets(): String? {
        return "$squidV2Host/v2/sdk-info"
    }

    fun squidV2Route(): String? {
        return "$squidV2Host/v2/route"
    }

    fun nobleChainId(): String {
        return if (environment.isMainNet) "noble-1" else "grand-1"
    }

    fun osmosisChainId(): String {
        return if (environment.isMainNet) "osmosis-1" else "osmo-test-5"
    }

    fun neutronChainId(): String {
        return if (environment.isMainNet) "neutron-1" else "pion-1"
    }

    fun skipV1Chains(): String {
        return "$skipHost/v2/info/chains?include_evm=true$includeSvmChains$onlyTestnets"
    }

    fun skipV1Assets(): String {
        return "$skipHost/v2/fungible/assets?include_evm_assets=true$includeSvmAssets$onlyTestnets"
    }

    fun skipV2MsgsDirect(): String {
        return "$skipHost/v2/fungible/msgs_direct"
    }

    fun skipV2Track(): String {
        return "$skipHost/v2/tx/track"
    }

    fun skipV2Status(): String {
        return "$skipHost/v2/tx/status"
    }

    val skipV2Venues = "$skipHost/v2/fungible/venues"

    val nobleDenom = "uusdc"

    private val includeSvmChains: String
        get() {
            return "&include_svm=true"
        }
    private val includeSvmAssets: String
        get() {
            return "&include_svm_assets=true"
        }

    private val onlyTestnets: String
        get() {
            return if (environment.isMainNet) "" else "&only_testnets=true"
        }

    private val skipHost: String
        get() {
            return environment.endpoints.skip ?: "https://api.skip.money"
        }

    private val squidV2Host: String
        get() {
            return if (environment.isMainNet) {
                "https://v2.api.squidrouter.com"
            } else {
                "https://testnet.v2.api.squidrouter.com"
            }
        }

    fun launchIncentivePath(type: String): String? {
        return parser.asString(parser.value(configs, "paths.launchIncentive.$type"))
    }

    fun launchIncentiveUrl(type: String): String? {
        val api = environment.links?.launchIncentive ?: return null
        val path = launchIncentivePath(type) ?: return null
        return "$api$path"
    }
}
