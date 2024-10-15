package indexer.models.configs

import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kotlinx.serialization.Serializable

/**
 * AssetJson from ${V4_WEB_URL}/configs/markets.json
 */
@Serializable
data class ConfigsMarketAsset(
    val name: String,
    val websiteLink: String? = null,
    val whitepaperLink: String? = null,
    val coinMarketCapsLink: String? = null,
    val tags: IList<String>? = null,
)

/**
 * @description Asset from MetadataService Info response
 */
@Suppress("ConstructorParameterNaming")
@Serializable
data class ConfigsAssetMetadata(
    val name: String,
    val logo: String,
    val urls: IMap<String, String?>,
    val sector_tags: IList<String>? = null,
//    val exchanges: IList<Any>
)
