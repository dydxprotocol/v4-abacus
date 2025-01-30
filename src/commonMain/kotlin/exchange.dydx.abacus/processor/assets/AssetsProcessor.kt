package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
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

    internal fun receivedConfigurations(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val assets = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((key, data) in payload) {
            val assetId = MarketId.getAssetId(key)
            if (assetId != null) {
                val marketPayload = parser.asNativeMap(data)
                if (marketPayload != null) {
                    val receivedAsset =
                        assetMetadataProcessor.receivedConfigurations(
                            assetId,
                            parser.asNativeMap(existing?.get(assetId)),
                            marketPayload,
                        )
                    assets[assetId] = receivedAsset
                }
            }
        }
        return assets
    }
}
