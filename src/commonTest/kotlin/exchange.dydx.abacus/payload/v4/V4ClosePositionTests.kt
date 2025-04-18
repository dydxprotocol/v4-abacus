package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.machine.ClosePositionInputField
import exchange.dydx.abacus.state.machine.closePosition
import exchange.dydx.abacus.state.manager.StatsigConfig
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class V4ClosePositionTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        var time = ServerTime.now()
        testClosePositionInput()
        time = perp.log("Close Position", time)

        testCloseShortPositionInput()
        time = perp.log("Close Position", time)

        testLimitClosePositionInput()
    }

    override fun setup() {
        perp.internalState.wallet.walletAddress = "0x1234567890"
        loadMarkets()
        loadMarketsConfigurations()
        // do not load account

        loadOrderbook()
        loadSubaccounts()
    }

    private fun testClosePositionInput() {
        /*
        Initial setup
         */
        perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)

        assertEquals(perp.internalState.input.currentType, InputType.CLOSE_POSITION)
        var closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.type, OrderType.Market)
        assertEquals(closePosition.side, OrderSide.Sell)
        assertEquals(closePosition.sizePercent, 1.0)
        assertEquals(closePosition.size?.size, 10.771)
        assertEquals(closePosition.size?.input, "size.percent")
        assertEquals(closePosition.reduceOnly, true)

        perp.closePosition("0.25", ClosePositionInputField.percent, 0)

        closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.sizePercent, 0.25)
        assertEquals(closePosition.size?.size, 2.692)
        assertEquals(closePosition.size?.input, "size.percent")
        assertEquals(closePosition.size?.usdcSize, 4453.3756)
        var summary = closePosition.summary!!
        assertEquals(summary.price, 1654.3)
        assertEquals(summary.size, 2.692)
        assertEquals(summary.usdcSize, 4453.3756)
        assertEquals(summary.total, 4453.3756)

        var subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        assertEquals(
            subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
            99872.368956,
        )
        assertEquals(
            subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
            104592.23425040001,
        )

        var position = subaccount.openPositions?.get("ETH-USD")!!
        assertEquals(position.calculated[CalculationPeriod.current]?.size, 10.771577)
        assertEquals(position.calculated[CalculationPeriod.post]?.size, 8.079577)

        perp.closePosition("9", ClosePositionInputField.size, 0)

        closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.sizePercent, null)
        assertEquals(closePosition.size?.size, 9.0)
        assertEquals(closePosition.size?.input, "size.size")
        assertEquals(closePosition.size?.usdcSize, 14888.699999999999)
        summary = closePosition.summary!!
        assertEquals(summary.price, 1654.3)
        assertEquals(summary.size, 9.0)
        assertEquals(summary.usdcSize, 14888.699999999999)
        assertEquals(summary.total, 14888.699999999999)

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        assertEquals(
            subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
            99872.368956,
        )
        assertEquals(
            subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
            115652.007756,
        )

        position = subaccount.openPositions?.get("ETH-USD")!!
        assertEquals(position.calculated[CalculationPeriod.current]?.size, 10.771577)
        assertEquals(position.calculated[CalculationPeriod.post]?.size, 1.7715770000000006)
    }

    private fun testCloseShortPositionInput() {
        perp.socket(mock.socketUrl, mock.accountsChannel.v4_subscribed, 0, null)

        /*
        Initial setup
         */

        perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)

        assertEquals(perp.internalState.input.currentType, InputType.CLOSE_POSITION)
        var closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.type, OrderType.Market)
        assertEquals(closePosition.side, OrderSide.Buy)
        assertEquals(closePosition.sizePercent, 1.0)
        assertEquals(closePosition.size?.size, 106.179)
        assertEquals(closePosition.size?.input, "size.percent")
        assertEquals(closePosition.reduceOnly, true)

        perp.closePosition("0.25", ClosePositionInputField.percent, 0)

        closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.sizePercent, 0.25)
        assertEquals(closePosition.size?.size, 2.6544E+1)
        assertEquals(closePosition.size?.input, "size.percent")

        var subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        assertEquals(
            subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
            68257.215192,
        )
        assertEquals(
            subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
            24308.314392,
        )

        var position = subaccount.openPositions?.get("ETH-USD")!!
        assertEquals(position.calculated[CalculationPeriod.current]?.size, -106.17985)
        assertEquals(position.calculated[CalculationPeriod.post]?.size, -79.63585)

        perp.closePosition("15", ClosePositionInputField.size, 0)

        closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.sizePercent, null)
        assertEquals(closePosition.size?.size, 15.0)
        assertEquals(closePosition.size?.input, "size.size")
        assertEquals(closePosition.size?.usdcSize, 24835.5)

        val summary = closePosition.summary!!
        assertEquals(summary.price, 1655.7)
        assertEquals(summary.size, 15.0)
        assertEquals(summary.usdcSize, 24835.5)
        assertEquals(summary.total, -24835.5)

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        assertEquals(
            subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
            68257.215192,
        )
        assertEquals(
            subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
            43421.715192,
        )

        position = subaccount.openPositions?.get("ETH-USD")!!
        assertEquals(position.calculated[CalculationPeriod.current]?.size, -106.17985)
        assertEquals(position.calculated[CalculationPeriod.post]?.size, -91.17985)
    }

    private fun testLimitClosePositionInput() {
        StatsigConfig.ff_enable_limit_close = true

        perp.socket(mock.socketUrl, mock.accountsChannel.v4_subscribed, 0, null)

        /*
        Initial setup
         */

        perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)

        assertEquals(perp.internalState.input.currentType, InputType.CLOSE_POSITION)
        var closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.type, OrderType.Market)
        assertEquals(closePosition.side, OrderSide.Buy)
        assertEquals(closePosition.sizePercent, 1.0)
        assertEquals(closePosition.size?.size, 106.179)
        assertEquals(closePosition.size?.input, "size.percent")
        assertEquals(closePosition.reduceOnly, true)

        perp.closePosition("true", ClosePositionInputField.useLimit, 0)

        closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.type, OrderType.Limit)
        assertEquals(closePosition.side, OrderSide.Buy)
        assertEquals(closePosition.sizePercent, 1.0)
        assertEquals(closePosition.size?.size, 106.179)
        assertEquals(closePosition.size?.input, "size.percent")
        assertEquals(closePosition.price?.limitPrice, 1655.0)
        assertEquals(closePosition.reduceOnly, true)

        perp.closePosition("2500", ClosePositionInputField.limitPrice, 0)

        closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.price?.limitPrice, 2500.0)

        perp.closePosition("false", ClosePositionInputField.useLimit, 0)

        closePosition = perp.internalState.input.closePosition
        assertEquals(closePosition.type, OrderType.Market)
        assertEquals(closePosition.side, OrderSide.Buy)
        assertEquals(closePosition.sizePercent, 1.0)
        assertEquals(closePosition.size?.size, 106.179)
        assertEquals(closePosition.size?.input, "size.percent")
        assertEquals(closePosition.price?.limitPrice, 2500.0)
        assertEquals(closePosition.reduceOnly, true)
    }
}
