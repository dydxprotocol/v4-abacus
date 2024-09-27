package exchange.dydx.abacus.processor.vault

import exchange.dydx.abacus.functional.vault.ThirtyDayPnl
import exchange.dydx.abacus.functional.vault.VaultCalculator
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.wallet.account.AssetPositionProcessor
import exchange.dydx.abacus.processor.wallet.account.PerpetualPositionProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalVaultPositionState
import exchange.dydx.abacus.state.internalstate.InternalVaultState
import indexer.codegen.IndexerMegavaultHistoricalPnlResponse
import indexer.codegen.IndexerMegavaultPositionResponse
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerVaultsHistoricalPnlResponse
import indexer.models.chain.OnChainAccountVaultResponse

internal class VaultProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : BaseProcessor(parser) {

    private val perpetualPositionProcessor = PerpetualPositionProcessor(parser, localizer)
    private val assetPositionProcessor = AssetPositionProcessor(parser)

    fun processMegaVaultsHistoricalPnl(
        existing: InternalVaultState?,
        payload: IndexerMegavaultHistoricalPnlResponse?,
    ): InternalVaultState? {
        if (payload == null) {
            return existing
        }

        val newValue = VaultCalculator.calculateVaultSummary(payload)
        return if (newValue != existing?.details) {
            existing?.copy(details = newValue) ?: InternalVaultState(details = newValue)
        } else {
            existing
        }
    }

    fun processVaultMarketHistoricalPnls(
        existing: InternalVaultState?,
        payload: IndexerVaultsHistoricalPnlResponse?,
    ): InternalVaultState? {
        if (payload == null) {
            return existing
        }

        val pnls: MutableMap<String, ThirtyDayPnl> = mutableMapOf()
        payload.vaultsPnl?.forEach {
            val marketId = it.ticker ?: return@forEach

            val thirtyDayPnl = VaultCalculator.calculateThirtyDayPnl(it) ?: return@forEach
            pnls[marketId] = thirtyDayPnl
        }

        return if (pnls != existing?.pnls) {
            existing?.copy(pnls = pnls) ?: InternalVaultState(pnls = pnls)
        } else {
            existing
        }
    }

    fun processVaultMarketPositions(
        existing: InternalVaultState?,
        payload: IndexerMegavaultPositionResponse?,
    ): InternalVaultState? {
        if (payload == null) {
            return existing
        }

        val positions: List<InternalVaultPositionState>? = payload.positions?.map {
            val perpetualPosition = perpetualPositionProcessor.process(null, it.perpetualPosition)
            val assetPosition = assetPositionProcessor.process(it.assetPosition)
            InternalVaultPositionState(
                openPosition = perpetualPosition,
                assetPosition = assetPosition,
                equity = parser.asDouble(it.equity),
                ticker = it.ticker,
            )
        }

        return if (positions != existing?.positions) {
            existing?.copy(positions = positions) ?: InternalVaultState(positions = positions)
        } else {
            existing
        }
    }

    fun processTransferBetween(
        existing: InternalVaultState?,
        payload: IndexerTransferBetweenResponse?,
    ): InternalVaultState? {
        if (payload == null) {
            return existing
        }

        return if (payload != existing?.transfers) {
            existing?.copy(transfers = payload) ?: InternalVaultState(transfers = payload)
        } else {
            existing
        }
    }

    fun processAccountOwnerShares(
        existing: InternalVaultState?,
        payload: OnChainAccountVaultResponse?,
    ): InternalVaultState? {
        if (payload == null) {
            return existing
        }

        return if (payload != existing?.account) {
            existing?.copy(account = payload) ?: InternalVaultState(account = payload)
        } else {
            existing
        }
    }
}
