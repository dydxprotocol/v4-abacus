package exchange.dydx.abacus.state.app.adaptors

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType

class V4TransactionErrors {
    companion object {
        private const val QUERY_RESULT_ERROR_PREFIX = "Query failed"
        private val FAILED_SUBACCOUNT_UPDATE_RESULT_PATTERN = Regex("""Subaccount with id \{[^}]+\} failed with UpdateResult:\s*([A-Za-z]+):""")

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
            } else if (message?.startsWith(QUERY_RESULT_ERROR_PREFIX) == true) {
                parseQueryResultErrorFromMessage(message)
            } else {
                ParsingError(ParsingErrorType.BackendError, message ?: "Unknown error", null)
            }
        }

        private fun parseQueryResultErrorFromMessage(message: String): ParsingError {
            // Workaround: Regex match different query results until protocol can return codespace/code
            parseSubaccountUpdateError(message)?.let { return it }
            return ParsingError(ParsingErrorType.BackendError, "Unknown query result error", null)
        }

        private fun parseSubaccountUpdateError(message: String): ParsingError? {
            val matchResult = FAILED_SUBACCOUNT_UPDATE_RESULT_PATTERN.find(message)
            return matchResult?.groups?.get(1)?.value?.let {
                val matchedUpdateResult = SubaccountUpdateFailedResult.invoke(it)
                ParsingError(
                    ParsingErrorType.BackendError,
                    "Subaccount update error: $it",
                    if (matchedUpdateResult != null) "ERRORS.QUERY_ERROR_SUBACCOUNTS_${matchedUpdateResult.toString().uppercase()}" else null,
                )
            }
        }
    }
}

// Copied from protocol subaccounts update (https://github.com/dydxprotocol/v4-chain/blob/b2dfda2a4b0ea587691c41ba436b46d0d9987a25/protocol/x/subaccounts/types/update.go#L58)
enum class SubaccountUpdateFailedResult(val rawValue: String) {
    NewlyUndercollateralized("NewlyUndercollateralized"),
    StillUndercollateralized("StillUndercollateralized"),
    WithdrawalsAndTransfersBlocked("WithdrawalsAndTransfersBlocked"),
    UpdateCausedError("UpdateCausedError"),
    ViolatesIsolatedSubaccountConstraints("ViolatesIsolatedSubaccountConstraints");

    companion object {
        fun invoke(rawValue: String): SubaccountUpdateFailedResult? {
            return SubaccountUpdateFailedResult.values().find { it.rawValue == rawValue }
        }
    }
}
