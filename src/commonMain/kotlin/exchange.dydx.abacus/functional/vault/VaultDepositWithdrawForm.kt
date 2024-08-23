package exchange.dydx.abacus.functional.vault

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class VaultFormData(
    val action: VaultTransactionAction,
    val amount: Double?,
    val acknowledgedSlippage: Boolean,
    val inConfirmationStep: Boolean,
)

@JsExport
@Serializable
enum class VaultTransactionAction {
    DEPOSIT,
    WITHDRAW,
}

@JsExport
@Serializable
data class VaultFormAccountData(
    val marginUsage: Double?,
    val freeCollateral: Double?
)

@JsExport
@Serializable
data class VaultDepositWithdrawSlippageResponse(
    val shares: Double,
    val expectedAmount: Double,
)

@JsExport
@Serializable
data class VaultFormValidationError(
    val severity: VaultValidationErrorSeverity,
    val type: VaultValidationErrorType
)

@JsExport
@Serializable
enum class VaultValidationErrorSeverity {
    WARNING,
    ERROR
}

@JsExport
@Serializable
enum class VaultValidationErrorType {
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
data class VaultDepositWithdrawSubmissionData(
    val deposit: VaultDepositData?,
    val withdraw: VaultWithdrawData?
)

@JsExport
@Serializable
data class VaultDepositData(
    val subaccountFrom: String,
    val amount: Double
)

@JsExport
@Serializable
data class VaultWithdrawData(
    val subaccountTo: String,
    val shares: Double,
    val minAmount: Double
)

@JsExport
@Serializable
data class VaultDepositWithdrawFormSummaryData(
    val needSlippageAck: Boolean?,
    val marginUsage: Double?,
    val freeCollateral: Double?,
    val vaultBalance: Double?,
    val estimatedSlippage: Double?,
    val estimatedAmountReceived: Double?
)

@JsExport
@Serializable
data class VaultFormValidationResult(
    val errors: List<VaultFormValidationError>,
    val submissionData: VaultDepositWithdrawSubmissionData?,
    val summaryData: VaultDepositWithdrawFormSummaryData
)

@JsExport
object VaultDepositWithdrawFormValidator {

    private const val SLIPPAGE_PERCENT_WARN = 0.01
    private const val SLIPPAGE_PERCENT_ACK = 0.04
    private const val SLIPPAGE_TOLERANCE = 0.01

    fun validateVaultForm(
        formData: VaultFormData,
        accountData: VaultFormAccountData?,
        vaultAccount: VaultAccount?,
        slippageResponse: VaultDepositWithdrawSlippageResponse?
    ): VaultFormValidationResult {
        val errors = mutableListOf<VaultFormValidationError>()
        var submissionData: VaultDepositWithdrawSubmissionData? = null

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
                VaultTransactionAction.DEPOSIT -> vaultAccount.balanceUsdc + amount
                VaultTransactionAction.WITHDRAW -> vaultAccount.balanceUsdc - amount
            }
        } else {
            null
        }

        val (postOpFreeCollateral, postOpMarginUsage) = if (accountData?.freeCollateral != null && accountData.marginUsage != null) {
            val equity = accountData.freeCollateral / (1 - accountData.marginUsage)
            val postOpEquity = when (formData.action) {
                VaultTransactionAction.DEPOSIT -> equity - amount
                VaultTransactionAction.WITHDRAW -> if (withdrawnAmountIncludingSlippage != null) equity + withdrawnAmountIncludingSlippage else null
            }
            val newFreeCollateral = when (formData.action) {
                VaultTransactionAction.DEPOSIT -> accountData.freeCollateral - amount
                VaultTransactionAction.WITHDRAW -> if (withdrawnAmountIncludingSlippage != null) accountData.freeCollateral + withdrawnAmountIncludingSlippage else null
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
            formData.action === VaultTransactionAction.WITHDRAW &&
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
            errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.ACCOUNT_DATA_MISSING))
        }

        if (amount == 0.0) {
            errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.AMOUNT_EMPTY))
        }

        // can't actually submit if we are missing key validation information
        if (formData.inConfirmationStep && formData.action === VaultTransactionAction.WITHDRAW) {
            if (vaultAccount == null) {
                errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.VAULT_ACCOUNT_MISSING))
            }
            if (slippageResponse == null || sharesToAttemptWithdraw == null) {
                errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.SLIPPAGE_RESPONSE_MISSING))
            }
        }

        if (formData.inConfirmationStep && formData.action === VaultTransactionAction.DEPOSIT) {
            if (accountData?.marginUsage == null || accountData.freeCollateral == null) {
                errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.ACCOUNT_DATA_MISSING))
            }
        }

        when (formData.action) {
            VaultTransactionAction.DEPOSIT -> {
                if (postOpFreeCollateral != null && postOpFreeCollateral < 0) {
                    errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.DEPOSIT_TOO_HIGH))
                }
            }
            VaultTransactionAction.WITHDRAW -> {
                if (postOpVaultBalance != null && postOpVaultBalance < 0) {
                    errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.WITHDRAW_TOO_HIGH))
                }
                if (postOpVaultBalance != null && postOpVaultBalance >= 0 && amount > 0 && vaultAccount?.withdrawableUsdc != null && amount > vaultAccount.withdrawableUsdc) {
                    errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.WITHDRAWING_LOCKED_BALANCE))
                }
                if (sharesToAttemptWithdraw != null && slippageResponse != null && sharesToAttemptWithdraw != slippageResponse.shares) {
                    errors.add(
                        VaultFormValidationError(
                            VaultValidationErrorSeverity.ERROR,
                            VaultValidationErrorType.SLIPPAGE_RESPONSE_WRONG_SHARES,
                        ),
                    )
                }
                if (needSlippageAck) {
                    errors.add(VaultFormValidationError(VaultValidationErrorSeverity.WARNING, VaultValidationErrorType.SLIPPAGE_TOO_HIGH))
                    if (slippagePercent >= SLIPPAGE_PERCENT_ACK && !formData.acknowledgedSlippage && formData.inConfirmationStep) {
                        errors.add(VaultFormValidationError(VaultValidationErrorSeverity.ERROR, VaultValidationErrorType.MUST_ACK_SLIPPAGE))
                    }
                }
            }
        }

        // Prepare submission data if no errors
        if (errors.none { it.severity === VaultValidationErrorSeverity.ERROR }) {
            submissionData = when (formData.action) {
                VaultTransactionAction.DEPOSIT -> VaultDepositWithdrawSubmissionData(
                    deposit = VaultDepositData(
                        subaccountFrom = "0",
                        amount = amount,
                    ),
                    withdraw = null,
                )
                VaultTransactionAction.WITHDRAW -> VaultDepositWithdrawSubmissionData(
                    deposit = null,
                    withdraw = if (sharesToAttemptWithdraw != null && sharesToAttemptWithdraw > 0 && slippageResponse != null) {
                        VaultWithdrawData(
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
        val summaryData = VaultDepositWithdrawFormSummaryData(
            needSlippageAck = needSlippageAck,
            marginUsage = postOpMarginUsage,
            freeCollateral = postOpFreeCollateral,
            vaultBalance = postOpVaultBalance,
            estimatedSlippage = slippagePercent,
            estimatedAmountReceived = if (formData.action === VaultTransactionAction.WITHDRAW) slippageResponse?.expectedAmount else null,
        )

        return VaultFormValidationResult(
            errors = errors,
            submissionData = submissionData,
            summaryData = summaryData,
        )
    }
}
