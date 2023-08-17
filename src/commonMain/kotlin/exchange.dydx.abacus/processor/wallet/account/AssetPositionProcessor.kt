package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

@Suppress("UNCHECKED_CAST")
internal class AssetPositionProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val positionKeyMap = iMapOf(
        "string" to iMapOf(
            "symbol" to "id",
            "side" to "side",
            "assetId" to "assetId"
        ),
        "double" to iMapOf(
            "size" to "size"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        return transform(existing, payload, positionKeyMap)
    }

    internal fun receivedChanges(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?
    ): IMap<String, Any>? {
        return if (payload != null) {
            received(existing, payload)
        } else {
            null
        }
    }
}