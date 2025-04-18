package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.IsolatedMarginInputType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.state.machine.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.machine.adjustIsolatedMargin
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AdjustIsolatedMarginInputTests : V4BaseTests(useParentSubaccount = true) {

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    private fun loadSubaccountsWithChildren(): StateResponse {
        return perp.socket(testWsUrl, mock.parentSubaccountsChannel.subscribed, 0, null)
    }

    private fun loadSubaccountsWithRealData(): StateResponse {
        return test({
            perp.rest(
                AbUrl.fromString("$testRestUrl/v4/addresses/dydxaddress"),
                mock.parentSubaccountsChannel.rest_response,
                0,
                null,
            )
        }, null)
    }

    @BeforeTest
    private fun prepareTest() {
        reset()
        loadMarketsConfigurations()
        loadMarkets()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadSubaccountsWithChildren()
    }

    @Test
    fun testInputs() {
        testChildSubaccountNumberInput()
        testMarketInput()
        testMarginAmountAddition()
        testMarginAmountRemoval()
        testZeroAmount()
        testMarginAmountPercent()
    }

    private fun testChildSubaccountNumberInput() {
        perp.adjustIsolatedMargin(
            data = "128",
            type = AdjustIsolatedMarginInputField.ChildSubaccountNumber,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals(128, perp.internalState.input.adjustIsolatedMargin.childSubaccountNumber)
    }

    private fun testMarketInput() {
        perp.adjustIsolatedMargin(
            data = "ETH-USD",
            type = AdjustIsolatedMarginInputField.Market,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals("ETH-USD", perp.internalState.input.adjustIsolatedMargin.market)
    }

    private fun testZeroAmount() {
        perp.adjustIsolatedMargin(
            data = "0",
            type = AdjustIsolatedMarginInputField.Amount,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals(0.0, perp.internalState.input.adjustIsolatedMargin.amount)

        val subaccount = perp.internalState.wallet.account.subaccounts[0]
        assertEquals(100000.0, subaccount?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
        val subaccount1 = perp.internalState.wallet.account.subaccounts[128]
        assertEquals(500.0, subaccount1?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
    }

    private fun testMarginAmountAddition() {
        perp.adjustIsolatedMargin(
            data = IsolatedMarginAdjustmentType.Add.name,
            type = AdjustIsolatedMarginInputField.Type,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals(
            IsolatedMarginAdjustmentType.Add,
            perp.internalState.input.adjustIsolatedMargin.type,
        )
        assertEquals(
            70675.46098618512,
            perp.internalState.input.adjustIsolatedMargin.summary?.crossFreeCollateral,
        )

        perp.adjustIsolatedMargin(
            data = "92.49",
            type = AdjustIsolatedMarginInputField.Amount,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals(92.49, perp.internalState.input.adjustIsolatedMargin.amount)
        assertEquals(
            IsolatedMarginInputType.Amount,
            perp.internalState.input.adjustIsolatedMargin.amountInput,
        )
        assertEquals(
            0.0013086578949669525,
            perp.internalState.input.adjustIsolatedMargin.amountPercent,
        )

        perp.adjustIsolatedMargin(
            data = "-92.49",
            type = AdjustIsolatedMarginInputField.Amount,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals(92.49, perp.internalState.input.adjustIsolatedMargin.amount)
        assertEquals(
            IsolatedMarginInputType.Amount,
            perp.internalState.input.adjustIsolatedMargin.amountInput,
        )
        assertEquals(
            0.0013086578949669525,
            perp.internalState.input.adjustIsolatedMargin.amountPercent,
        )
    }

    private fun testMarginAmountRemoval() {
        perp.adjustIsolatedMargin(
            data = IsolatedMarginAdjustmentType.Remove.name,
            type = AdjustIsolatedMarginInputField.Type,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals(
            IsolatedMarginAdjustmentType.Remove,
            perp.internalState.input.adjustIsolatedMargin.type,
        )
        assertEquals(
            70675.46098618512,
            perp.internalState.input.adjustIsolatedMargin.summary?.crossFreeCollateral,
        )

        var subaccount = perp.internalState.wallet.account.subaccounts[0]
        assertEquals(100000.0, subaccount?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
        var subaccount1 = perp.internalState.wallet.account.subaccounts[128]
        assertEquals(500.0, subaccount1?.calculated?.get(CalculationPeriod.current)?.quoteBalance)

        perp.adjustIsolatedMargin(
            data = "20",
            type = AdjustIsolatedMarginInputField.Amount,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals(20.0, perp.internalState.input.adjustIsolatedMargin.amount)
        assertEquals(
            IsolatedMarginInputType.Amount,
            perp.internalState.input.adjustIsolatedMargin.amountInput,
        )
        assertEquals(
            0.018185629293809846,
            perp.internalState.input.adjustIsolatedMargin.amountPercent,
        )

        subaccount = perp.internalState.wallet.account.subaccounts[0]
        assertEquals(100000.0, subaccount?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
        assertEquals(100020.0, subaccount?.calculated?.get(CalculationPeriod.post)?.quoteBalance)
        subaccount1 = perp.internalState.wallet.account.subaccounts[128]
        assertEquals(500.0, subaccount1?.calculated?.get(CalculationPeriod.current)?.quoteBalance)
        assertEquals(480.0, subaccount1?.calculated?.get(CalculationPeriod.post)?.quoteBalance)
    }

    private fun testMarginAmountPercent() {
        perp.adjustIsolatedMargin(
            data = IsolatedMarginAdjustmentType.Add.name,
            type = AdjustIsolatedMarginInputField.Type,
            parentSubaccountNumber = 0,
        )

        assertEquals(InputType.ADJUST_ISOLATED_MARGIN, perp.internalState.input.currentType)
        assertEquals(
            IsolatedMarginAdjustmentType.Add,
            perp.internalState.input.adjustIsolatedMargin.type,
        )
        assertEquals(null, perp.internalState.input.adjustIsolatedMargin.amount)
        assertEquals(null, perp.internalState.input.adjustIsolatedMargin.amountPercent)

        perp.adjustIsolatedMargin(
            data = "0.1",
            type = AdjustIsolatedMarginInputField.AmountPercent,
            parentSubaccountNumber = 0,
        )

        var input = perp.internalState.input.adjustIsolatedMargin
        assertEquals(IsolatedMarginAdjustmentType.Add, input.type)
        assertEquals(0.1, input.amountPercent)
        assertEquals(IsolatedMarginInputType.Percent, input.amountInput)
        assertEquals(7067.546098618513, input.amount)

        var subaccount = perp.internalState.wallet.account.subaccounts[0]
        assertEquals(
            70675.46098618512,
            subaccount?.calculated?.get(CalculationPeriod.current)?.freeCollateral,
        )

        perp.adjustIsolatedMargin(
            data = IsolatedMarginAdjustmentType.Remove.name,
            type = AdjustIsolatedMarginInputField.Type,
            parentSubaccountNumber = 0,
        )

        input = perp.internalState.input.adjustIsolatedMargin
        assertEquals(IsolatedMarginAdjustmentType.Remove, input.type)
        assertEquals(null, input.amount)
        assertEquals(null, input.amountPercent)

        perp.adjustIsolatedMargin(
            data = "1",
            type = AdjustIsolatedMarginInputField.AmountPercent,
            parentSubaccountNumber = 0,
        )

        input = perp.internalState.input.adjustIsolatedMargin
        assertEquals(IsolatedMarginAdjustmentType.Remove, input.type)
        assertEquals(1.0, input.amountPercent)
        assertEquals(IsolatedMarginInputType.Percent, input.amountInput)
        assertEquals(1099.7694760448978, input.amount)

        subaccount = perp.internalState.wallet.account.subaccounts[128]
        assertEquals(1132.0151468, subaccount?.calculated?.get(CalculationPeriod.current)?.equity)
        assertEquals(
            632.0151467999999,
            subaccount?.calculated?.get(CalculationPeriod.current)?.notionalTotal,
        )

        perp.adjustIsolatedMargin(
            data = "0.1",
            type = AdjustIsolatedMarginInputField.AmountPercent,
            parentSubaccountNumber = 0,
        )

        input = perp.internalState.input.adjustIsolatedMargin
        assertEquals(IsolatedMarginAdjustmentType.Remove, input.type)
        assertEquals(0.1, input.amountPercent)
        assertEquals(IsolatedMarginInputType.Percent, input.amountInput)
        assertEquals(109.9769476044898, input.amount)

        subaccount = perp.internalState.wallet.account.subaccounts[128]
        assertEquals(1132.0151468, subaccount?.calculated?.get(CalculationPeriod.current)?.equity)
    }
}
