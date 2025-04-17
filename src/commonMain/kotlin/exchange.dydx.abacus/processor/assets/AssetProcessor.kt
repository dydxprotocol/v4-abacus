package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.AssetResources
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.models.configs.ConfigsAssetMetadata

internal interface AssetMetadataProcessorProtocol {
    fun process(
        assetId: String,
        payload: ConfigsAssetMetadata,
    ): Asset
}

internal class AssetMetadataProcessor(
    parser: ParserProtocol,
    private val localizer: LocalizerProtocol?
) : BaseProcessor(parser), AssetMetadataProcessorProtocol {
    override fun process(
        assetId: String,
        payload: ConfigsAssetMetadata,
    ): Asset {
        val imageUrl = payload.logo
        val primaryDescriptionKey = "__ASSETS.$assetId.PRIMARY"
        val secondaryDescriptionKey = "__ASSETS.$assetId.SECONDARY"
        val primaryDescription = localizer?.localize(primaryDescriptionKey)
        val secondaryDescription = localizer?.localize(secondaryDescriptionKey)

        return Asset(
            id = assetId,
            name = payload.name,
            tags = payload.sector_tags,
            resources = AssetResources(
                websiteLink = payload.urls["website"],
                whitepaperLink = payload.urls["technical_doc"],
                coinMarketCapsLink = payload.urls["cmc"],
                imageUrl = imageUrl,
                primaryDescriptionKey = primaryDescriptionKey,
                secondaryDescriptionKey = secondaryDescriptionKey,
                primaryDescription = primaryDescription,
                secondaryDescription = secondaryDescription,
            ),
        )
    }
}
