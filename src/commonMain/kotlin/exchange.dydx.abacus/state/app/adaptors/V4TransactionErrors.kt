package exchange.dydx.abacus.state.app.adaptors

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType

class V4TransactionErrors {
    companion object {
        fun error(code: Int?, message: String?, codespace: String? = null): ParsingError? {
            return if (code != null) {
                if (code != 0 && codespace != null) {
                    ParsingError(
                        ParsingErrorType.BackendError,
                        message ?: "Unknown error",
                        "ERRORS.BROADCAST_ERROR_${codespace.uppercase()}_$code",
                    )
                } else {
                    null
                }
            } else {
                ParsingError(ParsingErrorType.BackendError, message ?: "Unknown error", null)
            }
        }
    }
}
