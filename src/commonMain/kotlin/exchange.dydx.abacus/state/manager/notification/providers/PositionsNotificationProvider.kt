package exchange.dydx.abacus.state.manager.notification

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.NotificationPriority
import exchange.dydx.abacus.output.NotificationType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import kollections.toIMap

class PositionsNotificationProvider(
    private val uiImplementations: UIImplementations,
    private val parser: ParserProtocol,
    private val jsonEncoder: JsonEncoder,
    private val useParentSubaccount: Boolean = false,
) : NotificationsProviderProtocol {
    override fun buildNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): IMap<String, Notification> {
        /*
        We have to go to the dynamic data to find closed positions
        Struct contains open positions only
         */
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
                if (positionStatus == "CLOSED") {
                    val closedAt = parser.asDatetime(position["closedAt"]) ?: continue
                    val asset = asset(stateMachine, marketId) ?: continue
                    val assetText = asset.name
                    val marketImageUrl = asset.resources?.imageUrl
                    val params = (
                        iMapOf(
                            "MARKET" to marketId,
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
}
