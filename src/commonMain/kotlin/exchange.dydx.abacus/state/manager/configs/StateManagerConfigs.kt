package exchange.dydx.abacus.state.manager.configs

import exchange.dydx.abacus.state.manager.IndexerURIs
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Parser

open class StateManagerConfigs(
    private val environment: V4Environment,
    private val configs: IMap<String, Any>
) {

    internal val indexerConfigs: IList<IndexerURIs>?
        get() = environment.URIs.indexers

    internal var indexerConfig: IndexerURIs? = null

    protected val parser = Parser()

    fun websocketPath(): String? {
        return parser.asString(parser.value(configs, "socket"))
    }

    fun publicApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "paths.public.$type"))
    }


    fun publicApiUrl(type: String): String? {
        val api = indexerConfig?.api ?: return null
        val path = publicApiPath(type) ?: return null
        return "$api$path"
    }

    fun heightUrl(indexerURIs: IndexerURIs): String? {
        val api = indexerURIs.api
        val path = publicApiPath("height") ?: return null
        return "$api$path"
    }

    private fun privateApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "paths.private.$type"))
    }

    fun privateApiUrl(type: String): String? {
        val api = indexerConfig?.api ?: return null
        val path = privateApiPath(type) ?: return null
        return "$api$path"
    }

    private fun configsApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "paths.configs.$type"))
    }

    fun configsUrl(type: String): String? {
        val api = environment.URIs.configs
        val path = configsApiPath(type) ?: return null
        return "$api$path"
    }

    private fun faucetApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "paths.faucet.$type"))
    }

    fun faucetUrl(): String? {
        return environment.URIs.faucet
    }

    fun marketsChannel(): String? {
        return parser.asString(parser.value(configs, "channels.markets"))
    }

    fun marketTradesChannel(): String? {
        return parser.asString(parser.value(configs, "channels.trades"))
    }

    fun marketOrderbookChannel(): String? {
        return parser.asString(parser.value(configs, "channels.orderbook"))
    }

    fun marketCandlesChannel(): String? {
        return parser.asString(parser.value(configs, "channels.candles"))
    }

    fun subaccountChannel(): String? {
        return parser.asString(parser.value(configs, "channels.subaccount"))
    }

    fun websocketUrl(): String? {
        val socket = indexerConfig?.socket ?: return null
        val path = websocketPath() ?: return null
        return "$socket$path"
    }

    fun validatorUrls(): IList<String>? {
        return environment.URIs.validators
    }

    fun isIndexer(url: String): Boolean {
        return environment.URIs.indexers?.any { url.contains(it.api) } ?: false
    }
}