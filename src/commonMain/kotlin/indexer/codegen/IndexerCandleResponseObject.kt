/**
 * Indexer API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v1.0.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package indexer.codegen

import kotlinx.serialization.Serializable

/**
 *
 * @param startedAt
 * @param ticker
 * @param resolution
 * @param low
 * @param high
 * @param &#x60;open&#x60;
 * @param close
 * @param baseTokenVolume
 * @param usdVolume
 * @param trades
 * @param startingOpenInterest
 * @param id
 */
@Serializable
data class IndexerCandleResponseObject(

    val startedAt: IndexerIsoString? = null,
    val ticker: kotlin.String? = null,
    val resolution: IndexerCandleResolution? = null,
    val low: kotlin.String? = null,
    val high: kotlin.String? = null,
    val `open`: kotlin.String? = null,
    val close: kotlin.String? = null,
    val baseTokenVolume: kotlin.String? = null,
    val usdVolume: kotlin.String? = null,
    val trades: kotlin.Double? = null,
    val startingOpenInterest: kotlin.String? = null,
    val id: kotlin.String? = null
)
