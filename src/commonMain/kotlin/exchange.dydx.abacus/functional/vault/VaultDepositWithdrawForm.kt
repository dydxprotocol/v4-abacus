package exchange.dydx.abacus.functional.vault

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class FormData(
    val action: TransactionAction,
    val amount: Double?,
    val acknowledgedSlippage: Boolean,
    val currentFormStep: String
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
    DISCONNECTED,
    AMOUNT_EMPTY,
    DEPOSIT_TOO_HIGH,
    WITHDRAW_TOO_HIGH,
    SLIPPAGE_TOO_HIGH,
    MUST_ACK_SLIPPAGE,
    VAULT_ACCOUNT_MISSING,
    SLIPPAGE_RESPONSE_MISSING,
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
object VaultFormValidator {

    private const val SLIPPAGE_PERCENT_WARN = 0.05 // 5%
    private const val SLIPPAGE_PERCENT_ACK = 0.10 // 10%

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

        val postOpVaultBalance = if (vaultAccount?.balanceUsdc != null) {
            when (formData.action) {
                TransactionAction.DEPOSIT -> vaultAccount.balanceUsdc + amount
                TransactionAction.WITHDRAW -> vaultAccount.balanceUsdc - amount
            }
        } else {
            null
        }

        var postOpFreeCollateral: Double? = null
        var postOpMarginUsage: Double? = null
        if (accountData?.freeCollateral != null && accountData.marginUsage != null) {
            val equity = accountData.freeCollateral / (1 - accountData.marginUsage)
            val postOpEquity = when (formData.action) {
                TransactionAction.DEPOSIT -> equity - amount
                TransactionAction.WITHDRAW -> equity + amount
            }
            postOpFreeCollateral = when (formData.action) {
                TransactionAction.DEPOSIT -> accountData.freeCollateral - amount
                TransactionAction.WITHDRAW -> accountData.freeCollateral + amount
            }
            postOpMarginUsage = if (postOpEquity > 0.0) {
                1.0 - postOpFreeCollateral / postOpEquity
            } else {
                null
            }
        }

        val slippagePercent = if (formData.action === TransactionAction.WITHDRAW && amount > 0 && slippageResponse != null && vaultAccount != null && (vaultAccount.balanceUsdc ?: 0.0) > 0.0 && (vaultAccount.balanceShares ?: 0.0) > 0.0) {
            val valuePerShare = vaultAccount.balanceUsdc!! / vaultAccount.balanceShares!!

            slippageResponse.expectedAmount
        } else {
            0.0
        }
        val needSlippageAck = slippagePercent >= SLIPPAGE_PERCENT_WARN

        // Perform validation checks and populate errors list
        if (accountData == null) {
            errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.DISCONNECTED))
        }

        if (amount == 0.0) {
            errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.AMOUNT_EMPTY))
        }

        if (formData.currentFormStep == "confirm") {
            if (vaultAccount == null) {
                errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.VAULT_ACCOUNT_MISSING))
            }
            if (slippageResponse == null) {
                errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.SLIPPAGE_RESPONSE_MISSING))
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
                if (needSlippageAck) {
                    errors.add(ValidationError(ErrorSeverity.WARNING, ErrorType.SLIPPAGE_TOO_HIGH))
                    if (slippagePercent >= SLIPPAGE_PERCENT_ACK && !formData.acknowledgedSlippage && formData.currentFormStep == "confirm") {
                        errors.add(ValidationError(ErrorSeverity.ERROR, ErrorType.MUST_ACK_SLIPPAGE))
                    }
                }
            }
        }

        // Prepare submission data if no errors
        if (errors.isEmpty() && accountData != null && vaultAccount != null && slippageResponse != null) {
            submissionData = when (formData.action) {
                TransactionAction.DEPOSIT -> SubmissionData(
                    deposit = DepositData(
                        subaccountFrom = "defaultSubaccount", // Replace with actual logic
                        amount = amount,
                    ),
                    withdraw = null,
                )
                TransactionAction.WITHDRAW -> SubmissionData(
                    deposit = null,
                    withdraw = WithdrawData(
                        subaccountTo = "defaultSubaccount", // Replace with actual logic
                        shares = slippageResponse.shares,
                        minAmount = slippageResponse.expectedAmount * (1 - 0.01), // Assuming 1% slippage tolerance
                    ),
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
            estimatedAmountReceived = slippageResponse?.expectedAmount ?: 0.0,
        )

        return ValidationResult(
            errors = errors,
            submissionData = submissionData,
            summaryData = summaryData,
        )
    }
}
