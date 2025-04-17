package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.models.configs.ConfigsAssetMetadata

internal class AssetsProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : BaseProcessor(parser) {
    private val assetMetadataProcessor = AssetMetadataProcessor(parser = parser, localizer = localizer)

    override fun environmentChanged() {
        assetMetadataProcessor.environment = environment
    }

    internal fun processMetadataConfigurations(
        existing: MutableMap<String, Asset>,
        payload: Map<String, ConfigsAssetMetadata>,
    ): MutableMap<String, Asset> {
        for ((assetId, data) in payload) {
            val asset = assetMetadataProcessor.process(
                assetId = assetId,
                payload = data,
            )

            existing[assetId] = asset
        }

        return existing
    }
}
