package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.output.WithdrawalGating
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.BaseProcessorProtocol
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalConfigsState
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.models.chain.OnChainEquityTiersResponse
import indexer.models.chain.OnChainFeeTiersResponse
import indexer.models.chain.OnChainWithdrawalAndTransferGatingStatusResponse
import indexer.models.chain.OnChainWithdrawalCapacityResponse

internal interface ConfigsProcessorProtocol : BaseProcessorProtocol {
    fun processOnChainEquityTiers(
        existing: InternalConfigsState,
        payload: OnChainEquityTiersResponse?
    ): InternalConfigsState

    fun processOnChainFeeTiers(
        existing: InternalConfigsState,
        payload: OnChainFeeTiersResponse?
    ): InternalConfigsState

    fun processWithdrawalGating(
        existing: InternalConfigsState,
        payload: OnChainWithdrawalAndTransferGatingStatusResponse?
    ): InternalConfigsState

    fun processWithdrawalCapacity(
        existing: InternalConfigsState,
        payload: OnChainWithdrawalCapacityResponse?
    ): InternalConfigsState
}

internal class ConfigsProcessor(
    parser: ParserProtocol,
    localier: LocalizerProtocol?,
    private val equityTiersProcessor: EquityTiersProcessorProtocol = EquityTiersProcessor(parser),
    private val feeTiersProcessor: FeeTiersProcessorProtocol = FeeTiersProcessor(parser, localier),
    private val withdrawalCapacityProcessor: WithdrawalCapacityProcessorProtocol = WithdrawalCapacityProcessor(parser)
) : BaseProcessor(parser), ConfigsProcessorProtocol {
    // Deprecated
    private val feeDiscountsProcessor = FeeDiscountsProcessor(parser)
    private val networkConfigsProcessor = NetworkConfigsProcessor(parser)
    private val withdrawalGatingProcessor = WithdrawalGatingProcessor(parser)

    override fun processOnChainEquityTiers(
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
                val equityTiersProcessor = equityTiersProcessor as EquityTiersProcessor
                equityTiersProcessor.receivedDeprecated(map)
            } else {
                null
            }
        }
    }

    override fun processOnChainFeeTiers(
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
                val feeTiersProcessor = feeTiersProcessor as FeeTiersProcessor
                feeTiersProcessor.receivedDeprecated(list)
            } else {
                null
            }
        }
    }

    // Not used
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

    // Not used
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

    override fun processWithdrawalGating(
        existing: InternalConfigsState,
        payload: OnChainWithdrawalAndTransferGatingStatusResponse?
    ): InternalConfigsState {
        existing.withdrawalGating = WithdrawalGating(
            withdrawalsAndTransfersUnblockedAtBlock = parser.asInt(payload?.withdrawalsAndTransfersUnblockedAtBlock),
        )
        return existing
    }

    internal fun receivedWithdrawalGatingDeprecated(
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

    override fun processWithdrawalCapacity(
        existing: InternalConfigsState,
        payload: OnChainWithdrawalCapacityResponse?
    ): InternalConfigsState {
        existing.withdrawalCapacity = withdrawalCapacityProcessor.process(payload)
        return existing
    }

    internal fun receivedWithdrawalCapacityDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return receivedObject(existing, "withdrawalCapacity", payload) { existing, payload ->
            val map = parser.asNativeMap(payload)
            if (map != null) {
                val withdrawalCapacityProcessor = withdrawalCapacityProcessor as WithdrawalCapacityProcessor
                withdrawalCapacityProcessor.received(parser.asNativeMap(existing), map)
            } else {
                null
            }
        }
    }
}
