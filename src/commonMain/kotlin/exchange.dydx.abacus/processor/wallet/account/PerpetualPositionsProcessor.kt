package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.modify
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class PerpetualPositionsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = PerpetualPositionProcessor(parser = parser)

    internal fun received(
        payload: Map<String, Any>?,
        subaccountNumber: Int?,
    ): Map<String, Any>? {
        if (payload != null) {
            val result = mutableMapOf<String, Any>()
            for ((key, value) in payload) {
                parser.asNativeMap(value)?.let { data ->

                    var modifiedData = data.toMutableMap()
                    subaccountNumber?.run {
                        modifiedData.modify("subaccountNumber", subaccountNumber)
                    }

                    val item = itemProcessor.received(null, modifiedData)
                    result.safeSet(key, item)
                }
            }
            return result
        }
        return null
    }

    internal fun receivedChanges(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val output = existing?.mutable() ?: mutableMapOf()
            for (item in payload) {
                parser.asNativeMap(item)?.let { item ->
                    parser.asString(item["market"])?.let {
                        val modified =
                            itemProcessor.receivedChanges(parser.asNativeMap(existing?.get(it)), item)
                        output.safeSet(it, modified)
                    }
                }
            }
            output
        } else {
            existing
        }
    }
}
