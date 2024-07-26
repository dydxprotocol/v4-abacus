package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAssetPositionState
import indexer.codegen.IndexerAssetPositionResponseObject

internal interface AssetPositionProcessorProtocol {
    fun process(
        payload: IndexerAssetPositionResponseObject?
    ): InternalAssetPositionState?
}

internal class AssetPositionProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), AssetPositionProcessorProtocol {
    private val positionKeyMap = mapOf(
        "string" to mapOf(
            "symbol" to "id",
            "side" to "side",
            "assetId" to "assetId",
        ),
        "double" to mapOf(
            "size" to "size",
        ),
    )

    override fun process(
        payload: IndexerAssetPositionResponseObject?
    ): InternalAssetPositionState? {
        return if (payload != null) {
            InternalAssetPositionState(
                symbol = payload.symbol,
                side = PositionSide.invoke(payload.side?.value),
                size = parser.asDouble(payload.size),
                assetId = payload.assetId,
                subaccountNumber = payload.subaccountNumber,
            )
        } else {
            null
        }
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, positionKeyMap)
    }

    internal fun receivedChanges(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?
    ): Map<String, Any>? {
        return if (payload != null) {
            received(existing, payload)
        } else {
            null
        }
    }
}
