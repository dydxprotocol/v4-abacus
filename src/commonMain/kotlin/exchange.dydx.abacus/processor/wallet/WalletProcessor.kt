package exchange.dydx.abacus.processor.wallet

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.wallet.account.V4AccountProcessor
import exchange.dydx.abacus.processor.wallet.account.deprecated.V3AccountProcessor
import exchange.dydx.abacus.processor.wallet.user.UserProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class WalletProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private var v3accountProcessor = V3AccountProcessor(parser = parser)
    private var v4accountProcessor = V4AccountProcessor(parser = parser)
    private var userProcessor = UserProcessor(parser = parser)

    internal fun subscribed(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        return receivedObject(
            existing,
            "account",
            parser.asNativeMap(content)
        ) { existing, payload ->
            parser.asNativeMap(payload)?.let {
                if (it["account"] != null) {
                    v3accountProcessor.subscribed(parser.asNativeMap(existing), it, height)
                } else {
                    v4accountProcessor.subscribed(parser.asNativeMap(existing), it, height)
                }
            }
        }
    }

    internal fun channel_data(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        info: SocketInfo,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        return receivedObject(
            existing,
            "account",
            parser.asNativeMap(content)
        ) { existing, payload ->
            parser.asNativeMap(payload)?.let { payload ->
                if (payload["accounts"] != null) {
                    v3accountProcessor.channel_data(parser.asNativeMap(existing), payload, height)
                } else {
                    v4accountProcessor.channel_data(parser.asNativeMap(existing), payload, info, height)
                }
            }
        }
    }

    internal fun receivedAccount(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedAccount(parser.asNativeMap(existing), payload as? Map<String, Any>?)
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
                    height
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

    internal fun receivedAccountBalances(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedAccountBalances(
                parser.asNativeMap(existing),
                payload as? List<Any>
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
                payload as? List<Any>
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

    internal fun receivedUser(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(
            existing,
            "user",
            parser.asNativeMap(payload?.get("user"))
        ) { existing, payload ->
            parser.asNativeMap(payload)?.let {
                userProcessor.received(parser.asNativeMap(existing), it)
            }
        }
    }

    internal fun receivedOnChainUserFeeTier(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(
            existing,
            "user",
            parser.asNativeMap(payload?.get("tier"))
        ) { existing, payload ->
            parser.asNativeMap(payload)?.let {
                userProcessor.receivedOnChainUserFeeTier(parser.asNativeMap(existing), it)
            }
        }
    }

    internal fun receivedOnChainUserStats(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        return receivedObject(existing, "user", payload) { existing, payload ->
            parser.asNativeMap(payload)?.let {
                userProcessor.receivedOnChainUserStats(parser.asNativeMap(existing), it)
            }
        }
    }

    internal fun receivedHistoricalPnls(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedHistoricalPnls(
                parser.asNativeMap(existing),
                parser.asNativeMap(payload),
                subaccountNumber
            )
        }
    }

    internal fun receivedFills(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedFills(
                parser.asNativeMap(existing),
                parser.asNativeMap(payload),
                subaccountNumber
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
                subaccountNumber
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
                height
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
                subaccountNumber
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
        v3accountProcessor.accountAddress = accountAddress
        v4accountProcessor.accountAddress = accountAddress
    }
}