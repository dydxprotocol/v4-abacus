package exchange.dydx.abacus.processor.input

import exchange.dydx.abacus.calculator.v2.TriggerOrdersInputCalculatorV2
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TriggerPrice
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.internalstate.InternalTriggerOrderState
import exchange.dydx.abacus.state.internalstate.InternalTriggerOrdersInputState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import kollections.iListOf

internal interface TriggerOrdersInputProcessorProtocol {
    fun triggerOrderInput(
        inputState: InternalInputState,
        account: InternalAccountState,
        data: String?,
        type: TriggerOrdersInputField?,
        subaccountNumber: Int,
    ): StateChanges
}

internal class TriggerOrdersInputProcessor(
    private val parser: ParserProtocol,
    private val calculator: TriggerOrdersInputCalculatorV2 = TriggerOrdersInputCalculatorV2()
) : TriggerOrdersInputProcessorProtocol {

    override fun triggerOrderInput(
        inputState: InternalInputState,
        account: InternalAccountState,
        data: String?,
        type: TriggerOrdersInputField?,
        subaccountNumber: Int,
    ): StateChanges {
        if (inputState.currentType != InputType.TRIGGER_ORDERS) {
            inputState.currentType = InputType.TRIGGER_ORDERS
            inputState.triggerOrders = InternalTriggerOrdersInputState()
            calculator.calculate(inputState.triggerOrders, account, subaccountNumber)
        }

        if (type != null) {
            when (type) {
                TriggerOrdersInputField.marketId -> {
                    inputState.triggerOrders.marketId = parser.asString(data)
                }

                TriggerOrdersInputField.stopLossOrderId -> {
                    inputState.triggerOrders.stopLossOrder =
                        inputState.triggerOrders.stopLossOrder ?: InternalTriggerOrderState()
                    inputState.triggerOrders.stopLossOrder?.orderId = parser.asString(data)
                }

                TriggerOrdersInputField.takeProfitOrderId -> {
                    inputState.triggerOrders.takeProfitOrder =
                        inputState.triggerOrders.takeProfitOrder ?: InternalTriggerOrderState()
                    inputState.triggerOrders.takeProfitOrder?.orderId = parser.asString(data)
                }

                TriggerOrdersInputField.stopLossOrderType -> {
                    inputState.triggerOrders.stopLossOrder =
                        inputState.triggerOrders.stopLossOrder ?: InternalTriggerOrderState()
                    inputState.triggerOrders.stopLossOrder?.type =
                        OrderType.invoke(parser.asString(data))
                }

                TriggerOrdersInputField.takeProfitOrderType -> {
                    inputState.triggerOrders.takeProfitOrder =
                        inputState.triggerOrders.takeProfitOrder ?: InternalTriggerOrderState()
                    inputState.triggerOrders.takeProfitOrder?.type =
                        OrderType.invoke(parser.asString(data))
                }

                TriggerOrdersInputField.size -> {
                    inputState.triggerOrders.size = parser.asDouble(data)
                }

                TriggerOrdersInputField.stopLossOrderSize -> {
                    inputState.triggerOrders.stopLossOrder =
                        inputState.triggerOrders.stopLossOrder ?: InternalTriggerOrderState()
                    inputState.triggerOrders.stopLossOrder?.size = parser.asDouble(data)
                }

                TriggerOrdersInputField.takeProfitOrderSize -> {
                    inputState.triggerOrders.takeProfitOrder =
                        inputState.triggerOrders.takeProfitOrder ?: InternalTriggerOrderState()
                    inputState.triggerOrders.takeProfitOrder?.size = parser.asDouble(data)
                }

                TriggerOrdersInputField.stopLossLimitPrice -> {
                    inputState.triggerOrders.stopLossOrder =
                        inputState.triggerOrders.stopLossOrder ?: InternalTriggerOrderState()
                    var price =
                        TriggerPrice.safeCreate(inputState.triggerOrders.stopLossOrder?.price)
                    price = price.copy(
                        limitPrice = parser.asDouble(data),
                    )
                    inputState.triggerOrders.stopLossOrder?.price = price
                }

                TriggerOrdersInputField.takeProfitLimitPrice -> {
                    inputState.triggerOrders.takeProfitOrder =
                        inputState.triggerOrders.takeProfitOrder ?: InternalTriggerOrderState()
                    var price =
                        TriggerPrice.safeCreate(inputState.triggerOrders.takeProfitOrder?.price)
                    price = price.copy(
                        limitPrice = parser.asDouble(data),
                    )
                    inputState.triggerOrders.takeProfitOrder?.price = price
                }

                TriggerOrdersInputField.stopLossPrice -> {
                    inputState.triggerOrders.stopLossOrder =
                        inputState.triggerOrders.stopLossOrder ?: InternalTriggerOrderState()
                    val newValue = parser.asDouble(data)
                    val stopLossPriceChanged =
                        (newValue != inputState.triggerOrders.stopLossOrder?.price?.triggerPrice)
                    var price =
                        TriggerPrice.safeCreate(inputState.triggerOrders.stopLossOrder?.price)
                    price = price.copy(
                        triggerPrice = newValue,
                    )
                    if (stopLossPriceChanged) {
                        price = price.copy(
                            input = type.rawValue,
                        )
                    }
                    inputState.triggerOrders.stopLossOrder?.price = price
                }

                TriggerOrdersInputField.stopLossPercentDiff -> {
                    inputState.triggerOrders.stopLossOrder =
                        inputState.triggerOrders.stopLossOrder ?: InternalTriggerOrderState()
                    val newValue = parser.asDouble(data)
                    val stopLossPriceChanged =
                        (newValue != inputState.triggerOrders.stopLossOrder?.price?.percentDiff)
                    var price =
                        TriggerPrice.safeCreate(inputState.triggerOrders.stopLossOrder?.price)
                    price = price.copy(
                        percentDiff = newValue,
                    )
                    if (stopLossPriceChanged) {
                        price = price.copy(
                            input = type.rawValue,
                        )
                    }
                    inputState.triggerOrders.stopLossOrder?.price = price
                }

                TriggerOrdersInputField.stopLossUsdcDiff -> {
                    inputState.triggerOrders.stopLossOrder =
                        inputState.triggerOrders.stopLossOrder ?: InternalTriggerOrderState()
                    val newValue = parser.asDouble(data)
                    val stopLossPriceChanged =
                        (newValue != inputState.triggerOrders.stopLossOrder?.price?.usdcDiff)
                    var price =
                        TriggerPrice.safeCreate(inputState.triggerOrders.stopLossOrder?.price)
                    price = price.copy(
                        usdcDiff = newValue,
                    )
                    if (stopLossPriceChanged) {
                        price = price.copy(
                            input = type.rawValue,
                        )
                    }
                    inputState.triggerOrders.stopLossOrder?.price = price
                }

                TriggerOrdersInputField.takeProfitPrice -> {
                    inputState.triggerOrders.takeProfitOrder =
                        inputState.triggerOrders.takeProfitOrder ?: InternalTriggerOrderState()
                    val newValue = parser.asDouble(data)
                    val takeProfitPriceChanged =
                        (newValue != inputState.triggerOrders.takeProfitOrder?.price?.triggerPrice)
                    var price =
                        TriggerPrice.safeCreate(inputState.triggerOrders.takeProfitOrder?.price)
                    price = price.copy(
                        triggerPrice = newValue,
                    )
                    if (takeProfitPriceChanged) {
                        price = price.copy(
                            input = type.rawValue,
                        )
                    }
                    inputState.triggerOrders.takeProfitOrder?.price = price
                }

                TriggerOrdersInputField.takeProfitPercentDiff -> {
                    inputState.triggerOrders.takeProfitOrder =
                        inputState.triggerOrders.takeProfitOrder ?: InternalTriggerOrderState()
                    val newValue = parser.asDouble(data)
                    val takeProfitPriceChanged =
                        (newValue != inputState.triggerOrders.takeProfitOrder?.price?.percentDiff)
                    var price =
                        TriggerPrice.safeCreate(inputState.triggerOrders.takeProfitOrder?.price)
                    price = price.copy(
                        percentDiff = newValue,
                    )
                    if (takeProfitPriceChanged) {
                        price = price.copy(
                            input = type.rawValue,
                        )
                    }
                    inputState.triggerOrders.takeProfitOrder?.price = price
                }

                TriggerOrdersInputField.takeProfitUsdcDiff -> {
                    inputState.triggerOrders.takeProfitOrder =
                        inputState.triggerOrders.takeProfitOrder ?: InternalTriggerOrderState()
                    val newValue = parser.asDouble(data)
                    val takeProfitPriceChanged =
                        (newValue != inputState.triggerOrders.takeProfitOrder?.price?.usdcDiff)
                    var price =
                        TriggerPrice.safeCreate(inputState.triggerOrders.takeProfitOrder?.price)
                    price = price.copy(
                        usdcDiff = newValue,
                    )
                    if (takeProfitPriceChanged) {
                        price = price.copy(
                            input = type.rawValue,
                        )
                    }
                    inputState.triggerOrders.takeProfitOrder?.price = price
                }
            }
        }

        return StateChanges(
            changes = iListOf(Changes.input),
            markets = null,
            subaccountNumbers = iListOf(subaccountNumber),
        )
    }
}
