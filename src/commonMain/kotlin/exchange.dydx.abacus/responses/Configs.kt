package exchange.dydx.abacus.responses

import exchange.dydx.abacus.utils.IList
import kotlinx.serialization.Serializable

/**
 * AssetJson from ${V4_WEB_URL}/configs/markets.json
 */
@Serializable
data class AssetJson(
    val name: String,
    val websiteLink: String?,
    val whitepaperLink: String?,
    val coinMarketCapsLink: String?,
    val tags: IList<String>,
)
