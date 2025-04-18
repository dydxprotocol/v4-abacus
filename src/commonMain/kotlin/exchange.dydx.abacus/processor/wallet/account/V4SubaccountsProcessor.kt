package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.InternalSubaccountState
import exchange.dydx.abacus.state.manager.BlockAndTime
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
            val modifiedSubaccountNumbers = mutableListOf<Int>()
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
                        modifiedSubaccountNumbers.add(subaccountNumber)
                    }
                }
            }
            val keys = internalState.keys.toSet()
            for (key in keys) {
                if (!modifiedSubaccountNumbers.contains(key)) {
                    internalState.remove(key)
                }
            }
            internalState
        } else {
            internalState
        }
    }

    override fun subaccountPayload(content: Map<String, Any>): Map<String, Any>? {
        return parser.asNativeMap(content["subaccount"])
            ?: parser.asNativeMap(content["subaccounts"])
    }

    override fun received(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        return subaccountProcessor.received(existing, height)
    }

    fun updateSubaccountsHeight(
        existing: MutableMap<Int, InternalSubaccountState>,
        height: BlockAndTime?,
    ): Triple<Map<Int, InternalSubaccountState>, Boolean, List<Int>?> {
        var updated = false
        val modifiedSubaccountIds = mutableListOf<Int>()
        for ((key, value) in existing) {
            val (modifiedSubaccount, subaccountUpdated) = subaccountProcessor.updateHeight(
                existing = value,
                height = height,
            )
            if (subaccountUpdated) {
                existing[key] = modifiedSubaccount
                updated = true
                modifiedSubaccountIds.add(key)
            }
        }
        return if (updated) {
            Triple(existing, true, modifiedSubaccountIds)
        } else {
            Triple(existing, false, null)
        }
    }

    fun orderCanceled(
        existing: MutableMap<Int, InternalSubaccountState>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<MutableMap<Int, InternalSubaccountState>, Boolean> {
        val subaccount = existing[subaccountNumber]
        if (subaccount != null) {
            val (modifiedSubaccount, updated) = subaccountProcessor.orderCanceled(
                existing = subaccount,
                orderId = orderId,
            )
            if (updated) {
                existing[subaccountNumber] = modifiedSubaccount
                return Pair(existing, true)
            }
        }
        return Pair(existing, false)
    }
}
