package exchange.dydx.abacus.state.manager.configs

import com.ionspin.kotlin.bignum.decimal.BigDecimal
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
                         "complianceGeoblockKeplr":"/v4/compliance/geoblock-keplr",
                         "height":"/v4/height",
                         "vaultPositions":"/v4/vault/v1/megavault/positions",
                         "vaultHistoricalPnl":"/v4/vault/v1/megavault/historicalPnl",
                         "vaultMarketPnls":"/v4/vault/v1/vaults/historicalPnl",
                         "transfers":"/v4/transfers/between",
                         "gasSponsorship":"/v4/gasSponsorship"
                      },
                      "private":{
                         "account":"/v4/addresses",
                         "fills":"/v4/fills",
                         "historical-pnl":"/v4/historical-pnl",
                         "parent-historical-pnl":"/v4/historical-pnl/parentSubaccountNumber",
                         "funding-payment": "/v4/fundingPayments",
                         "parent-funding-payment": "/v4/fundingPayments/parentSubaccount", 
                         "transfers":"/v4/transfers",
                         "historicalTradingRewardAggregations":"/v4/historicalTradingRewardAggregations",
                         "parent-fills":"/v4/fills/parentSubaccountNumber",
                         "parent-transfers": "/v4/transfers/parentSubaccountNumber"
                      },
                      "faucet":{
                         "faucet":"/faucet/tokens"
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

    fun nobleChainId(): String {
        return if (environment.isMainNet) "noble-1" else "grand-1"
    }

    fun injectiveChainId(): String {
        return if (environment.isMainNet) "injective-1" else "injective-888"
    }

    val injectiveEvmChainId = "1776"

    val injectiveUsdcDenom = "erc20:0xa00C59fF5a080D2b954d0c75e46E22a0c371235a"

    val injectiveEvmUsdcAddress = "0xa00C59fF5a080D2b954d0c75e46E22a0c371235a"

    val injectiveFeeDenom = "inj"

    val injectiveIbcTransferGasLimit: BigDecimal = BigDecimal.fromLong(450_000)

    // Price per unit of gas, in inj. Comfortably above the minimum base fee (1.6e8).
    val injectiveGasPrice: BigDecimal = BigDecimal.fromLong(1_000_000_000)

    val injectiveIbcTransferFee: BigDecimal = injectiveIbcTransferGasLimit * injectiveGasPrice

    val injectiveToDydxChannel = "channel-258"

    private val injectiveRestHost: String
        get() {
            return environment.endpoints.injectiveRest
                ?: if (environment.isMainNet) "https://sentry.lcd.injective.network" else "https://testnet.sentry.lcd.injective.network"
        }

    fun injectiveBalanceUrl(address: String, denom: String): String {
        return "$injectiveRestHost/cosmos/bank/v1beta1/balances/$address/by_denom?denom=$denom"
    }

    fun injectiveAccountUrl(address: String): String {
        return "$injectiveRestHost/cosmos/auth/v1beta1/accounts/$address"
    }

    fun injectiveBroadcastUrl(): String {
        return "$injectiveRestHost/cosmos/tx/v1beta1/txs"
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

    fun launchIncentivePath(type: String): String? {
        return parser.asString(parser.value(configs, "paths.launchIncentive.$type"))
    }

    fun launchIncentiveUrl(type: String): String? {
        val api = environment.links?.launchIncentive ?: return null
        val path = launchIncentivePath(type) ?: return null
        return "$api$path"
    }

    fun metadataServiceInfo(): String? {
        val metadataServiceUrl = environment.endpoints.metadataService ?: return null
        return "$metadataServiceUrl/info"
    }

    fun metadataServicePrices(): String? {
        val metadataServiceUrl = environment.endpoints.metadataService ?: return null
        return "$metadataServiceUrl/prices"
    }
}
