package exchange.dydx.abacus.processor.wallet

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.wallet.account.V3AccountProcessor
import exchange.dydx.abacus.processor.wallet.account.V4AccountProcessor
import exchange.dydx.abacus.processor.wallet.user.UserProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class WalletProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private var v3accountProcessor = V3AccountProcessor(parser = parser)
    private var v4accountProcessor = V4AccountProcessor(parser = parser)
    private var userProcessor = UserProcessor(parser = parser)

    internal fun subscribed(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        height: Int?,
    ): IMap<String, Any>? {
        return receivedObject(
            existing,
            "account",
            parser.asMap(content)
        ) { existing, payload ->
            parser.asMap(payload)?.let {
                if (it["account"] != null) {
                    v3accountProcessor.subscribed(parser.asMap(existing), it, height)
                } else {
                    v4accountProcessor.subscribed(parser.asMap(existing), it, height)
                }
            }
        }
    }

    internal fun channel_data(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>,
        info: SocketInfo,
        height: Int?,
    ): IMap<String, Any>? {
        return receivedObject(
            existing,
            "account",
            parser.asMap(content)
        ) { existing, payload ->
            parser.asMap(payload)?.let { payload ->
                if (payload["accounts"] != null) {
                    v3accountProcessor.channel_data(parser.asMap(existing), payload, height)
                } else {
                    v4accountProcessor.channel_data(parser.asMap(existing), payload, info, height)
                }
            }
        }
    }

    internal fun receivedSubaccounts(
        existing: IMap<String, Any>?,
        payload: IList<Any>?,
    ): IMap<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedSubaccounts(parser.asMap(existing), payload as? IList<Any>)
        }
    }

    internal fun receivedAccountBalances(
        existing: IMap<String, Any>?,
        payload: IList<Any>?,
    ): IMap<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v4accountProcessor.receivedAccountBalances(
                parser.asMap(existing),
                payload as? IList<Any>
            )
        }
    }

    internal fun receivedUser(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
    ): IMap<String, Any>? {
        return receivedObject(
            existing,
            "user",
            parser.asMap(payload?.get("user"))
        ) { existing, payload ->
            parser.asMap(payload)?.let {
                userProcessor.received(parser.asMap(existing), it)
            }
        }
    }

    internal fun receivedOnChainUserFeeTier(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
    ): IMap<String, Any>? {
        return receivedObject(
            existing,
            "user",
            parser.asMap(payload?.get("tier"))
        ) { existing, payload ->
            parser.asMap(payload)?.let {
                userProcessor.receivedOnChainUserFeeTier(parser.asMap(existing), it)
            }
        }
    }

    internal fun receivedOnChainUserStats(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?,
    ): IMap<String, Any>? {
        return receivedObject(existing, "user", payload) { existing, payload ->
            parser.asMap(payload)?.let {
                userProcessor.receivedOnChainUserStats(parser.asMap(existing), it)
            }
        }
    }

    internal fun receivedHistoricalPnls(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>,
        subaccountNumber: Int,
    ): IMap<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v3accountProcessor.receivedHistoricalPnls(
                parser.asMap(existing),
                parser.asMap(payload),
                subaccountNumber
            )
        }
    }

    internal fun receivedFills(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>,
        subaccountNumber: Int,
    ): IMap<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v3accountProcessor.receivedFills(
                parser.asMap(existing),
                parser.asMap(payload),
                subaccountNumber
            )
        }
    }

    internal fun receivedTransfers(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>,
        subaccountNumber: Int,
    ): IMap<String, Any>? {
        return receivedObject(existing, "account", payload) { existing, payload ->
            v3accountProcessor.receivedTransfers(
                parser.asMap(existing),
                parser.asMap(payload),
                subaccountNumber
            )
        }
    }

    internal fun received(
        existing: IMap<String, Any>,
        subaccountNumber: Int,
        height: Int?,
    ): Pair<IMap<String, Any>, Boolean> {
        val account = parser.asMap(existing["account"])
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
        existing: IMap<String, Any>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<IMap<String, Any>, Boolean> {
        val account = parser.asMap(existing["account"])
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