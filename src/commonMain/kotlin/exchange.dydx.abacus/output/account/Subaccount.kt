package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.processor.base.ComparisonOrder
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_DURATION
import kollections.JsExport
import kollections.toIList
import kotlinx.serialization.Serializable

/*
ethereumeAddress is passed in from client. All other fields
are filled when socket v4_subaccounts channel is subscribed
*/
@JsExport
@Serializable
data class Subaccount(
    val subaccountNumber: Int,
    val positionId: String?,
    val pnlTotal: Double?,
    val pnl24h: Double?,
    val pnl24hPercent: Double?,
    val quoteBalance: TradeStatesWithDoubleValues?,
    val notionalTotal: TradeStatesWithDoubleValues?,
    val valueTotal: TradeStatesWithDoubleValues?,
    val initialRiskTotal: TradeStatesWithDoubleValues?,
    val adjustedImf: TradeStatesWithDoubleValues?,
    val equity: TradeStatesWithDoubleValues?,
    val freeCollateral: TradeStatesWithDoubleValues?,
    val leverage: TradeStatesWithDoubleValues?,
    val marginUsage: TradeStatesWithDoubleValues?,
    val buyingPower: TradeStatesWithDoubleValues?,
    val openPositions: IList<SubaccountPosition>?,
    val pendingPositions: IList<SubaccountPendingPosition>?,
    val orders: IList<SubaccountOrder>?,
    val marginEnabled: Boolean?,
) {
    companion object {
        internal fun create(
            existing: Subaccount?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
            staticTyping: Boolean,
            internalState: InternalSubaccountState?,
        ): Subaccount? {
            Logger.d { "creating Account\n" }

            if (staticTyping && internalState == null) {
                Logger.d { "Internal state is null" }
                return null
            }
            data?.let {
                val ethereumeAddress = parser.asString(data["ethereumeAddress"])
                val positionId = parser.asString(data["positionId"])
                val pnlTotal = parser.asDouble(data["pnlTotal"])
                val pnl24h = parser.asDouble(data["pnl24h"])
                val pnl24hPercent = parser.asDouble(data["pnl24hPercent"])
                /*
                val historicalPnl = (data["historicalPnl"] as? List<*>)?.let {
                    val historicalPnl = iMutableListOf<AccountHistoricalPNL>()
                    for (i in it.indices) {
                        val itemData = it[i]
                        AccountHistoricalPNL.create(
                            existing?.historicalPnl?.a?.getOrNull(i),
                            parser, itemData as? IMap<*, *>
                        )?.let {
                            historicalPnl.add(it)
                        }
                    }
                    AccountHistoricalPNLs.fromArray(historicalPnl)
                }
                 */
                val subaccountNumber = parser.asInt(data["subaccountNumber"]) ?: 0
                val quoteBalance =
                    TradeStatesWithDoubleValues.create(
                        existing?.quoteBalance,
                        parser,
                        parser.asMap(data["quoteBalance"]),
                    )
                val notionalTotal =
                    TradeStatesWithDoubleValues.create(
                        existing?.notionalTotal,
                        parser,
                        parser.asMap(data["notionalTotal"]),
                    )
                val valueTotal =
                    TradeStatesWithDoubleValues.create(
                        existing?.valueTotal,
                        parser,
                        parser.asMap(data["valueTotal"]),
                    )
                val initialRiskTotal =
                    TradeStatesWithDoubleValues.create(
                        existing?.initialRiskTotal,
                        parser,
                        parser.asMap(data["initialRiskTotal"]),
                    )
                val adjustedImf =
                    TradeStatesWithDoubleValues.create(
                        existing?.adjustedImf,
                        parser,
                        parser.asMap(data["adjustedImf"]),
                    )
                val equity =
                    TradeStatesWithDoubleValues.create(
                        existing?.equity,
                        parser,
                        parser.asMap(data["equity"]),
                    )
                val freeCollateral =
                    TradeStatesWithDoubleValues.create(
                        existing?.freeCollateral,
                        parser,
                        parser.asMap(data["freeCollateral"]),
                    )
                val leverage =
                    TradeStatesWithDoubleValues.create(
                        existing?.leverage,
                        parser,
                        parser.asMap(data["leverage"]),
                    )
                val marginUsage =
                    TradeStatesWithDoubleValues.create(
                        existing?.marginUsage,
                        parser,
                        parser.asMap(data["marginUsage"]),
                    )
                val buyingPower =
                    TradeStatesWithDoubleValues.create(
                        existing?.buyingPower,
                        parser,
                        parser.asMap(data["buyingPower"]),
                    )

                val openPositions = if (staticTyping) {
                    openPositions(
                        existing = existing?.openPositions,
                        parser = parser,
                        data = parser.asMap(data["openPositions"]),
                        openPositions = internalState?.openPositions,
                    )
                } else {
                    openPositionsDeprecated(
                        existing = existing?.openPositions,
                        parser = parser,
                        data = parser.asMap(data["openPositions"]),
                    )
                }

                val pendingPositions = pendingPositions(
                    existing?.pendingPositions,
                    parser,
                    parser.asList(data["pendingPositions"]),
                )
                val orders =
                    if (staticTyping) {
                        internalState?.orders?.toIList()
                    } else {
                        orders(parser, existing?.orders, parser.asMap(data["orders"]), localizer)
                    }

                /*
                val transfers = AccountTransfers.fromArray(
                    transfers(parser, existing?.transfers?.a, data["transfers"] as? List<*>)
                )
                val fundingPayments = AccountFundingPayments.fromArray(
                    fundingPayments(
                        parser,
                        existing?.fundingPayments?.a,
                        data["fundingPayments"] as? List<*>
                    ))

                 */
                val marginEnabled = parser.asBool(data["marginEnabled"]) ?: true

                return if (existing?.subaccountNumber != subaccountNumber ||
                    existing.positionId != positionId ||
                    existing.pnlTotal != pnlTotal ||
                    existing.pnl24h != pnl24h ||
                    existing.pnl24hPercent != pnl24hPercent ||
                    existing.quoteBalance !== quoteBalance ||
                    existing.notionalTotal !== notionalTotal ||
                    existing.valueTotal !== valueTotal ||
                    existing.initialRiskTotal !== initialRiskTotal ||
                    existing.adjustedImf !== adjustedImf ||
                    existing.equity !== equity ||
                    existing.freeCollateral !== freeCollateral ||
                    existing.leverage !== leverage ||
                    existing.marginUsage !== marginUsage ||
                    existing.buyingPower !== buyingPower ||
                    existing.openPositions != openPositions ||
                    existing.pendingPositions != pendingPositions ||
                    existing.orders != orders ||
                    existing.marginEnabled != marginEnabled
                ) {
                    Subaccount(
                        subaccountNumber,
                        positionId,
                        pnlTotal,
                        pnl24h,
                        pnl24hPercent,
                        quoteBalance,
                        notionalTotal,
                        valueTotal,
                        initialRiskTotal,
                        adjustedImf,
                        equity,
                        freeCollateral,
                        leverage,
                        marginUsage,
                        buyingPower,
                        openPositions,
                        pendingPositions,
                        orders,
                        marginEnabled,
                    )
                } else {
                    existing
                }
            }
            return null
        }

        private fun openPositions(
            existing: IList<SubaccountPosition>?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
            openPositions: Map<String, InternalPerpetualPosition>?,
        ): IList<SubaccountPosition>? {
            val newEntries: MutableList<SubaccountPosition> = mutableListOf()
            for ((key, value) in openPositions?.entries ?: emptySet()) {
                val position = SubaccountPosition.create(
                    existing = null,
                    parser = parser,
                    data = data?.get(key) as? Map<String, Any>,
                    positionId = key,
                    internalState = value,
                )
                if (position != null) {
                    newEntries.add(position)
                }
            }
            newEntries.sortByDescending { it.createdAtMilliseconds }
            return if (newEntries != existing) {
                newEntries.toIList()
            } else {
                existing
            }
        }

        private fun openPositionsDeprecated(
            existing: IList<SubaccountPosition>?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): IList<SubaccountPosition>? {
            return ParsingHelper.transform(
                parser = parser,
                existing = existing,
                data = data,
                key = {
                    (it as SubaccountPosition).id
                },
                changed = { _, _ ->
                    // not worth the optimization
                    true
                },
                comparison = { obj1, obj2 ->
                    val time1 = (obj1 as SubaccountPosition).createdAtMilliseconds
                    val time2 = (obj2 as SubaccountPosition).createdAtMilliseconds
                    ParsingHelper.compare(time1 ?: 0.0, time2 ?: 0.0, false)
                },
                createObject = { _, obj, itemData ->
                    parser.asMap(itemData)?.let {
                        SubaccountPosition.create(
                            existing = obj as? SubaccountPosition,
                            parser = parser,
                            data = it,
                            positionId = null,
                            internalState = null,
                        )
                    }
                },
            )?.toIList()
        }

        private fun pendingPositions(
            existing: IList<SubaccountPendingPosition>?,
            parser: ParserProtocol,
            data: List<*>?,
        ): IList<SubaccountPendingPosition>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val orderId1 = (obj as SubaccountPendingPosition).firstOrderId
                val orderId2 =
                    parser.asString(itemData["firstOrderId"])
                ParsingHelper.compare(orderId1, orderId2, false)
            }, { _, obj, itemData ->
                SubaccountPendingPosition.create(
                    obj as? SubaccountPendingPosition,
                    parser,
                    parser.asMap(itemData),
                )
            }, true)?.toIList()
        }

        private fun orders(
            parser: ParserProtocol,
            existing: IList<SubaccountOrder>?,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): IList<SubaccountOrder>? {
            val orders = ParsingHelper.transform(parser, existing, data, {
                (it as SubaccountOrder).id
            }, { _, _ ->
                // not worth the optimization
                true
            }, { obj1, obj2 ->
                val block1 = block(obj1 as SubaccountOrder)
                val block2 = block(obj2 as SubaccountOrder)
                if (block1 != null || block2 != null) {
                    var result = ParsingHelper.compare(block1 ?: 0, block2 ?: 0, false)
                    if (result == ComparisonOrder.same) {
                        result = ParsingHelper.compare(obj1.id, obj2.id, true)
                    }
                    result
                } else {
                    val time1 = (obj1 as SubaccountOrder).createdAtMilliseconds
                    val time2 = (obj2 as SubaccountOrder).createdAtMilliseconds
                    if (time1 != null) {
                        if (time2 != null) {
                            ParsingHelper.compare(time1, time2, false)
                        } else {
                            ComparisonOrder.ascending
                        }
                    } else {
                        if (time2 != null) {
                            ComparisonOrder.descending
                        } else {
                            ParsingHelper.compare(obj1.id, obj2.id, true)
                        }
                    }
                }
            }, { _, obj, itemData ->
                parser.asMap(itemData)?.let {
                    SubaccountOrder.create(obj as? SubaccountOrder, parser, it, localizer)
                }
            })?.toIList()
            return orders
        }

        private inline fun block(order: SubaccountOrder): Int? {
            return order.createdAtHeight ?: if (order.goodTilBlock != null) {
                order.goodTilBlock - SHORT_TERM_ORDER_DURATION
            } else {
                null
            }
        }

        private fun transfers(
            parser: ParserProtocol,
            existing: IList<SubaccountTransfer>?,
            data: List<*>?,
        ): IList<SubaccountTransfer>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountTransfer).updatedAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["confirmedAt"])?.toEpochMilliseconds()?.toDouble()
                        ?: parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()
                            ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                SubaccountTransfer.create(
                    null,
                    parser,
                    parser.asMap(itemData),
                )
            }, true)?.toIList()
        }

        private fun fundingPayments(
            parser: ParserProtocol,
            existing: IList<SubaccountFundingPayment>?,
            data: List<*>?,
        ): IList<SubaccountFundingPayment>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFundingPayment).effectiveAtMilliSeconds
                val time2 =
                    parser.asDatetime(itemData["effectiveAt"])?.toEpochMilliseconds()?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: SubaccountFundingPayment.create(null, parser, itemData)
            }, true)?.toIList()
        }
    }
}
