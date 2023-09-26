package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable

@Suppress("UNCHECKED_CAST")
internal class AssetsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val assetProcessor = AssetProcessor(parser)

    override fun environmentChanged() {
        assetProcessor.environment = environment
    }

    internal fun subscribed(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any>? {
        val payload = parser.asNativeMap(content["markets"])
        return if (payload != null) {
            received(existing, payload)
        } else {
            existing
        }
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val assets = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((_, data) in payload) {
            val marketPayload = parser.asNativeMap(data)
            val assetId = assetIdFromMarket(marketPayload)
            if (marketPayload != null && assetId != null && assetId != "") {
                assetProcessor.received(
                    parser.asNativeMap(existing?.get(assetId)),
                    marketPayload
                )?.let {
                    assets[assetId] = it
                }
            }
        }
        return assets
    }

    private fun assetIdFromMarket(payload: Map<String, Any>?): String? {
        return parser.asString(payload?.get("baseAsset")) ?:
            parser.asString(payload?.get("ticker") ?: payload?.get("market"))?.split("-")?.firstOrNull()
    }

    internal fun receivedConfigurations(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val assets = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((_, data) in payload) {
            val marketPayload = parser.asNativeMap(data)
            val assetId = parser.asString(marketPayload?.get("baseSymbol"))
            if (marketPayload != null && assetId != null) {
                val receivedAsset = assetProcessor.receivedConfigurations(
                    parser.asNativeMap(existing?.get(assetId)),
                    marketPayload
                )
                assets[assetId] = receivedAsset
            }
        }
        return assets
    }
}