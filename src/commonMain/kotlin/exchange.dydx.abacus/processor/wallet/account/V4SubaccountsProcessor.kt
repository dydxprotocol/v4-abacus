package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class V4SubaccountProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : SubaccountProcessor(parser, localizer) {
    override fun received(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        val orders = parser.asNativeMap(existing["orders"])
        if (orders != null) {
            val updated = false
            val (modifiedOrders, ordersUpdated) = ordersProcessor.received(
                orders,
                height,
            )
            if (ordersUpdated) {
                val modified = existing.mutable()
                modified.safeSet("orders", modifiedOrders)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }
}

internal class V4SubaccountsProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : SubaccountProcessor(parser, localizer) {
    private val subaccountProcessor =
        V4SubaccountProcessor(parser, localizer)

    fun processSubaccounts(
        internalState: MutableMap<Int, InternalSubaccountState>,
        payload: List<Any>?,
    ): MutableMap<Int, InternalSubaccountState> {
        return if (payload != null) {
            internalState.clear()
            for (item in payload) {
                val data = parser.asNativeMap(item)
                if (data != null) {
                    val subaccountNumber = parser.asInt(data["subaccountNumber"])
                    if (subaccountNumber != null) {
                        val existing =
                            internalState[subaccountNumber] ?: InternalSubaccountState(
                                subaccountNumber = subaccountNumber,
                            )
                        val subaccount = process(
                            existing = existing,
                            payload = parser.asTypedObject(data),
                            firstTime = true,
                        )
                        internalState[subaccountNumber] = subaccount
                    }
                }
            }
            internalState
        } else {
            internalState
        }
    }

    internal fun receivedSubaccounts(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val modified = mutableMapOf<String, Any>()
            for (itemPayload in payload) {
                val data = parser.asNativeMap(itemPayload)
                if (data != null) {
                    val subaccountNumber = parser.asInt(data?.get("subaccountNumber"))
                    if (subaccountNumber != null) {
                        val key = "$subaccountNumber"
                        val existing = parser.asNativeMap(existing?.get(key))
                        val subaccount = subaccountProcessor.receivedDeprecated(existing, data, true)
                        modified.safeSet(key, subaccount)
                    }
                }
            }
            return modified
        } else {
            null
        }
    }

    override fun subaccountPayload(content: Map<String, Any>): Map<String, Any>? {
        return parser.asNativeMap(content["subaccount"])
            ?: parser.asNativeMap(content["subaccounts"])
    }

    override fun socketDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        subscribed: Boolean,
        height: BlockAndTime?,
    ): Map<String, Any> {
        val modified = super.socketDeprecated(existing, content, subscribed, height).mutable()

        val assetPositionsPayload =
            parser.asNativeList(content["assetPositions"]) as? List<Map<String, Any>>
        if (assetPositionsPayload != null) {
            val existing = parser.asNativeMap(modified["assetPositions"])
            modified.safeSet(
                "assetPositions",
                assetPositionsProcessor.receivedChangesDeprecated(existing, assetPositionsPayload),
            )
        }
        modified["quoteBalance"] = calculateQuoteBalanceDeprecated(modified, content)
        return modified
    }

    override fun received(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        return subaccountProcessor.received(existing, height)
    }

    internal fun updateSubaccountsHeight(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Triple<Map<String, Any>, Boolean, List<Int>?> {
        var updated = false
        val modifiedSubaccounts = existing.mutable()
        val modifiedSubaccountIds = mutableListOf<Int>()
        for ((key, value) in existing) {
            val keyInt = parser.asInt(key)
            if (keyInt == null) {
                Logger.e { "Invalid subaccount key: $key" }
                continue
            }
            val subaccount = parser.asNativeMap(value)
            if (subaccount != null) {
                val (modifiedSubaccount, subaccountUpdated) = subaccountProcessor.updateHeight(
                    subaccount,
                    height,
                )
                if (subaccountUpdated) {
                    modifiedSubaccounts.safeSet(key, modifiedSubaccount)
                    updated = true
                    modifiedSubaccountIds.add(key.toInt())
                }
            }
        }
        if (updated) {
            return Triple(modifiedSubaccounts, true, modifiedSubaccountIds)
        }
        return Triple(existing, false, null)
    }

    internal fun orderCanceled(
        existing: Map<String, Any>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<Map<String, Any>, Boolean> {
        val subaccountIndex = "$subaccountNumber"
        val subaccount = parser.asNativeMap(existing[subaccountIndex])
        if (subaccount != null) {
            val (modifiedSubaccount, updated) = subaccountProcessor.orderCanceled(
                subaccount,
                orderId,
            )
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet(subaccountIndex, modifiedSubaccount)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }
}
