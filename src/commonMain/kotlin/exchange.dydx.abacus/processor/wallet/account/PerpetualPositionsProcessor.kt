package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableMapOf

internal class PerpetualPositionsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = PerpetualPositionProcessor(parser = parser)

    internal fun received(payload: IMap<String, Any>?): IMap<String, Any>? {
        if (payload != null) {
            val result = iMutableMapOf<String, Any>()
            for ((key, value) in payload) {
                parser.asMap(value)?.let { value ->
                    val item = itemProcessor.received(null, value)
                    result.safeSet(key, item)
                }
            }
            return result
        }
        return null
    }

    internal fun receivedChanges(
        existing: IMap<String, Any>?,
        payload: IList<Any>?,
    ): IMap<String, Any>? {
        return if (payload != null) {
            val output = existing?.mutable() ?: iMutableMapOf()
            for (item in payload) {
                parser.asMap(item)?.let { item ->
                    parser.asString(item["market"])?.let {
                        val modified =
                            itemProcessor.receivedChanges(parser.asMap(existing?.get(it)), item)
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