package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.functional.vault.VaultAccountCalculator.calculateUserVaultInfo
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerTransferResponseObject
import indexer.codegen.IndexerTransferType
import kotlin.test.Test
import kotlin.test.assertEquals

class VaultFormTests {

    private fun makeVaultAccount(balanceShares: Double, balanceUsdc: Double, withdrawableUsdc: Double): VaultAccount {
        return VaultAccount(
            balanceShares = balanceShares,
            lockedShares = null, // currently ignored but we should probably populate this at some point
            vaultTransfers = null,
            allTimeReturnUsdc = null,
            totalVaultTransfersCount = null,
            withdrawableUsdc = withdrawableUsdc,
            balanceUsdc = balanceUsdc,
        )
    }

    @Test
    fun testDepositValidation() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = FormData(
                action = TransactionAction.DEPOSIT,
                amount = 100.0,
                acknowledgedSlippage = true,
                inConfirmationStep = true
            ),
            accountData = AccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                balanceShares = 250.0,
                withdrawableUsdc = 500.0,
            ),
            slippageResponse = null
        )

        assertEquals(
            ValidationResult(
                errors = emptyList(),
                submissionData = SubmissionData(
                    deposit = DepositData(
                        subaccountFrom = "0",
                        amount = 100.0
                    ),
                    withdraw = null
                ),
                summaryData = SummaryData(
                    needSlippageAck = false,
                    marginUsage = 0.5263157894736843,
                    freeCollateral = 900.0,
                    vaultBalance = 600.0,
                    estimatedSlippage = 0.0,
                    estimatedAmountReceived = null
                )
            ),
            result
        )
    }

    @Test
    fun testWithdrawValidation() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = FormData(
                action = TransactionAction.WITHDRAW,
                amount = 100.0,
                acknowledgedSlippage = true,
                inConfirmationStep = true
            ),
            accountData = AccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0
            ),
            slippageResponse = SlippageResponse(
                shares = 100.0,
                expectedAmount = 98.0
            )
        )

        assertEquals(
            ValidationResult(
                errors = listOf(
                    ValidationError(ErrorSeverity.WARNING, ErrorType.SLIPPAGE_TOO_HIGH)
                ),
                submissionData = SubmissionData(
                    deposit = null,
                    withdraw = WithdrawData(
                        subaccountTo = "0",
                        shares = 100.0,
                        minAmount = 98 * .99
                    )
                ),
                summaryData = SummaryData(
                    needSlippageAck = true,
                    marginUsage = 0.4766444232602478,
                    freeCollateral = 1098.0,
                    vaultBalance = 400.0,
                    estimatedSlippage = 0.02,
                    estimatedAmountReceived = 98.0
                )
            ),
            result
        )
    }

    @Test
    fun testWithdrawBadSlippageResponse() {
        // same as previous but your slippage response is out of date
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = FormData(
                action = TransactionAction.WITHDRAW,
                amount = 100.0,
                acknowledgedSlippage = true,
                inConfirmationStep = true
            ),
            accountData = AccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0
            ),
            slippageResponse = SlippageResponse(
                shares = 120.0,
                expectedAmount = 98.0
            )
        )

        assertEquals(
            ValidationResult(
                errors = listOf(
                    ValidationError(ErrorSeverity.ERROR, ErrorType.SLIPPAGE_RESPONSE_WRONG_SHARES),
                    ValidationError(ErrorSeverity.WARNING, ErrorType.SLIPPAGE_TOO_HIGH),
                ),
                submissionData = null,
                summaryData = SummaryData(
                    needSlippageAck = true,
                    marginUsage = 0.4766444232602478,
                    freeCollateral = 1098.0,
                    vaultBalance = 400.0,
                    estimatedSlippage = 0.020000000000000018, // unfortunate precision issues with direct equality checks
                    estimatedAmountReceived = 98.0
                )
            ),
            result
        )
    }

    @Test
    fun testInvalidWithdraw() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = FormData(
                action = TransactionAction.WITHDRAW,
                amount = 600.0,
                acknowledgedSlippage = false,
                inConfirmationStep = true
            ),
            accountData = AccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0,
            ),
            slippageResponse = SlippageResponse(
                shares = 600.0,
                expectedAmount = 500.0
            )
        )

        assertEquals(
            ValidationResult(
                errors = listOf(
                    ValidationError(ErrorSeverity.ERROR, ErrorType.WITHDRAW_TOO_HIGH),
                    ValidationError(ErrorSeverity.WARNING, ErrorType.SLIPPAGE_TOO_HIGH),
                    ValidationError(ErrorSeverity.ERROR, ErrorType.MUST_ACK_SLIPPAGE)
                ),
                submissionData = null,
                summaryData = SummaryData(
                    needSlippageAck = true,
                    marginUsage = 0.4,
                    freeCollateral = 1500.0,
                    vaultBalance = -100.0,
                    estimatedSlippage = 0.16666666666666663,
                    estimatedAmountReceived = 500.0
                )
            ),
            result
        )
    }
}