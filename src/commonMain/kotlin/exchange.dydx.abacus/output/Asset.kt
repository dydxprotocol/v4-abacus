package exchange.dydx.abacus.output

import exchange.dydx.abacus.utils.IList
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
)

@JsExport
@Serializable
data class Asset(
    val id: String,
    val name: String?,
    val tags: IList<String>?,
    val resources: AssetResources?
) {
    val displayableAssetId: String get() {
        return id.split(",").firstOrNull() ?: ""
    }
}
