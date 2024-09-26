package exchange.dydx.abacus.processor.input

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.changes.StateChanges

internal class InputProcessorResult(
    val changes: StateChanges? = null,
    val error: ParsingError? = null,
)
