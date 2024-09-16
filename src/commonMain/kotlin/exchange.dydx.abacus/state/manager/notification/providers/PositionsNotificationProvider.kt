package exchange.dydx.abacus.state.manager.notification

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.NotificationPriority
import exchange.dydx.abacus.output.NotificationType
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import indexer.codegen.IndexerPerpetualPositionStatus
import kollections.toIMap

class PositionsNotificationProvider(
    private val stateMachine: TradingStateMachine,
    private val uiImplementations: UIImplementations,
    private val parser: ParserProtocol,
    private val jsonEncoder: JsonEncoder,
    private val useParentSubaccount: Boolean = false,
) : NotificationsProviderProtocol {
    override fun buildNotifications(
        subaccountNumber: Int
    ): IMap<String, Notification> {
        /*
        We have to go to the dynamic data to find closed positions
        Struct contains open positions only
         */
        if (stateMachine.staticTyping) {
            val notifications = exchange.dydx.abacus.utils.mutableMapOf<String, Notification>()
            val positions = if (useParentSubaccount) {
                stateMachine.internalState.wallet.account.groupedSubaccounts[subaccountNumber]?.positions
            } else {
                stateMachine.internalState.wallet.account.subaccounts[subaccountNumber]?.positions
            }

            if (positions != null) {
                for ((marketId, position) in positions) {
                    val positionStatus = position.status
                    val displayId = MarketId.getDisplayId(marketId)
                    if (positionStatus == IndexerPerpetualPositionStatus.CLOSED) {
                        val closedAt = position.closedAt ?: continue
                        val asset = stateMachine.state?.assetOfMarket(marketId) ?: continue
                        val assetText = asset.name
                        val marketImageUrl = asset.resources?.imageUrl
                        val params = (
                            iMapOf(
                                "MARKET" to displayId,
                                "ASSET" to assetText,
                            ).filterValues { it != null } as Map<String, String>
                            ).toIMap()
                        val paramsAsJson = jsonEncoder.encode(params)

                        val title =
                            uiImplementations.localizer?.localize("NOTIFICATIONS.POSITION_CLOSED.TITLE")
                                ?: continue
                        val text = uiImplementations.localizer?.localize(
                            "NOTIFICATIONS.POSITION_CLOSED.BODY",
                            paramsAsJson,
                        )

                        val notificationId = "position:$marketId"
                        notifications[notificationId] = Notification(
                            id = notificationId,
                            type = NotificationType.INFO,
                            priority = NotificationPriority.NORMAL,
                            image = marketImageUrl,
                            title = title,
                            text = text,
                            link = null,
                            data = paramsAsJson,
                            updateTimeInMilliseconds = closedAt.toEpochMilliseconds().toDouble(),
                        )
                    }
                }
            }
            return notifications
        } else {
            val notifications = exchange.dydx.abacus.utils.mutableMapOf<String, Notification>()
            val positions = parser.asMap(
                parser.value(
                    stateMachine.data,
                    if (useParentSubaccount) {
                        "wallet.account.groupedSubaccounts.$subaccountNumber.positions"
                    } else {
                        "wallet.account.subaccounts.$subaccountNumber.positions"
                    },
                ),
            )

            if (positions != null) {
                for ((marketId, data) in positions) {
                    val position = parser.asMap(data) ?: continue
                    val positionStatus = parser.asString(position["status"])
                    val displayId = parser.asString(position["displayId"])
                    if (positionStatus == "CLOSED") {
                        val closedAt = parser.asDatetime(position["closedAt"]) ?: continue
                        val asset = stateMachine.state?.assetOfMarket(marketId) ?: continue
                        val assetText = asset.name
                        val marketImageUrl = asset.resources?.imageUrl
                        val params = (
                            iMapOf(
                                "MARKET" to displayId,
                                "ASSET" to assetText,
                            ).filterValues { it != null } as Map<String, String>
                            ).toIMap()
                        val paramsAsJson = jsonEncoder.encode(params)

                        val title =
                            uiImplementations.localizer?.localize("NOTIFICATIONS.POSITION_CLOSED.TITLE")
                                ?: continue
                        val text = uiImplementations.localizer?.localize(
                            "NOTIFICATIONS.POSITION_CLOSED.BODY",
                            paramsAsJson,
                        )

                        val notificationId = "position:$marketId"
                        notifications[notificationId] = Notification(
                            id = notificationId,
                            type = NotificationType.INFO,
                            priority = NotificationPriority.NORMAL,
                            image = marketImageUrl,
                            title = title,
                            text = text,
                            link = null,
                            data = paramsAsJson,
                            updateTimeInMilliseconds = closedAt.toEpochMilliseconds().toDouble(),
                        )
                    }
                }
            }
            return notifications
        }
    }
}
