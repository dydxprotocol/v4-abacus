package exchange.dydx.abacus.state.manager.notification.providers

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.NotificationPriority
import exchange.dydx.abacus.output.NotificationType
import exchange.dydx.abacus.output.account.BlockReward
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.notification.NotificationsProviderProtocol
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.typedSafeSet
import kollections.toIMap

class BlockRewardNotificationProvider(
    private val stateMachine: TradingStateMachine,
    private val uiImplementations: UIImplementations,
    private val environment: V4Environment,
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
        val account =
            stateMachine.state?.account ?: return kollections.iMapOf()

        val notifications = exchange.dydx.abacus.utils.mutableMapOf<String, Notification>()
        val accountBlockRewards = account.tradingRewards?.blockRewards
        val token = environment.tokens["chain"]?.name
        if (accountBlockRewards != null && token != null) {
            for (blockReward in accountBlockRewards) {
                createBlockRewardNotification(blockReward, token)?.let {
                    notifications.typedSafeSet(
                        it.id,
                        it,
                    )
                }
            }
        }
        return notifications
    }

    private fun createBlockRewardNotification(
        blockReward: BlockReward,
        token: String,
    ): Notification? {
        val blockHeight = blockReward.createdAtHeight
        val blockRewardAmount = blockReward.tradingReward
        val params = iMapOf(
            "BLOCK_REWARD_HEIGHT" to blockHeight,
            "TOKEN_NAME" to token,
            "BLOCK_REWARD_AMOUNT" to blockRewardAmount,
            "BLOCK_REWARD_TIME_MILLISECONDS" to blockReward.createdAtMilliseconds,
        ).toIMap()
        val paramsAsJson = jsonEncoder.encode(params)

        val title =
            uiImplementations.localizer?.localize("NOTIFICATIONS.BLOCK_REWARD.TITLE")
                ?: return null
        val text =
            uiImplementations.localizer?.localize(
                "NOTIFICATIONS.BLOCK_REWARD.BODY",
                paramsAsJson,
            )

        val notificationId = "blockReward:$blockHeight"
        return Notification(
            id = notificationId,
            type = NotificationType.INFO,
            priority = NotificationPriority.NORMAL,
            image = null,
            title = title,
            text = text,
            link = null,
            data = paramsAsJson,
            updateTimeInMilliseconds = blockReward.createdAtMilliseconds,
        )
    }
}
