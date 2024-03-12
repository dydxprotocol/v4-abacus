package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable

internal class AssetsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val assetProcessor = AssetProcessor(parser)

    override fun environmentChanged() {
        assetProcessor.environment = environment
    }

    internal fun receivedConfigurations(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        deploymentUri: String,
    ): Map<String, Any> {
        val assets = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((key, data) in payload) {
            val assetId = MarketId.assetid(key)
            if (assetId != null) {
                val marketPayload = parser.asNativeMap(data)
                if (marketPayload != null) {
                    val receivedAsset = assetProcessor.receivedConfigurations(
                        assetId,
                        parser.asNativeMap(existing?.get(assetId)),
                        marketPayload,
                        deploymentUri,
                    )
                    assets[assetId] = receivedAsset
                }
            }
        }
        return assets
    }
}
