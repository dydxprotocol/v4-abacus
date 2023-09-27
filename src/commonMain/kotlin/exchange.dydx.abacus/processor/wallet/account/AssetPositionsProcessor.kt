package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class AssetPositionsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = AssetPositionProcessor(parser = parser)

    internal fun received(payload: Map<String, Any>?): Map<String, Any>? {
        if (payload != null) {
            val result = mutableMapOf<String, Any>()
            for ((key, value) in payload) {
                val map = parser.asNativeMap(value)
                if (map != null) {
                    val assetPosition = itemProcessor.received(null, map)
                    result.safeSet(key, assetPosition)
                }
            }
            return result
        }
        return null
    }

    internal fun receivedChanges(
        existing: Map<String, Any>?,
        payload: List<Any>?
    ): Map<String, Any>? {
        return if (payload != null) {
            val output = existing?.mutable() ?: mutableMapOf<String, Any>()
            for (item in payload) {
                parser.asNativeMap(item)?.let { item ->
                    val assetId = parser.asString(item["symbol"] ?: item["denom"])
                    if (assetId != null) {
                        val modified =
                            itemProcessor.receivedChanges(parser.asNativeMap(existing?.get(assetId)), item)
                        output.safeSet(assetId, modified)
                    }
                }
            }
            output
        } else {
            existing
        }
    }
}