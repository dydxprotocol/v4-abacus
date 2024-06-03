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
                         "fills":"/v4/fills",
                         "candles":"/v4/candles/perpetualMarkets",
                         "config":"/v4/config",
                         "historical-funding":"/v4/historicalFunding",
                         "historical-pnl":"/v4/historical-pnl",
                         "sparklines":"/v4/sparklines",
                         "account":"/v4/addresses",
                         "time":"/v4/time",
                         "screen":"/v4/screen",
                         "complianceScreen":"/v4/compliance/screen",
                         "complianceGeoblock":"/v4/compliance/geoblock",
                         "height":"/v4/height"
                      },
                      "private":{
                         "account":"/v4/addresses",
                         "fills":"/v4/fills",
                         "historical-pnl":"/v4/historical-pnl",
                         "transfers":"/v4/transfers",
                         "historicalTradingRewardAggregations":"/v4/historicalTradingRewardAggregations",
                         "parent-fills":"/v4/fills/parentSubaccountNumber",
                         "parent-transfers": "/v4/transfers/parentSubaccountNumber"
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

    fun squidChains(): String? {
        val squid = environment.endpoints.squid ?: return null
        val path = parser.asString(parser.value(configs, "paths.0xsquid.chains"))
        return "$squid$path"
    }

    fun squidToken(): String? {
        val squid = environment.endpoints.squid ?: return null
        val path = parser.asString(parser.value(configs, "paths.0xsquid.tokens"))
        return "$squid$path"
    }

    fun squidV2Assets(): String? {
        return "$squidV2Host/v2/sdk-info"
    }

    fun squidV2Route(): String? {
        return "$squidV2Host/v2/route"
    }

    fun nobleChainId(): String? {
        return if (environment.isMainNet) "noble-1" else "grand-1"
    }

    fun skipV1Chains(): String {
        return "$skipHost/v1/info/chains?include_evm=true"
    }

    fun skipV1Assets(): String {
        return "$skipHost/v1/fungible/assets?include_evm_assets=true"
    }

    fun nobleDenom(): String? {
        return "uusdc"
    }

    private val skipHost: String
        get() {
            return "https://api.skip.money"
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
