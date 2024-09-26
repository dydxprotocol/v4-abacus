package exchange.dydx.abacus.state.app.helper

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.isOppositeOf
import exchange.dydx.abacus.protocols.FormatterProtocol
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.Toast
import exchange.dydx.abacus.protocols.ToastType
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger

interface TriggerOrderToastGeneratorProtocol {
    fun onTriggerOrderSubmitted(subaccountNumber: Int, payload: HumanReadableTriggerOrdersPayload, state: PerpetualState?)
    fun onTriggerOrderResponse(subaccountNumber: Int, successful: Boolean, error: ParsingError?, data: Any?)
}

class TriggerOrderToastGenerator(
    private val presentation: PresentationProtocol?,
    private val parser: ParserProtocol,
    private val formatter: FormatterProtocol?,
    private val localizer: LocalizerProtocol?,
    private val threading: ThreadingProtocol?,
) : TriggerOrderToastGeneratorProtocol {
    private enum class TriggerOrderStatus {
        Submitting,
        Success,
        Failed
    }

    private var orderPayloadStatus: MutableMap<String, TriggerOrderStatus> = mutableMapOf()

    private var state: PerpetualState? = null

    override fun onTriggerOrderSubmitted(
        subaccountNumber: Int,
        payload: HumanReadableTriggerOrdersPayload,
        state: PerpetualState?,
    ) {
        if (presentation == null) {
            return
        }

        this.state = state

        payload.cancelOrderPayloads.forEach { cancelOrderPayload ->
            orderPayloadStatus[cancelOrderPayload.orderId] = TriggerOrderStatus.Submitting
        }
        payload.placeOrderPayloads.forEach { placeOrderPayload ->
            orderPayloadStatus[placeOrderPayload.clientId.toString()] =
                TriggerOrderStatus.Submitting
        }

        val toasts = generateTriggerOrderToast(subaccountNumber, payload)
        showToasts(toasts)
    }

    override fun onTriggerOrderResponse(
        subaccountNumber: Int,
        successful: Boolean,
        error: ParsingError?,
        data: Any?
    ) {
        if (presentation == null) {
            return
        }

        val responsePayload = data as? HumanReadableTriggerOrdersPayload ?: return
        if (successful) {
            updateOrderStatus(responsePayload, TriggerOrderStatus.Success)
        } else {
            updateOrderStatus(responsePayload, TriggerOrderStatus.Failed)
        }
        val toasts = generateTriggerOrderToast(subaccountNumber, responsePayload)
        showToasts(toasts)
    }

    private fun updateOrderStatus(payload: HumanReadableTriggerOrdersPayload, status: TriggerOrderStatus) {
        payload.cancelOrderPayloads.forEach { cancelOrderPayload ->
            orderPayloadStatus[cancelOrderPayload.orderId] = status
        }
        payload.placeOrderPayloads.forEach { placeOrderPayload ->
            orderPayloadStatus[placeOrderPayload.clientId.toString()] = status
        }
    }

    private fun generateTriggerOrderToast(
        subaccountNumber: Int,
        responsePayload: HumanReadableTriggerOrdersPayload?
    ): List<Toast> {
        if (responsePayload == null || state == null) {
            return emptyList()
        }
        val marketId = state?.input?.triggerOrders?.marketId ?: return emptyList()
        val market = state?.market(marketId) ?: return emptyList()
        val subaccountNumberString = parser.asString(subaccountNumber) ?: return emptyList()
        val selectedSubaccount =
            state?.account?.subaccounts?.get(subaccountNumberString) ?: return emptyList()
        val selectedSubaccountPosition = selectedSubaccount.openPositions?.firstOrNull { position ->
            position.id == marketId && (
                position.side.current == PositionSide.SHORT ||
                    position.side.current == PositionSide.LONG
                )
        }
        val triggerOrders = selectedSubaccount.orders?.filter { order ->
            order.status == OrderStatus.Untriggered
        }
        val takeProfitOrders = triggerOrders?.filter { order ->
            selectedSubaccountPosition?.side?.current?.let { currentSide ->
                (order.type == OrderType.TakeProfitMarket || order.type == OrderType.TakeProfitLimit) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }
        val stopLossOrders = triggerOrders?.filter { order ->
            selectedSubaccountPosition?.side?.current?.let { currentSide ->
                (order.type == OrderType.StopMarket || order.type == OrderType.StopLimit) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }

        val tickSize = parser.asString(market.configs?.tickSize)

        val toasts = mutableListOf<Toast>()
        responsePayload.cancelOrderPayloads.forEach { cancelOrderPayload ->
            val toast = generateForCancelOrder(
                cancelOrderPayload,
                takeProfitOrders,
                stopLossOrders,
                tickSize,
            )
            if (toast != null) {
                toasts.add(toast)
            }
        }
        responsePayload.placeOrderPayloads.forEach { placeOrderPayload ->
            val toast = generateForPlaceOrder(
                placeOrderPayload,
                tickSize,
            )
            if (toast != null) {
                toasts.add(toast)
            }
        }

        return toasts
    }

    private fun generateForCancelOrder(
        cancelOrderPayload: HumanReadableCancelOrderPayload,
        takeProfitOrders: IList<SubaccountOrder>?,
        stopLossOrders: IList<SubaccountOrder>?,
        tickSize: String?,
    ): Toast? {
        if (formatter == null || localizer == null) {
            Logger.e { "Formatter or Localizer is null" }
            return null
        }

        val status = orderPayloadStatus[cancelOrderPayload.orderId] ?: TriggerOrderStatus.Submitting

        val takeProfitOrder = takeProfitOrders?.firstOrNull { it.id == cancelOrderPayload.orderId }
        val stopLossOrder = stopLossOrders?.firstOrNull { it.id == cancelOrderPayload.orderId }

        if (takeProfitOrder != null) {
            return generateForCancelOrderForTP(
                cancelOrderPayload.orderId,
                status,
                takeProfitOrder,
                tickSize,
            )
        } else if (stopLossOrder != null) {
            return generateForCancelOrderForSL(
                cancelOrderPayload.orderId,
                status,
                stopLossOrder,
                tickSize,
            )
        } else {
            return null
        }
    }

    private fun generateForCancelOrderForTP(
        id: String,
        status: TriggerOrderStatus,
        takeProfitOrder: SubaccountOrder?,
        tickSize: String?,
    ): Toast? {
        if (formatter == null || localizer == null) {
            Logger.e { "Formatter or Localizer is null" }
            return null
        }

        val orderType: String?
        val detail: String?
        if (takeProfitOrder != null) {
            val detailStringPath = when (status) {
                TriggerOrderStatus.Submitting -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVING.BODY"
                TriggerOrderStatus.Success -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVED.BODY"
                TriggerOrderStatus.Failed -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVING_ERROR.BODY"
            }
            val orderTypePath = when (status) {
                TriggerOrderStatus.Submitting -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVING.TITLE"
                TriggerOrderStatus.Success -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVED.TITLE"
                TriggerOrderStatus.Failed -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVING_ERROR.TITLE"
            }
            orderType = localizer.localize(orderTypePath)
            detail = localizer.localizeWithParams(
                path = detailStringPath,
                params = mapOf(
                    "OLD_VALUE" to (
                        formatter.dollar(
                            takeProfitOrder.triggerPrice,
                            tickSize,
                        ) ?: ""
                        ),
                ),
            )
        } else {
            orderType = null
            detail = null
        }

        return if (orderType != null && detail != null) {
            Toast(
                id = id,
                type = if (status == TriggerOrderStatus.Failed) ToastType.Warning else ToastType.Info,
                title = orderType,
                text = detail,
            )
        } else {
            null
        }
    }

    private fun generateForCancelOrderForSL(
        id: String,
        status: TriggerOrderStatus,
        stopLossOrder: SubaccountOrder?,
        tickSize: String?,
    ): Toast? {
        if (formatter == null || localizer == null) {
            Logger.e { "Formatter or Localizer is null" }
            return null
        }

        val orderType: String?
        val detail: String?
        if (stopLossOrder != null) {
            val detailStringPath = when (status) {
                TriggerOrderStatus.Submitting -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_REMOVING.BODY"
                TriggerOrderStatus.Success -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_REMOVED.BODY"
                TriggerOrderStatus.Failed -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_REMOVING_ERROR.BODY"
            }
            val orderTypePath = when (status) {
                TriggerOrderStatus.Submitting -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_REMOVING.TITLE"
                TriggerOrderStatus.Success -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_REMOVED.TITLE"
                TriggerOrderStatus.Failed -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_REMOVING_ERROR.TITLE"
            }
            orderType = localizer.localize(orderTypePath)
            detail = localizer.localizeWithParams(
                path = detailStringPath,
                params = mapOf(
                    "OLD_VALUE" to (
                        formatter.dollar(
                            stopLossOrder.triggerPrice,
                            tickSize,
                        ) ?: ""
                        ),
                ),
            )
        } else {
            orderType = null
            detail = null
        }

        return if (orderType != null && detail != null) {
            Toast(
                id = id,
                type = if (status == TriggerOrderStatus.Failed) ToastType.Warning else ToastType.Info,
                title = orderType,
                text = detail,
            )
        } else {
            null
        }
    }

    private fun generateForPlaceOrder(
        placeOrderPayload: HumanReadablePlaceOrderPayload,
        tickSize: String?,
    ): Toast? {
        if (formatter == null || localizer == null) {
            Logger.e { "Formatter or Localizer is null" }
            return null
        }

        val status = orderPayloadStatus[placeOrderPayload.clientId.toString()]
            ?: TriggerOrderStatus.Submitting
        val triggerPrice = formatter.dollar(placeOrderPayload.triggerPrice, tickSize) ?: ""

        val detailStringPath: String?
        val orderTypePath: String?

        when (placeOrderPayload.type) {
            "TAKE_PROFIT_MARKET", "TAKE_PROFIT" -> {
                detailStringPath = when (status) {
                    TriggerOrderStatus.Submitting -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_CREATING.BODY"
                    TriggerOrderStatus.Success -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_CREATED.BODY"
                    TriggerOrderStatus.Failed -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_CREATING_ERROR.BODY"
                }
                orderTypePath = when (status) {
                    TriggerOrderStatus.Submitting -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_CREATING.TITLE"
                    TriggerOrderStatus.Success -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_CREATED.TITLE"
                    TriggerOrderStatus.Failed -> "NOTIFICATIONS.TAKE_PROFIT_TRIGGER_CREATING_ERROR.TITLE"
                }
            }

            "STOP_LIMIT", "STOP_MARKET" -> {
                detailStringPath = when (status) {
                    TriggerOrderStatus.Submitting -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_CREATING.BODY"
                    TriggerOrderStatus.Success -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_CREATED.BODY"
                    TriggerOrderStatus.Failed -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_CREATING_ERROR.BODY"
                }
                orderTypePath = when (status) {
                    TriggerOrderStatus.Submitting -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_CREATING.TITLE"
                    TriggerOrderStatus.Success -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_CREATED.TITLE"
                    TriggerOrderStatus.Failed -> "NOTIFICATIONS.STOP_LOSS_TRIGGER_CREATING_ERROR.TITLE"
                }
            }

            else -> {
                detailStringPath = null
                orderTypePath = null
            }
        }

        val orderType = orderTypePath?.let { localizer.localize(it) }
        val detail = detailStringPath?.let {
            localizer.localizeWithParams(
                path = it,
                params = mapOf("NEW_VALUE" to triggerPrice),
            )
        }

        return if (orderType != null && detail != null) {
            Toast(
                id = placeOrderPayload.clientId.toString(),
                type = if (status == TriggerOrderStatus.Failed) ToastType.Warning else ToastType.Info,
                title = orderType,
                text = detail,
            )
        } else {
            null
        }
    }

    private fun showToasts(toasts: List<Toast>) {
        if (threading == null) {
            Logger.e { "Threading is null" }
            return
        }
        threading?.async(ThreadingType.main) {
            for (toast in toasts) {
                presentation?.showToast(toast)
            }
        }
    }
}
