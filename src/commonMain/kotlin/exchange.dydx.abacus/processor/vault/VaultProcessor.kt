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
import indexer.codegen.IndexerVaultsHistoricalPnlResponse

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

        val vaultState = existing ?: InternalVaultState()
        vaultState.details = VaultCalculator.calculateVaultSummary(payload)
        return vaultState
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

            val thirtyDayPnl = VaultCalculator.calculateThirtyDayPnl(it)
            if (thirtyDayPnl != null) {
                pnls[marketId] = thirtyDayPnl
            }
        }

        val vaultState = existing ?: InternalVaultState()
        vaultState.pnls =  pnls
        return vaultState
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

        val vaultState = existing ?: InternalVaultState()
        vaultState.positions =  positions
        return vaultState
    }
}
