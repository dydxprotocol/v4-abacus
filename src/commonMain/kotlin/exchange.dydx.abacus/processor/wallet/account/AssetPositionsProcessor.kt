package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAssetPositionState
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerAssetPositionResponseObject

internal class AssetPositionsProcessor(
    parser: ParserProtocol,
    private val itemProcessor: AssetPositionProcessorProtocol = AssetPositionProcessor(parser = parser)
) : BaseProcessor(parser) {

    // From REST call
    fun process(
        payload: Map<String, IndexerAssetPositionResponseObject>?
    ): Map<String, InternalAssetPositionState>? {
        return if (payload != null) {
            val result = mutableMapOf<String, InternalAssetPositionState>()
            for ((key, value) in payload) {
                val assetPosition = itemProcessor.process(value)
                if (assetPosition != null) {
                    result[key] = assetPosition
                }
            }
            result
        } else {
            null
        }
    }

    // From Websocket
    fun processChanges(
        existing: Map<String, InternalAssetPositionState>?,
        payload: List<IndexerAssetPositionResponseObject>?
    ): Map<String, InternalAssetPositionState>? {
        return if (payload != null) {
            var modified = existing?.mutable() ?: mutableMapOf()
            for (item in payload) {
                val assetPosition = itemProcessor.process(item)
                if (assetPosition?.symbol != null) {
                    modified[assetPosition.symbol] = assetPosition
                }
            }
            modified
        } else {
            existing
        }
    }

    internal fun receivedDeprecated(payload: Map<String, Any>?): Map<String, Any>? {
        if (payload != null) {
            val result = mutableMapOf<String, Any>()
            for ((key, value) in payload) {
                val map = parser.asNativeMap(value)
                if (map != null) {
                    val itemProcessor = itemProcessor as AssetPositionProcessor
                    val assetPosition = itemProcessor.received(null, map)
                    result.safeSet(key, assetPosition)
                }
            }
            return result
        }
        return null
    }

    internal fun receivedChangesDeprecated(
        existing: Map<String, Any>?,
        payload: List<Any>?
    ): Map<String, Any>? {
        return if (payload != null) {
            val output = existing?.mutable() ?: mutableMapOf<String, Any>()
            for (item in payload) {
                parser.asNativeMap(item)?.let { item ->
                    val assetId = parser.asString(item["symbol"] ?: item["denom"])
                    if (assetId != null) {
                        val itemProcessor = itemProcessor as AssetPositionProcessor
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
