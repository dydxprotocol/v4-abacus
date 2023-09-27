package exchange.dydx.abacus.output

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class AssetResources(
    val websiteLink: String?,
    val whitepaperLink: String?,
    val coinMarketCapsLink: String?,
    val imageUrl: String?,
    val primaryDescriptionKey: String?,
    val secondaryDescriptionKey: String?
) {
    companion object {
        internal fun create(
            existing: AssetResources?,
            parser: ParserProtocol,
            data: Map<*, *>?
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
                    AssetResources(
                        websiteLink,
                        whitepaperLink,
                        coinMarketCapsLink,
                        imageUrl,
                        primaryDescriptionKey,
                        secondaryDescriptionKey
                    )
                } else existing
            }
            DebugLogger.debug("Asset Resources not valid")
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
    val symbol: String?,
    val name: String?,
    val tags: IList<String>?,
    val circulatingSupply: Double?,
    val resources: AssetResources?
) {
    companion object {
        internal fun create(
            existing: Asset?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): Asset? {
            data?.let {
                val id = parser.asString(data["id"])
                val resourcesData = parser.asMap(data["resources"])
                if (id != null) {
                    val resources =
                        AssetResources.create(existing?.resources, parser, resourcesData)
                    val symbol = parser.asString(data["symbol"])
                    val name = parser.asString(data["name"])
                    val tags = parser.asStrings(data["tags"])
                    val circulatingSupply = parser.asDouble(data["circulatingSupply"])

                    return if (existing?.id != id ||
                        existing.symbol != symbol ||
                        existing.name != name ||
                        existing.tags != tags ||
                        existing.circulatingSupply != circulatingSupply ||
                        existing.resources !== resources
                    ) {
                        Asset(id, symbol, name, tags, circulatingSupply, resources)
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Asset not valid")
            return null
        }
    }
}
