package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.utils.modify
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerPerpetualPositionResponseObject

internal class PerpetualPositionsProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
    private val itemProcessor: PerpetualPositionProcessorProtocol =
        PerpetualPositionProcessor(parser = parser, localizer = localizer),
) : BaseProcessor(parser) {

    fun process(
        payload: Map<String, IndexerPerpetualPositionResponseObject>?,
    ): Map<String, InternalPerpetualPosition>? {
        return if (payload != null) {
            val result = mutableMapOf<String, InternalPerpetualPosition>()
            for ((key, value) in payload.entries) {
                val newPosition = itemProcessor.process(null, value)
                if (newPosition != null) {
                    result[key] = newPosition
                } else {
                    result.remove(key)
                }
            }
            return result
        } else {
            null
        }
    }

    fun processChanges(
        existing: Map<String, InternalPerpetualPosition>?,
        payload: List<IndexerPerpetualPositionResponseObject>?,
    ): Map<String, InternalPerpetualPosition>? {
        return if (payload != null) {
            val result = existing?.toMutableMap() ?: mutableMapOf()
            for (item in payload) {
                if (item.market != null) {
                    val newPosition = itemProcessor.processChanges(result[item.market], item)
                    if (newPosition != null) {
                        result[item.market] = newPosition
                    } else {
                        result.remove(item.market)
                    }
                }
            }
            return if (result != existing) {
                result
            } else {
                existing
            }
        } else {
            existing
        }
    }

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

                    val itemProcessor = itemProcessor as? PerpetualPositionProcessor
                    val item = itemProcessor?.received(null, modifiedData)
                    result.safeSet(key, item)
                }
            }
            return result
        }
        return null
    }

    internal fun receivedChangesDeprecated(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val output = existing?.mutable() ?: mutableMapOf()
            for (item in payload) {
                parser.asNativeMap(item)?.let { item ->
                    parser.asString(item["market"])?.let {
                        val itemProcessor = itemProcessor as? PerpetualPositionProcessor
                        val modified =
                            itemProcessor?.receivedChangesDeprecated(parser.asNativeMap(existing?.get(it)), item)
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
