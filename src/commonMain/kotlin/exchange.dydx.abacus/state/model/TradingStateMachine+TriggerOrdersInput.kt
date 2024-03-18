package exchange.dydx.abacus.state.model
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
enum class TriggerOrdersInputField(val rawValue: String) {
    type("type")

    companion object {
        operator fun invoke(rawValue: String) = 
        TriggerOrdersInputField.values().firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.triggerOrders(
    data: String?,
    type: TriggerOrdersInputField?,
    subaccountNumber: Int,
): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    val typeText = type?.rawValue

    val input = this.input?.mutable() ?: mutableMapOf()
    input["current"] = "triggerOrders"
    val triggerOrders = parser.asMap(input["triggerOrders"])?.mutable() ?: kotlin.run {
        val triggerOrders = mutableMapOf<String, Any>()

        val calculator = TriggerOrdersCalculator(parser)
        val params = mutableMapOf<String, Any>()
        params.safeSet("triggerOrders", triggerOrders)
        val modified = calculator.calculate(params, subaccountNumber)

        parser.asMap(modified["triggerOrders"])?.mutable() ?: triggerOrders
    }

    if (typeText != null) {
        changes = StateChanges(iListOf(Changes.wallet, Changes.subaccount, Changes.input), null, iListOf(subaccountNumber))
    } else {
        changes = StateChanges(iListOf(Changes.wallet, Changes.subaccount, Changes.input), null, iListOf(subaccountNumber))
    }

    input["triggerOrders"] = triggerOrders
    this.input = input

    changes?.let {
        update(it)
    }
    return StateResponse(state, changes, if (error != null) iListOf(error) else null)
}