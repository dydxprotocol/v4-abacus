package exchange.dydx.abacus.state.manager.configs

import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IMap
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class V4StateManagerConfigs(
    deploymentUrl: String,
    environment: V4Environment,
): StateManagerConfigs(deploymentUrl, environment, configs) {
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
                         "subaccounts":"/v4/addresses",
                         "time":"/v4/time",
                         "screen":"/v4/screen",
                         "height":"/v4/height"
                      },
                      "private":{
                         "subaccounts":"/v4/addresses",
                         "fills":"/v4/fills",
                         "historical-pnl":"/v4/historical-pnl",
                         "transfers":"/v4/transfers"
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
                      }
                   },
                   "socket":"/v4/ws",
                   "channels":{
                      "markets":"v4_markets",
                      "trades":"v4_trades",
                      "candles":"v4_candles",
                      "orderbook":"v4_orderbook",
                      "subaccount":"v4_subaccounts"
                   }
                }
                """.trimIndent()
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
}