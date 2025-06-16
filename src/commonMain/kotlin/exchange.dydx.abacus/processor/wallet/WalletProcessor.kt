package exchange.dydx.abacus.processor.wallet

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.wallet.account.V4AccountProcessor
import exchange.dydx.abacus.processor.wallet.user.UserProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.InternalWalletState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerFundingPaymentResponseObject
import indexer.codegen.IndexerHistoricalTradingRewardAggregation
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.codegen.IndexerTransferResponseObject
import indexer.models.IndexerCompositeFillObject
import indexer.models.chain.OnChainAccountBalanceObject
import indexer.models.chain.OnChainDelegationResponse
import indexer.models.chain.OnChainStakingRewardsResponse
import indexer.models.chain.OnChainUnbondingResponse
import indexer.models.chain.OnChainUserFeeTierResponse
import indexer.models.chain.OnChainUserStatsResponse
import indexer.models.configs.ConfigsLaunchIncentivePoints

internal class WalletProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : BaseProcessor(parser) {
    private val v4accountProcessor = V4AccountProcessor(parser = parser, localizer = localizer)
    private val userProcessor = UserProcessor(parser = parser)

    internal fun processSubscribed(
        existing: InternalWalletState,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): InternalWalletState {
        val account = v4accountProcessor.processSubscribed(
            existing = existing.account,
            content = content,
            height = height,
        )
        existing.account = account
        return existing
    }

    internal fun processChannelData(
        existing: InternalWalletState,
        content: Map<String, Any>,
        info: SocketInfo,
        height: BlockAndTime?,
    ): InternalWalletState {
        val account = v4accountProcessor.processChannelData(
            existing = existing.account,
            content = content,
            info = info,
            height = height,
        )
        existing.account = account
        return existing
    }

    fun processAccount(
        internalState: InternalWalletState,
        payload: Map<String, Any>?,
    ): InternalWalletState {
        val account = v4accountProcessor.processAccount(
            internalState = internalState.account,
            content = payload,
        )
        internalState.account = account
        return internalState
    }

    fun updateHeight(
        existing: InternalWalletState?,
        height: BlockAndTime?,
    ): Triple<InternalWalletState?, Boolean, List<Int>?> {
        if (existing != null) {
            val (modifiedAccount, accountUpdated, subaccountIds) = v4accountProcessor.updateHeight(
                existing = existing.account,
                height = height,
            )
            if (accountUpdated) {
                existing.account = modifiedAccount
                return Triple(existing, true, subaccountIds)
            }
        }
        return Triple(existing, false, null)
    }

    internal fun processAccountBalances(
        existing: InternalWalletState,
        payload: List<OnChainAccountBalanceObject>?,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processAccountBalances(
            existing = existing.account,
            payload = payload,
        )
        return existing
    }

    internal fun processStakingDelegations(
        existing: InternalWalletState,
        payload: OnChainDelegationResponse?
    ): InternalWalletState {
        existing.account = v4accountProcessor.processStakingDelegations(
            existing = existing.account,
            payload = payload,
        )
        return existing
    }

    fun processUnbonding(
        existing: InternalWalletState,
        payload: OnChainUnbondingResponse?,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processUnbonding(
            existing = existing.account,
            payload = payload,
        )
        return existing
    }

    internal fun processStakingRewards(
        existing: InternalWalletState,
        payload: OnChainStakingRewardsResponse?,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processStakingRewards(
            existing = existing.account,
            payload = payload,
        )
        return existing
    }

    internal fun processHistoricalTradingRewards(
        existing: InternalWalletState,
        payload: List<IndexerHistoricalTradingRewardAggregation>?,
        period: HistoricalTradingRewardsPeriod,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processHistoricalTradingRewards(
            existing = existing.account,
            payload = payload,
            period = period,
        )
        return existing
    }

    internal fun processOnChainUserFeeTier(
        existing: InternalWalletState,
        payload: OnChainUserFeeTierResponse?,
    ): InternalWalletState {
        existing.user = userProcessor.processOnChainUserFeeTier(
            existing = existing.user,
            payload = payload?.tier,
        )
        return existing
    }

    internal fun processOnChainUserStats(
        existing: InternalWalletState,
        payload: OnChainUserStatsResponse?,
    ): InternalWalletState {
        existing.user = userProcessor.processOnChainUserStats(
            existing = existing.user,
            payload = payload,
        )
        return existing
    }

    internal fun processHistoricalPnls(
        existing: InternalWalletState,
        payload: List<IndexerPnlTicksResponseObject>?,
        subaccountNumber: Int,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processHistoricalPnls(
            existing = existing.account,
            payload = payload,
            subaccountNumber = subaccountNumber,
        )
        return existing
    }

    internal fun processFundingPayments(
        existing: InternalWalletState,
        payload: List<IndexerFundingPaymentResponseObject>?,
        subaccountNumber: Int,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processFundingPayments(
            existing = existing.account,
            payload = payload,
            subaccountNumber = subaccountNumber,
        )
        return existing
    }

    internal fun processFills(
        existing: InternalWalletState,
        payload: List<IndexerCompositeFillObject>?,
        subaccountNumber: Int,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processFills(
            existing = existing.account,
            payload = payload,
            subaccountNumber = subaccountNumber,
        )
        return existing
    }

    fun processTransfers(
        existing: InternalWalletState,
        payload: List<IndexerTransferResponseObject>?,
        subaccountNumber: Int,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processTransfers(
            existing = existing.account,
            payload = payload,
            subaccountNumber = subaccountNumber,
        )
        return existing
    }

    internal fun received(
        existing: Map<String, Any>,
        subaccountNumber: Int,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        val account = parser.asNativeMap(existing["account"])
        if (account != null) {
            val (modifiedAccount, accountUpdated) = v4accountProcessor.received(
                account,
                subaccountNumber,
                height,
            )
            if (accountUpdated) {
                val modified = existing.mutable()
                modified.safeSet("account", modifiedAccount)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    fun orderCanceled(
        existing: InternalWalletState,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<InternalWalletState, Boolean> {
        val (modifiedAccount, updated) = v4accountProcessor.orderCanceled(
            existing = existing.account,
            orderId = orderId,
            subaccountNumber = subaccountNumber,
        )
        return Pair(existing, updated)
    }

    override fun accountAddressChanged() {
        super.accountAddressChanged()
        v4accountProcessor.accountAddress = accountAddress
    }

    fun processLaunchIncentiveSeasons(
        existing: InternalWalletState,
        season: String,
        payload: ConfigsLaunchIncentivePoints?,
    ): InternalWalletState {
        existing.account = v4accountProcessor.processLaunchIncentivePoints(
            existing = existing.account,
            season = season,
            payload = payload,
        )
        return existing
    }
}
