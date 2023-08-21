package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap

internal class ConfigsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val feeTiersProcessor = FeeTiersProcessor(parser)
    private val feeDiscountsProcessor = FeeDiscountsProcessor(parser)
    private val networkConfigsProcessor = NetworkConfigsProcessor(parser)

    internal fun receivedFeeTiers(
        existing: IMap<String, Any>?,
        payload: IList<Any>
    ): IMap<String, Any>? {
        return receivedObject(existing, "feeTiers", payload) { existing, payload ->
            val list = parser.asList(payload)
            if (list != null) {
                feeTiersProcessor.received(list)
            } else {
                null
            }
        }
    }

    internal fun receivedOnChainFeeTiers(
        existing: IMap<String, Any>?,
        payload: IList<Any>
    ): IMap<String, Any>? {
        return receivedObject(existing, "feeTiers", payload) { existing, payload ->
            val list = parser.asList(payload)
            if (list != null) {
                feeTiersProcessor.received(list)
            } else {
                null
            }
        }
    }

    internal fun receivedFeeDiscounts(
        existing: IMap<String, Any>?,
        payload: IList<Any>
    ): IMap<String, Any>? {
        return receivedObject(existing, "feeDiscounts", payload) { existing, payload ->
            val list = parser.asList(payload)
            if (list != null) {
                feeDiscountsProcessor.received(list)
            } else {
                null
            }
        }
    }

    internal fun receivedNetworkConfigs(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        return receivedObject(existing, "network", payload) { existing, payload ->
            val map = parser.asMap(payload)
            if (map != null) {
                networkConfigsProcessor.received(parser.asMap(existing), map)
            } else {
                null
            }
        }
    }
}