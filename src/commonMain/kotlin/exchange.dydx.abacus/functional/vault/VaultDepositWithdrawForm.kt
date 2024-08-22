package exchange.dydx.abacus.functional.vault

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class FormData(
    val action: TransactionAction,
    val amount: Double?,
    val acknowledgedSlippage: Boolean,
    val inConfirmationStep: Boolean,
)

@JsExport
@Serializable
enum class TransactionAction {
    DEPOSIT,
    WITHDRAW,
}

@JsExport
@Serializable
data class AccountData(
    val marginUsage: Double?,
    val freeCollateral: Double?
)

@JsExport
@Serializable
data class SlippageResponse(
    val shares: Double,
    val expectedAmount: Double,
)

@JsExport
@Serializable
data class ValidationError(
    val severity: ErrorSeverity,
    val type: ErrorType
)

@JsExport
@Serializable
enum class ErrorSeverity {
    WARNING,
    ERROR
}

@JsExport
@Serializable
enum class ErrorType {
    AMOUNT_EMPTY,
    DEPOSIT_TOO_HIGH,
    WITHDRAW_TOO_HIGH,
    WITHDRAWING_LOCKED_BALANCE,
    SLIPPAGE_TOO_HIGH,
    MUST_ACK_SLIPPAGE,
    VAULT_ACCOUNT_MISSING,
    ACCOUNT_DATA_MISSING,
    SLIPPAGE_RESPONSE_MISSING,
    SLIPPAGE_RESPONSE_WRONG_SHARES,
}

@JsExport
@Serializable
data class SubmissionData(
    val deposit: DepositData?,
    val withdraw: WithdrawData?
)

@JsExport
@Serializable
data class DepositData(
    val subaccountFrom: String,
    val amount: Double
)

@JsExport
@Serializable
data class WithdrawData(
    val subaccountTo: String,
    val shares: Double,
    val minAmount: Double
)

@JsExport
@Serializable
data class SummaryData(
    val needSlippageAck: Boolean?,
    val marginUsage: Double?,
    val freeCollateral: Double?,
    val vaultBalance: Double?,
    val estimatedSlippage: Double?,
    val estimatedAmountReceived: Double?
)

@JsExport
@Serializable
data class ValidationResult(
    val errors: List<ValidationError>,
    val submissionData: SubmissionData?,
    val summaryData: SummaryData
)

@JsExport
object VaultDepositWithdrawFormValidator {

    private const val SLIPPAGE_PERCENT_WARN = 0.01
    private const val SLIPPAGE_PERCENT_ACK = 0.04
    private const val SLIPPAGE_TOLERANCE = 0.01

    fun validateVaultForm(
        formData: FormData,
        accountData: AccountData?,
        vaultAccount: VaultAccount?,
        slippageResponse: SlippageResponse?
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        var submissionData: SubmissionData? = null

        // Calculate post-operation values and slippage
        val amount = formData.amount ?: 0.0

        val shareValue = if (vaultAccount?.balanceUsdc != null && vaultAccount.balanceShares != null && vaultAccount.balanceShares > 0) {
            vaultAccount.balanceUsdc / vaultAccount.balanceShares
        } else {
            null
        }
        val sharesToAttemptWithdraw = if (amount > 0 && shareValue != null && shareValue > 0) {
            amount / shareValue
        } else {
            null
        }

        val withdrawnAmountIncludingSlippage = slippageResponse?.expectedAmount
        val postOpVaultBalance = if (vaultAccount?.balanceUsdc != null) {
            when (formData.action) {
                TransactionAction.DEPOSIT -> vaultAccount.balanceUsdc + amount
                TransactionAction.WITHDRAW -> vaultAccount.balanceUsdc - amount
            }
        } else {
            null
        }

        val (postOpFreeCollateral, postOpMarginUsage) = if (accountData?.freeCollateral != null && accountData.marginUsage != null) {
            val equity = accountData.freeCollateral / (1 - accountData.marginUsage)
            val postOpEquity = when (formData.action) {
                TransactionAction.DEPOSIT -> equity - amount
                TransactionAction.WITHDRAW -> if (withdrawnAmountIncludingSlippage != null) equity + withdrawnAmountIncludingSlippage else null
            }
            val newFreeCollateral = when (formData.action) {
                TransactionAction.DEPOSIT -> accountData.freeCollateral - amount
                TransactionAction.WITHDRAW -> if (withdrawnAmountIncludingSlippage != null) accountData.freeCollateral + withdrawnAmountIncludingSlippage else null
            }
            val newMarginUsage = if (newFreeCollateral != null && postOpEquity != null && postOpEquity > 0.0) {
                1.0 - newFreeCollateral / postOpEquity
            } else {
                null
            }

            Pair(newFreeCollateral, newMarginUsage)
        } else {
            Pair(null, null)
        }

        val slippagePercent = if (
            formData.action === TransactionAction.WITHDRAW &&
            amount > 0 &&
            withdrawnAmountIncludingSlippage != null
        ) {
            1.0 - withdrawnAmountIncludingSlippage / amount
        } else {
            0.0
        }
        val needSlippageAck = slippagePercent >= SLIPPAGE_PERCENT_WARN

        // Perform validation checks and populate errors list
        if (accountData == null) {
            errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.ACCOUNT_DATA_MISSING))
        }

        if (amount == 0.0) {
            errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.AMOUNT_EMPTY))
        }

        // can't actually submit if we are missing key validation information
        if (formData.inConfirmationStep && formData.action === TransactionAction.WITHDRAW) {
            if (vaultAccount == null) {
                errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.VAULT_ACCOUNT_MISSING))
            }
            if (slippageResponse == null || sharesToAttemptWithdraw == null) {
                errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.SLIPPAGE_RESPONSE_MISSING))
            }
        }

        if (formData.inConfirmationStep && formData.action === TransactionAction.DEPOSIT) {
            if (accountData?.marginUsage == null || accountData.freeCollateral == null) {
                errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.ACCOUNT_DATA_MISSING))
            }
        }

        when (formData.action) {
            TransactionAction.DEPOSIT -> {
                if (postOpFreeCollateral != null && postOpFreeCollateral < 0) {
                    errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.DEPOSIT_TOO_HIGH))
                }
            }
            TransactionAction.WITHDRAW -> {
                if (postOpVaultBalance != null && postOpVaultBalance < 0) {
                    errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.WITHDRAW_TOO_HIGH))
                }
                if (postOpVaultBalance != null && postOpVaultBalance >= 0 && amount > 0 && vaultAccount?.withdrawableUsdc != null && amount > vaultAccount.withdrawableUsdc) {
                    errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.WITHDRAWING_LOCKED_BALANCE))
                }
                if (sharesToAttemptWithdraw != null && slippageResponse != null && sharesToAttemptWithdraw != slippageResponse.shares) {
                    errors.add(
                        ValidationError(
                            ErrorSeverity.ERROR,
                            ErrorType.SLIPPAGE_RESPONSE_WRONG_SHARES,
                        ),
                    )
                }
                if (needSlippageAck) {
                    errors.add(ValidationError(ErrorSeverity.WARNING, ErrorType.SLIPPAGE_TOO_HIGH))
                    if (slippagePercent >= SLIPPAGE_PERCENT_ACK && !formData.acknowledgedSlippage && formData.inConfirmationStep) {
                        errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.MUST_ACK_SLIPPAGE))
                    }
                }
            }
        }

        // Prepare submission data if no errors
        if (errors.none { it.severity === ErrorSeverity.ERROR }) {
            submissionData = when (formData.action) {
                TransactionAction.DEPOSIT -> SubmissionData(
                    deposit = DepositData(
                        subaccountFrom = "0",
                        amount = amount,
                    ),
                    withdraw = null,
                )
                TransactionAction.WITHDRAW -> SubmissionData(
                    deposit = null,
                    withdraw = if (sharesToAttemptWithdraw != null && sharesToAttemptWithdraw > 0 && slippageResponse != null) {
                        WithdrawData(
                            subaccountTo = "0",
                            shares = sharesToAttemptWithdraw,
                            minAmount = slippageResponse.expectedAmount * (1 - SLIPPAGE_TOLERANCE),
                        )
                    } else {
                        null
                    },
                )
            }
        }

        // Prepare summary data
        val summaryData = SummaryData(
            needSlippageAck = needSlippageAck,
            marginUsage = postOpMarginUsage,
            freeCollateral = postOpFreeCollateral,
            vaultBalance = postOpVaultBalance,
            estimatedSlippage = slippagePercent,
            estimatedAmountReceived = if (formData.action === TransactionAction.WITHDRAW) slippageResponse?.expectedAmount else null,
        )

        return ValidationResult(
            errors = errors,
            submissionData = submissionData,
            summaryData = summaryData,
        )
    }
}
