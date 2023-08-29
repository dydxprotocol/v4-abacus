package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.mutable
import kollections.iMutableMapOf

@Suppress("UNCHECKED_CAST")
internal class AssetsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val assetProcessor = AssetProcessor(parser)

    override fun environmentChanged() {
        assetProcessor.environment = environment
    }

    internal fun subscribed(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>
    ): IMap<String, Any>? {
        val payload = parser.asMap(content["markets"])
        return if (payload != null) {
            received(existing, payload)
        } else {
            existing
        }
    }

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val assets = existing?.mutable() ?: iMutableMapOf<String, Any>()
        for ((_, data) in payload) {
            val marketPayload = parser.asMap(data)
            val assetId = parser.asString(marketPayload?.get("baseAsset"))
            if (marketPayload != null && assetId != null) {
                assetProcessor.received(
                    parser.asMap(existing?.get(assetId)),
                    marketPayload
                )?.let {
                    assets[assetId] = it
                }
            }
        }
        return assets
    }

    internal fun receivedConfigurations(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val assets = existing?.mutable() ?: iMutableMapOf<String, Any>()
        for ((_, data) in payload) {
            val marketPayload = parser.asMap(data)
            val assetId = parser.asString(marketPayload?.get("baseSymbol"))
            if (marketPayload != null && assetId != null) {
                val receivedAsset = assetProcessor.receivedConfigurations(
                    parser.asMap(existing?.get(assetId)),
                    marketPayload
                )
                assets[assetId] = receivedAsset
            }
        }
        return assets
    }
}