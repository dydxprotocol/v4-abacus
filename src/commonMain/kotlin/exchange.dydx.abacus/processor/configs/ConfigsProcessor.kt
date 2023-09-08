package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.*
import exchange.dydx.abacus.utils.mutable

internal class ConfigsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val equityTiersProcessor = EquityTiersProcessor(parser)
    private val feeTiersProcessor = FeeTiersProcessor(parser)
    private val feeDiscountsProcessor = FeeDiscountsProcessor(parser)
    private val networkConfigsProcessor = NetworkConfigsProcessor(parser)
    private val rewardsParamsProcessor = RewardsParamsProcessor(parser)

    internal fun receivedOnChainEquityTiers(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val map = parser.asMap(payload) as IMap<String, IList<Any>>?
        modified?.safeSet("equityTiers", map)

        return receivedObject(existing, "equityTiers", modified) { existing, payload ->
            val map = parser.asMap(payload) as IMap<String, IMap<String, IList<Any>>>?
            if (map != null) {
                equityTiersProcessor.received(map)
            } else {
                null
            }
        }
    }

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

    internal fun receivedRewardsParams(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        return receivedObject(existing, "rewardsParams", payload) { existing, payload ->
            val map = parser.asMap(payload)
            val params = parser.asMap(map?.get("params"))
            if (params != null) {
                rewardsParamsProcessor.received(parser.asMap(existing), params)
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