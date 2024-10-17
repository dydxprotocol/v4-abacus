package exchange.dydx.abacus.output

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class AssetResources(
    val websiteLink: String?,
    val whitepaperLink: String?,
    val coinMarketCapsLink: String?,
    val imageUrl: String?,
    val primaryDescription: String?,
    val secondaryDescription: String?,
    val primaryDescriptionKey: String?,
    val secondaryDescriptionKey: String?
) {
    companion object {
        internal fun create(
            existing: AssetResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): AssetResources? {
            data?.let {
                val websiteLink = parser.asString(data["websiteLink"])
                val whitepaperLink = parser.asString(data["whitepaperLink"])
                val coinMarketCapsLink = parser.asString(data["coinMarketCapsLink"])
                val imageUrl = parser.asString(data["imageUrl"])
                val primaryDescriptionKey = parser.asString(data["primaryDescriptionKey"])
                val secondaryDescriptionKey = parser.asString(data["secondaryDescriptionKey"])
                return if (existing?.websiteLink != websiteLink ||
                    existing?.whitepaperLink != whitepaperLink ||
                    existing?.coinMarketCapsLink != coinMarketCapsLink ||
                    existing?.imageUrl != imageUrl ||
                    existing?.primaryDescriptionKey != primaryDescriptionKey ||
                    existing?.secondaryDescriptionKey != secondaryDescriptionKey
                ) {
                    val primaryDescription =
                        if (primaryDescriptionKey != null) localizer?.localize(primaryDescriptionKey) else null
                    val secondaryDescription =
                        if (secondaryDescriptionKey != null) localizer?.localize(secondaryDescriptionKey) else null
                    AssetResources(
                        websiteLink,
                        whitepaperLink,
                        coinMarketCapsLink,
                        imageUrl,
                        primaryDescription,
                        secondaryDescription,
                        primaryDescriptionKey,
                        secondaryDescriptionKey,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Asset Resources not valid" }
            return null
        }
    }
}

/*
depending on the timing of v3_markets socket channel and /config/markets.json,
the object may contain empty fields until both payloads are received and processed
*/
@JsExport
@Serializable
data class Asset(
    val id: String,
    val name: String?,
    val tags: IList<String>?,
    val resources: AssetResources?
) {
    companion object {
        internal fun create(
            existing: Asset?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): Asset? {
            data?.let {
                val id = parser.asString(data["id"])
                val resourcesData = parser.asMap(data["resources"])
                if (id != null) {
                    val resources = AssetResources.create(
                        existing?.resources,
                        parser,
                        resourcesData,
                        localizer,
                    )
                    val name = parser.asString(data["name"])
                    val tags = parser.asStrings(data["tags"])

                    return if (existing?.id != id ||
                        existing.name != name ||
                        existing.tags != tags ||
                        existing.resources !== resources
                    ) {
                        Asset(id, name, tags, resources)
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Asset not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
enum class AssetTags(val rawValue: String) {
    MEMES("memes"),
    AI("ai-big-data"),
    GAMING("gaming"),
    RWA("real-world-assets"),
    DEPIN("depin"),
    LAYER1("layer-1"),
    LAYER2("layer-2"),
    DEFI("defi"),
}
