package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalAssetPositionState
import exchange.dydx.abacus.utils.mutable
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
            val modified = existing?.mutable() ?: mutableMapOf()
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
}
