package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.ErrorFormat
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.abacus.state.machine.onChainWithdrawalCapacity
import exchange.dydx.abacus.state.machine.onChainWithdrawalGating
import exchange.dydx.abacus.state.machine.transfer
import exchange.dydx.abacus.tests.extensions.loadAccounts
import kollections.iListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class V4WithdrawalSafetyChecksTests : V4BaseTests() {

    override fun setup() {
        perp.loadAccounts(mock)
        perp.currentBlockAndHeight = mock.heightMock.currentBlockAndHeight
        perp.transfer(
            TransferType.deposit.rawValue,
            TransferInputField.type,
            environment = mock.v4Environment,
        )
    }

    @Test
    fun testGating() {
        setup()

        perp.parseOnChainWithdrawalGating(mock.v4WithdrawalSafetyChecksMock.withdrawal_and_transfer_gating_status_data)
        val withdrwalGating = perp.internalState.configs.withdrawalGating
        assertEquals(16750, withdrwalGating?.withdrawalsAndTransfersUnblockedAtBlock)

        perp.currentBlockAndHeight = mock.heightMock.beforeCurrentBlockAndHeight
        perp.transfer(
            TransferType.withdrawal.rawValue,
            TransferInputField.type,
            environment = mock.v4Environment,
        )

        perp.transfer("1235.0", TransferInputField.usdcSize, environment = mock.v4Environment)
        var error = perp.internalState.input.errors?.firstOrNull {
            it.type == ErrorType.error
        }
        assertEquals(ErrorType.error, error?.type)
        assertEquals("", error?.code)
        assertEquals("APP.GENERAL.LEARN_MORE_ARROW", error?.linkText)
        val resources = error?.resources
        assertEquals(
            "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_TITLE",
            resources?.title?.stringKey,
        )
        assertEquals(
            "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_DESCRIPTION",
            resources?.text?.stringKey,
        )
        assertEquals(
            "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_PAUSED_ACTION",
            resources?.action?.stringKey,
        )
        assertEquals(1, resources?.text?.params?.size)
        val param = resources?.text?.params?.get(0)
        assertEquals("1", param?.value)
        assertEquals(ErrorFormat.StringVal, param?.format)
        assertEquals("SECONDS", param?.key)

        perp.currentBlockAndHeight = mock.heightMock.afterCurrentBlockAndHeight
        perp.transfer(
            TransferType.transferOut.rawValue,
            TransferInputField.type,
            environment = mock.v4Environment,
        )

        perp.transfer("1235.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(ErrorType.required, error?.type)
        assertEquals("REQUIRED_ADDRESS", error?.code)
        assertEquals("address", error?.fields?.get(0))
        assertEquals(
            "APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS",
            error?.resources?.action?.stringKey,
        )
    }

    @Test
    fun testCapacity() {
        setup()

        perp.transfer("WITHDRAWAL", TransferInputField.type, environment = mock.v4Environment)
        perp.transfer("1235.0", TransferInputField.usdcSize, environment = mock.v4Environment)

        perp.parseOnChainWithdrawalCapacity(mock.v4WithdrawalSafetyChecksMock.withdrawal_capacity_by_denom_data_daily_less_than_weekly)
        val errors = perp.internalState.input.errors
        val error = errors?.firstOrNull { it.type == ErrorType.error }
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "")
        assertEquals(error?.linkText, "APP.GENERAL.LEARN_MORE_ARROW")
        val resources = error?.resources
        assertEquals(
            resources?.title?.stringKey,
            "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_TITLE",
        )
        assertEquals(
            resources?.text?.stringKey,
            "WARNINGS.ACCOUNT_FUND_MANAGEMENT.WITHDRAWAL_LIMIT_OVER_DESCRIPTION",
        )
        assertEquals(resources?.text?.params?.size, 1)
        val param = resources?.text?.params?.get(0)
        assertEquals(param?.value, "1234.567891")
        assertEquals(param?.format, ErrorFormat.Price)
        assertEquals(param?.key, "USDC_LIMIT")

        assertEquals(
            parser.asDouble(perp.internalState.configs.withdrawalCapacity?.maxWithdrawalCapacity),
            1234.567891,
        )
        assertEquals(perp.internalState.configs.withdrawalCapacity?.capacity, "1234567891")
    }
}

private fun TradingStateMachine.parseOnChainWithdrawalCapacity(payload: String): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    try {
        changes = onChainWithdrawalCapacity(payload)
    } catch (e: ParsingException) {
        error = e.toParsingError()
    }
    if (changes != null) {
        updateStateChanges(changes)
    }

    val errors = if (error != null) iListOf(error) else null
    return StateResponse(state, changes, errors)
}

private fun TradingStateMachine.parseOnChainWithdrawalGating(payload: String): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    try {
        changes = onChainWithdrawalGating(payload)
    } catch (e: ParsingException) {
        error = e.toParsingError()
    }
    if (changes != null) {
        updateStateChanges(changes)
    }

    val errors = if (error != null) iListOf(error) else null
    return StateResponse(state, changes, errors)
}
