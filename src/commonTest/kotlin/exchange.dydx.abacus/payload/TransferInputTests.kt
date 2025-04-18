package exchange.dydx.abacus.payload

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.abacus.state.machine.transfer
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransferInputTests : V4BaseTests() {
    @Test
    fun testDepositTransferInputOnce() {
        setup()
        var time = ServerTime.now()
        testDepositTransferInput()
        time = perp.log("Deposit", time)
    }

    @Test
    fun testSlowWithdrawalTransferInputOnce() {
        setup()
        var time = ServerTime.now()
        testSlowWithdrawalTransferInput()
        time = perp.log("Slow Withdrawal", time)
    }

    @Test
    fun testTransferOutTransferInputOnce() {
        setup()
        var time = ServerTime.now()
        testTransferOutTransferInput()
        perp.log("Transfer Out", time)
    }

    @Test
    fun testTransferInputTypeChangeOnce() {
        setup()
        testTransferInputTypeChange()
    }

    private fun testDepositTransferInput() {
        /*
        Designed workflow
        transfer("DEPOSIT", TransferInputField.type)
        transfer("1000", TransferInputField.usdcSize)
        transfer("0", TransferInputField.usdcFee)   // if fee is charged outside usdcSize
        transfer("1.3", TransferInputField.usdcFee) // if fee is deducted from usdcSize
         */

        perp.transfer("DEPOSIT", TransferInputField.type, environment = mock.v4Environment)

        var transfer = perp.state?.input?.transfer
        assertEquals(TransferType.deposit, transfer?.type)

        perp.transfer("1", TransferInputField.usdcSize, environment = mock.v4Environment)

        transfer = perp.state?.input?.transfer
        assertEquals(TransferType.deposit, transfer?.type)
        assertEquals("1", transfer?.size?.usdcSize)

        var subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        var calculatedCurrent = subaccount.calculated[CalculationPeriod.current]!!
        var calculatedPostOrder = subaccount.calculated[CalculationPeriod.post]!!
        assertEquals(108116.7318528828, calculatedCurrent.equity)
        assertEquals(108117.7318528828, calculatedPostOrder.equity)
        assertEquals(106640.3767269893, calculatedCurrent.freeCollateral)
        assertEquals(106641.3767269893, calculatedPostOrder.freeCollateral)
        assertEquals(99872.368956, calculatedCurrent.quoteBalance)
        assertEquals(99873.368956, calculatedPostOrder.quoteBalance)
        assertEquals(0.2731039128897115, calculatedCurrent.leverage)
        assertEquals(0.27310138690337965, calculatedPostOrder.leverage)
        assertEquals(0.013655195644485585, calculatedCurrent.marginUsage)
        assertEquals(0.013655069345169024, calculatedPostOrder.marginUsage)
        assertEquals(5332018.836349465, calculatedCurrent.buyingPower)
        assertEquals(5332068.836349465, calculatedPostOrder.buyingPower)

        perp.transfer("5000.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        transfer = perp.state?.input?.transfer
        assertEquals(TransferType.deposit, transfer?.type)
        assertEquals("5000.0", transfer?.size?.usdcSize)
        assertEquals(5000.0, transfer?.summary?.usdcSize)

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        calculatedCurrent = subaccount.calculated[CalculationPeriod.current]!!
        calculatedPostOrder = subaccount.calculated[CalculationPeriod.post]!!
        assertEquals(108116.7318528828, calculatedCurrent.equity)
        assertEquals(113116.7318528828, calculatedPostOrder.equity)
        assertEquals(106640.3767269893, calculatedCurrent.freeCollateral)
        assertEquals(111640.3767269893, calculatedPostOrder.freeCollateral)
        assertEquals(99872.368956, calculatedCurrent.quoteBalance)
        assertEquals(104872.368956, calculatedPostOrder.quoteBalance)
        assertEquals(0.2731039128897115, calculatedCurrent.leverage)
        assertEquals(0.2610321394033229, calculatedPostOrder.leverage)
        assertEquals(0.013655195644485585, calculatedCurrent.marginUsage)
        assertEquals(0.013051606970166163, calculatedPostOrder.marginUsage)
        assertEquals(5332018.836349465, calculatedCurrent.buyingPower)
        assertEquals(5582018.836349465, calculatedPostOrder.buyingPower)

        /*
        size = 1000.0
         */
        perp.transfer("1000.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        transfer = perp.state?.input?.transfer
        assertEquals(TransferType.deposit, transfer?.type)
        assertEquals("1000.0", transfer?.size?.usdcSize)
        assertEquals(1000.0, transfer?.summary?.usdcSize)

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        calculatedCurrent = subaccount.calculated[CalculationPeriod.current]!!
        calculatedPostOrder = subaccount.calculated[CalculationPeriod.post]!!
        assertEquals(108116.7318528828, calculatedCurrent.equity)
        assertEquals(109116.7318528828, calculatedPostOrder.equity)
        assertEquals(106640.3767269893, calculatedCurrent.freeCollateral)
        assertEquals(107640.3767269893, calculatedPostOrder.freeCollateral)
        assertEquals(99872.368956, calculatedCurrent.quoteBalance)
        assertEquals(100872.368956, calculatedPostOrder.quoteBalance)
        assertEquals(0.2731039128897115, calculatedCurrent.leverage)
        assertEquals(0.2706010528035248, calculatedPostOrder.leverage)
        assertEquals(0.013655195644485585, calculatedCurrent.marginUsage)
        assertEquals(0.01353005264017626, calculatedPostOrder.marginUsage)
        assertEquals(5332018.836349465, calculatedCurrent.buyingPower)
        assertEquals(5382018.836349465, calculatedPostOrder.buyingPower)

        perp.transfer("10.0", TransferInputField.usdcFee, environment = mock.v4Environment)

        transfer = perp.state?.input?.transfer
        assertEquals(TransferType.deposit, transfer?.type)
        assertEquals("1000.0", transfer?.size?.usdcSize)
        assertEquals(1000.0, transfer?.summary?.usdcSize)
        assertEquals(10.0, transfer?.summary?.fee)
        assertEquals(true, transfer?.summary?.filled)

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        calculatedCurrent = subaccount.calculated[CalculationPeriod.current]!!
        calculatedPostOrder = subaccount.calculated[CalculationPeriod.post]!!
        assertEquals(108116.7318528828, calculatedCurrent.equity)
        assertEquals(109106.7318528828, calculatedPostOrder.equity)
        assertEquals(106640.3767269893, calculatedCurrent.freeCollateral)
        assertEquals(107630.3767269893, calculatedPostOrder.freeCollateral)
        assertEquals(99872.368956, calculatedCurrent.quoteBalance)
        assertEquals(100862.368956, calculatedPostOrder.quoteBalance)
        assertEquals(0.2731039128897115, calculatedCurrent.leverage)
        assertEquals(0.27062585430277314, calculatedPostOrder.leverage)
        assertEquals(0.013655195644485585, calculatedCurrent.marginUsage)
        assertEquals(0.013531292715138643, calculatedPostOrder.marginUsage)
        assertEquals(5332018.836349465, calculatedCurrent.buyingPower)
        assertEquals(5381518.836349465, calculatedPostOrder.buyingPower)
    }

    private fun testSlowWithdrawalTransferInput() {
        test({
            perp.transfer("WITHDRAWAL", TransferInputField.type, environment = mock.v4Environment)
        }, null)

        test({
            perp.transfer("false", TransferInputField.fastSpeed, environment = mock.v4Environment)
        }, null)

        test({
            perp.transfer("0", TransferInputField.usdcFee, environment = mock.v4Environment)
        }, null)

        /*
        size = 1000.0
         */
        test({
            perp.transfer("5000.0", TransferInputField.usdcSize, environment = mock.v4Environment)
        }, null)

        perp.transfer("1000.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        var transfer = perp.state?.input?.transfer
        assertEquals(TransferType.withdrawal, transfer?.type)
        assertEquals("1000.0", transfer?.size?.usdcSize)
        assertEquals(1000.0, transfer?.summary?.usdcSize)
        assertEquals(true, transfer?.summary?.filled)

        var subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        var calculatedCurrent = subaccount.calculated[CalculationPeriod.current]!!
        var calculatedPostOrder = subaccount.calculated[CalculationPeriod.post]!!
        assertEquals(108116.7318528828, calculatedCurrent.equity)
        assertEquals(107116.7318528828, calculatedPostOrder.equity)
        assertEquals(106640.3767269893, calculatedCurrent.freeCollateral)
        assertEquals(105640.3767269893, calculatedPostOrder.freeCollateral)
        assertEquals(99872.368956, calculatedCurrent.quoteBalance)
        assertEquals(98872.368956, calculatedPostOrder.quoteBalance)
        assertEquals(0.2731039128897115, calculatedCurrent.leverage)
        assertEquals(0.2756535044256519, calculatedPostOrder.leverage)
        assertEquals(0.013655195644485585, calculatedCurrent.marginUsage)
        assertEquals(0.013782675221282625, calculatedPostOrder.marginUsage)
        assertEquals(5332018.836349465, calculatedCurrent.buyingPower)
        assertEquals(5282018.836349465, calculatedPostOrder.buyingPower)

        perp.transfer("10.0", TransferInputField.usdcFee, environment = mock.v4Environment)

        transfer = perp.state?.input?.transfer
        assertEquals(TransferType.withdrawal, transfer?.type)
        assertEquals("1000.0", transfer?.size?.usdcSize)
        assertEquals(1000.0, transfer?.summary?.usdcSize)
        assertEquals(true, transfer?.summary?.filled)

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        calculatedCurrent = subaccount.calculated[CalculationPeriod.current]!!
        calculatedPostOrder = subaccount.calculated[CalculationPeriod.post]!!
        assertEquals(108116.7318528828, calculatedCurrent.equity)
        assertEquals(107106.7318528828, calculatedPostOrder.equity)
        assertEquals(106640.3767269893, calculatedCurrent.freeCollateral)
        assertEquals(105630.3767269893, calculatedPostOrder.freeCollateral)
        assertEquals(99872.368956, calculatedCurrent.quoteBalance)
        assertEquals(98862.368956, calculatedPostOrder.quoteBalance)
        assertEquals(0.2731039128897115, calculatedCurrent.leverage)
        assertEquals(0.2756792407635699, calculatedPostOrder.leverage)
        assertEquals(0.013655195644485585, calculatedCurrent.marginUsage)
        assertEquals(0.013783962038178554, calculatedPostOrder.marginUsage)
        assertEquals(5332018.836349465, calculatedCurrent.buyingPower)
        assertEquals(5281518.836349465, calculatedPostOrder.buyingPower)
    }

    private fun testTransferOutTransferInput() {
        test({
            perp.transfer("TRANSFER_OUT", TransferInputField.type, environment = mock.v4Environment)
        }, null)

        test({
            perp.transfer("0.0", TransferInputField.usdcFee, environment = mock.v4Environment)
        }, null)

        /*
        size = 1000.0
         */
        test({
            perp.transfer("5000.0", TransferInputField.usdcSize, environment = mock.v4Environment)
        }, null)

        test({
            perp.transfer("test memo", TransferInputField.MEMO, environment = mock.v4Environment)
        }, null)

        perp.transfer("1000.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        val transfer = perp.state?.input?.transfer
        assertEquals(TransferType.transferOut, transfer?.type)
        assertEquals("1000.0", transfer?.size?.usdcSize)
        assertEquals(1000.0, transfer?.summary?.usdcSize)
        assertEquals(true, transfer?.summary?.filled)

        val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        val calculatedCurrent = subaccount.calculated[CalculationPeriod.current]!!
        val calculatedPostOrder = subaccount.calculated[CalculationPeriod.post]!!
        assertEquals(108116.7318528828, calculatedCurrent.equity)
        assertEquals(107116.7318528828, calculatedPostOrder.equity)
        assertEquals(106640.3767269893, calculatedCurrent.freeCollateral)
        assertEquals(105640.3767269893, calculatedPostOrder.freeCollateral)
        assertEquals(99872.368956, calculatedCurrent.quoteBalance)
        assertEquals(98872.368956, calculatedPostOrder.quoteBalance)
        assertEquals(0.2731039128897115, calculatedCurrent.leverage)
        assertEquals(0.2756535044256519, calculatedPostOrder.leverage)
        assertEquals(0.013655195644485585, calculatedCurrent.marginUsage)
        assertEquals(0.013782675221282625, calculatedPostOrder.marginUsage)
        assertEquals(5332018.836349465, calculatedCurrent.buyingPower)
        assertEquals(5282018.836349465, calculatedPostOrder.buyingPower)

        assertTrue { perp.state?.input?.transfer?.transferOutOptions?.assets?.count() == 2 }
        assertTrue { perp.state?.input?.transfer?.transferOutOptions?.chains?.count() == 1 }
    }

    private fun testTransferInputTypeChange() {
        perp.transfer(
            data = "DEPOSIT",
            type = TransferInputField.type,
            environment = mock.v4Environment,
        )

        var transfer = perp.state?.input?.transfer
        assertEquals(TransferType.deposit, transfer?.type)
        assertEquals(null, transfer?.memo)

        perp.transfer(
            data = "TRANSFER_OUT",
            type = TransferInputField.type,
            environment = mock.v4Environment,
        )

        transfer = perp.state?.input?.transfer
        assertEquals(TransferType.transferOut, transfer?.type)
        assertEquals(null, transfer?.memo)

        perp.transfer(
            data = "test memo",
            type = TransferInputField.MEMO,
            environment = mock.v4Environment,
        )

        transfer = perp.state?.input?.transfer
        assertEquals(TransferType.transferOut, transfer?.type)
        assertEquals("test memo", transfer?.memo)

        perp.transfer("WITHDRAWAL", TransferInputField.type, environment = mock.v4Environment)

        transfer = perp.state?.input?.transfer
        assertEquals(TransferType.withdrawal, transfer?.type)
        assertEquals(null, transfer?.memo)
    }
}
