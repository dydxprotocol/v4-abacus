package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.output.input.ErrorFormat
import exchange.dydx.abacus.output.input.ErrorParam
import exchange.dydx.abacus.output.input.ErrorResources
import exchange.dydx.abacus.output.input.ErrorString
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.format
import indexer.models.chain.OnChainVaultDepositWithdrawSlippageResponse
import kollections.toIList
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.math.abs

@JsExport
@Serializable
data class VaultFormData(
    val action: VaultFormAction,
    val amount: Double?,
    val acknowledgedSlippage: Boolean,
    val acknowledgedTerms: Boolean,
    val inConfirmationStep: Boolean,
)

@JsExport
@Serializable
enum class VaultFormAction {
    DEPOSIT,
    WITHDRAW,
}

@JsExport
@Serializable
data class VaultFormAccountData(
    val marginUsage: Double?,
    val freeCollateral: Double?,
    val canViewAccount: Boolean?,
)

internal class VaultFormValidationErrors(
    private val localizer: LocalizerProtocol? = null,
) {
    private fun createError(
        code: String,
        type: ErrorType,
        fields: List<String>? = null,
        titleKey: String? = null,
        textKey: String? = null,
        textKeyParams: List<ErrorParam>? = null
    ): ValidationError {
        val paramsMap = mutableMapOf<String, String>()
        for (param in textKeyParams ?: emptyList()) {
            if (param.value != null) {
                paramsMap[param.key] = param.value
            }
        }
        return ValidationError(
            code = code,
            type = type,
            fields = fields?.toIList(),
            action = null,
            link = null,
            linkText = null,
            resources = ErrorResources(
                title = titleKey?.let {
                    ErrorString(
                        stringKey = it,
                        params = null,
                        localized = localizer?.localize(it),
                    )
                },
                text = textKey?.let {
                    ErrorString(
                        stringKey = it,
                        params = textKeyParams?.toIList(),
                        localized = localizer?.localizeWithParams(it, paramsMap),
                    )
                },
                action = null,
            ),
        )
    }

    fun amountEmpty(operation: VaultFormAction) = createError(
        code = "AMOUNT_EMPTY",
        type = ErrorType.error,
        fields = listOf("amount"),
        titleKey = if (operation == VaultFormAction.DEPOSIT) {
            "APP.VAULTS.ENTER_AMOUNT_TO_DEPOSIT"
        } else {
            "APP.VAULTS.ENTER_AMOUNT_TO_WITHDRAW"
        },
    )

    fun accountDataMissing(canViewAccount: Boolean?) = createError(
        code = "ACCOUNT_DATA_MISSING",
        type = ErrorType.error,
        titleKey = if (canViewAccount != null && canViewAccount) {
            "APP.GENERAL.NOT_ALLOWED"
        } else {
            "APP.GENERAL.CONNECT_WALLET"
        },
    )

    fun depositTooHigh() = createError(
        code = "DEPOSIT_TOO_HIGH",
        type = ErrorType.error,
        fields = listOf("amount"),
        titleKey = "APP.TRADE.MODIFY_SIZE_FIELD",
        textKey = "APP.VAULTS.DEPOSIT_TOO_HIGH",
    )

    fun depositTooLow() = createError(
        code = "DEPOSIT_TOO_LOW",
        type = ErrorType.error,
        fields = listOf("amount"),
        titleKey = "APP.TRADE.MODIFY_SIZE_FIELD",
        textKey = "APP.VAULTS.DEPOSIT_TOO_LOW",
    )

    fun withdrawTooHigh() = createError(
        code = "WITHDRAW_TOO_HIGH",
        type = ErrorType.error,
        fields = listOf("amount"),
        titleKey = "APP.TRADE.MODIFY_SIZE_FIELD",
        textKey = "APP.VAULTS.WITHDRAW_TOO_HIGH",
    )

    fun withdrawTooLow() = createError(
        code = "WITHDRAW_TOO_LOW",
        type = ErrorType.error,
        fields = listOf("amount"),
        titleKey = "APP.TRADE.MODIFY_SIZE_FIELD",
        textKey = "APP.VAULTS.WITHDRAW_TOO_LOW",
    )

    fun withdrawingLockedBalance() = createError(
        code = "WITHDRAWING_LOCKED_BALANCE",
        type = ErrorType.error,
        fields = listOf("amount"),
        titleKey = "APP.TRADE.MODIFY_SIZE_FIELD",
        textKey = "APP.VAULTS.WITHDRAW_TOO_HIGH",
    )

    fun slippageTooHigh(slippagePercent: Double) = createError(
        code = "SLIPPAGE_TOO_HIGH",
        type = ErrorType.warning,
        textKey = "APP.VAULTS.SLIPPAGE_WARNING",
        textKeyParams = listOf(
            ErrorParam(key = "AMOUNT", value = slippagePercent.format(4), format = ErrorFormat.Percent),
            ErrorParam(key = "LINK", value = "", format = null),
        ),
    )

    fun mustAckSlippage() = createError(
        code = "MUST_ACK_SLIPPAGE",
        type = ErrorType.error,
        fields = listOf("acknowledgeSlippage"),
        titleKey = "APP.VAULTS.ACKNOWLEDGE_HIGH_SLIPPAGE",
    )

    fun mustAckTerms() = createError(
        code = "MUST_ACK_TERMS",
        type = ErrorType.error,
        fields = listOf("acknowledgeTerms"),
        titleKey = "APP.VAULTS.ACKNOWLEDGE_MEGAVAULT_TERMS",
    )

    fun vaultAccountMissing() = createError(
        code = "VAULT_ACCOUNT_MISSING",
        type = ErrorType.error,
    )

    fun slippageResponseMissing() = createError(
        code = "SLIPPAGE_RESPONSE_MISSING",
        type = ErrorType.error,
    )

    fun slippageResponseWrongShares() = createError(
        code = "SLIPPAGE_RESPONSE_WRONG_SHARES",
        type = ErrorType.error,
    )
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
data class VaultFormSummaryData(
    val needSlippageAck: Boolean?,
    val needTermsAck: Boolean?,
    val marginUsage: Double?,
    val freeCollateral: Double?,
    val vaultBalance: Double?,
    val withdrawableVaultBalance: Double?,
    val estimatedSlippage: Double?,
    val estimatedAmountReceived: Double?
)

@JsExport
@Serializable
data class VaultFormValidationResult(
    val errors: IList<ValidationError>,
    val submissionData: VaultDepositWithdrawSubmissionData?,
    val summaryData: VaultFormSummaryData
)

@JsExport
object VaultDepositWithdrawFormValidator {
    private val parser = Parser()

    private const val SLIPPAGE_PERCENT_WARN = 0.01
    private const val SLIPPAGE_PERCENT_ACK = 0.04
    private const val SLIPPAGE_TOLERANCE = 0.01

    private const val MIN_DEPOSIT_FE_THRESHOLD = 20.0

    fun getVaultDepositWithdrawSlippageResponse(apiResponse: String): OnChainVaultDepositWithdrawSlippageResponse? {
        return parser.asTypedObject<OnChainVaultDepositWithdrawSlippageResponse>(apiResponse)
    }

    fun calculateSharesToWithdraw(
        vaultAccount: VaultAccount?,
        amount: Double
    ): Double {
        val shareValue = vaultAccount?.shareValue ?: 0.0
        if (shareValue == 0.0) {
            return 0.0
        }

        val amountToUse = if (vaultAccount?.withdrawableUsdc != null &&
            vaultAccount.withdrawableUsdc - amount >= 0 &&
            vaultAccount.withdrawableUsdc - amount <= 0.01
        ) {
            vaultAccount.withdrawableUsdc
        } else {
            amount
        }

        return (amountToUse / shareValue).toLong().toDouble()
    }

    fun validateVaultForm(
        formData: VaultFormData,
        accountData: VaultFormAccountData?,
        vaultAccount: VaultAccount?,
        slippageResponse: OnChainVaultDepositWithdrawSlippageResponse?,
        localizer: LocalizerProtocol? = null,
    ): VaultFormValidationResult {
        val vaultFormValidationErrors = VaultFormValidationErrors(localizer)
        val errors = mutableListOf<ValidationError>()
        var submissionData: VaultDepositWithdrawSubmissionData? = null

        val sharesToAttemptWithdraw = if (formData.action == VaultFormAction.WITHDRAW &&
            vaultAccount != null &&
            (vaultAccount.shareValue ?: 0.0) > 0.0 &&
            formData.amount != null
        ) {
            calculateSharesToWithdraw(vaultAccount, formData.amount)
        } else {
            null
        }

        val amount = when (formData.action) {
            VaultFormAction.DEPOSIT -> formData.amount ?: 0.0
            VaultFormAction.WITHDRAW -> if (sharesToAttemptWithdraw != null) {
                sharesToAttemptWithdraw * (vaultAccount?.shareValue ?: 0.0)
            } else {
                formData.amount ?: 0.0
            }
        }

        val withdrawnAmountIncludingSlippage = slippageResponse?.expectedQuoteQuantums?.let { it / 1_000_000.0 }
        val postOpVaultBalance = when (formData.action) {
            VaultFormAction.DEPOSIT -> (vaultAccount?.balanceUsdc ?: 0.0) + amount
            VaultFormAction.WITHDRAW -> (vaultAccount?.balanceUsdc ?: 0.0) - amount
        }
        val postOpWithdrawableVaultBalance = when (formData.action) {
            VaultFormAction.DEPOSIT -> (vaultAccount?.withdrawableUsdc ?: 0.0) + amount
            VaultFormAction.WITHDRAW -> (vaultAccount?.withdrawableUsdc ?: 0.0) - amount
        }

        val (postOpFreeCollateral, postOpMarginUsage) = if (accountData?.freeCollateral != null && accountData.marginUsage != null) {
            val equity = accountData.freeCollateral / (1 - accountData.marginUsage)
            val postOpEquity = when (formData.action) {
                VaultFormAction.DEPOSIT -> equity - amount
                VaultFormAction.WITHDRAW -> if (withdrawnAmountIncludingSlippage != null) equity + withdrawnAmountIncludingSlippage else null
            }
            val newFreeCollateral = when (formData.action) {
                VaultFormAction.DEPOSIT -> accountData.freeCollateral - amount
                VaultFormAction.WITHDRAW -> if (withdrawnAmountIncludingSlippage != null) accountData.freeCollateral + withdrawnAmountIncludingSlippage else null
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
            formData.action === VaultFormAction.WITHDRAW &&
            amount > 0 &&
            withdrawnAmountIncludingSlippage != null
        ) {
            1.0 - withdrawnAmountIncludingSlippage / amount
        } else {
            0.0
        }
        val needSlippageAck = slippagePercent >= SLIPPAGE_PERCENT_ACK && formData.inConfirmationStep
        val needTermsAck = formData.action == VaultFormAction.DEPOSIT && formData.inConfirmationStep

        // Perform validation checks and populate errors list
        if (accountData == null) {
            errors.add(vaultFormValidationErrors.accountDataMissing(accountData?.canViewAccount))
        }

        if (amount == 0.0) {
            errors.add(vaultFormValidationErrors.amountEmpty(formData.action))
        }

        // can't actually submit if we are missing key validation information
        if (formData.inConfirmationStep && formData.action === VaultFormAction.WITHDRAW) {
            if (vaultAccount == null) {
                errors.add(vaultFormValidationErrors.vaultAccountMissing())
            }
            if (slippageResponse == null || sharesToAttemptWithdraw == null) {
                errors.add(vaultFormValidationErrors.slippageResponseMissing())
            }
        }

        if (formData.inConfirmationStep && formData.action === VaultFormAction.DEPOSIT) {
            if (accountData?.marginUsage == null || accountData.freeCollateral == null) {
                errors.add(vaultFormValidationErrors.accountDataMissing(accountData?.canViewAccount))
            }
        }

        if (needTermsAck && !formData.acknowledgedTerms) {
            errors.add(vaultFormValidationErrors.mustAckTerms())
        }

        when (formData.action) {
            VaultFormAction.DEPOSIT -> {
                if (postOpFreeCollateral != null && postOpFreeCollateral < 0) {
                    errors.add(vaultFormValidationErrors.depositTooHigh())
                }
                if (amount > 0 && amount < MIN_DEPOSIT_FE_THRESHOLD) {
                    errors.add(vaultFormValidationErrors.depositTooLow())
                }
            }
            VaultFormAction.WITHDRAW -> {
                if (postOpVaultBalance != null && postOpVaultBalance < 0) {
                    errors.add(vaultFormValidationErrors.withdrawTooHigh())
                }
                if (amount > 0 && amount < MIN_DEPOSIT_FE_THRESHOLD) {
                    // only allowed if withdrawing entire balance
                    if (!(
                            vaultAccount?.withdrawableUsdc != null &&
                                abs(vaultAccount.withdrawableUsdc - amount) <= 0.01
                            )
                    ) {
                        errors.add(vaultFormValidationErrors.withdrawTooLow())
                    }
                }
                if (postOpVaultBalance != null && postOpVaultBalance >= 0 && amount > 0 &&
                    vaultAccount?.withdrawableUsdc != null && amount > vaultAccount.withdrawableUsdc
                ) {
                    errors.add(vaultFormValidationErrors.withdrawingLockedBalance())
                }
                if (sharesToAttemptWithdraw != null && slippageResponse != null && sharesToAttemptWithdraw != slippageResponse.sharesToWithdraw.numShares) {
                    errors.add(
                        vaultFormValidationErrors.slippageResponseWrongShares(),
                    )
                }
                if (slippagePercent >= SLIPPAGE_PERCENT_WARN) {
                    errors.add(vaultFormValidationErrors.slippageTooHigh(slippagePercent))
                }
                if (needSlippageAck && !formData.acknowledgedSlippage && formData.inConfirmationStep) {
                    errors.add(vaultFormValidationErrors.mustAckSlippage())
                }
            }
        }

        // Prepare submission data if no errors
        if (errors.none { it.type === ErrorType.error }) {
            submissionData = when (formData.action) {
                VaultFormAction.DEPOSIT -> VaultDepositWithdrawSubmissionData(
                    deposit = VaultDepositData(
                        subaccountFrom = "0",
                        amount = amount,
                    ),
                    withdraw = null,
                )
                VaultFormAction.WITHDRAW -> VaultDepositWithdrawSubmissionData(
                    deposit = null,
                    withdraw = if (sharesToAttemptWithdraw != null && sharesToAttemptWithdraw > 0 && slippageResponse != null && withdrawnAmountIncludingSlippage != null) {
                        VaultWithdrawData(
                            subaccountTo = "0",
                            shares = sharesToAttemptWithdraw,
                            minAmount = withdrawnAmountIncludingSlippage * (1 - SLIPPAGE_TOLERANCE),
                        )
                    } else {
                        null
                    },
                )
            }
        }

        // Prepare summary data
        val summaryData = VaultFormSummaryData(
            needSlippageAck = needSlippageAck,
            needTermsAck = needTermsAck,
            marginUsage = postOpMarginUsage,
            freeCollateral = postOpFreeCollateral,
            vaultBalance = postOpVaultBalance,
            withdrawableVaultBalance = postOpWithdrawableVaultBalance,
            estimatedSlippage = slippagePercent,
            estimatedAmountReceived = if (formData.action === VaultFormAction.WITHDRAW && withdrawnAmountIncludingSlippage != null) withdrawnAmountIncludingSlippage else null,
        )

        return VaultFormValidationResult(
            errors = errors.toIList(),
            submissionData = submissionData,
            summaryData = summaryData,
        )
    }
}
