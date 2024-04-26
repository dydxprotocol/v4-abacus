package exchange.dydx.abacus.state.manager.notification

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.notification.providers.BlockRewardNotificationProvider
import exchange.dydx.abacus.state.manager.notification.providers.OrderStatusChangesNotificationProvider
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.UIImplementations
import kollections.toIMap

interface NotificationsProviderProtocol {
    fun buildNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): IMap<String, Notification>

    fun asset(stateMachine: TradingStateMachine, marketId: String): Asset? {
        val market = stateMachine.state?.market(marketId) ?: return null
        val assetId = market.assetId
        return stateMachine.state?.asset(assetId)
    }
}

class NotificationsProvider(
    private val uiImplementations: UIImplementations,
    private val environment: V4Environment,
    private val parser: ParserProtocol,
    private val jsonEncoder: JsonEncoder,
    private val useParentSubaccount: Boolean = false,
) : NotificationsProviderProtocol {

    private val providers = listOf(
        BlockRewardNotificationProvider(
            uiImplementations,
            environment,
            jsonEncoder,
        ),
        FillsNotificationProvider(
            uiImplementations,
            parser,
            jsonEncoder,
        ),
        PositionsNotificationProvider(
            uiImplementations,
            parser,
            jsonEncoder,
            useParentSubaccount,
        ),
        OrderStatusChangesNotificationProvider(
            uiImplementations,
            parser,
            jsonEncoder,
        ),
    )

    override fun buildNotifications(
        stateMachine: TradingStateMachine,
        subaccountNumber: Int
    ): IMap<String, Notification> {
        var merged: Map<String, Notification>? = null

        providers.forEach { provider ->
            val notifications = provider.buildNotifications(stateMachine, subaccountNumber)
            merged = ParsingHelper.merge(merged, notifications) as? Map<String, Notification>
        }

        return merged?.toIMap() ?: kollections.iMapOf()
    }
}
