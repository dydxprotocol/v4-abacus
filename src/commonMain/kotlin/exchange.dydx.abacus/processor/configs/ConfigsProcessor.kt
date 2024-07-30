package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalConfigsState
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.models.chain.OnChainEquityTiersResponse
import indexer.models.chain.OnChainFeeTiersResponse

internal class ConfigsProcessor(
    parser: ParserProtocol,
    localier: LocalizerProtocol?,
) : BaseProcessor(parser) {
    private val equityTiersProcessor = EquityTiersProcessor(parser)
    private val feeTiersProcessor = FeeTiersProcessor(parser, localier)
    private val feeDiscountsProcessor = FeeDiscountsProcessor(parser)
    private val networkConfigsProcessor = NetworkConfigsProcessor(parser)
    private val withdrawalGatingProcessor = WithdrawalGatingProcessor(parser)
    private val withdrawalCapacityProcessor = WithdrawalCapacityProcessor(parser)

    fun processOnChainEquityTiers(
        existing: InternalConfigsState,
        payload: OnChainEquityTiersResponse?
    ): InternalConfigsState {
        existing.equityTiers = equityTiersProcessor.process(payload)
        return existing
    }

    internal fun receivedOnChainEquityTiersDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val map = parser.asNativeMap(payload) as Map<String, List<Any>>?
        modified.safeSet("equityTiers", map)

        return receivedObject(existing, "equityTiers", modified) { existing, payload ->
            val map = parser.asNativeMap(payload) as Map<String, Map<String, List<Any>>>?
            if (map != null) {
                equityTiersProcessor.receivedDeprecated(map)
            } else {
                null
            }
        }
    }

    fun processOnChainFeeTiers(
        existing: InternalConfigsState,
        payload: OnChainFeeTiersResponse?,
    ): InternalConfigsState {
        existing.feeTiers = feeTiersProcessor.process(payload?.params?.tiers)
        return existing
    }

    internal fun receivedOnChainFeeTiersDeprecated(
        existing: Map<String, Any>?,
        payload: List<Any>
    ): Map<String, Any>? {
        return receivedObject(existing, "feeTiers", payload) { existing, payload ->
            val list = parser.asNativeList(payload)
            if (list != null) {
                feeTiersProcessor.receivedDeprecated(list)
            } else {
                null
            }
        }
    }

    internal fun receivedFeeDiscounts(
        existing: Map<String, Any>?,
        payload: List<Any>
    ): Map<String, Any>? {
        return receivedObject(existing, "feeDiscounts", payload) { existing, payload ->
            val list = parser.asNativeList(payload)
            if (list != null) {
                feeDiscountsProcessor.received(list)
            } else {
                null
            }
        }
    }

    internal fun receivedNetworkConfigs(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return receivedObject(existing, "network", payload) { existing, payload ->
            val map = parser.asNativeMap(payload)
            if (map != null) {
                networkConfigsProcessor.received(parser.asNativeMap(existing), map)
            } else {
                null
            }
        }
    }

    internal fun receivedWithdrawalGating(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return receivedObject(existing, "withdrawalGating", payload) { existing, payload ->
            val map = parser.asNativeMap(payload)
            if (map != null) {
                withdrawalGatingProcessor.received(parser.asNativeMap(existing), map)
            } else {
                null
            }
        }
    }

    internal fun receivedWithdrawalCapacity(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return receivedObject(existing, "withdrawalCapacity", payload) { existing, payload ->
            val map = parser.asNativeMap(payload)
            if (map != null) {
                withdrawalCapacityProcessor.received(parser.asNativeMap(existing), map)
            } else {
                null
            }
        }
    }
}
