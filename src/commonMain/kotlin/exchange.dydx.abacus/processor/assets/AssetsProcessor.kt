package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import indexer.models.configs.ConfigsAssetMetadata
import indexer.models.configs.ConfigsMarketAsset

internal class AssetsProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
    val metadataService: Boolean = false,
) : BaseProcessor(parser) {
    private val assetProcessor = AssetProcessor(parser = parser, localizer = localizer)
    private val assetMetadataProcessor = AssetMetadataProcessor(parser = parser, localizer = localizer)

    override fun environmentChanged() {
        assetProcessor.environment = environment
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

    internal fun processConfigurations(
        existing: MutableMap<String, Asset>,
        payload: Map<String, ConfigsMarketAsset>,
        deploymentUri: String
    ): MutableMap<String, Asset> {
        for ((key, data) in payload) {
            val assetId = MarketId.getAssetId(key)
            if (assetId != null) {
                val asset = assetProcessor.process(
                    assetId = assetId,
                    payload = data,
                    deploymentUri = deploymentUri,
                )

                existing[assetId] = asset
            }
        }

        return existing
    }

    internal fun receivedConfigurations(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        deploymentUri: String,
    ): Map<String, Any> {
        val assets = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((key, data) in payload) {
            val assetId = MarketId.getAssetId(key)
            if (assetId != null) {
                val marketPayload = parser.asNativeMap(data)
                if (marketPayload != null) {
                    val receivedAsset = if (metadataService) {
                        assetMetadataProcessor.receivedConfigurations(
                            assetId,
                            parser.asNativeMap(existing?.get(assetId)),
                            marketPayload,
                        )
                    } else {
                        assetProcessor.receivedConfigurations(
                            assetId,
                            parser.asNativeMap(existing?.get(assetId)),
                            marketPayload,
                            deploymentUri,
                        )
                    }
                    assets[assetId] = receivedAsset
                }
            }
        }
        return assets
    }
}
