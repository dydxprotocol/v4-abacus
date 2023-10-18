package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.JsExport
import kollections.iMutableMapOf
import kollections.toIList

@JsExport
data class IndexerURIs(
    val api: String,
    val socket: String
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): IndexerURIs? {
            val api = parser.asString(data["api"]) ?: return null
            val socket = parser.asString(data["socket"]) ?: return null
            return IndexerURIs(api, socket)
        }
    }
}

@JsExport
data class EnvironmentEndpoints(
    val indexers: IList<IndexerURIs>?,
    val validators: IList<String>?,
    val faucet: String?,
    val squid: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): EnvironmentEndpoints? {
            val indexers = parser.asList(data["indexers"])?.mapNotNull {
                IndexerURIs.parse(parser.asMap(it) ?: return null, parser)
            }?.toIList() ?: return null
            val validators = parser.asList(data["validators"])?.mapNotNull {
                parser.asString(it)
            }?.toIList() ?: return null
            val faucet = parser.asString(data["faucet"])
            val squid = parser.asString(data["0xsquid"])
            return EnvironmentEndpoints(indexers, validators, faucet, squid)
        }
    }
}

@JsExport
data class EnvironmentLinks(
    val tos: String?,
    val privacy: String?,
    val mintscan: String?,
    val documentation: String?,
    val community: String?,
    val feedback: String?,
    val blogs: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): EnvironmentLinks? {
            val tos = parser.asString(data["tos"])
            val privacy = parser.asString(data["privacy"])
            val mintscan = parser.asString(data["mintscan"])
            val documentation = parser.asString(data["documentation"])
            val community = parser.asString(data["community"])
            val feedback = parser.asString(data["feedback"])
            val blogs = parser.asString(data["blogs"])
            return EnvironmentLinks(
                tos,
                privacy,
                mintscan,
                documentation,
                community,
                feedback,
                blogs
            )
        }
    }
}

@JsExport
data class TokenInfo(
    val name: String,
    val denom: String,
    val decimals: Int,
    val gasDenom: String?,
    val imageUrl: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
            defaultDecimals: Int,
        ): TokenInfo? {
            val name = parser.asString(data["name"]) ?: return null
            val denom = parser.asString(data["denom"]) ?: return null
            val decimals = parser.asInt(data["decimals"]) ?: defaultDecimals
            val gasDenom = parser.asString(data["gasDenom"])
            val imageUrl = parser.asString(data["imageUrl"])
            return TokenInfo(name, denom, decimals, gasDenom, imageUrl)
        }
    }
}

@JsExport
data class WalletConnectClient(
    val name: String,
    val description: String,
    val iconUrl: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): WalletConnectClient? {
            val name = parser.asString(data?.get("name")) ?: return null
            val description = parser.asString(data?.get("description")) ?: return null
            val iconUrl = parser.asString(data?.get("iconUrl"))
            return WalletConnectClient(name, description, "$deploymentUri$iconUrl")
        }
    }
}

@JsExport
data class WalletConnectV1(
    val bridgeUrl: String,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): WalletConnectV1? {
            val bridgeUrl = parser.asString(data?.get("bridgeUrl")) ?: return null
            return WalletConnectV1(bridgeUrl)
        }
    }
}

@JsExport
data class WalletConnectV2(
    val projectId: String,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): WalletConnectV2? {
            val projectId = parser.asString(data?.get("projectId")) ?: return null
            return WalletConnectV2(projectId)
        }
    }
}

@JsExport
data class WalletConnect(
    val client: WalletConnectClient,
    val v1: WalletConnectV1?,
    val v2: WalletConnectV2?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): WalletConnect? {
            val client =
                WalletConnectClient.parse(parser.asMap(data?.get("client")), parser, deploymentUri)
                    ?: return null
            val v1 = WalletConnectV1.parse(parser.asMap(data?.get("v1")), parser)
            val v2 = WalletConnectV2.parse(parser.asMap(data?.get("v2")), parser)
            return if (v1 != null || v2 != null)
                WalletConnect(client, v1, v2)
            else null
        }
    }
}

@JsExport
data class WalletSegue(
    val callbackUrl: String,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): WalletSegue? {
            val callbackUrl = parser.asString(data?.get("callbackUrl")) ?: return null
            return WalletSegue("$deploymentUri$callbackUrl")
        }
    }
}

@JsExport
data class WalletConnection(
    val walletConnect: WalletConnect?,
    val walletSegue: WalletSegue?,
    val images: String,
    val signTypedDataAction: String?,
    val signTypedDataDomainName: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): WalletConnection? {
            val walletConnect =
                WalletConnect.parse(
                    parser.asMap(data?.get("walletConnect"))
                        ?: parser.asMap(data?.get("walletconnect")),
                    parser,
                    deploymentUri
                )
            val walletSegue =
                WalletSegue.parse(parser.asMap(data?.get("walletSegue")), parser, deploymentUri)
            val images = parser.asString(data?.get("images")) ?: return null
            val signTypedDataAction = parser.asString(data?.get("signTypedDataAction"))
            val signTypedDataDomainName = parser.asString(data?.get("signTypedDataDomainName"))
            return if (walletConnect != null || walletSegue != null)
                WalletConnection(
                    walletConnect,
                    walletSegue,
                    "$deploymentUri$images",
                    signTypedDataAction,
                    signTypedDataDomainName
                )
            else null
        }
    }
}


@JsExport
open class Environment(
    val id: String,
    val name: String?,
    val ethereumChainId: String,
    val dydxChainId: String?,
    val isMainNet: Boolean,
    val endpoints: EnvironmentEndpoints,
    val links: EnvironmentLinks?,
    val walletConnection: WalletConnection?,
)

@JsExport
class V4Environment(
    id: String,
    name: String?,
    ethereumChainId: String,
    dydxChainId: String?,
    val chainName: String?,
    val chainLogo: String?,
    isMainNet: Boolean,
    endpoints: EnvironmentEndpoints,
    links: EnvironmentLinks?,
    walletConnection: WalletConnection?,
    val tokens: IMap<String, TokenInfo>,
) : Environment(
    id,
    name,
    ethereumChainId,
    dydxChainId,
    isMainNet,
    endpoints,
    links,
    walletConnection,
) {
    companion object {
        fun parse(
            id: String,
            data: Map<String, Any>,
            parser: ParserProtocol,
            deploymentUri: String,
        ): V4Environment? {
            val name = parser.asString(data["name"])
            val ethereumChainId = parser.asString(data["ethereumChainId"]) ?: return null
            val dydxChainId = parser.asString(data["dydxChainId"])
            val chainName = parser.asString(data["chainName"])
            val chainLogo = parser.asString(data["chainLogo"])
            val isMainNet = parser.asBool(data["isMainNet"]) ?: return null
            val endpoints =
                EnvironmentEndpoints.parse(parser.asMap(data["endpoints"]) ?: return null, parser)
                    ?: return null
            val links = EnvironmentLinks.parse(parser.asMap(data["links"]) ?: return null, parser)
            val walletConnection = WalletConnection.parse(
                parser.asMap(data["walletConnection"])
                    ?: parser.asMap(data["wallets"]),
                parser,
                deploymentUri
            )
            val tokens = parseTokens(parser.asMap(data["tokens"]), parser, deploymentUri)

            return V4Environment(
                id,
                name,
                ethereumChainId,
                dydxChainId,
                chainName,
                "$deploymentUri$chainLogo",
                isMainNet,
                endpoints,
                links,
                walletConnection,
                tokens
            )
        }

        private fun parseTokens(
            item: IMap<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): IMap<String, TokenInfo> {
            val tokens = iMutableMapOf<String, TokenInfo>()
            if (item != null) {
                for (key in item.keys) {
                    val token = parser.asMap(item[key])
                    if (token != null) {
                        val name = parser.asString(token["name"]) ?: continue
                        val denom = parser.asString(token["denom"]) ?: continue
                        val decimals =
                            parser.asInt(token["decimals"]) ?: (if (key == "chain") 18 else 6)
                        val gasDenom = parser.asString(token["gasDenom"])
                        val imageUrl = parser.asString(token["image"])?.let {
                            "$deploymentUri$it"
                        }
                        tokens[key] = TokenInfo(
                            name,
                            denom,
                            decimals,
                            gasDenom,
                            imageUrl
                        )
                    }
                }
            }

            return tokens
        }
    }
}
