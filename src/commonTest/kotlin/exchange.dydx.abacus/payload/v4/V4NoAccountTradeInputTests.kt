package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.trade
import exchange.dydx.abacus.state.machine.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadOrderbook
import kotlin.test.Test
import kotlin.test.assertEquals

class V4NoAccountTradeInputTests : V4BaseTests() {
    @Test
    fun testNoAccount() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testOnce()

        reset()

        print("--------Second round----------\n")

        testOnce()
    }

    override fun setup() {
        loadMarkets()
        loadMarketsConfigurations()
        loadOrderbook()
    }

    private fun testOnce() {
        perp.tradeInMarket("ETH-USD", 0)
        assertEquals(perp.internalState.input.trade.marketId, "ETH-USD")

        test({
            perp.trade("LIMIT", TradeInputField.type, 0)
        }, null)

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

        val errors = perp.internalState.input.errors
        val error = errors?.get(0)
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "REQUIRED_WALLET")
        assertEquals(error?.action?.rawValue, "/onboard")
        assertEquals(
            error?.resources?.title?.stringKey,
            "ERRORS.TRADE_BOX_TITLE.CONNECT_WALLET_TO_TRADE",
        )
        assertEquals(error?.resources?.text?.stringKey, "ERRORS.TRADE_BOX.CONNECT_WALLET_TO_TRADE")
        assertEquals(
            error?.resources?.action?.stringKey,
            "ERRORS.TRADE_BOX_TITLE.CONNECT_WALLET_TO_TRADE",
        )
    }
}
