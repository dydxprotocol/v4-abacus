package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.ServerTime
import kollections.JsExport
import kollections.iMutableMapOf
import kollections.toIList
import kotlin.time.Duration.Companion.days

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
    val skip: String?,
    val metadataService: String?,
    val nobleValidator: String?,
    val geo: String?,
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
            val skip = parser.asString(data["skip"])
            val metadataService = parser.asString(data["metadataService"])
            val nobleValidator = parser.asString(data["nobleValidator"])
            val geo = parser.asString(data["geo"])
            return EnvironmentEndpoints(
                indexers = indexers,
                validators = validators,
                faucet = faucet,
                squid = squid,
                skip = skip,
                metadataService = metadataService,
                nobleValidator = nobleValidator,
                geo = geo,
            )
        }
    }
}

@JsExport
data class EnvironmentLinks(
    val tos: String?,
    val privacy: String?,
    val mintscan: String?,
    val mintscanBase: String?,
    val documentation: String?,
    val community: String?,
    val feedback: String?,
    val blogs: String?,
    val help: String?,
    val vaultLearnMore: String?,
    val vaultTos: String?,
    val launchIncentive: String?,
    val statusPage: String?,
    val withdrawalGateLearnMore: String?,
    val equityTiersLearnMore: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): EnvironmentLinks {
            val tos = parser.asString(data["tos"])
            val privacy = parser.asString(data["privacy"])
            val mintscan = parser.asString(data["mintscan"])
            val mintscanBase = parser.asString(data["mintscanBase"])
            val documentation = parser.asString(data["documentation"])
            val community = parser.asString(data["community"])
            val feedback = parser.asString(data["feedback"])
            val blogs = parser.asString(data["blogs"])
            val help = parser.asString(data["help"])
            val vaultLearnMore = parser.asString(data["vaultLearnMore"])
            val launchIncentive = parser.asString(data["launchIncentive"])
            val statusPage = parser.asString(data["statusPage"])
            val withdrawalGateLearnMore = parser.asString(data["withdrawalGateLearnMore"])
            val equityTiersLearnMore = parser.asString(data["equityTiersLearnMore"])
            val vaultTos = parser.asString(data["vaultTos"])
            return EnvironmentLinks(
                tos,
                privacy,
                mintscan,
                mintscanBase,
                documentation,
                community,
                feedback,
                blogs,
                help,
                vaultLearnMore,
                vaultTos,
                launchIncentive,
                statusPage,
                withdrawalGateLearnMore,
                equityTiersLearnMore,
            )
        }
    }
}

@JsExport
data class EnvironmentFeatureFlags(
    val withdrawalSafetyEnabled: Boolean,
    val isSlTpLimitOrdersEnabled: Boolean,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): EnvironmentFeatureFlags {
            val withdrawalSafetyEnabled = parser.asBool(data?.get("withdrawalSafetyEnabled")) ?: false
            val isSlTpLimitOrdersEnabled = parser.asBool(data?.get("isSlTpLimitOrdersEnabled")) ?: false

            return EnvironmentFeatureFlags(
                withdrawalSafetyEnabled,
                isSlTpLimitOrdersEnabled,
            )
        }
    }
}

@JsExport
data class EnvironmentGovernanceNewMarketProposal(
    val initialDepositAmount: Int,
    val delayBlocks: Int,
    val newMarketsMethodology: String,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): EnvironmentGovernanceNewMarketProposal? {
            val initialDepositAmount = parser.asInt(data?.get("initialDepositAmount")) ?: return null
            val delayBlocks = parser.asInt(data?.get("delayBlocks")) ?: return null
            val newMarketsMethodology = parser.asString(data?.get("newMarketsMethodology")) ?: return null

            return EnvironmentGovernanceNewMarketProposal(
                initialDepositAmount,
                delayBlocks,
                newMarketsMethodology,
            )
        }
    }
}

@JsExport
data class EnvironmentGovernance(
    val newMarketProposal: EnvironmentGovernanceNewMarketProposal,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): EnvironmentGovernance? {
            val newMarketsMethodologyData = parser.asMap(data?.get("newMarketsMethodology")) ?: return null
            val newMarketsMethodology = EnvironmentGovernanceNewMarketProposal.parse(newMarketsMethodologyData, parser) ?: return null

            return EnvironmentGovernance(
                newMarketsMethodology,
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
            return if (v1 != null || v2 != null) {
                WalletConnect(client, v1, v2)
            } else {
                null
            }
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
                    deploymentUri,
                )
            val walletSegue =
                WalletSegue.parse(parser.asMap(data?.get("walletSegue")), parser, deploymentUri)
            val images = parser.asString(data?.get("images")) ?: return null
            val signTypedDataAction = parser.asString(data?.get("signTypedDataAction"))
            val signTypedDataDomainName = parser.asString(data?.get("signTypedDataDomainName"))
            return if (walletConnect != null || walletSegue != null) {
                WalletConnection(
                    walletConnect,
                    walletSegue,
                    "$deploymentUri$images",
                    signTypedDataAction,
                    signTypedDataDomainName,
                )
            } else {
                null
            }
        }
    }
}

@JsExport
class AppsRequirements(
    val ios: AppRequirements?,
    val android: AppRequirements?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
            localizer: LocalizerProtocol?
        ): AppsRequirements {
            val ios = AppRequirements.parse(
                parser.asMap(data["ios"]),
                parser,
                localizer,
            )
            val android = AppRequirements.parse(
                parser.asMap(data["android"]),
                parser,
                localizer,
            )
            return AppsRequirements(ios, android)
        }
    }
}

@JsExport
class AppRequirements(
    val minimalVersion: String,
    val build: Int,
    val url: String,
    val title: String?,
    val text: String?,
    val action: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            localizer: LocalizerProtocol?
        ): AppRequirements? {
            if (data == null) return null
            val minimalVersion = parser.asString(data["minimalVersion"]) ?: return null
            val build = parser.asInt(data["build"]) ?: return null
            val url = parser.asString(data["url"]) ?: return null
            val title = localizer?.localize("FORCED_UPDATE.TITLE")
            val text = localizer?.localize("FORCED_UPDATE.TEXT")
            val action = localizer?.localize("FORCED_UPDATE.ACTION")
            return AppRequirements(minimalVersion, build, url, title, text, action)
        }
    }
}

@JsExport
open class Environment(
    val id: String,
    val name: String?,
    val ethereumChainId: String,
    val dydxChainId: String?,
    val squidIntegratorId: String?,
    val rewardsHistoryStartDateMs: String,
    val isMainNet: Boolean,
    val endpoints: EnvironmentEndpoints,
    val links: EnvironmentLinks?,
    val walletConnection: WalletConnection?,
    val apps: AppsRequirements?,
    val governance: EnvironmentGovernance?,
    val featureFlags: EnvironmentFeatureFlags,
)

@JsExport
class V4Environment(
    id: String,
    name: String?,
    ethereumChainId: String,
    dydxChainId: String?,
    squidIntegratorId: String?,
    val chainName: String?,
    val chainLogo: String?,
    rewardsHistoryStartDateMs: String,
    isMainNet: Boolean,
    endpoints: EnvironmentEndpoints,
    links: EnvironmentLinks?,
    walletConnection: WalletConnection?,
    apps: AppsRequirements?,
    val tokens: IMap<String, TokenInfo>,
    governance: EnvironmentGovernance?,
    featureFlags: EnvironmentFeatureFlags,
) : Environment(
    id,
    name,
    ethereumChainId,
    dydxChainId,
    squidIntegratorId,
    rewardsHistoryStartDateMs,
    isMainNet,
    endpoints,
    links,
    walletConnection,
    apps,
    governance,
    featureFlags,
) {
    companion object {
        fun parse(
            id: String,
            data: Map<String, Any>,
            parser: ParserProtocol,
            deploymentUri: String,
            localizer: LocalizerProtocol?,
            tokensData: Map<String, Any>?,
            linksData: Map<String, Any>?,
            walletsData: Map<String, Any>?,
            governanceData: Map<String, Any>?,
        ): V4Environment? {
            val name = parser.asString(data["name"])
            val ethereumChainId = parser.asString(data["ethereumChainId"]) ?: return null
            val dydxChainId = parser.asString(data["dydxChainId"])
            val squidIntegratorId = parser.asString(data["squidIntegratorId"])
            val chainName = parser.asString(data["chainName"])
            val chainLogo = parser.asString(data["chainLogo"])
            val rewardsHistoryStartDateMs = parser.asString(data["rewardsHistoryStartDateMs"]) ?: ServerTime.now().minus(180.days).toEpochMilliseconds().toString()
            val isMainNet = parser.asBool(data["isMainNet"]) ?: return null
            val endpoints =
                EnvironmentEndpoints.parse(parser.asNativeMap(data["endpoints"]) ?: return null, parser)
                    ?: return null
            val links = EnvironmentLinks.parse(linksData ?: parser.asNativeMap(data["links"]) ?: return null, parser)
            val walletConnection = WalletConnection.parse(
                walletsData
                    ?: parser.asNativeMap(data["walletConnection"])
                    ?: parser.asNativeMap(data["wallets"]),
                parser,
                deploymentUri,
            )
            val appsData = parser.asNativeMap(data["apps"])
            val apps = if (appsData != null) {
                AppsRequirements.parse(appsData, parser, localizer)
            } else {
                null
            }
            val tokens = parseTokens(tokensData ?: parser.asNativeMap(data["tokens"]), parser, deploymentUri)
            val governance = EnvironmentGovernance.parse(governanceData ?: parser.asNativeMap(data["governance"]) ?: return null, parser)

            val featureFlags = EnvironmentFeatureFlags.parse(parser.asMap(data["featureFlags"]), parser)

            return V4Environment(
                id = id,
                name = name,
                ethereumChainId = ethereumChainId,
                dydxChainId = dydxChainId,
                squidIntegratorId = squidIntegratorId,
                chainName = chainName,
                chainLogo = "$deploymentUri$chainLogo",
                rewardsHistoryStartDateMs = rewardsHistoryStartDateMs,
                isMainNet = isMainNet,
                endpoints = endpoints,
                links = links,
                walletConnection = walletConnection,
                apps = apps,
                tokens = tokens,
                governance = governance,
                featureFlags = featureFlags,
            )
        }

        private const val DEFAULT_CHAIN_DECIMALS = 18
        private const val DEFAULT_NON_CHAIN_DECIMALS = 6
        private fun parseTokens(
            item: Map<String, Any>?,
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
                            parser.asInt(token["decimals"]) ?: (if (key == "chain") DEFAULT_CHAIN_DECIMALS else DEFAULT_NON_CHAIN_DECIMALS)
                        val gasDenom = parser.asString(token["gasDenom"])
                        val imageUrl = parser.asString(token["image"])?.let {
                            "$deploymentUri$it"
                        }
                        tokens[key] = TokenInfo(
                            name = name,
                            denom = denom,
                            decimals = decimals,
                            gasDenom = gasDenom,
                            imageUrl = imageUrl,
                        )
                    }
                }
            }

            return tokens
        }
    }
}

@JsExport
@Suppress("PropertyName")
data object StatsigConfig {
    var dc_max_safe_bridge_fees: Float = Float.POSITIVE_INFINITY
    var ff_enable_limit_close: Boolean = false
    var ff_enable_timestamp_nonce: Boolean = false
}

@JsExport
@Suppress("PropertyName")
data object AutoSweepConfig {
    var disable_autosweep: Boolean = false
}

@JsExport
class AppSettings(
    val ios: AppSetting?,
    val android: AppSetting?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): AppSettings {
            val ios = AppSetting.parse(
                parser.asMap(data["ios"]),
                parser,
            )
            val android = AppSetting.parse(
                parser.asMap(data["android"]),
                parser,
            )
            return AppSettings(ios, android)
        }
    }
}

@JsExport
class AppSetting(
    val scheme: String?
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): AppSetting? {
            if (data == null) return null
            val scheme = parser.asString(data["scheme"])
            return AppSetting(scheme)
        }
    }
}
