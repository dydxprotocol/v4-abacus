package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.machine.ClosePositionInputField
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.closePosition
import exchange.dydx.abacus.state.machine.trade
import exchange.dydx.abacus.state.machine.tradeInMarket
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import exchange.dydx.abacus.tests.extensions.socket
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IsolatedMarginModeTests : V4BaseTests(true) {

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    override fun loadOrderbook(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.orderbookChannel.subscribed_ape, 0, null)
        }, null)
    }

    @BeforeTest
    private fun prepareToTest() {
        reset()
        loadMarketsConfigurations()
        loadMarkets()
        loadOrderbook()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
    }

    private fun testParentSubaccountSubscribedWithPendingPositions() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.parentSubaccountsChannel.real_subscribed_with_pending,
            subaccountNumber = 0,
            height = null,
        )

        val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        val calculated = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(2021.402434402, calculated?.equity)
        assertEquals(1711.959192, calculated?.freeCollateral)
        assertEquals(1711.959192, calculated?.quoteBalance)

        val pendingPosition = subaccount?.pendingPositions?.firstOrNull()
        assertEquals("ARB", pendingPosition?.assetId)
        assertEquals("d1deed71-d743-5528-aff2-cf3daf8b6413", pendingPosition?.firstOrderId)
        val positionCalculated = pendingPosition?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculated?.equity)
        assertEquals(20.0, positionCalculated?.freeCollateral)
        assertEquals(20.0, positionCalculated?.quoteBalance)
    }

    private fun testParentSubaccountSubscribedWithUnpopulatedChild() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.parentSubaccountsChannel.real_subscribed_with_unpopulated_child,
            subaccountNumber = 0,
            height = null,
        )

        val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        val calculated = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(1979.850249, calculated?.equity)
        assertEquals(1711.959192, calculated?.freeCollateral)
        assertEquals(1711.959192, calculated?.quoteBalance)

        val pendingPosition = subaccount?.pendingPositions?.firstOrNull()
        assertEquals("ARB", pendingPosition?.assetId)
        assertEquals("d1deed71-d743-5528-aff2-cf3daf8b6413", pendingPosition?.firstOrderId)
        val positionCalculated = pendingPosition?.calculated?.get(CalculationPeriod.current)
        assertEquals(267.891057, positionCalculated?.equity)
        assertEquals(267.891057, positionCalculated?.freeCollateral)
        assertEquals(267.891057, positionCalculated?.quoteBalance)
    }

    @Test
    fun testMarginMode() {
        testDefaultTargetLeverage()
        testMarginModeOnMarketChange()
        testMarginAmountForSubaccountTransfer()
    }

    @Test
    fun testMarginModeWithExistingPosition() {
        testMarginAmountForSubaccountTransferWithExistingPosition()
        testMarginAmountForSubaccountTransferWithExistingPositionAndOpenOrders()
    }

    private fun testDefaultTargetLeverage() {
        perp.tradeInMarket("NEAR-USD", 0)

        val input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        val trade = input.trade
        assertEquals("NEAR-USD", trade.marketId)
        assertEquals(MarginMode.Cross, trade.marginMode)
        assertEquals(true, trade.options.needsMarginMode)
        assertEquals(2.0, trade.targetLeverage)
    }

    // MarginMode should automatically to match the current market based on a variety of factors
    private fun testMarginModeOnMarketChange() {
        testParentSubaccountSubscribedWithPendingPositions()

        // needsMarginMode should be false to prevent user from changing margin mode
        // Attaching to V4ParentSubaccountTests to test the tradeInMarket function with a subaccount that has a pending position

        perp.tradeInMarket("LDO-USD", 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        var trade = input.trade
        assertEquals("LDO-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(false, trade.options.needsMarginMode)
        assertEquals(null, trade.options.marginModeOptions)

        // Test the placeholder openPosition's equity
        perp.tradeInMarket("APE-USD", 0)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Cross, trade.marginMode)
        assertEquals(true, trade.options.needsMarginMode)

        // Test dummy market with perpetualMarketType ISOLATED
        perp.tradeInMarket("ISO-USD", 0)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        trade = input.trade
        assertEquals("ISO-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(false, trade.options.needsMarginMode)
    }

    private fun testMarginAmountForSubaccountTransferWithExistingPosition() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.parentSubaccountsChannel.real_subscribe_with_isolated_position,
            subaccountNumber = 0,
            height = null,
        )

        var subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        val calculated = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(137.128721, calculated?.freeCollateral)

        var openPosition = subaccount?.openPositions?.get("APE-USD")
        val positionCalculated = openPosition?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculated?.size)
        // IndexerPerpetualPositionResponseObject does not have equity
        // assertEquals(25.2, positionCalculated?.equity)

        // close all existing position should transfer out + subaccount more free collateral
        perp.closePosition("APE-USD", ClosePositionInputField.market, 0)

        perp.closePosition("1", ClosePositionInputField.percent, 0)

        subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        var calculatedCurrent = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(137.128721, calculatedCurrent?.freeCollateral)
        var calculatedPostOrder = subaccount?.calculated?.get(CalculationPeriod.post)
        assertEquals(157.11943100000002, calculatedPostOrder?.freeCollateral)

        openPosition = subaccount?.openPositions?.get("APE-USD")
        var positionCalculatedCurrent = openPosition?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculatedCurrent?.size)
        var positionCalculatedPostOrder = openPosition?.calculated?.get(CalculationPeriod.post)
        assertEquals(0.0, positionCalculatedPostOrder?.size)

        // trade that will reduce existing position by 10 via trade
        perp.tradeInMarket("APE-USD", 0)
        perp.trade("SELL", TradeInputField.side, 0)
        perp.trade("1", TradeInputField.limitPrice, 0)
        perp.trade("10", TradeInputField.size, 0)

        perp.trade("1", TradeInputField.targetLeverage, 0)

        subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        calculatedCurrent = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(137.128721, calculatedCurrent?.freeCollateral)
        var position = subaccount?.openPositions?.get("APE-USD")
        positionCalculatedCurrent = position?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculatedCurrent?.size)
        positionCalculatedPostOrder = position?.calculated?.get(CalculationPeriod.post)
        assertEquals(10.0, positionCalculatedPostOrder?.size)

        var input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        var trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(1.0, trade.targetLeverage)
        assertEquals(1.0, trade.summary?.price)
        assertEquals(10.0, trade.summary?.size)

        // input a trade that will flip position absolute net size +10
        perp.trade("50", TradeInputField.size, 0)

        perp.trade("2", TradeInputField.targetLeverage, 0)

        subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        calculatedCurrent = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(137.128721, calculatedCurrent?.freeCollateral)
        calculatedPostOrder = subaccount?.calculated?.get(CalculationPeriod.post)
        assertEquals(128.22092409, calculatedPostOrder?.freeCollateral)

        position = subaccount?.openPositions?.get("APE-USD")
        positionCalculatedCurrent = position?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculatedCurrent?.size)
        positionCalculatedPostOrder = position?.calculated?.get(CalculationPeriod.post)
        assertEquals(-30.0, positionCalculatedPostOrder?.size)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(2.0, trade.targetLeverage)
        assertEquals(1.0, trade.summary?.price)
        assertEquals(50.0, trade.summary?.size)
    }

    private fun testMarginAmountForSubaccountTransferWithExistingPositionAndOpenOrders() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.parentSubaccountsChannel.real_subscribe_with_isolated_position_and_open_orders,
            subaccountNumber = 0,
            height = null,
        )

        var subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        var calculated = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(137.128721, calculated?.freeCollateral)

        var openPosition = subaccount?.openPositions?.get("APE-USD")
        var positionCalculated = openPosition?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculated?.size)

        val order =
            subaccount?.orders?.firstOrNull { it.id == "bbc7cfe6-8837-5c46-94c4-36a4319231ac" }
        assertEquals(OrderSide.Buy, order?.side)
        assertEquals("APE-USD", order?.marketId)

        // close all existing position
        perp.closePosition("APE-USD", ClosePositionInputField.market, 0)

        perp.closePosition("1", ClosePositionInputField.percent, 0)

        subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        var calculatedCurrent = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(137.128721, calculatedCurrent?.freeCollateral)

        openPosition = subaccount?.openPositions?.get("APE-USD")
        var positionCalculatedCurrent = openPosition?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculatedCurrent?.size)
        var positionCalculatedPostOrder = openPosition?.calculated?.get(CalculationPeriod.post)
        assertEquals(0.0, positionCalculatedPostOrder?.size)

        var input = perp.internalState.input
        assertEquals(InputType.CLOSE_POSITION, input.currentType)
        val closePosition = input.closePosition
        assertEquals("APE-USD", closePosition.marketId)
        assertEquals(MarginMode.Isolated, closePosition.marginMode)
        assertEquals(1.0003686346164424, closePosition.targetLeverage)
        assertEquals(20.0, closePosition.size?.size)

        // trade that will reduce existing position by 10 via trade
        perp.tradeInMarket("APE-USD", 0)
        perp.trade("SELL", TradeInputField.side, 0)
        perp.trade("1", TradeInputField.limitPrice, 0)
        perp.trade("10", TradeInputField.size, 0)

        perp.trade("1", TradeInputField.targetLeverage, 0)

        subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        calculatedCurrent = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(137.128721, calculatedCurrent?.freeCollateral)

        var position = subaccount?.openPositions?.get("APE-USD")
        positionCalculatedCurrent = position?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculatedCurrent?.size)
        positionCalculatedPostOrder = position?.calculated?.get(CalculationPeriod.post)
        assertEquals(10.0, positionCalculatedPostOrder?.size)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        var trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(1.0, trade.targetLeverage)
        assertEquals(1.0, trade.summary?.price)
        assertEquals(10.0, trade.summary?.size)

        // input a trade that will flip position absolute net size +10
        perp.trade("50", TradeInputField.size, 0)

        perp.trade("2", TradeInputField.targetLeverage, 0)

        subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        calculatedCurrent = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(137.128721, calculatedCurrent?.freeCollateral)
        val calculatedPostOrder = subaccount?.calculated?.get(CalculationPeriod.post)
        assertEquals(128.22092409, calculatedPostOrder?.freeCollateral)

        position = subaccount?.openPositions?.get("APE-USD")
        positionCalculatedCurrent = position?.calculated?.get(CalculationPeriod.current)
        assertEquals(20.0, positionCalculatedCurrent?.size)
        positionCalculatedPostOrder = position?.calculated?.get(CalculationPeriod.post)
        assertEquals(-30.0, positionCalculatedPostOrder?.size)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(2.0, trade.targetLeverage)
        assertEquals(1.0, trade.summary?.price)
        assertEquals(50.0, trade.summary?.size)
    }

    // Test the margin amount for subaccount transfer
    private fun testMarginAmountForSubaccountTransfer() {
        testParentSubaccountSubscribedWithPendingPositions()

        perp.tradeInMarket("APE-USD", 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        var trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Cross, trade.marginMode)
        assertEquals(true, trade.options.needsMarginMode)

        perp.trade("ISOLATED", TradeInputField.marginMode, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(true, trade.options.needsMarginMode)

        perp.trade("20", TradeInputField.usdcSize, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(true, trade.options.needsMarginMode)

        perp.trade("2", TradeInputField.limitPrice, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(true, trade.options.needsMarginMode)
        assertEquals(2.0, trade.price?.limitPrice)
        assertEquals(20.0, trade.size?.usdcSize)

        perp.trade("2", TradeInputField.targetLeverage, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRADE, input.currentType)
        trade = input.trade
        assertEquals("APE-USD", trade.marketId)
        assertEquals(MarginMode.Isolated, trade.marginMode)
        assertEquals(true, trade.options.needsMarginMode)
        assertEquals(2.0, trade.targetLeverage)
        assertEquals(20.0, trade.size?.usdcSize)
        assertEquals(2.0, trade.price?.limitPrice)

        val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]
        val position = subaccount?.openPositions?.get("APE-USD")
        val positionCalculated = position?.calculated?.get(CalculationPeriod.post)
        // TODO
    }

    // Test getChildSubaccountNumberForIsolatedMarginTrade when subaccount 256 has a pending position but 128 does not
    @Test
    fun testGetChildSubaccountNumberForIsolatedMarginTrade() {
        testParentSubaccountSubscribedWithUnpopulatedChild()

        val childSubaccountNumber = MarginCalculator.getChildSubaccountNumberForIsolatedMarginTrade(
            parser = parser,
            subaccounts = perp.internalState.wallet.account.subaccounts,
            subaccountNumber = 0,
            marketId = "APE-USD",
        )
        assertEquals(childSubaccountNumber, 128)
    }

    @Test
    fun testGetTransferAmountFromTargetLeverage() {
        assertEquals(
            116.26514285714283,
            MarginCalculator.getTransferAmountFromTargetLeverage(
                price = 0.1465,
                oraclePrice = 0.1211,
                side = "BUY",
                size = 2320.0, // ~$400 usdcSize
                targetLeverage = 4.9,
            ),
            "Significant orderbook drift should result in $116.27 transfer amount instead of $80",
        )

        assertEquals(
            67.976,
            MarginCalculator.getTransferAmountFromTargetLeverage(
                price = 0.1465,
                oraclePrice = 0.1211,
                side = "SELL",
                size = 2320.0, // ~$400 usdcSize
                targetLeverage = 5.0,
            ),
            "A sell when there is significant orderbook drift should result in $67.976 transfer amount which is the naive (askPrice * size) / targetLeverage",
        )
    }
}
