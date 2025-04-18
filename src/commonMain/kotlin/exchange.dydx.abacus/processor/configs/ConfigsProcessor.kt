package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.output.WithdrawalGating
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.BaseProcessorProtocol
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalConfigsState
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

    override fun processOnChainEquityTiers(
        existing: InternalConfigsState,
        payload: OnChainEquityTiersResponse?
    ): InternalConfigsState {
        existing.equityTiers = equityTiersProcessor.process(payload)
        return existing
    }

    override fun processOnChainFeeTiers(
        existing: InternalConfigsState,
        payload: OnChainFeeTiersResponse?,
    ): InternalConfigsState {
        existing.feeTiers = feeTiersProcessor.process(payload?.params?.tiers)
        return existing
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

    override fun processWithdrawalCapacity(
        existing: InternalConfigsState,
        payload: OnChainWithdrawalCapacityResponse?
    ): InternalConfigsState {
        existing.withdrawalCapacity = withdrawalCapacityProcessor.process(payload)
        return existing
    }
}
