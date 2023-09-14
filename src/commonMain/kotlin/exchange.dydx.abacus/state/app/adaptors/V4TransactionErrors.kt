package exchange.dydx.abacus.state.app.adaptors

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.utils.iMapOf

class V4TransactionErrors {
    companion object {
        fun error(code: Int?, message: String?): ParsingError? {
            return if (code != null) {
                if (code != 0) {
                    ParsingError(
                        ParsingErrorType.BackendError,
                        message ?: "Unknown error",
                        "ERRORS.BROADCAST_ERROR_$code"
                    )
                } else null
            } else {
                ParsingError(ParsingErrorType.BackendError, message ?: "Unknown error", null)
            }
        }
    }
}