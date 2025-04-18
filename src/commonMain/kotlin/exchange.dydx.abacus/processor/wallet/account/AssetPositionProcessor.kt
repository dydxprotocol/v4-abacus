package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalAssetPositionState
import indexer.codegen.IndexerAssetPositionResponseObject

internal interface AssetPositionProcessorProtocol {
    fun process(
        payload: IndexerAssetPositionResponseObject?
    ): InternalAssetPositionState?
}

internal class AssetPositionProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), AssetPositionProcessorProtocol {
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
}
