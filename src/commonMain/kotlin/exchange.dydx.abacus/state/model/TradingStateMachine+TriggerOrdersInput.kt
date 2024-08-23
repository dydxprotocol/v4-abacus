package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.TriggerOrdersInputCalculator
import exchange.dydx.abacus.processor.input.TriggerOrdersInputProcessor
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.responses.cannotModify
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class TriggerOrdersInputField(val rawValue: String) {
    marketId("marketId"),
    size("size"),

    stopLossOrderId("stopLossOrder.orderId"),
    stopLossOrderSize("stopLossOrder.size"),
    stopLossOrderType("stopLossOrder.type"),
    stopLossLimitPrice("stopLossOrder.price.limitPrice"),
    stopLossPrice("stopLossOrder.price.triggerPrice"),
    stopLossPercentDiff("stopLossOrder.price.percentDiff"),
    stopLossUsdcDiff("stopLossOrder.price.usdcDiff"),

    takeProfitOrderId("takeProfitOrder.orderId"),
    takeProfitOrderSize("takeProfitOrder.size"),
    takeProfitOrderType("takeProfitOrder.type"),
    takeProfitLimitPrice("takeProfitOrder.price.limitPrice"),
    takeProfitPrice("takeProfitOrder.price.triggerPrice"),
    takeProfitPercentDiff("takeProfitOrder.price.percentDiff"),
    takeProfitUsdcDiff("takeProfitOrder.price.usdcDiff");

    companion object {
        operator fun invoke(rawValue: String) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.triggerOrders(
    data: String?,
    type: TriggerOrdersInputField?,
    subaccountNumber: Int,
): StateResponse {
    if (staticTyping) {
        val triggerOrdersInputProcessor = TriggerOrdersInputProcessor(parser)
        val changes = triggerOrdersInputProcessor.triggerOrderInput(
            inputState = internalState.input,
            account = internalState.wallet.account,
            data = data,
            type = type,
            subaccountNumber = subaccountNumber,
        )
        updateStateChanges(changes)
        return StateResponse(
            state = state,
            changes = changes,
            errors = null,
        )
    } else {
        var changes: StateChanges? = null
        var error: ParsingError? = null
        val typeText = type?.rawValue

        val input = this.input?.mutable() ?: mutableMapOf()
        input["current"] = "triggerOrders"
        val triggerOrders =
            parser.asMap(input["triggerOrders"])?.mutable()
                ?: kotlin.run {
                    val triggerOrders = mutableMapOf<String, Any>()

                    val calculator = TriggerOrdersInputCalculator(parser)
                    val params = mutableMapOf<String, Any>()
                    params.safeSet("triggerOrders", triggerOrders)
                    val modified = calculator.calculate(params, subaccountNumber)

                    parser.asMap(modified["triggerOrders"])?.mutable() ?: triggerOrders
                }

        var stopLossPriceChanged = false
        var takeProfitPriceChanged = false

        if (typeText != null) {
            if (validTriggerOrdersInput(triggerOrders, typeText)) {
                when (typeText) {
                    TriggerOrdersInputField.marketId.rawValue -> {
                        triggerOrders.safeSet(typeText, parser.asString(data))
                        changes =
                            StateChanges(
                                iListOf(Changes.input),
                                null,
                                iListOf(subaccountNumber),
                            )
                    }

                    TriggerOrdersInputField.stopLossOrderId.rawValue,
                    TriggerOrdersInputField.takeProfitOrderId.rawValue,
                    TriggerOrdersInputField.stopLossOrderType.rawValue,
                    TriggerOrdersInputField.takeProfitOrderType.rawValue -> {
                        triggerOrders.safeSet(typeText, parser.asString(data))
                        changes =
                            StateChanges(
                                iListOf(Changes.input),
                                null,
                                iListOf(subaccountNumber),
                            )
                    }

                    TriggerOrdersInputField.size.rawValue,
                    TriggerOrdersInputField.stopLossOrderSize.rawValue,
                    TriggerOrdersInputField.takeProfitOrderSize.rawValue,
                    TriggerOrdersInputField.stopLossLimitPrice.rawValue,
                    TriggerOrdersInputField.takeProfitLimitPrice.rawValue -> {
                        triggerOrders.safeSet(typeText, parser.asDouble(data))
                        changes =
                            StateChanges(
                                iListOf(Changes.input),
                                null,
                                iListOf(subaccountNumber),
                            )
                    }

                    TriggerOrdersInputField.stopLossPrice.rawValue,
                    TriggerOrdersInputField.stopLossPercentDiff.rawValue,
                    TriggerOrdersInputField.stopLossUsdcDiff.rawValue -> {
                        stopLossPriceChanged =
                            (
                                parser.asDouble(data) != parser.asDouble(
                                    parser.value(
                                        triggerOrders,
                                        typeText,
                                    ),
                                )
                                )
                        triggerOrders.safeSet(typeText, parser.asDouble(data))
                        changes =
                            StateChanges(
                                iListOf(Changes.input),
                                null,
                                iListOf(subaccountNumber),
                            )
                    }

                    TriggerOrdersInputField.takeProfitPrice.rawValue,
                    TriggerOrdersInputField.takeProfitPercentDiff.rawValue,
                    TriggerOrdersInputField.takeProfitUsdcDiff.rawValue -> {
                        takeProfitPriceChanged =
                            (
                                parser.asDouble(data) != parser.asDouble(
                                    parser.value(
                                        triggerOrders,
                                        typeText,
                                    ),
                                )
                                )
                        triggerOrders.safeSet(typeText, parser.asDouble(data))
                        changes =
                            StateChanges(
                                iListOf(Changes.input),
                                null,
                                iListOf(subaccountNumber),
                            )
                    }

                    else -> {}
                }
            } else {
                error = ParsingError.cannotModify(typeText)
            }
        } else {
            changes =
                StateChanges(
                    iListOf(Changes.input),
                    null,
                    iListOf(subaccountNumber),
                )
        }

        if (stopLossPriceChanged) {
            when (typeText) {
                TriggerOrdersInputField.stopLossPrice.rawValue,
                TriggerOrdersInputField.stopLossPercentDiff.rawValue,
                TriggerOrdersInputField.stopLossUsdcDiff.rawValue,
                -> {
                    triggerOrders.safeSet("stopLossOrder.price.input", typeText)
                }
            }
        }
        if (takeProfitPriceChanged) {
            when (typeText) {
                TriggerOrdersInputField.takeProfitPrice.rawValue,
                TriggerOrdersInputField.takeProfitPercentDiff.rawValue,
                TriggerOrdersInputField.takeProfitUsdcDiff.rawValue,
                -> {
                    triggerOrders.safeSet("takeProfitOrder.price.input", typeText)
                }
            }
        }

        input["triggerOrders"] = triggerOrders
        this.input = input
        changes?.let { updateStateChanges(it) }
        return StateResponse(state, changes, if (error != null) iListOf(error) else null)
    }
}

fun TradingStateMachine.validTriggerOrdersInput(
    triggerOrders: Map<String, Any>,
    typeText: String?
): Boolean {
    return true
}
