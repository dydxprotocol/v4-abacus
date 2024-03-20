package exchange.dydx.abacus.processor.wallet.account.deprecated

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.wallet.account.SubaccountProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

/*
V3AccountProcess is used to process generic account data, which is used by both V3 and V4
 */
@Suppress("UNCHECKED_CAST")
internal class V3AccountProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val subaccountProcessor = V3SubaccountProcessor(parser)

    internal fun subscribed(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.0"))
        val modifiedsubaccount = subaccountProcessor.subscribed(subaccount, content, height)
        modified.safeSet("subaccounts.0", modifiedsubaccount)
        return modified
    }

    @Suppress("FunctionName")
    internal fun channel_data(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.0"))
        val modifiedsubaccount = subaccountProcessor.channel_data(subaccount, 0, content, height)
        modified.safeSet("subaccounts.0", modifiedsubaccount)
        return modified
    }

    internal fun receivedHistoricalPnls(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.0"))
        val modifiedsubaccount = subaccountProcessor.receivedHistoricalPnls(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun receivedFills(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountProcessor.receivedFills(subaccount, payload, 0)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun receivedTransfers(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountProcessor.receivedTransfers(subaccount, payload, 0)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    override fun accountAddressChanged() {
        super.accountAddressChanged()
        subaccountProcessor.accountAddress = accountAddress
    }
}

internal class V3SubaccountProcessor(parser: ParserProtocol) : SubaccountProcessor(parser)
