package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class AssetProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val assetKeyMap = mapOf(
        "string" to mapOf(
            "baseAsset" to "symbol"
        )
    )

    private val assetConfigurationsResourcesKeyMap = mapOf(
        "string" to mapOf(
            "websiteLink" to "websiteLink",
            "whitepaperLink" to "whitepaperLink",
            "coinMarketCapsLink" to "coinMarketCapsLink",
            "imageUrl" to "imageUrl",
            "primaryDescriptionKey" to "primaryDescriptionKey",
            "secondaryDescriptionKey" to "secondaryDescriptionKey"
        )
    )

    private val assetConfigurationsKeyMap = mapOf(
        "string" to mapOf(
            "baseSymbol" to "symbol",
            "name" to "name"
        ),
        "double" to mapOf(
            "circulatingSupply" to "circulatingSupply"
        ),
        "strings" to mapOf(
            "tags" to "tags"
        )
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val received = transform(existing, payload, assetKeyMap)
        val symbol = received["symbol"]
        if (symbol != null) {
            received["id"] = symbol
        } else {
            received.safeSet(
                "id",
                parser.asString(payload["ticker"] ?: payload["market"])?.split("-")?.firstOrNull()
            )
        }
        return received
    }

    internal fun receivedConfigurations(
        asset: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val received = transform(asset, payload, assetConfigurationsKeyMap)
        val symbol = parser.asString(received["symbol"])
        if (symbol != null) {
            received["id"] = symbol
        }
        val resources = transform(
            parser.asNativeMap(asset?.get("resources")),
            payload,
            assetConfigurationsResourcesKeyMap
        )
        environment?.URIs?.marketImageUrl?.let {
            if (symbol != null) {
                resources["imageUrl"] = it.replace("{asset}", symbol.lowercase())
            }
        }
        received["resources"] = resources

        return received
    }
}