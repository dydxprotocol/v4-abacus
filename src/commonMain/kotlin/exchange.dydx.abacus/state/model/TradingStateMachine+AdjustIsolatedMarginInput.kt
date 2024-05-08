package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.AdjustIsolatedMarginInputCalculator
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class AdjustIsolatedMarginInputField {
    Type,
    Amount,
    ChildSubaccountNumber,
}

fun TradingStateMachine.adjustIsolatedMargin(
    data: String?,
    type: AdjustIsolatedMarginInputField?,
    parentSubaccountNumber: Int,
): StateResponse {
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

                parser.asMap(modified["adjustIsolatedMargin"])?.mutable() ?: adjustIsolatedMargin
            }
    val childSubaccountNumber = parser.asInt(adjustIsolatedMargin["childSubaccountNumber"])
    val subaccountNumbers = if (childSubaccountNumber != null) {
        iListOf(parentSubaccountNumber, childSubaccountNumber)
    } else {
        iListOf(parentSubaccountNumber)
    }

    if (type != null) {
        if (validAdjustIsolatedMarginInput(adjustIsolatedMargin, parentSubaccountNumber, type.name)) {
            when (type) {
                AdjustIsolatedMarginInputField.Type -> {
                    if (adjustIsolatedMargin["type"] != parser.asString(data)) {
                        adjustIsolatedMargin.safeSet(type.name, parser.asString(data))
                        adjustIsolatedMargin.safeSet("amount", null)
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }
                AdjustIsolatedMarginInputField.Amount -> {
                    val amount = parser.asString(data)
                    adjustIsolatedMargin.safeSet(type.name, amount)
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }
                AdjustIsolatedMarginInputField.ChildSubaccountNumber -> {
                    val childSubaccountNumber = parser.asInt(data)
                    adjustIsolatedMargin.safeSet(type.name, childSubaccountNumber)
                    val subaccountNumbers = if (childSubaccountNumber != null) {
                        iListOf(parentSubaccountNumber, childSubaccountNumber)
                    } else {
                        iListOf(parentSubaccountNumber)
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }
                else -> {}
            }
        } else {
            error = cannotModify(type.name)
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
    changes?.let { update(it) }
    return StateResponse(state, changes, if (error != null) iListOf(error) else null)
}

fun TradingStateMachine.validAdjustIsolatedMarginInput(
    adjustIsolatedMargin: Map<String, Any>,
    parentSubaccountNumber: Int?,
    typeText: String?,
): Boolean {
    if (typeText == null) return false

    when (typeText) {
        AdjustIsolatedMarginInputField.Type.name -> {
            val typeString = parser.asString(adjustIsolatedMargin["type"]) ?: return true
            val type = IsolatedMarginAdjustmentType.valueOf(typeString)
            return type == IsolatedMarginAdjustmentType.Add || type == IsolatedMarginAdjustmentType.Remove
        }
        AdjustIsolatedMarginInputField.Amount.name -> {
            val amount = parser.asDouble(adjustIsolatedMargin["amount"])
            return amount == null || amount > 0
        }
        AdjustIsolatedMarginInputField.ChildSubaccountNumber.name -> {
            val childSubaccountNumber = parser.asInt(adjustIsolatedMargin["childSubaccountNumber"])
            return childSubaccountNumber == null || childSubaccountNumber % NUM_PARENT_SUBACCOUNTS == parentSubaccountNumber
        }
        else -> {
            return false
        }
    }
}
