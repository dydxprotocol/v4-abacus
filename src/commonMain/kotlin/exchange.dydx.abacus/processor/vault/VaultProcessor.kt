package exchange.dydx.abacus.processor.vault

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalVaultState
import indexer.codegen.IndexerVaultsHistoricalPnlResponse

internal class VaultProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : BaseProcessor(parser) {

    fun processVaultsHistoricalPnl(
        existing: InternalVaultState?,
        payload: IndexerVaultsHistoricalPnlResponse?,
    ): InternalVaultState? {
        if (payload == null) {
            return existing
        }

//        val pnls: List<IndexerVaultHistoricalPnl>?= payload.vaultsPnl?.mapNotNull { it ->
//            it.historicalPnl
//
//        }
//        return existing.copy(
//            vaultsHistoricalPnl = payload.vaultsHistoricalPnl,
//        )
        return null
    }
}
