package exchange.dydx.abacus.state.manager.notification

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.NotificationPriority
import exchange.dydx.abacus.output.NotificationType
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.typedSafeSet
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIMap

class FillsNotificationProvider(
    private val stateMachine: TradingStateMachine,
    private val uiImplementations: UIImplementations,
    private val parser: ParserProtocol,
    private val jsonEncoder: JsonEncoder,
) : NotificationsProviderProtocol {
    override fun buildNotifications(
        subaccountNumber: Int
    ): IMap<String, Notification> {
        /*
               We have to go through fills instead of orders, because
               1. Order doesn't have an updatedAt timestamp
               2. Order doesn't have an average filled price
         */
        val notifications = exchange.dydx.abacus.utils.mutableMapOf<String, Notification>()
        val subaccount =
            stateMachine.state?.subaccount(subaccountNumber) ?: return kollections.iMapOf()

        val subaccountFills = stateMachine.state?.fills?.get("$subaccountNumber")
        if (subaccountFills != null) {
            // Cache the orders
            val orders = exchange.dydx.abacus.utils.mutableMapOf<String, SubaccountOrder>()
            for (order in subaccount.orders ?: iListOf()) {
                orders[order.id] = order
            }
            // Cache the fills
            val fills =
                exchange.dydx.abacus.utils.mutableMapOf<String, IMutableList<SubaccountFill>>()
            val fillsList = iMutableListOf<SubaccountFill>()

            for (fill in subaccountFills) {
                val orderId = fill.orderId
                if (orderId != null) {
                    val order = orders[orderId]
                    if (order != null) {
                        val fillsForOrder = fills[orderId] ?: iMutableListOf()
                        fillsForOrder.add(fill)
                        fills[orderId] = fillsForOrder
                    }
                } else {
                    fillsList.add(fill)
                }
            }

            // Create notifications
            //
            for ((orderId, fillsForOrder) in fills) {
                val order = orders[orderId] ?: continue
                val notificationId = "order:$orderId"
                notifications.typedSafeSet(
                    notificationId,
                    createFillNotification(stateMachine, fillsForOrder, order),
                )
            }

            for (fill in fillsList) {
                val fillId = fill.id
                val notificationId = "fill:$fillId"
                notifications.typedSafeSet(
                    notificationId,
                    createNotificationForFill(stateMachine, fill),
                )
            }
        }
        return notifications
    }

    private fun createFillNotification(
        stateMachine: TradingStateMachine,
        fillsForOrder: IList<SubaccountFill>,
        order: SubaccountOrder,
    ): Notification? {
        // Fills are in reverse chronological order
        // First in the list is the newest fill
        val fill = fillsForOrder.firstOrNull() ?: return null
        val orderId = order.id
        val marketId = fill.marketId
        val market = market(stateMachine, marketId) ?: return null
        val tickSize = market.configs?.tickSize ?: return null
        val asset = stateMachine.state?.assetOfMarket(marketId)
        val assetText = asset?.name ?: marketId
        val marketImageUrl = asset?.resources?.imageUrl
        val side = fill.side.rawValue
        val sideText = uiImplementations.localizer?.localize("APP.GENERAL.$side")
        val amountText = parser.asString(order.size)
        val filledAmountText = parser.asString(order.totalFilled)
        val priceText = priceText(fill.price, tickSize)
        val averagePriceText = parser.asString(averagePrice(fillsForOrder))
        val orderType = order.type.rawValue
        val orderTypeText = text(orderType)
        val params = (
            iMapOf(
                "MARKET" to marketId,
                "ASSET" to assetText,
                "SIDE" to sideText,
                "AMOUNT" to amountText,
                "FILLED_AMOUNT" to filledAmountText,
                "PRICE" to priceText,
                "AVERAGE_PRICE" to averagePriceText,
                "ORDER_TYPE" to orderType,
                "ORDER_TYPE_TEXT" to orderTypeText,
                "ORDER_STATUS" to order.status.rawValue,
            ).filterValues { it != null } as Map<String, String>
            ).toIMap()
        val paramsAsJson = jsonEncoder.encode(params)

        val title = orderStatusTitle(order.status) ?: return null
        val text = orderStatusText(order.status, paramsAsJson)

        val notificationId = "order:$orderId"
        return Notification(
            notificationId,
            NotificationType.INFO,
            NotificationPriority.NORMAL,
            marketImageUrl,
            title,
            text,
            "/orders/$orderId",
            paramsAsJson,
            fill.createdAtMilliseconds,
        )
    }

    private fun createNotificationForFill(
        stateMachine: TradingStateMachine,
        fill: SubaccountFill,
    ): Notification? {
        val fillId = fill.id
        val marketId = fill.marketId
        val asset = stateMachine.state?.assetOfMarket(marketId) ?: return null
        val assetText = asset.name
        val marketImageUrl = asset.resources?.imageUrl
        val side = fill.side.rawValue
        val sideText = uiImplementations.localizer?.localize("APP.GENERAL.$side")
        val amountText = parser.asString(fill.size)
        val priceText = parser.asString(fill.price)
        val fillType = fill.type.rawValue
        val fillTypeText = text(fillType)
        val params = (
            iMapOf(
                "MARKET" to marketId,
                "ASSET" to assetText,
                "SIDE" to sideText,
                "AMOUNT" to amountText,
                "PRICE" to priceText,
                "FILL_TYPE" to fillType,
                "FILL_TYPE_TEXT" to fillTypeText,
            ).filterValues { it != null } as Map<String, String>
            ).toIMap()
        val paramsAsJson = jsonEncoder.encode(params)

        var title: String? = null
        var text: String? = null

        when (fill.type) {
            OrderType.Deleveraged -> {
                title = uiImplementations.localizer?.localize("NOTIFICATIONS.DELEVERAGED.TITLE")
                    ?: return null
                text = uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.DELEVERAGED.BODY",
                    paramsAsJson,
                )
            }

            OrderType.FinalSettlement -> {
                title =
                    uiImplementations.localizer?.localize("NOTIFICATIONS.FINAL_SETTLEMENT.TITLE")
                        ?: return null
                text = uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.FINAL_SETTLEMENT.BODY",
                    paramsAsJson,
                )
            }

            OrderType.Liquidated -> {
                title = uiImplementations.localizer?.localize("NOTIFICATIONS.LIQUIDATION.TITLE")
                    ?: return null
                text = uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.LIQUIDATION.BODY",
                    paramsAsJson,
                )
            }

            OrderType.Offsetting -> {
                title = uiImplementations.localizer?.localize("NOTIFICATIONS.OFFSETTING.TITLE")
                    ?: return null
                text = uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.OFFSETTING.BODY",
                    paramsAsJson,
                )
            }

            else -> return null
        }

        val notificationId = "fill:$fillId"
        return Notification(
            notificationId,
            NotificationType.INFO,
            NotificationPriority.NORMAL,
            marketImageUrl,
            title,
            text,
            null,
            paramsAsJson,
            fill.createdAtMilliseconds,
        )
    }

    private fun market(stateMachine: TradingStateMachine, marketId: String): PerpetualMarket? {
        return stateMachine.state?.market(marketId)
    }

    private fun priceText(price: Double, tickSize: Double): String {
        val rounded = Rounder.round(price, tickSize, Rounder.RoundingMode.NEAREST)
        return "$rounded"
    }

    private fun averagePrice(fillsForOrder: IList<SubaccountFill>): Double? {
        var total = 0.0
        var totalSize = 0.0
        for (fill in fillsForOrder) {
            total += fill.price * fill.size
            totalSize += fill.size
        }
        return if (totalSize != 0.0) total / totalSize else null
    }

    private fun orderStatusTitle(status: OrderStatus): String? {
        return when (status) {
            OrderStatus.Filled -> {
                uiImplementations.localizer?.localize("NOTIFICATIONS.ORDER_FILL.TITLE")
            }

            OrderStatus.PartiallyFilled, OrderStatus.PartiallyCanceled -> {
                uiImplementations.localizer?.localize("NOTIFICATIONS.ORDER_PARTIAL_FILL.TITLE")
            }

            OrderStatus.Canceled -> {
                uiImplementations.localizer?.localize("NOTIFICATIONS.ORDER_CANCEL.TITLE")
            }

            else -> null
        }
    }

    private fun orderStatusText(status: OrderStatus, paramsAsJson: String?): String? {
        return when (status) {
            OrderStatus.Filled -> {
                uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.ORDER_FILL.BODY",
                    paramsAsJson,
                )
            }

            OrderStatus.PartiallyFilled, OrderStatus.PartiallyCanceled -> {
                uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.ORDER_PARTIAL_FILL.BODY",
                    paramsAsJson,
                )
            }

            OrderStatus.Canceled -> {
                uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.ORDER_CANCEL.BODY",
                    paramsAsJson,
                )
            }

            else -> null
        }
    }

    private fun text(orderType: String): String? {
        return uiImplementations.localizer?.localize("APP.ENUMS.ORDER_TYPE.$orderType")
            ?: return null
    }
}
