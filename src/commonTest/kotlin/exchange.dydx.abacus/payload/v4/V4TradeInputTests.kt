package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.ErrorFormat
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.trade
import exchange.dydx.abacus.state.machine.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadv4Accounts
import exchange.dydx.abacus.tests.extensions.socket
import kollections.iListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

open class V4TradeInputTests : V4BaseTests() {
    @Test
    fun testLimit() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testOnce()

        reset()

        print("--------Second round----------\n")

        testOnce()

        testAdjustedMarginFraction()

        testConditional()

        testReduceOnly()
        testExecution()
    }

    override fun setup() {
        super.setup()
        loadOrderbook()

        // connect wallet
        perp.internalState.wallet.walletAddress = "0x1234567890"
    }

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.v4_subscribed_r1, 0, null)
        }, null)
    }

    override fun loadSubaccounts(): StateResponse {
        return test({
            perp.loadv4Accounts(mock, "$testRestUrl/v4/addresses/cosmo")
        }, null)
    }

    private fun testOnce() {
        testIsolatedLimitTradeInputOnce()

        testMarketTradeInputOnce()
        testLimitTradeInputOnce()
        testUpdates()
    }

    private fun testLimitTradeInputOnce() {
        perp.tradeInMarket("ETH-USD", 0)
        var trade = perp.internalState.input.trade
        assertEquals(trade.marketId, "ETH-USD")

        perp.trade("LIMIT", TradeInputField.type, 0)
        val size = perp.internalState.input.trade.size
        assertNotNull(size)
        assertEquals(size.input, "size.size")

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("0.2", TradeInputField.size, 0)
        }, null)

        test({
            perp.trade("1500", TradeInputField.limitPrice, 0)
        }, null)

        perp.trade("1", TradeInputField.limitPrice, 0)

        var account = perp.internalState.wallet.account
        var subaccount = account.subaccounts[0]
        var openPosition = subaccount?.openPositions?.get("ETH-USD")
        assertEquals(subaccount?.calculated?.get(CalculationPeriod.current)?.equity, 100000.0)
        assertEquals(subaccount?.calculated?.get(CalculationPeriod.post)?.equity, 100299.8)
        assertEquals(openPosition?.calculated?.get(CalculationPeriod.current)?.valueTotal, 0.0)
        assertEquals(openPosition?.calculated?.get(CalculationPeriod.post)?.valueTotal, 300.0)

        perp.trade("10000", TradeInputField.limitPrice, 0)

        account = perp.internalState.wallet.account
        subaccount = account.subaccounts[0]
        openPosition = subaccount?.openPositions?.get("ETH-USD")
        assertEquals(subaccount?.calculated?.get(CalculationPeriod.current)?.equity, 100000.0)
        assertEquals(subaccount?.calculated?.get(CalculationPeriod.post)?.equity, 100000.0)
        assertEquals(openPosition?.calculated?.get(CalculationPeriod.current)?.valueTotal, 0.0)
        assertEquals(openPosition?.calculated?.get(CalculationPeriod.post)?.valueTotal, 300.0)

        perp.trade("SELL", TradeInputField.side, 0)

        account = perp.internalState.wallet.account
        subaccount = account.subaccounts[0]
        openPosition = subaccount?.openPositions?.get("ETH-USD")
        assertEquals(subaccount?.calculated?.get(CalculationPeriod.current)?.equity, 100000.0)
        assertEquals(subaccount?.calculated?.get(CalculationPeriod.post)?.equity, 101700.0)
        assertEquals(openPosition?.calculated?.get(CalculationPeriod.current)?.valueTotal, 0.0)
        assertEquals(openPosition?.calculated?.get(CalculationPeriod.post)?.valueTotal, -300.0)

        trade = perp.internalState.input.trade
        assertEquals(trade.side, OrderSide.Sell)
        assertEquals(trade.marketId, "ETH-USD")
        assertEquals(trade.size?.size, 0.2)
        assertEquals(trade.price?.limitPrice, 10000.0)

        assertEquals(
            perp.internalState.input.receiptLines,
            listOf(
                ReceiptLine.LiquidationPrice,
                ReceiptLine.PositionMargin,
                ReceiptLine.PositionLeverage,
                ReceiptLine.Fee,
                ReceiptLine.Reward,
            ),
        )

        perp.trade("1", TradeInputField.limitPrice, 0)

        account = perp.internalState.wallet.account
        subaccount = account.subaccounts[0]
        openPosition = subaccount?.openPositions?.get("ETH-USD")
        assertEquals(subaccount?.calculated?.get(CalculationPeriod.current)?.equity, 100000.0)
        assertEquals(subaccount?.calculated?.get(CalculationPeriod.post)?.equity, 100000.0)
        assertEquals(openPosition?.calculated?.get(CalculationPeriod.current)?.valueTotal, 0.0)
        assertEquals(openPosition?.calculated?.get(CalculationPeriod.post)?.valueTotal, -300.0)

        test({
            perp.trade("1500", TradeInputField.limitPrice, 0)
        }, null)

        perp.tradeInMarket("BTC-USD", 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.side, OrderSide.Sell)
        assertEquals(trade.marketId, "BTC-USD")
        assertEquals(trade.size, null)
        assertEquals(trade.price, null)

        test({
            perp.trade("10000", TradeInputField.limitPrice, 0)
        }, null)

        test({
            perp.trade("0.1", TradeInputField.size, 0)
        }, null)

        perp.trade("190", TradeInputField.goodTilDuration, 0)

        val errors = perp.internalState.input.errors
        assertNotNull(errors)
        val error = errors[0]
        assertEquals(error.type, ErrorType.error)
        assertEquals(error.code, "INVALID_GOOD_TIL")
    }

    private fun testMarketTradeInputOnce() {
        test({
            perp.trade("CROSS", TradeInputField.marginMode, 0)
        }, null)

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        perp.tradeInMarket("LTC-USD", 0)

        perp.trade("1", TradeInputField.size, 0)
        var errors = perp.internalState.input.errors
        assertNotNull(errors)
        var error = errors[0]
        assertEquals(error.type, ErrorType.error)
        assertEquals(error.code, "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY")
        assertEquals(error.fields, iListOf("size.size"))

        perp.tradeInMarket("ETH-USD", 0)
        perp.trade("SELL", TradeInputField.side, 0)
        perp.trade(null, TradeInputField.usdcSize, 0)

        perp.trade("0.5", TradeInputField.usdcSize, 0)

        var size = perp.internalState.input.trade.size
        assertNotNull(size)
        assertEquals(size.usdcSize, 0.5)
        assertEquals(size.size, 0.0)

        errors = perp.internalState.input.errors
        assertNotNull(errors)
        error = errors[0]
        assertEquals(error.type, ErrorType.error)
        assertEquals(error.code, "ORDER_SIZE_BELOW_MIN_SIZE")

        perp.trade(null, TradeInputField.usdcSize, 0)
        errors = perp.internalState.input.errors
        assertNotNull(errors)
        error = errors[0]
        assertEquals(error.type, ErrorType.required)
        assertEquals(error.code, "REQUIRED_SIZE")

        perp.trade("10", TradeInputField.usdcSize, 0)
        size = perp.internalState.input.trade.size
        assertNotNull(size)
        assertEquals(10.0, size.usdcSize)
        assertEquals(0.006, size.size)
        assertEquals(0.0000049629, size.balancePercent) // freeCollateral: 100000, 20x leverage
        assertEquals("size.usdcSize", size.input)
        errors = perp.internalState.input.errors
        assertEquals(0, errors?.size)

        perp.trade("10", TradeInputField.size, 0)
        size = perp.internalState.input.trade.size
        assertNotNull(size)
        assertEquals(16543.0, size.usdcSize)
        assertEquals(10.0, size.size)
        assertEquals(0.0082715, size.balancePercent) // freeCollateral: 100000, 20x leverage
        assertEquals("size.size", size.input)
        errors = perp.internalState.input.errors
        assertEquals(0, errors?.size)

        perp.trade("0.5", TradeInputField.balancePercent, 0)
        size = perp.internalState.input.trade.size
        assertNotNull(size)
        assertEquals(1000000.1169, size.usdcSize)
        assertEquals(605.7059999999999, size.size)
        assertEquals(0.5, size.balancePercent) // freeCollateral: 100000, 20x leverage
        assertEquals("size.balancePercent", size.input)
        errors = perp.internalState.input.errors
        assertEquals(0, errors?.size)
    }

    private fun testIsolatedLimitTradeInputOnce() {
        perp.tradeInMarket("BTC-USD", 0)

        val trade = perp.internalState.input.trade
        assertEquals(trade.marketId, "BTC-USD")

        test({
            perp.trade("ISOLATED", TradeInputField.marginMode, 0)
        }, null)

        test({
            perp.trade("LIMIT", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("12", TradeInputField.goodTilDuration, 0)
        }, null)

        test({
            perp.trade("380", TradeInputField.usdcSize, 0)
        }, null)

        test({
            perp.trade("20", TradeInputField.targetLeverage, 0)
        }, null)

        perp.trade("1500", TradeInputField.limitPrice, 0)

        var subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        assertEquals(100000.0, subaccount.calculated[CalculationPeriod.current]?.equity)
        assertEquals(99980.61224492347, subaccount.calculated[CalculationPeriod.post]?.equity)

        var error = perp.internalState.input.errors?.first()
        assertEquals(ErrorType.error, error?.type)
        assertEquals("ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM", error?.code)
        assertEquals(iListOf("size.size"), error?.fields)
        assertEquals("APP.GENERAL.LEARN_MORE_ARROW", error?.linkText)
        assertEquals(
            "https://help.dydx.trade/en/articles/171918-equity-tiers-and-rate-limits",
            error?.link,
        )
        assertEquals(
            "ERRORS.TRADE_BOX_TITLE.ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM",
            error?.resources?.title?.stringKey,
        )
        assertEquals(
            "ERRORS.TRADE_BOX.ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM",
            error?.resources?.text?.stringKey,
        )
        val param = error?.resources?.text?.params?.first()!!
        assertEquals("20.0", param.value)
        assertEquals(ErrorFormat.UsdcPrice, param.format)
        assertEquals("MIN_VALUE", param.key)
        assertEquals("APP.TRADE.MODIFY_SIZE_FIELD", error?.resources?.action?.stringKey)

        perp.trade("1", TradeInputField.size, 0)

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        assertEquals(100000.0, subaccount.calculated[CalculationPeriod.current]?.equity)
        assertEquals(99923.4693877551, subaccount.calculated[CalculationPeriod.post]?.equity)

        perp.trade("0", TradeInputField.size, 0)

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        assertEquals(100000.0, subaccount.calculated[CalculationPeriod.current]?.equity)
        assertEquals(null, subaccount.calculated[CalculationPeriod.post]?.equity)

        error = perp.internalState.input.errors?.first()
        assertEquals(ErrorType.required, error?.type)
        assertEquals("REQUIRED_SIZE", error?.code)
        assertEquals(iListOf("size.size"), error?.fields)
        assertEquals("APP.TRADE.ENTER_AMOUNT", error?.resources?.action?.stringKey)
    }

    private fun testUpdates() {
        perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_1, 0, null)
        var account = perp.internalState.wallet.account
        var subaccount = account.subaccounts[0]
        var calculated = subaccount?.calculated
        assertEquals(4185.625704, calculated?.get(CalculationPeriod.current)?.equity)
        assertEquals(7250.506704, calculated?.get(CalculationPeriod.current)?.quoteBalance)
        var position = subaccount?.openPositions?.get("ETH-USD")
        assertEquals(-2.043254, position?.calculated?.get(CalculationPeriod.current)?.size)

        perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_2, 0, null)

        account = perp.internalState.wallet.account
        subaccount = account.subaccounts[0]
        val calculated2 = subaccount?.calculated?.get(CalculationPeriod.current)
        assertEquals(4272.436277000001, calculated2?.equity)
        assertEquals(8772.436277, calculated2?.quoteBalance)
        position = subaccount?.openPositions?.get("ETH-USD")
        assertEquals(-3.0, position?.calculated?.get(CalculationPeriod.current)?.size)

        perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_3, 0, null)

        account = perp.internalState.wallet.account
        subaccount = account.subaccounts[0]
        var order = subaccount?.orders?.firstOrNull()
        assertEquals(order?.status, OrderStatus.PartiallyFilled)

        perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_4, 0, null)

        account = perp.internalState.wallet.account
        subaccount = account.subaccounts[0]
        order = subaccount?.orders?.firstOrNull()
        assertEquals(order?.status, OrderStatus.Filled)

        perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_5, 0, null)

        account = perp.internalState.wallet.account
        subaccount = account.subaccounts[0]
        order = subaccount?.orders?.firstOrNull()
        assertEquals(order?.status, OrderStatus.Filled)

        perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_6, 0, null)

        account = perp.internalState.wallet.account
        subaccount = account.subaccounts[0]
        order = subaccount?.orders?.firstOrNull()
        assertEquals(OrderStatus.Filled, order?.status)
    }

    private fun testAdjustedMarginFraction() {
        perp.socket(
            url = mock.socketUrl,
            jsonString = mock.marketsChannel.v4_subscribed_for_adjusted_mf_calculation,
            subaccountNumber = 0,
            height = null,
        )

        val account = perp.internalState.wallet.account
        val subaccount = account.subaccounts[0]
        val ethPosition = subaccount?.openPositions?.get("ETH-USD")
        assertEquals(0.05, ethPosition?.calculated?.get(CalculationPeriod.current)?.adjustedImf)
        assertEquals(0.05, ethPosition?.calculated?.get(CalculationPeriod.post)?.adjustedImf)
        assertEquals(0.03, ethPosition?.calculated?.get(CalculationPeriod.current)?.adjustedMmf)
        assertEquals(0.03, ethPosition?.calculated?.get(CalculationPeriod.post)?.adjustedMmf)
        assertEquals(2838.976141423949, ethPosition?.calculated?.get(CalculationPeriod.current)?.liquidationPrice)
        assertEquals(2829.267403559871, ethPosition?.calculated?.get(CalculationPeriod.post)?.liquidationPrice)
        val btcPosition = subaccount?.openPositions?.get("BTC-USD")
        assertEquals(64878.02210679612, btcPosition?.calculated?.get(CalculationPeriod.post)?.liquidationPrice)
    }

    fun testConditional() {
        test({
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("CROSS", TradeInputField.marginMode, 0)
        }, null)

        test({
            perp.trade("12", TradeInputField.goodTilDuration, 0)
        }, null)

        test({
            perp.trade("D", TradeInputField.goodTilUnit, 0)
        }, null)

        test({
            perp.trade("900.0", TradeInputField.limitPrice, 0)
        }, null)

        perp.trade("1000.0", TradeInputField.triggerPrice, 0)

        var trade = perp.internalState.input.trade
        assertEquals(1000.0, trade.price?.triggerPrice)
        assertEquals(900.0, trade.price?.limitPrice)

        perp.tradeInMarket("ETH-USD", 0)
        perp.trade("0.1", TradeInputField.size, 0)
        perp.trade("1000", TradeInputField.triggerPrice, 0)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(949.5770392749245, trade.summary?.payloadPrice)

        perp.trade("2000.0", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(2000.0, trade.price?.triggerPrice)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(1899.154078549849, trade.summary?.payloadPrice)
    }

    fun testReduceOnly() {
        perp.trade("MARKET", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Market)
        assertEquals(trade.options.needsReduceOnly, true)
        assertEquals(trade.options.needsPostOnly, false)
        assertEquals(trade.options.reduceOnlyTooltip, null)
        assertEquals(trade.options.postOnlyTooltip, null)

        perp.trade("LIMIT", TradeInputField.type, 0)

        perp.trade("GTT", TradeInputField.timeInForceType, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.Limit, trade.type)
        assertEquals(false, trade.options.needsReduceOnly)
        assertEquals(true, trade.options.needsPostOnly)
        assertEquals(null, trade.options.postOnlyTooltip)

        perp.trade("IOC", TradeInputField.timeInForceType, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.Limit, trade.type)
        assertEquals(true, trade.options.needsReduceOnly)
        assertEquals(false, trade.options.needsPostOnly)
        assertEquals(null, trade.options.reduceOnlyTooltip)
        assertEquals(
            "GENERAL.TRADE.POST_ONLY_TIMEINFORCE_GTT.BODY",
            trade.options.postOnlyTooltip?.bodyStringKey,
        )

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        perp.trade("DEFAULT", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopLimit, trade.type)
        assertEquals(false, trade.options.needsReduceOnly)
        assertEquals(false, trade.options.needsPostOnly)
        assertEquals(null, trade.options.postOnlyTooltip)

        perp.trade("IOC", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopLimit, trade.type)
        assertEquals(true, trade.options.needsReduceOnly)
        assertEquals(false, trade.options.needsPostOnly)
        assertEquals(null, trade.options.reduceOnlyTooltip)
        assertEquals(null, trade.options.postOnlyTooltip)

        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopLimit)
        assertEquals(trade.options.needsReduceOnly, false)
        assertEquals(trade.options.needsPostOnly, false)
        assertEquals(trade.options.postOnlyTooltip, null)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        perp.trade("DEFAULT", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitLimit, trade.type)
        assertEquals(false, trade.options.needsReduceOnly)
        assertEquals(false, trade.options.needsPostOnly)
        assertEquals(null, trade.options.postOnlyTooltip)

        perp.trade("IOC", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitLimit, trade.type)
        assertEquals(true, trade.options.needsReduceOnly)
        assertEquals(false, trade.options.needsPostOnly)
        assertEquals(null, trade.options.reduceOnlyTooltip)
        assertEquals(null, trade.options.postOnlyTooltip)

        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitLimit, trade.type)
        assertEquals(false, trade.options.needsReduceOnly)
        assertEquals(false, trade.options.needsPostOnly)
        assertEquals(null, trade.options.postOnlyTooltip)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        perp.trade("IOC", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopMarket, trade.type)
        assertEquals(true, trade.options.needsReduceOnly)
        assertEquals(false, trade.options.needsPostOnly)
        assertEquals(null, trade.options.reduceOnlyTooltip)
        assertEquals(null, trade.options.postOnlyTooltip)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        perp.trade("IOC", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitMarket)
        assertEquals(trade.options.needsReduceOnly, true)
        assertEquals(trade.options.needsPostOnly, false)
        assertEquals(trade.options.reduceOnlyTooltip, null)
        assertEquals(trade.options.postOnlyTooltip, null)
    }

    private fun testExecution() {
        testExecutionStopLimit()
        testExecutionStopMarket()
        testExecutionTakeProfit()
        testExecutionTakeProfitMarket()
    }

    private fun testExecutionStopLimit() {
        testExecutionStopLimitToStopMarket()
        testExecutionStopLimitToTakeProfit()
        testExecutionStopLimitToTakeProfitMarket()
    }

    private fun testExecutionStopLimitToStopMarket() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(OrderType.StopMarket, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopMarket, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopMarket, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionStopLimitToTakeProfit() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitLimit, trade.type)
        assertEquals("DEFAULT", trade.execution)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitLimit, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitLimit, trade.type)
        assertEquals("POST_ONLY", trade.execution)
    }

    private fun testExecutionStopLimitToTakeProfitMarket() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitMarket, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitMarket, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitMarket, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionStopMarket() {
        testExecutionStopMarketToStopLimit()
        testExecutionStopMarketToTakeProfit()
        testExecutionStopMarketToTakeProfitMarket()
    }

    private fun testExecutionStopMarketToStopLimit() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        val trade = perp.internalState.input.trade
        assertEquals(OrderType.StopLimit, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionStopMarketToTakeProfit() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        val trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitLimit, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionStopMarketToTakeProfitMarket() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        val trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitMarket, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionTakeProfit() {
        testExecutionTakeProfitToStopMarket()
        testExecutionTakeProfitToStopLimit()
        testExecutionTakeProfitToTakeProfitMarket()
    }

    private fun testExecutionTakeProfitToStopMarket() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(OrderType.StopMarket, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopMarket, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopMarket, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionTakeProfitToStopLimit() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(OrderType.StopLimit, trade.type)
        assertEquals("DEFAULT", trade.execution)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopLimit, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.StopLimit, trade.type)
        assertEquals("POST_ONLY", trade.execution)
    }

    private fun testExecutionTakeProfitToTakeProfitMarket() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitMarket, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitMarket, trade.type)
        assertEquals("IOC", trade.execution)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitMarket, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionTakeProfitMarket() {
        testExecutionTakeProfitMarketToStopLimit()
        testExecutionTakeProfitMarketToTakeProfit()
        testExecutionTakeProfitMarketToStopMarket()
    }

    private fun testExecutionTakeProfitMarketToStopLimit() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        val trade = perp.internalState.input.trade
        assertEquals(OrderType.StopLimit, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionTakeProfitMarketToTakeProfit() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        val trade = perp.internalState.input.trade
        assertEquals(OrderType.TakeProfitLimit, trade.type)
        assertEquals("IOC", trade.execution)
    }

    private fun testExecutionTakeProfitMarketToStopMarket() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        val trade = perp.internalState.input.trade
        assertEquals(OrderType.StopMarket, trade.type)
        assertEquals("IOC", trade.execution)
    }
}
