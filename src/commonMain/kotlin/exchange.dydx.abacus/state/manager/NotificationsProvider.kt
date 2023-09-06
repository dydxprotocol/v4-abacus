package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.NotificationPriority
import exchange.dydx.abacus.output.NotificationType
import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.modal.TradingStateMachine
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.typedSafeSet
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIMap

class NotificationsProvider(
    private val uiImplementations: UIImplementations,
    private val parser: ParserProtocol,
    private val jsonEncoder: JsonEncoder
) {
    internal fun buildNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): IMap<String, Notification> {
        val fillsNotifcations = buildFillsNotifications(stateMachine, subaccountNumber)
        val positionsNotifications = buildPositionsNotifications(stateMachine, subaccountNumber)
        return ParsingHelper.merge(fillsNotifcations, positionsNotifications) as IMap<String, Notification>
    }

    private fun asset(marketId: String): String {
        return marketId.split("-").first()
    }

    private fun buildFillsNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): IMap<String, Notification> {
        /*
        We have to go through fills instead of orders, because
        1. Order doesn't have an updatedAt timestamp
        2. Order doesn't have an average filled price
         */
        val notifications = iMutableMapOf<String, Notification>()
        val subaccount =
            stateMachine.state?.subaccount(subaccountNumber) ?: return kollections.iMapOf()

        val subaccountFills = stateMachine.state?.fills?.get("$subaccountNumber")
        if (subaccountFills != null) {
            // Cache the orders
            val orders = iMutableMapOf<String, SubaccountOrder>()
            for (order in subaccount.orders ?: iListOf()) {
                orders[order.id] = order
            }
            // Cache the fills
            val fills = iMutableMapOf<String, IMutableList<SubaccountFill>>()
            val liquidated = iMutableListOf<SubaccountFill>()

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
                    if (fill.type == OrderType.liquidated) {
                        liquidated.add(fill)
                    }
                }
            }

            // Create notifications
            //
            for ((orderId, fillsForOrder) in fills) {
                val order = orders[orderId] ?: continue
                val notificationId = "order:$orderId"
                notifications.typedSafeSet(notificationId, createFillNotification(stateMachine, fillsForOrder, order))
            }

            for (fill in liquidated) {
                val fillId = fill.id
                val notificationId = "fill:$fillId"
                notifications.typedSafeSet(notificationId, createLiquidationNotification(stateMachine, fill))
            }
        }
        return notifications
    }

    private fun asset(stateMachine:TradingStateMachine, marketId: String): Asset? {
        val market = stateMachine.state?.market(marketId) ?: return null
        val assetId = market.assetId
        return stateMachine.state?.asset(assetId)
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
        val asset = asset(stateMachine, marketId) ?: return null
        val assetText = asset.symbol
        val marketImageUrl = asset.resources?.imageUrl
        val side = fill.side.rawValue
        val sideText = uiImplementations.localizer?.localize("APP.GENERAL.$side")
        val amountText = parser.asString(order.size)
        val filledAmountText = parser.asString(order.totalFilled)
        val priceText = parser.asString(fill.price)
        val averagePriceText = parser.asString(averagePrice(fillsForOrder))
        val params = (iMapOf(
            "MARKET" to marketId,
            "ASSET" to assetText,
            "SIDE" to sideText,
            "AMOUNT" to amountText,
            "FILLED_AMOUNT" to filledAmountText,
            "PRICE" to priceText,
            "AVERAGE_PRICE" to averagePriceText,
        ).filterValues { it != null } as Map<String, String>).toIMap()
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
            params,
            fill.createdAtMilliseconds,
        )
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
            OrderStatus.filled -> {
                uiImplementations.localizer?.localize("NOTIFICATIONS.ORDER_FILL.TITLE")
            }

            OrderStatus.partiallyFilled -> {
                uiImplementations.localizer?.localize("NOTIFICATIONS.ORDER_PARTIAL_FILL.TITLE")
            }

            OrderStatus.cancelled -> {
                uiImplementations.localizer?.localize("NOTIFICATIONS.ORDER_CANCEL.TITLE")
            }

            else -> null
        }
    }

    private fun orderStatusText(status: OrderStatus, paramsAsJson: String?): String? {
        return when (status) {
            OrderStatus.filled -> {
                uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.ORDER_FILL.BODY",
                    paramsAsJson
                )
            }

            OrderStatus.partiallyFilled -> {
                uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.ORDER_PARTIAL_FILL.BODY",
                    paramsAsJson
                )
            }

            OrderStatus.cancelled -> {
                uiImplementations.localizer?.localize(
                    "NOTIFICATIONS.ORDER_CANCEL.BODY",
                    paramsAsJson
                )
            }

            else -> null
        }
    }

    private fun createLiquidationNotification(
        stateMachine: TradingStateMachine,
        fill: SubaccountFill,
    ): Notification? {
        val fillId = fill.id
        val marketId = fill.marketId
        val asset = asset(stateMachine, marketId) ?: return null
        val assetText = asset.symbol
        val marketImageUrl = asset.resources?.imageUrl
        val side = fill.side.rawValue
        val sideText = uiImplementations.localizer?.localize("APP.GENERAL.$side")
        val amountText = parser.asString(fill.size)
        val priceText = parser.asString(fill.price)
        val params = (iMapOf(
            "MARKET" to marketId,
            "ASSET" to assetText,
            "SIDE" to sideText,
            "AMOUNT" to amountText,
            "PRICE" to priceText,
        ).filterValues { it != null } as Map<String, String>).toIMap()
        val paramsAsJson = jsonEncoder.encode(params)

        val title =
            uiImplementations.localizer?.localize("NOTIFICATIONS.LIQUIDATION.TITLE") ?: return null
        val text =
            uiImplementations.localizer?.localize("NOTIFICATIONS.LIQUIDATION.BODY", paramsAsJson)

        val notificationId = "fill:$fillId"
        return Notification(
            notificationId,
            NotificationType.INFO,
            NotificationPriority.NORMAL,
            marketImageUrl,
            title,
            text,
            null,
            params,
            fill.createdAtMilliseconds,
        )
    }

    private fun buildPositionsNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): IMap<String, Notification> {
        /*
        We have to go to the dynamic data to find closed positions
        Struct contains open positions only
         */
        val notifications = iMutableMapOf<String, Notification>()
        val positions = parser.asMap(parser.value(stateMachine.data, "wallet.account.subaccounts.$subaccountNumber.positions"))

        if (positions != null) {
            for ((marketId, data) in positions) {
                val position = parser.asMap(data) ?: continue
                val positionStatus = parser.asString(position["status"])
                if (positionStatus == "CLOSED") {
                    val closedAt = parser.asDatetime(position["closedAt"]) ?: continue
                    val asset = asset(stateMachine, marketId) ?: continue
                    val assetText = asset.symbol
                    val marketImageUrl = asset.resources?.imageUrl
                    val params = (iMapOf(
                        "MARKET" to marketId,
                        "ASSET" to assetText,
                    ).filterValues { it != null } as Map<String, String>).toIMap()
                    val paramsAsJson = jsonEncoder.encode(params)

                    val title =
                        uiImplementations.localizer?.localize("NOTIFICATIONS.POSITION_CLOSED.TITLE")
                            ?: continue
                    val text = uiImplementations.localizer?.localize(
                        "NOTIFICATIONS.POSITION_CLOSED.BODY",
                        paramsAsJson
                    )


                    val notificationId = "position:$marketId"
                    notifications[notificationId] = Notification(
                        notificationId,
                        NotificationType.INFO,
                        NotificationPriority.NORMAL,
                        marketImageUrl,
                        title,
                        text,
                        null,
                        params,
                        closedAt.toEpochMilliseconds().toDouble(),
                    )
                }
            }
        }
        return notifications
    }

}