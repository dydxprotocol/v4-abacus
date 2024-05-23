package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class ClosePositionInputField(val rawValue: String) {
    market("market"),
    size("size.size"),
    percent("size.percent");

    companion object {
        operator fun invoke(rawValue: String) =
            ClosePositionInputField.values().firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.closePosition(
    data: String?,
    type: ClosePositionInputField,
    subaccountNumber: Int
): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    val typeText = type.rawValue

    val input = this.input?.mutable() ?: mutableMapOf()
    input["current"] = "closePosition"
    val trade =
        parser.asMap(input["closePosition"])?.mutable() ?: initiateClosePosition(
            null,
            subaccountNumber,
        )

    var sizeChanged = false
    when (typeText) {
        ClosePositionInputField.market.rawValue -> {
            val position = if (data != null) getPosition(data, subaccountNumber) else null
            if (position != null) {
                if (data != null) {
                    if (parser.asString(trade["marketId"]) != data) {
                        trade.safeSet("marketId", data)
                        trade.safeSet("size", null)
                    }
                }
                trade["type"] = "MARKET"
                trade["reduceOnly"] = featureFlags.reduceOnlySupported

                val positionSize =
                    parser.asDouble(parser.value(position, "size.current")) ?: Numeric.double.ZERO
                trade["side"] = if (positionSize > Numeric.double.ZERO) "SELL" else "BUY"

                changes = StateChanges(
                    iListOf(Changes.subaccount, Changes.input),
                    null,
                    iListOf(subaccountNumber),
                )
            } else {
                error = cannotModify(typeText)
            }
        }
        ClosePositionInputField.size.rawValue, ClosePositionInputField.percent.rawValue -> {
            sizeChanged = (parser.asDouble(data) != parser.asDouble(trade[typeText]))
            trade.safeSet(typeText, data)
            changes = StateChanges(
                iListOf(Changes.subaccount, Changes.input),
                null,
                iListOf(subaccountNumber),
            )
        }
        else -> {}
    }
    if (sizeChanged) {
        when (typeText) {
            ClosePositionInputField.size.rawValue,
            ClosePositionInputField.percent.rawValue -> {
                trade.safeSet("size.input", typeText)
            }
            else -> {}
        }
    }
    input["closePosition"] = trade
    this.input = input

    changes?.let {
        update(it)
    }
    return StateResponse(state, changes, if (error != null) iListOf(error) else null)
}

fun TradingStateMachine.getPosition(
    marketId: String,
    subaccountNumber: Int,
): Map<String, Any>? {
    val groupedSubaccounts = parser.asMap(parser.value(wallet, "account.groupedSubaccounts"))
    val path = if (groupedSubaccounts != null) {
        "account.groupedSubaccounts.$subaccountNumber.openPositions.$marketId"
    } else {
        "account.subaccounts.$subaccountNumber.openPositions.$marketId"
    }
    val position = parser.asMap(
        parser.value(
            wallet,
            path,
        ),
    )

    return if (position != null && (
            parser.asDouble(parser.value(position, "size.current"))
                ?: Numeric.double.ZERO
            ) != Numeric.double.ZERO
    ) {
        position
    } else {
        null
    }
}
