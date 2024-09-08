package exchange.dydx.abacus.processor.input

import exchange.dydx.abacus.calculator.v2.AdjustIsolatedMarginInputCalculatorV2
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.IsolatedMarginInputType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.cannotModify
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalAdjustIsolatedMarginInputState
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import kollections.iListOf
import kotlin.math.absoluteValue

internal class AdjustIsolatedMarginInputProcessor(
    private val parser: ParserProtocol
) {
    fun adjustIsolatedMargin(
        inputState: InternalInputState,
        walletState: InternalWalletState,
        markets: Map<String, InternalMarketState>?,
        data: String?,
        type: AdjustIsolatedMarginInputField?,
        parentSubaccountNumber: Int,
    ): InputProcessorResult {
        var changes: StateChanges? = null
        var error: ParsingError? = null

        inputState.currentType = InputType.ADJUST_ISOLATED_MARGIN
        if (inputState.adjustIsolatedMargin.parentSubaccountNumber != parentSubaccountNumber) {
            inputState.adjustIsolatedMargin = InternalAdjustIsolatedMarginInputState()
            inputState.adjustIsolatedMargin.parentSubaccountNumber = parentSubaccountNumber

            val calculator = AdjustIsolatedMarginInputCalculatorV2(parser)
            inputState.adjustIsolatedMargin = calculator.calculate(
                adjustIsolatedMargin = inputState.adjustIsolatedMargin,
                walletState = walletState,
                markets = markets,
                parentSubaccountNumber = parentSubaccountNumber,
            )
        }

        val adjustIsolatedMargin = inputState.adjustIsolatedMargin

        val childSubaccountNumber = adjustIsolatedMargin.childSubaccountNumber
        val subaccountNumbers = if (childSubaccountNumber != null) {
            iListOf(parentSubaccountNumber, childSubaccountNumber)
        } else {
            iListOf(parentSubaccountNumber)
        }

        if (type != null) {
            if (validAdjustIsolatedMarginInput(
                    adjustIsolatedMargin = adjustIsolatedMargin,
                    parentSubaccountNumber = parentSubaccountNumber,
                    type = type,
                )
            ) {
                when (type) {
                    AdjustIsolatedMarginInputField.Market -> {
                        adjustIsolatedMargin.market = parser.asString(data)
                        changes = getStateChanges(subaccountNumbers)
                    }

                    AdjustIsolatedMarginInputField.Type -> {
                        val typeValue = parser.asString(data)
                        if (adjustIsolatedMargin.type?.name != typeValue) {
                            if (typeValue != null) {
                                adjustIsolatedMargin.type =
                                    IsolatedMarginAdjustmentType.valueOf(typeValue)
                            } else {
                                adjustIsolatedMargin.type = null
                            }
                            adjustIsolatedMargin.amount = null
                            adjustIsolatedMargin.amountPercent = null
                        }
                        changes = getStateChanges(subaccountNumbers)
                    }

                    AdjustIsolatedMarginInputField.AmountPercent -> {
                        val amountValue = parser.asDouble(data)?.absoluteValue
                        if (adjustIsolatedMargin.amountPercent != amountValue) {
                            adjustIsolatedMargin.amountPercent = amountValue
                            adjustIsolatedMargin.amountInput = IsolatedMarginInputType.Percent
                        }
                        changes = getStateChanges(subaccountNumbers)
                    }

                    AdjustIsolatedMarginInputField.Amount -> {
                        val amountValue = parser.asDouble(data)?.absoluteValue
                        if (adjustIsolatedMargin.amount != amountValue) {
                            adjustIsolatedMargin.amount = amountValue
                            adjustIsolatedMargin.amountInput = IsolatedMarginInputType.Amount
                        }
                        changes = getStateChanges(subaccountNumbers)
                    }

                    AdjustIsolatedMarginInputField.ChildSubaccountNumber -> {
                        var updatedSubaccountNumbers = iListOf(parentSubaccountNumber)
                        val updatedChildSubaccountNumber = parser.asInt(data)
                        adjustIsolatedMargin.childSubaccountNumber = updatedChildSubaccountNumber
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
                changes = iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                markets = null,
                subaccountNumbers = subaccountNumbers,
            )
        }

        return InputProcessorResult(
            changes = changes,
            error = error,
        )
    }

    private fun validAdjustIsolatedMarginInput(
        adjustIsolatedMargin: InternalAdjustIsolatedMarginInputState,
        parentSubaccountNumber: Int?,
        type: AdjustIsolatedMarginInputField,
    ): Boolean {
        when (type) {
            AdjustIsolatedMarginInputField.Market -> {
                return true
            }

            AdjustIsolatedMarginInputField.Type -> {
                val inputType = adjustIsolatedMargin.type ?: return true
                return inputType == IsolatedMarginAdjustmentType.Add || inputType == IsolatedMarginAdjustmentType.Remove
            }

            AdjustIsolatedMarginInputField.Amount -> {
                val amount = adjustIsolatedMargin.amount
                return amount == null || amount >= 0
            }

            AdjustIsolatedMarginInputField.AmountPercent -> {
                val amountPercent = adjustIsolatedMargin.amountPercent
                return amountPercent == null || amountPercent >= 0
            }

            AdjustIsolatedMarginInputField.ChildSubaccountNumber -> {
                val childSubaccountNumber = adjustIsolatedMargin.childSubaccountNumber
                return childSubaccountNumber == null || childSubaccountNumber % NUM_PARENT_SUBACCOUNTS == parentSubaccountNumber
            }
        }
    }

    private fun getStateChanges(
        subaccountNumbers: IList<Int>,
    ): StateChanges {
        return StateChanges(
            changes = iListOf(Changes.wallet, Changes.subaccount, Changes.input),
            markets = null,
            subaccountNumbers = subaccountNumbers,
        )
    }
}
