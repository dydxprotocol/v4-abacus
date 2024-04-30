package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.AdjustIsolatedMarginInputCalculator
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.StateResponse
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
enum class AdjustIsolatedMarginInputField(val rawValue: String) {
    type("type"),
    amount("amount"),
    childSubaccountNumber("childSubaccountNumber");

    companion object {
        operator fun invoke(rawValue: String) =
            AdjustIsolatedMarginInputField.values().firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.adjustIsolatedMargin(
    data: String?,
    type: AdjustIsolatedMarginInputField?,
    parentSubaccountNumber: Int,
): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    val typeText = type?.rawValue

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

    if (typeText != null) {
        if (validAdjustIsolatedMarginInput(adjustIsolatedMargin, typeText)) {
            when (typeText) {
                AdjustIsolatedMarginInputField.type.rawValue -> {
                    if (adjustIsolatedMargin["type"] != parser.asString(data)) {
                        adjustIsolatedMargin.safeSet(typeText, parser.asString(data))
                        adjustIsolatedMargin.safeSet("amount", null)
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }
                AdjustIsolatedMarginInputField.amount.rawValue -> {
                    val amount = parser.asString(data)
                    adjustIsolatedMargin.safeSet(typeText, amount)
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }
                AdjustIsolatedMarginInputField.childSubaccountNumber.rawValue -> {
                    val childSubaccountNumber = parser.asInt(data)
                    adjustIsolatedMargin.safeSet(typeText, childSubaccountNumber)
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
            error = cannotModify(typeText)
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
    typeText: String?
): Boolean {
    return true
}
