package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.AdjustIsolatedMarginInputCalculator
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.IsolatedMarginInputType
import exchange.dydx.abacus.processor.input.AdjustIsolatedMarginInputProcessor
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.responses.cannotModify
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

@JsExport
@Serializable
enum class AdjustIsolatedMarginInputField {
    Market,
    Type,
    Amount,
    AmountPercent,
    ChildSubaccountNumber,
}

fun TradingStateMachine.adjustIsolatedMargin(
    data: String?,
    type: AdjustIsolatedMarginInputField?,
    parentSubaccountNumber: Int,
): StateResponse {
    if (staticTyping) {
        val adjustIsolatedMarginInputProcessor = AdjustIsolatedMarginInputProcessor(parser)
        val result = adjustIsolatedMarginInputProcessor.adjustIsolatedMargin(
            inputState = internalState.input,
            walletState = internalState.wallet,
            markets = internalState.marketsSummary.markets,
            data = data,
            type = type,
            parentSubaccountNumber = parentSubaccountNumber,
        )

        result.changes?.let { updateStateChanges(it) }

        return StateResponse(
            state = state,
            changes = result.changes,
            errors = if (result.error != null) iListOf(result.error) else null,
        )
    } else {
        var changes: StateChanges? = null
        var error: ParsingError? = null

        val input = this.input?.mutable() ?: mutableMapOf()
        input["current"] = "adjustIsolatedMargin"

        val adjustIsolatedMargin =
            parser.asMap(input["adjustIsolatedMargin"])?.mutable()
                ?: kotlin.run {
                    val adjustIsolatedMargin = mutableMapOf<String, Any>()
                    val calculator = AdjustIsolatedMarginInputCalculator(parser)
                    val params = mutableMapOf<String, Any>()
                    params.safeSet("adjustIsolatedMargin", adjustIsolatedMargin)
                    params.safeSet("account", account)
                    params.safeSet("wallet", wallet)

                    val modified = calculator.calculate(
                        state = params,
                        parentSubaccountNumber = parentSubaccountNumber,
                    )

                    parser.asMap(modified["adjustIsolatedMargin"])?.mutable()
                        ?: adjustIsolatedMargin
                }
        val childSubaccountNumber = parser.asInt(adjustIsolatedMargin["ChildSubaccountNumber"])
        val subaccountNumbers = if (childSubaccountNumber != null) {
            iListOf(parentSubaccountNumber, childSubaccountNumber)
        } else {
            iListOf(parentSubaccountNumber)
        }

        if (type != null) {
            if (validAdjustIsolatedMarginInput(
                    adjustIsolatedMargin = adjustIsolatedMargin,
                    parentSubaccountNumber = parentSubaccountNumber,
                    typeText = type.name,
                )
            ) {
                when (type) {
                    AdjustIsolatedMarginInputField.Market -> {
                        if (adjustIsolatedMargin["Market"] != parser.asString(data)) {
                            adjustIsolatedMargin.safeSet("Market", parser.asString(data))
                        }
                    }

                    AdjustIsolatedMarginInputField.Type -> {
                        if (adjustIsolatedMargin["Type"] != parser.asString(data)) {
                            adjustIsolatedMargin.safeSet(type.name, parser.asString(data))
                            adjustIsolatedMargin.safeSet("Amount", null)
                            adjustIsolatedMargin.safeSet("AmountPercent", null)
                        }
                        changes = getStateChanges(subaccountNumbers)
                    }

                    AdjustIsolatedMarginInputField.AmountPercent -> {
                        val amountValue = parser.asDouble(data)?.absoluteValue
                        if (adjustIsolatedMargin["AmountPercent"] != amountValue) {
                            adjustIsolatedMargin.safeSet(
                                "AmountInput",
                                IsolatedMarginInputType.Percent,
                            )
                            adjustIsolatedMargin.safeSet("AmountPercent", amountValue)
                        }
                        changes = getStateChanges(subaccountNumbers)
                    }

                    AdjustIsolatedMarginInputField.Amount -> {
                        val amountValue = parser.asDouble(data)?.absoluteValue
                        if (adjustIsolatedMargin["Amount"] != amountValue) {
                            adjustIsolatedMargin.safeSet(
                                "AmountInput",
                                IsolatedMarginInputType.Amount,
                            )
                            adjustIsolatedMargin.safeSet("Amount", amountValue)
                        }
                        changes = getStateChanges(subaccountNumbers)
                    }

                    AdjustIsolatedMarginInputField.ChildSubaccountNumber -> {
                        var updatedSubaccountNumbers = iListOf(parentSubaccountNumber)
                        val updatedChildSubaccountNumber = parser.asInt(data)
                        adjustIsolatedMargin.safeSet(type.name, updatedChildSubaccountNumber)
                        if (updatedChildSubaccountNumber != null) {
                            updatedSubaccountNumbers =
                                iListOf(parentSubaccountNumber, updatedChildSubaccountNumber)
                        }
                        changes = getStateChanges(updatedSubaccountNumbers)
                    }
                }
            } else {
                error = ParsingError.cannotModify(type.name)
            }
        } else {
            changes = StateChanges(
                iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                null,
                subaccountNumbers,
            )
        }

        input["adjustIsolatedMargin"] = adjustIsolatedMargin
        this.input = input
        changes?.let { updateStateChanges(it) }
        return StateResponse(state, changes, if (error != null) iListOf(error) else null)
    }
}

private fun getStateChanges(
    subaccountNumbers: IList<Int>,
): StateChanges {
    return StateChanges(
        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
        null,
        subaccountNumbers,
    )
}

private fun TradingStateMachine.validAdjustIsolatedMarginInput(
    adjustIsolatedMargin: Map<String, Any>,
    parentSubaccountNumber: Int?,
    typeText: String?,
): Boolean {
    if (typeText == null) return false

    when (typeText) {
        AdjustIsolatedMarginInputField.Market.name -> {
            return true
        }
        AdjustIsolatedMarginInputField.Type.name -> {
            val typeString = parser.asString(adjustIsolatedMargin["Type"]) ?: return true
            val type = IsolatedMarginAdjustmentType.valueOf(typeString)
            return type == IsolatedMarginAdjustmentType.Add || type == IsolatedMarginAdjustmentType.Remove
        }
        AdjustIsolatedMarginInputField.Amount.name -> {
            val amount = parser.asDouble(adjustIsolatedMargin["Amount"])
            return amount == null || amount >= 0
        }
        AdjustIsolatedMarginInputField.AmountPercent.name -> {
            val amountPercent = parser.asDouble(adjustIsolatedMargin["AmountPercent"])
            return amountPercent == null || amountPercent >= 0
        }
        AdjustIsolatedMarginInputField.ChildSubaccountNumber.name -> {
            val childSubaccountNumber = parser.asInt(adjustIsolatedMargin["ChildSubaccountNumber"])
            return childSubaccountNumber == null || childSubaccountNumber % NUM_PARENT_SUBACCOUNTS == parentSubaccountNumber
        }
        else -> {
            return false
        }
    }
}
