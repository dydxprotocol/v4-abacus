package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class AssetPositionsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = AssetPositionProcessor(parser = parser)

    internal fun received(payload: IMap<String, Any>?): IMap<String, Any>? {
        if (payload != null) {
            val result = iMutableMapOf<String, Any>()
            for ((key, value) in payload) {
                val map = parser.asMap(value)
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
        existing: IMap<String, Any>?,
        payload: IList<Any>?
    ): IMap<String, Any>? {
        return if (payload != null) {
            val output = existing?.mutable() ?: iMutableMapOf<String, Any>()
            for (item in payload) {
                parser.asMap(item)?.let { item ->
                    val assetId = parser.asString(item["symbol"] ?: item["denom"])
                    if (assetId != null) {
                        val modified =
                            itemProcessor.receivedChanges(parser.asMap(existing?.get(assetId)), item)
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