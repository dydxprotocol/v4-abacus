package exchange.dydx.abacus.state.helper

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType

class V4TransactionErrors {
    companion object {
        private const val QUERY_RESULT_ERROR_PREFIX = "Query failed"
        private const val OUT_OF_GAS_ERROR_RAW_LOG_PREFIX = "out of gas"
        private val FAILED_SUBACCOUNT_UPDATE_RESULT_PATTERN = Regex("""Subaccount with id \{[^}]+\} failed with UpdateResult:\s*([A-Za-z]+):""")

        fun error(code: Int?, message: String?, codespace: String? = null): ParsingError? {
            return if (code != null) {
                if (code != 0 && codespace != null) {
                    ParsingError(
                        type = ParsingErrorType.BackendError,
                        message = message ?: "Unknown error",
                        stringKey = "ERRORS.BROADCAST_ERROR_${codespace.uppercase()}_$code",
                    )
                } else {
                    null
                }
            } else if (message?.startsWith(QUERY_RESULT_ERROR_PREFIX) == true) {
                parseQueryResultErrorFromMessage(message)
            } else {
                ParsingError(
                    type = ParsingErrorType.BackendError,
                    message = message ?: "Unknown error",
                    stringKey = null,
                )
            }
        }

        private fun parseQueryResultErrorFromMessage(message: String): ParsingError {
            // Workaround: Regex match different query results until protocol can return codespace/code
            parseSubaccountUpdateError(message)?.let { return it }
            return ParsingError(
                type = ParsingErrorType.BackendError,
                message = "Unknown query result error",
                stringKey = null,
            )
        }

        private fun parseSubaccountUpdateError(message: String): ParsingError? {
            val matchResult = FAILED_SUBACCOUNT_UPDATE_RESULT_PATTERN.find(message)
            return matchResult?.groups?.get(1)?.value?.let {
                val matchedUpdateResult = SubaccountUpdateFailedResult.invoke(it)
                ParsingError(
                    type = ParsingErrorType.BackendError,
                    message = "Subaccount update error: $it",
                    stringKey = if (matchedUpdateResult != null) "ERRORS.QUERY_ERROR_SUBACCOUNTS_${matchedUpdateResult.toString().uppercase()}" else null,
                )
            }
        }

        fun parseErrorFromRawLog(rawLog: String): ParsingError? {
            return if (rawLog.startsWith(OUT_OF_GAS_ERROR_RAW_LOG_PREFIX)) {
                return ParsingError(
                    type = ParsingErrorType.BackendError,
                    message = "Out of gas: inaccurate gas estimation for transaction",
                )
            } else {
                null
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
            return values().find { it.rawValue == rawValue }
        }
    }
}
