package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType

internal fun TradingStateMachine.cannotModify(typeText: String): ParsingError {
    return ParsingError(
        ParsingErrorType.InvalidInput,
        "$typeText cannot be modified for the selected trade input",
    )
}
