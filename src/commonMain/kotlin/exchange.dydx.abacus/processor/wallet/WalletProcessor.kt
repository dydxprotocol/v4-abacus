package exchange.dydx.abacus.processor.wallet

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.wallet.account.V4AccountProcessor
import exchange.dydx.abacus.processor.wallet.user.UserProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerFillResponseObject
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.models.chain.OnChainAccountBalanceObject
import indexer.models.chain.OnChainUserFeeTierResponse
import indexer.models.chain.OnChainUserStatsResponse

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

    internal fun subscribedDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        return receivedObject(
            existing,
            "account",
            parser.asNativeMap(content),
        ) { existing, payload ->
            parser.asNativeMap(payload)?.let {
                v4accountProcessor.subscribedDeprecated(parser.asNativeMap(existing), it, height)
            }
        }
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

    @Suppress("FunctionName")
    internal fun channel_dataDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        info: SocketInfo,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        return receivedObject(
            existing,
            "account",
            parser.asNativeMap(content),
        ) { existing, payload ->
            parser.asNativeMap(payload)?.let { payload ->
                v4accountProcessor.channel_data(
                    parser.asNativeMap(existing),
                    payload,
                    info,
                    height,
                )
            }
        }
    }

    internal fun receivedAccount(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedAccount(
                parser.asNativeMap(existing),
                payload as? Map<String, Any>?,
            )
        }
    }

    internal fun updateHeight(
        existing: Map<String, Any>?,
        height: BlockAndTime?,
    ): Triple<Map<String, Any>?, Boolean, List<Int>?> {
        if (existing != null) {
            val account = parser.asNativeMap(existing["account"])
            if (account != null) {
                val (modifiedAccount, accountUpdated, subaccountIds) = v4accountProcessor.updateHeight(
                    account,
                    height,
                )
                if (accountUpdated) {
                    val modified = existing.mutable()
                    modified.safeSet("account", modifiedAccount)
                    return Triple(modified, true, subaccountIds)
                }
            }
        }
        return Triple(existing, false, null)
    }

    internal fun processAccountBalances(
        existing: InternalWalletState,
        payload: List<OnChainAccountBalanceObject>?,
    ): InternalWalletState {
        val newAccount = v4accountProcessor.processAccountBalances(
            existing = existing.account,
            payload = payload,
        )
        if (newAccount != existing.account) {
            existing.account = newAccount
        }
        return existing
    }

    internal fun receivedAccountBalances(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedAccountBalancesDeprecated(
                parser.asNativeMap(existing),
                payload as? List<Any>,
            )
        }
    }

    internal fun receivedDelegations(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedDelegations(
                parser.asNativeMap(existing),
                payload as? List<Any>,
            )
        }
    }

    internal fun receivedUnbonding(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedUnbonding(
                parser.asNativeMap(existing),
                payload as? List<Any>,
            )
        }
    }

    internal fun receivedStakingRewards(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedStakingRewards(
                parser.asNativeMap(existing),
                payload as? Map<String, Any>,
            )
        }
    }

    internal fun receivedHistoricalTradingRewards(
        existing: Map<String, Any>?,
        payload: List<Any>?,
        period: String?,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedHistoricalTradingRewards(
                parser.asNativeMap(existing),
                payload as? List<Any>,
                period as? String,
            )
        }
    }

    internal fun processOnChainUserFeeTier(
        existing: InternalWalletState,
        payload: OnChainUserFeeTierResponse?,
    ): InternalWalletState {
        val user = userProcessor.processOnChainUserFeeTier(
            existing = existing.user,
            payload = payload?.tier,
        )
        if (user != existing.user) {
            existing.user = user
        }
        return existing
    }

    internal fun receivedOnChainUserFeeTierDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(
            existing,
            "user",
            parser.asNativeMap(payload?.get("tier")),
        ) { existing, payload ->
            parser.asNativeMap(payload)?.let {
                userProcessor.receivedOnChainUserFeeTierDeprecated(parser.asNativeMap(existing), it)
            }
        }
    }

    internal fun processOnChainUserStats(
        existing: InternalWalletState,
        payload: OnChainUserStatsResponse?,
    ): InternalWalletState {
        val user = userProcessor.processOnChainUserStats(
            existing = existing.user,
            payload = payload,
        )
        if (user != existing.user) {
            existing.user = user
        }
        return existing
    }

    internal fun receivedOnChainUserStatsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "user", payload) { existing, payload ->
            parser.asNativeMap(payload)?.let {
                userProcessor.receivedOnChainUserStatsDeprecated(parser.asNativeMap(existing), it)
            }
        }
    }

    internal fun processHistoricalPnls(
        existing: InternalWalletState,
        payload: List<IndexerPnlTicksResponseObject>?,
        subaccountNumber: Int,
    ): InternalWalletState {
        val newAccount = v4accountProcessor.processHistoricalPnls(
            existing = existing.account,
            payload = payload,
            subaccountNumber = subaccountNumber,
        )
        if (newAccount != existing.account) {
            existing.account = newAccount
        }
        return existing
    }

    internal fun receivedHistoricalPnlsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedHistoricalPnls(
                parser.asNativeMap(existing),
                parser.asNativeMap(payload),
                subaccountNumber,
            )
        }
    }

    internal fun processFills(
        existing: InternalWalletState,
        payload: List<IndexerFillResponseObject>?,
        subaccountNumber: Int,
    ): InternalWalletState {
        val newAccount = v4accountProcessor.processFills(
            existing = existing.account,
            payload = payload,
            subaccountNumber = subaccountNumber,
        )
        if (newAccount != existing.account) {
            existing.account = newAccount
        }
        return existing
    }

    internal fun receivedFillsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedFillsDeprecated(
                parser.asNativeMap(existing),
                parser.asNativeMap(payload),
                subaccountNumber,
            )
        }
    }

    internal fun receivedTransfers(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedTransfers(
                parser.asNativeMap(existing),
                parser.asNativeMap(payload),
                subaccountNumber,
            )
        }
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

    internal fun orderCanceled(
        existing: Map<String, Any>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<Map<String, Any>, Boolean> {
        val account = parser.asNativeMap(existing["account"])
        if (account != null) {
            val (modifiedAccount, updated) = v4accountProcessor.orderCanceled(
                account,
                orderId,
                subaccountNumber,
            )
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet("account", modifiedAccount)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    override fun accountAddressChanged() {
        super.accountAddressChanged()
        v4accountProcessor.accountAddress = accountAddress
    }

    internal fun receivedLaunchIncentivePoint(
        existing: Map<String, Any>,
        season: String,
        payload: Any,
    ): Map<String, Any> {
        val account = parser.asNativeMap(existing["account"]) ?: mapOf()
        val modifiedAccount = v4accountProcessor.receivedLaunchIncentivePoint(
            account,
            season,
            payload,
        )

        val modified = existing.mutable()
        modified.safeSet("account", modifiedAccount)
        return modified
    }
}
