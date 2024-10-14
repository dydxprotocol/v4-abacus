package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.output.input.ValidationError
import indexer.models.chain.OnChainNumShares
import indexer.models.chain.OnChainVaultDepositWithdrawSlippageResponse
import kollections.iListOf
import kollections.toIList
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
            vaultShareUnlocks = null,
        )
    }

    @Test
    fun testShareCalculation() {
        assertEquals(
            VaultDepositWithdrawFormValidator.calculateSharesToWithdraw(
                vaultAccount = makeVaultAccount(
                    balanceUsdc = 500.0,
                    balanceShares = 250.0,
                    withdrawableUsdc = 500.0,
                ),
                amount = 100.0,
            ),
            50.0,
        )
    }

    @Test
    fun testDepositValidation() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.DEPOSIT,
                amount = 100.0,
                acknowledgedSlippage = true,
                inConfirmationStep = true,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                balanceShares = 250.0,
                withdrawableUsdc = 500.0,
            ),
            slippageResponse = null,
        )

        assertEquals(
            VaultFormValidationResult(
                errors = listOf<ValidationError>().toIList(),
                submissionData = VaultDepositWithdrawSubmissionData(
                    deposit = VaultDepositData(
                        subaccountFrom = "0",
                        amount = 100.0,
                    ),
                    withdraw = null,
                ),
                summaryData = VaultFormSummaryData(
                    needSlippageAck = false,
                    marginUsage = 0.5263157894736843,
                    freeCollateral = 900.0,
                    vaultBalance = 600.0,
                    withdrawableVaultBalance = 600.0,
                    estimatedSlippage = 0.0,
                    estimatedAmountReceived = null,
                ),
            ),
            result,
        )
    }

    @Test
    fun testWithdrawValidation() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.WITHDRAW,
                amount = 100.0,
                acknowledgedSlippage = true,
                inConfirmationStep = true,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0,
            ),
            slippageResponse = OnChainVaultDepositWithdrawSlippageResponse(
                sharesToWithdraw = OnChainNumShares(numShares = 100.0),
                expectedQuoteQuantums = 98.0 * 1_000_000,
            ),
        )

        assertEquals(
            VaultFormValidationResult(
                errors = iListOf(
                    VaultFormValidationErrors().slippageTooHigh(0.02),
                ),
                submissionData = VaultDepositWithdrawSubmissionData(
                    deposit = null,
                    withdraw = VaultWithdrawData(
                        subaccountTo = "0",
                        shares = 100.0,
                        minAmount = 98 * .99,
                    ),
                ),
                summaryData = VaultFormSummaryData(
                    needSlippageAck = false,
                    marginUsage = 0.4766444232602478,
                    freeCollateral = 1098.0,
                    vaultBalance = 400.0,
                    withdrawableVaultBalance = 400.0,
                    estimatedSlippage = 0.020000000000000018,
                    estimatedAmountReceived = 98.0,
                ),
            ),
            result,
        )
    }

    @Test
    fun testWithdrawBadSlippageResponse() {
        // same as previous but your slippage response is out of date
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.WITHDRAW,
                amount = 100.0,
                acknowledgedSlippage = true,
                inConfirmationStep = true,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0,
            ),
            slippageResponse = OnChainVaultDepositWithdrawSlippageResponse(
                sharesToWithdraw = OnChainNumShares(numShares = 120.0),
                expectedQuoteQuantums = 98.0 * 1_000_000,
            ),
        )

        assertEquals(
            VaultFormValidationResult(
                errors = iListOf(
                    VaultFormValidationErrors().slippageResponseWrongShares(),
                    VaultFormValidationErrors().slippageTooHigh(0.02),
                ),
                submissionData = null,
                summaryData = VaultFormSummaryData(
                    needSlippageAck = false,
                    marginUsage = 0.4766444232602478,
                    freeCollateral = 1098.0,
                    vaultBalance = 400.0,
                    withdrawableVaultBalance = 400.0,
                    estimatedSlippage = 0.020000000000000018, // unfortunate precision issues with direct equality checks
                    estimatedAmountReceived = 98.0,
                ),
            ),
            result,
        )
    }

    @Test
    fun testInvalidWithdraw() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.WITHDRAW,
                amount = 600.0,
                acknowledgedSlippage = false,
                inConfirmationStep = true,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0,
            ),
            slippageResponse = OnChainVaultDepositWithdrawSlippageResponse(
                sharesToWithdraw = OnChainNumShares(numShares = 600.0),
                expectedQuoteQuantums = 500.0 * 1_000_000,
            ),
        )

        assertEquals(
            VaultFormValidationResult(
                errors = iListOf(
                    VaultFormValidationErrors().withdrawTooHigh(),
                    VaultFormValidationErrors().slippageTooHigh(0.166666),
                    VaultFormValidationErrors().mustAckSlippage(),
                ),
                submissionData = null,
                summaryData = VaultFormSummaryData(
                    needSlippageAck = true,
                    marginUsage = 0.4,
                    freeCollateral = 1500.0,
                    vaultBalance = -100.0,
                    withdrawableVaultBalance = -100.0,
                    estimatedSlippage = 0.16666666666666663,
                    estimatedAmountReceived = 500.0,
                ),
            ),
            result,
        )
    }

    @Test
    fun testLowDeposit() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.DEPOSIT,
                amount = 10.0,
                acknowledgedSlippage = false,
                inConfirmationStep = false,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 1000.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0,
            ),
            slippageResponse = null,
        )

        assertEquals(
            VaultFormValidationResult(
                errors = iListOf(
                    VaultFormValidationErrors().depositTooLow(),
                ),
                submissionData = null,
                summaryData = VaultFormSummaryData(
                    needSlippageAck = false,
                    marginUsage = 0.5025125628140703,
                    freeCollateral = 990.0,
                    vaultBalance = 1010.0,
                    withdrawableVaultBalance = 510.0,
                    estimatedSlippage = 0.0,
                    estimatedAmountReceived = null,
                ),
            ),
            result,
        )
    }

    @Test
    fun testLowWithdraw() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.WITHDRAW,
                amount = 10.0,
                acknowledgedSlippage = false,
                inConfirmationStep = false,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 1000.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0,
            ),
            slippageResponse = OnChainVaultDepositWithdrawSlippageResponse(
                sharesToWithdraw = OnChainNumShares(numShares = 5.0),
                expectedQuoteQuantums = 10.0 * 1_000_000,
            ),
        )

        assertEquals(
            VaultFormValidationResult(
                errors = iListOf(
                    VaultFormValidationErrors().withdrawTooLow(),
                ),
                submissionData = null,
                summaryData = VaultFormSummaryData(
                    needSlippageAck = false,
                    marginUsage = 0.49751243781094523,
                    freeCollateral = 1010.0,
                    vaultBalance = 990.0,
                    withdrawableVaultBalance = 490.0,
                    estimatedSlippage = 0.0,
                    estimatedAmountReceived = 10.0,
                ),
            ),
            result,
        )
    }

    @Test
    fun testLowWithdrawFull() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.WITHDRAW,
                amount = 10.0,
                acknowledgedSlippage = false,
                inConfirmationStep = false,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 1000.0,
                withdrawableUsdc = 10.0,
                balanceShares = 500.0,
            ),
            slippageResponse = OnChainVaultDepositWithdrawSlippageResponse(
                sharesToWithdraw = OnChainNumShares(numShares = 5.0),
                expectedQuoteQuantums = 10.0 * 1_000_000,
            ),
        )

        assertEquals(
            VaultFormValidationResult(
                errors = listOf<ValidationError>().toIList(),
                submissionData = VaultDepositWithdrawSubmissionData(
                    withdraw = VaultWithdrawData(
                        subaccountTo = "0",
                        shares = 5.0,
                        minAmount = 9.90,
                    ),
                    deposit = null,
                ),
                summaryData = VaultFormSummaryData(
                    needSlippageAck = false,
                    marginUsage = 0.49751243781094523,
                    freeCollateral = 1010.0,
                    vaultBalance = 990.0,
                    withdrawableVaultBalance = 0.0,
                    estimatedSlippage = 0.0,
                    estimatedAmountReceived = 10.0,
                ),
            ),
            result,
        )
    }

    @Test
    fun testLowWithdrawNonFull() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.WITHDRAW,
                amount = 6.0,
                acknowledgedSlippage = false,
                inConfirmationStep = false,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 1000.0,
                withdrawableUsdc = 10.0,
                balanceShares = 500.0,
            ),
            slippageResponse = OnChainVaultDepositWithdrawSlippageResponse(
                sharesToWithdraw = OnChainNumShares(numShares = 3.0),
                expectedQuoteQuantums = 6.0 * 1_000_000,
            ),
        )

        assertEquals(
            VaultFormValidationResult(
                errors = iListOf(
                    VaultFormValidationErrors().withdrawTooLow(),
                ),
                submissionData = null,
                summaryData = VaultFormSummaryData(
                    needSlippageAck = false,
                    marginUsage = 0.4985044865403788,
                    freeCollateral = 1006.0,
                    vaultBalance = 994.0,
                    withdrawableVaultBalance = 4.0,
                    estimatedSlippage = 0.0,
                    estimatedAmountReceived = 6.0,
                ),
            ),
            result,
        )
    }

    @Test
    fun testValidHighSlippageWithdrawWithAck() {
        val result = VaultDepositWithdrawFormValidator.validateVaultForm(
            formData = VaultFormData(
                action = VaultFormAction.WITHDRAW,
                amount = 500.0,
                acknowledgedSlippage = true,
                inConfirmationStep = true,
            ),
            accountData = VaultFormAccountData(
                marginUsage = 0.5,
                freeCollateral = 1000.0,
                canViewAccount = true,
            ),
            vaultAccount = makeVaultAccount(
                balanceUsdc = 500.0,
                withdrawableUsdc = 500.0,
                balanceShares = 500.0,
            ),
            slippageResponse = OnChainVaultDepositWithdrawSlippageResponse(
                sharesToWithdraw = OnChainNumShares(numShares = 500.0),
                expectedQuoteQuantums = 400.0 * 1_000_000,
            ),
        )

        assertEquals(
            VaultFormValidationResult(
                errors = iListOf(
                    VaultFormValidationErrors().slippageTooHigh(0.1999),
                ),
                submissionData = VaultDepositWithdrawSubmissionData(
                    deposit = null,
                    withdraw = VaultWithdrawData(
                        subaccountTo = "0",
                        shares = 500.0,
                        minAmount = 396.0,
                    ),
                ),
                summaryData = VaultFormSummaryData(
                    needSlippageAck = true,
                    marginUsage = 0.41666666666666663,
                    freeCollateral = 1400.0,
                    vaultBalance = 0.0,
                    withdrawableVaultBalance = 0.0,
                    estimatedSlippage = 0.19999999999999996,
                    estimatedAmountReceived = 400.0,
                ),
            ),
            result,
        )
    }
}
