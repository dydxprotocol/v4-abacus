package indexer.models.configs

import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kotlinx.serialization.Serializable

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

@Suppress("ConstructorParameterNaming")
@Serializable
data class ConfigsAssetMetadataPrice(
    val price: Double,
    val percent_change_24h: Double,
    val volume_24h: Double,
    val market_cap: Double,
    val self_reported_market_cap: Double
)
