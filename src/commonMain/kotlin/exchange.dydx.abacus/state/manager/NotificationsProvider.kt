package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.NotificationPriority
import exchange.dydx.abacus.output.NotificationType
import exchange.dydx.abacus.output.PerpetualMarket
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
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutableMapOf
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
        val fillsNotifications = buildFillsNotifications(stateMachine, subaccountNumber)
        val positionsNotifications = buildPositionsNotifications(stateMachine, subaccountNumber)
        val orderStatusChangesNotifications =
            buildOrderStatusChangesNotifications(stateMachine, subaccountNumber)
        val merged1 = ParsingHelper.merge(
            fillsNotifications,
            positionsNotifications
        )
        val merged2 = ParsingHelper.merge(
            merged1,
            orderStatusChangesNotifications
        )
        return (merged2 as? Map<String, Notification>)!!.toIMap()
    }

    private fun asset(marketId: String): String {
        return marketId.split("-").first()
    }

    private fun buildFillsNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): Map<String, Notification> {
        /*
        We have to go through fills instead of orders, because
        1. Order doesn't have an updatedAt timestamp
        2. Order doesn't have an average filled price
         */
        val notifications = mutableMapOf<String, Notification>()
        val subaccount =
            stateMachine.state?.subaccount(subaccountNumber) ?: return kollections.iMapOf()

        val subaccountFills = stateMachine.state?.fills?.get("$subaccountNumber")
        if (subaccountFills != null) {
            // Cache the orders
            val orders = mutableMapOf<String, SubaccountOrder>()
            for (order in subaccount.orders ?: iListOf()) {
                orders[order.id] = order
            }
            // Cache the fills
            val fills = mutableMapOf<String, IMutableList<SubaccountFill>>()
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
                    createFillNotification(stateMachine, fillsForOrder, order)
                )
            }

            for (fill in fillsList) {
                val fillId = fill.id
                val notificationId = "fill:$fillId"
                notifications.typedSafeSet(
                    notificationId,
                    createNotificationForFill(stateMachine, fill)
                )
            }
        }
        return notifications
    }

    private fun market(stateMachine: TradingStateMachine, marketId: String): PerpetualMarket? {
        return stateMachine.state?.market(marketId)
    }

    private fun asset(stateMachine: TradingStateMachine, marketId: String): Asset? {
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
        val market = market(stateMachine, marketId) ?: return null
        val tickSize = market.configs?.tickSize ?: return null
        val asset = asset(stateMachine, marketId)
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
        val params = (iMapOf(
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
            paramsAsJson,
            fill.createdAtMilliseconds,
        )
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

    private fun createNotificationForFill(
        stateMachine: TradingStateMachine,
        fill: SubaccountFill,
    ): Notification? {
        val fillId = fill.id
        val marketId = fill.marketId
        val asset = asset(stateMachine, marketId) ?: return null
        val assetText = asset.name
        val marketImageUrl = asset.resources?.imageUrl
        val side = fill.side.rawValue
        val sideText = uiImplementations.localizer?.localize("APP.GENERAL.$side")
        val amountText = parser.asString(fill.size)
        val priceText = parser.asString(fill.price)
        val fillType = fill.type.rawValue
        val fillTypeText = text(fillType)
        val params = (iMapOf(
            "MARKET" to marketId,
            "ASSET" to assetText,
            "SIDE" to sideText,
            "AMOUNT" to amountText,
            "PRICE" to priceText,
            "FILL_TYPE" to fillType,
            "FILL_TYPE_TEXT" to fillTypeText,
        ).filterValues { it != null } as Map<String, String>).toIMap()
        val paramsAsJson = jsonEncoder.encode(params)

        var title: String? = null
        var text: String? = null

        when (fill.type) {
            OrderType.deleveraged -> {
                title = uiImplementations.localizer?.localize("NOTIFICATIONS.DELEVERAGED.TITLE") ?: return null
                text = uiImplementations.localizer?.localize("NOTIFICATIONS.DELEVERAGED.BODY", paramsAsJson)
            }
            OrderType.finalSettlement -> {
                title = uiImplementations.localizer?.localize("NOTIFICATIONS.FINAL_SETTLEMENT.TITLE") ?: return null
                text = uiImplementations.localizer?.localize("NOTIFICATIONS.FINAL_SETTLEMENT.BODY", paramsAsJson)
            }
            OrderType.liquidation -> {
                title = uiImplementations.localizer?.localize("NOTIFICATIONS.LIQUIDATION.TITLE") ?: return null
                text =  uiImplementations.localizer?.localize("NOTIFICATIONS.LIQUIDATION.BODY", paramsAsJson)

            }
            OrderType.offsetting -> {
                title = uiImplementations.localizer?.localize("NOTIFICATIONS.OFFSETTING.TITLE") ?: return null
                text = uiImplementations.localizer?.localize("NOTIFICATIONS.OFFSETTING.BODY", paramsAsJson)
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

    private fun buildPositionsNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): Map<String, Notification> {
        /*
        We have to go to the dynamic data to find closed positions
        Struct contains open positions only
         */
        val notifications = mutableMapOf<String, Notification>()
        val positions = parser.asMap(
            parser.value(
                stateMachine.data,
                "wallet.account.subaccounts.$subaccountNumber.positions"
            )
        )

        if (positions != null) {
            for ((marketId, data) in positions) {
                val position = parser.asMap(data) ?: continue
                val positionStatus = parser.asString(position["status"])
                if (positionStatus == "CLOSED") {
                    val closedAt = parser.asDatetime(position["closedAt"]) ?: continue
                    val asset = asset(stateMachine, marketId) ?: continue
                    val assetText = asset.name
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
                        paramsAsJson,
                        closedAt.toEpochMilliseconds().toDouble(),
                    )
                }
            }
        }
        return notifications
    }

    private fun buildOrderStatusChangesNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): Map<String, Notification> {
        /*
        We have to go through fills instead of orders, because
        1. Order doesn't have an updatedAt timestamp
        2. Order doesn't have an average filled price
         */
        val notifications = mutableMapOf<String, Notification>()
        val subaccount =
            stateMachine.state?.subaccount(subaccountNumber) ?: return kollections.iMapOf()
        subaccount.orders

        val subaccountOrders = subaccount.orders

        if (subaccountOrders != null) {
            for (order in subaccountOrders) {
                val orderStatusNotification = createOrderStatusNotification(stateMachine, order)
                if (orderStatusNotification != null) {
                    val orderId = order.id
                    val notificationId = "order_status:$orderId"
                    notifications.typedSafeSet(notificationId, orderStatusNotification)
                }
            }
        }
        return notifications
    }

    private fun createOrderStatusNotification(
        stateMachine: TradingStateMachine,
        order: SubaccountOrder,
    ): Notification? {
        var timestamp: Double? = null
        val statusNotificationStringKey = when (order.status) {
            OrderStatus.open -> {
                when (order.type) {
                    OrderType.stopLimit, OrderType.stopMarket, OrderType.takeProfitLimit, OrderType.takeProfitMarket -> {
                        timestamp = order.updatedAtMilliseconds
                        if (timestamp != null && order.totalFilled == Numeric.double.ZERO) {
                            "NOTIFICATIONS.ORDER_TRIGGERED"
                        } else null
                    }

                    OrderType.limit, OrderType.market -> {
                        /*
                        Short term orders should get filled/partially filled immediately, so we don't need to handle OPENED notification
                        And it doesn't have a timestamp
                         */
                        timestamp = order.createdAtMilliseconds
                        if (timestamp != null && order.totalFilled == Numeric.double.ZERO) {
                            "NOTIFICATIONS.ORDER_OPENED"
                        } else null
                    }

                    else -> null
                }
            }

            OrderStatus.cancelled -> {
                if ((order.totalFilled ?: Numeric.double.ZERO) > Numeric.double.ZERO) {
                    "NOTIFICATIONS.ORDER_CANCEL_WITH_PARTIAL_FILL"
                } else {
                    "NOTIFICATIONS.ORDER_CANCEL"
                }
            }

            else -> null
        }
        return if (statusNotificationStringKey != null && timestamp != null) {
            val marketId = order.marketId
            val asset = asset(stateMachine, marketId) ?: return null
            val marketImageUrl = asset.resources?.imageUrl
            val side = order.side.rawValue
            val sideText = uiImplementations.localizer?.localize("APP.GENERAL.$side")
            val amountText = parser.asString(order.size)
            val totalFilled = parser.asString(order.totalFilled)
            val orderType = order.type.rawValue
            val orderTypeText = text(orderType)
            val params = (iMapOf(
                "MARKET" to marketId,
                "SIDE" to sideText,
                "AMOUNT" to amountText,
                "TOTAL_FILLED" to totalFilled,
                "ORDER_TYPE" to orderType,
                "ORDER_TYPE_TEXT" to orderTypeText,
                "ORDER_STATUS" to order.status.rawValue,
            ).filterValues { it != null } as Map<String, String>).toIMap()
            val paramsAsJson = jsonEncoder.encode(params)

            val title =
                uiImplementations.localizer?.localize("$statusNotificationStringKey.TITLE")
                    ?: return null
            val text =
                uiImplementations.localizer?.localize(
                    "$statusNotificationStringKey.BODY",
                    paramsAsJson
                )

            val orderId = order.id
            val notificationId = "orderstatus:$orderId"
            return Notification(
                notificationId,
                NotificationType.INFO,
                NotificationPriority.NORMAL,
                marketImageUrl,
                title,
                text,
                "/orders/$orderId",
                paramsAsJson,
                timestamp,
            )
        } else null
    }

    private fun text(orderType: String): String? {
        return uiImplementations.localizer?.localize("APP.ENUMS.ORDER_TYPE.$orderType")
            ?: return null
    }
}