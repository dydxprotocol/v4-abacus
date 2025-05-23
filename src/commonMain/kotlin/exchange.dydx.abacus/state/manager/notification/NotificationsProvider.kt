package exchange.dydx.abacus.state.manager.notification

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.notification.providers.BlockRewardNotificationProvider
import exchange.dydx.abacus.state.manager.notification.providers.OrderStatusChangesNotificationProvider
import exchange.dydx.abacus.state.supervisor.NotificationProviderType
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.UIImplementations
import kollections.toIMap

internal interface NotificationsProviderProtocol {
    fun buildNotifications(
        subaccountNumber: Int
    ): IMap<String, Notification>
}

internal class NotificationsProvider(
    private val stateMachine: TradingStateMachine,
    private val uiImplementations: UIImplementations,
    private val environment: V4Environment,
    private val parser: ParserProtocol,
    private val jsonEncoder: JsonEncoder,
    private val useParentSubaccount: Boolean = false,
    val notifications: List<NotificationProviderType> = listOf(
        NotificationProviderType.BlockReward,
        NotificationProviderType.Fills,
        NotificationProviderType.Positions,
        NotificationProviderType.OrderStatusChange,
    ),
    private val providers: List<NotificationsProviderProtocol> = listOfNotNull(
        if (notifications.contains(NotificationProviderType.BlockReward)) {
            BlockRewardNotificationProvider(
                stateMachine = stateMachine,
                uiImplementations = uiImplementations,
                environment = environment,
                jsonEncoder = jsonEncoder,
            )
        } else {
            null
        },

        if (notifications.contains(NotificationProviderType.Fills)) {
            FillsNotificationProvider(
                stateMachine = stateMachine,
                uiImplementations = uiImplementations,
                parser = parser,
                jsonEncoder = jsonEncoder,
            )
        } else {
            null
        },

        if (notifications.contains(NotificationProviderType.Positions)) {
            PositionsNotificationProvider(
                stateMachine = stateMachine,
                uiImplementations = uiImplementations,
                jsonEncoder = jsonEncoder,
                useParentSubaccount = useParentSubaccount,
            )
        } else {
            null
        },

        if (notifications.contains(NotificationProviderType.OrderStatusChange)) {
            OrderStatusChangesNotificationProvider(
                stateMachine = stateMachine,
                uiImplementations = uiImplementations,
                parser = parser,
                jsonEncoder = jsonEncoder,
            )
        } else {
            null
        },
    ),
) : NotificationsProviderProtocol {

    override fun buildNotifications(
        subaccountNumber: Int
    ): IMap<String, Notification> {
        var merged: Map<String, Notification>? = null

        providers.forEach { provider ->
            val notifications = provider.buildNotifications(subaccountNumber)
            merged = ParsingHelper.merge(merged, notifications) as? Map<String, Notification>
        }

        return merged?.toIMap() ?: kollections.iMapOf()
    }
}
