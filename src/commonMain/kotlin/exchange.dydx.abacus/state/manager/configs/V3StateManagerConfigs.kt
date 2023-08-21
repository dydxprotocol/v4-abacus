package exchange.dydx.abacus.state.manager.configs

import exchange.dydx.abacus.state.app.V4Environment
import exchange.dydx.abacus.utils.IMap
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class V3StateManagerConfigs(
    private val environment: V4Environment
): StateManagerConfigs(environment, configs) {
    companion object {
        internal val configs: IMap<String, Any> =
            Json.parseToJsonElement(
                """
                {
                   "paths":{
                      "private":{
                         "fills":"/v3/fills",
                         "historical-pnl":"/v3/historical-pnl",
                         "users":"/v3/users",
                         "transfers":"/v3/transfers"
                      },
                      "public":{
                         "candles":"/v3/candles",
                         "config":"/v3/config",
                         "historical-funding":"/v3/historical-funding",
                         "sparklines":"/v3/candles",
                         "time":"/v3/time",
                         "user-exists":"/v3/users/exists",
                         "username-exists":"/v3/usernames"
                      },
                      "configs":{
                         "0x_assets":"/config/prod/0x_assets.json",
                         "countries":"/config/countries.json",
                         "epoch_start":"/config/epoch_start.json",
                         "fee_discounts":"/config/fee_discounts.json",
                         "fee_tiers":"/config/fee_tiers.json",
                         "markets":"/config/markets.json",
                         "network_configs":"/config/network_configs.json",
                         "version":"/config/version_ios.json",
                         "walletsV2":"/config/prod/walletsV2.json"
                      }
                   },
                   "socket":"/v3/ws",
                   "channels":{
                      "markets":"v3_markets",
                      "trades":"v3_trades",
                      "orderbook":"v3_orderbook",
                      "subaccount":"v3_accounts"
                   }
                }
                """.trimIndent()
            ).jsonObject.toIMap()
    }

    fun referrer(): String? {
        return parser.asString(parser.value(configs, "http.referrer.$environment"))
    }
}