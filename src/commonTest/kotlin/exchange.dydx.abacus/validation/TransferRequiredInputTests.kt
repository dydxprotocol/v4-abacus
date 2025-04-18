package exchange.dydx.abacus.validation

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.abacus.state.machine.transfer
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TransferRequiredInputTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTransferInputOnce()
    }

    private fun testTransferInputOnce() {
        var time = ServerTime.now()
        reset()
        testTransferInputDeposit()
        time = perp.log("Deposit", time)

        reset()
        testTransferInputWithdraw()
        time = perp.log("Withdrawal", time)

        reset()
        testTransferInputTransferOut()
        time = perp.log("Transfer", time)
    }

    override fun reset() {
        super.reset()

        perp.transfer(null, null, environment = mock.v4Environment)

        assertEquals(InputType.TRANSFER, perp.internalState.input.currentType)

        val transfer = perp.internalState.input.transfer
        assertEquals(TransferType.deposit, transfer.type)
    }

    private fun testTransferInputDeposit() {
        perp.transfer("DEPOSIT", TransferInputField.type, environment = mock.v4Environment)

        var transfer = perp.internalState.input.transfer
        assertEquals(TransferType.deposit, transfer.type)

        var error = perp.internalState.input.errors?.firstOrNull()
        assertEquals("REQUIRED_SIZE", error?.code)
        assertEquals(ErrorType.required, error?.type)
        assertEquals("size.usdcSize", error?.fields?.first())
        assertEquals("APP.TRADE.ENTER_AMOUNT", error?.resources?.action?.stringKey)

        perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        transfer = perp.internalState.input.transfer
        assertEquals(TransferType.deposit, transfer.type)

        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(null, error)
    }

    private fun testTransferInputWithdraw() {
        perp.transfer("WITHDRAWAL", TransferInputField.type, environment = mock.v4Environment)

        var transfer = perp.internalState.input.transfer
        assertEquals(TransferType.withdrawal, transfer.type)

        var error = perp.internalState.input.errors?.firstOrNull()
        assertEquals("REQUIRED_SIZE", error?.code)
        assertEquals(ErrorType.required, error?.type)
        assertEquals("size.usdcSize", error?.fields?.first())
        assertEquals("APP.TRADE.ENTER_AMOUNT", error?.resources?.action?.stringKey)

        perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        transfer = perp.internalState.input.transfer
        assertEquals(TransferType.withdrawal, transfer.type)

        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals("REQUIRED_ADDRESS", error?.code)
        assertEquals(ErrorType.required, error?.type)

        perp.transfer(
            "dydx16zfx8g4jg9vels3rsvcym490tkn5la304c57e9",
            TransferInputField.address,
            environment = mock.v4Environment,
        )
        assertNull(perp.internalState.input.errors?.firstOrNull())
    }

    private fun testTransferInputTransferOut() {
        perp.transfer("TRANSFER_OUT", TransferInputField.type, environment = mock.v4Environment)

        var transfer = perp.internalState.input.transfer
        assertEquals(TransferType.transferOut, transfer.type)

        var error = perp.internalState.input.errors?.firstOrNull()
        assertEquals("REQUIRED_ADDRESS", error?.code)
        assertEquals(ErrorType.required, error?.type)
        assertEquals("address", error?.fields?.first())
        assertEquals(
            "APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS",
            error?.resources?.action?.stringKey,
        )

        perp.transfer("1.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        transfer = perp.internalState.input.transfer
        assertEquals(TransferType.transferOut, transfer.type)

        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals("REQUIRED_ADDRESS", error?.code)

        perp.transfer("dydx1111111", TransferInputField.address, environment = mock.v4Environment)

        transfer = perp.internalState.input.transfer
        assertEquals(TransferType.transferOut, transfer.type)

        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals("INVALID_ADDRESS", error?.code)
        assertEquals(ErrorType.error, error?.type)
        assertEquals("address", error?.fields?.first())
        assertEquals("APP.DIRECT_TRANSFER_MODAL.ADDRESS_FIELD", error?.resources?.action?.stringKey)

        perp.transfer(
            "dydx16zfx8g4jg9vels3rsvcym490tkn5la304c57e9",
            TransferInputField.address,
            environment = mock.v4Environment,
        )

        transfer = perp.internalState.input.transfer
        assertEquals(TransferType.transferOut, transfer.type)

        assertNull(perp.internalState.input.errors?.firstOrNull())
    }
}
