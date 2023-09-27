package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class AssetPositionProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val positionKeyMap = mapOf(
        "string" to mapOf(
            "symbol" to "id",
            "side" to "side",
            "assetId" to "assetId"
        ),
        "double" to mapOf(
            "size" to "size"
        )
    )

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