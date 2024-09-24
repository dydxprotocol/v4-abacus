package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.ErrorFormat
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
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
        if (perp.staticTyping) {
            perp.tradeInMarket("ETH-USD", 0)
            val trade = perp.internalState.input.trade
            assertEquals(trade.marketId, "ETH-USD")
        } else {
            test(
                {
                    perp.tradeInMarket("ETH-USD", 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "marketId": "ETH-USD"
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("LIMIT", TradeInputField.type, 0)
            val size = perp.internalState.input.trade.size
            assertNotNull(size)
            assertEquals(size.input, "size.size")
        } else {
            test(
                {
                    perp.trade("LIMIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "trade": {
                            "size": {
                                "input": "size.size"
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("0.2", TradeInputField.size, 0)
        }, null)

        test({
            perp.trade("1500", TradeInputField.limitPrice, 0)
        }, null)

        if (perp.staticTyping) {
            perp.trade("1", TradeInputField.limitPrice, 0)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val openPosition = subaccount?.openPositions?.get("ETH-USD")
            assertEquals(subaccount?.calculated?.get(CalculationPeriod.current)?.equity, 100000.0)
            assertEquals(subaccount?.calculated?.get(CalculationPeriod.post)?.equity, 100299.8)
            assertEquals(openPosition?.calculated?.get(CalculationPeriod.current)?.valueTotal, 0.0)
            assertEquals(openPosition?.calculated?.get(CalculationPeriod.post)?.valueTotal, 300.0)
        } else {
            test(
                {
                    perp.trade("1", TradeInputField.limitPrice, 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 100299.8
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "valueTotal": {
                                            "current": 0.0,
                                            "postOrder": 300.0
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("10000", TradeInputField.limitPrice, 0)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val openPosition = subaccount?.openPositions?.get("ETH-USD")
            assertEquals(subaccount?.calculated?.get(CalculationPeriod.current)?.equity, 100000.0)
            assertEquals(subaccount?.calculated?.get(CalculationPeriod.post)?.equity, 100000.0)
            assertEquals(openPosition?.calculated?.get(CalculationPeriod.current)?.valueTotal, 0.0)
            assertEquals(openPosition?.calculated?.get(CalculationPeriod.post)?.valueTotal, 300.0)
        } else {
            test(
                {
                    perp.trade("10000", TradeInputField.limitPrice, 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 100000.0
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "valueTotal": {
                                            "current": 0.0,
                                            "postOrder": 300
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("SELL", TradeInputField.side, 0)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val openPosition = subaccount?.openPositions?.get("ETH-USD")
            assertEquals(subaccount?.calculated?.get(CalculationPeriod.current)?.equity, 100000.0)
            assertEquals(subaccount?.calculated?.get(CalculationPeriod.post)?.equity, 101700.0)
            assertEquals(openPosition?.calculated?.get(CalculationPeriod.current)?.valueTotal, 0.0)
            assertEquals(openPosition?.calculated?.get(CalculationPeriod.post)?.valueTotal, -300.0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.side, OrderSide.Sell)
            assertEquals(trade.marketId, "ETH-USD")
            assertEquals(trade.size?.size, 0.2)
            assertEquals(trade.price?.limitPrice, 10000.0)

            assertEquals(
                perp.internalState.input.receiptLines,
                listOf(ReceiptLine.LiquidationPrice, ReceiptLine.PositionMargin, ReceiptLine.PositionLeverage, ReceiptLine.Fee, ReceiptLine.Reward),
            )
        } else {
            test(
                {
                    perp.trade("SELL", TradeInputField.side, 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 101700.0
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "valueTotal": {
                                            "current": 0.0,
                                            "postOrder": -300.0
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                "input": {
                    "current": "trade",
                    "trade": {
                        "marketId": "ETH-USD",
                        "size": {
                            "size": 0.2
                        },
                        "price": {
                            "limitPrice": 10000
                        }
                    },
                    "receiptLines": [
                        "LIQUIDATION_PRICE",
                        "POSITION_MARGIN",
                        "POSITION_LEVERAGE",
                        "FEE",
                        "REWARD"
                    ]
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("1", TradeInputField.limitPrice, 0)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val openPosition = subaccount?.openPositions?.get("ETH-USD")
            assertEquals(subaccount?.calculated?.get(CalculationPeriod.current)?.equity, 100000.0)
            assertEquals(subaccount?.calculated?.get(CalculationPeriod.post)?.equity, 100000.0)
            assertEquals(openPosition?.calculated?.get(CalculationPeriod.current)?.valueTotal, 0.0)
            assertEquals(openPosition?.calculated?.get(CalculationPeriod.post)?.valueTotal, -300.0)
        } else {
            test(
                {
                    perp.trade("1", TradeInputField.limitPrice, 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 100000.0
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "valueTotal": {
                                            "current": 0.0,
                                            "postOrder": -300.0
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        test({
            perp.trade("1500", TradeInputField.limitPrice, 0)
        }, null)

        if (perp.staticTyping) {
            perp.tradeInMarket("BTC-USD", 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.side, OrderSide.Sell)
            assertEquals(trade.marketId, "BTC-USD")
            assertEquals(trade.size, null)
            assertEquals(trade.price, null)
        } else {
            test(
                {
                    perp.tradeInMarket("BTC-USD", 0)
                },
                """
            {
                "input": {
                    "current": "trade",
                    "trade": {
                        "marketId": "BTC-USD",
                        "size": null,
                        "price": null
                    }
                }
            }
                """.trimIndent(),
            )
        }

        test({
            perp.trade("10000", TradeInputField.limitPrice, 0)
        }, null)

        test({
            perp.trade("0.1", TradeInputField.size, 0)
        }, null)

        if (perp.staticTyping) {
            perp.trade("190", TradeInputField.goodTilDuration, 0)

            val errors = perp.internalState.input.errors
            assertNotNull(errors)
            val error = errors[0]
            assertEquals(error.type, ErrorType.error)
            assertEquals(error.code, "INVALID_GOOD_TIL")
        } else {
            test(
                {
                    perp.trade("190", TradeInputField.goodTilDuration, 0)
                },
                """
            {
                "input": {
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "INVALID_GOOD_TIL",
                            "fields": [
                                "goodTil"
                            ],
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRADE_BOX_TITLE.INVALID_GOOD_TIL"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRADE_BOX.INVALID_GOOD_TIL_MAX_90_DAYS"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_GOOD_TIL"
                                }
                            }
                        }
                    ]
                }
            }
                """.trimIndent(),
            )
        }
    }

    private fun testMarketTradeInputOnce() {
        test({
            perp.trade("CROSS", TradeInputField.marginMode, 0)
        }, null)

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        if (perp.staticTyping) {
            perp.tradeInMarket("LTC-USD", 0)
        } else {
            test(
                {
                    perp.tradeInMarket("LTC-USD", 0)
                },
                """
            {
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("1", TradeInputField.size, 0)
            val errors = perp.internalState.input.errors
            assertNotNull(errors)
            val error = errors[0]
            assertEquals(error.type, ErrorType.error)
            assertEquals(error.code, "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY")
            assertEquals(error.fields, iListOf("size.size"))
        } else {
            test(
                {
                    perp.trade("1", TradeInputField.size, 0)
                },
                """
            {
                "input": {
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
                            "fields": [
                                "size.size"
                            ],
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_NOT_ENOUGH_LIQUIDITY"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRADE_BOX.MARKET_ORDER_NOT_ENOUGH_LIQUIDITY"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_SIZE_FIELD"
                                }
                            }
                        }
                    ]
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.tradeInMarket("ETH-USD", 0)
            perp.trade("SELL", TradeInputField.side, 0)
            perp.trade(null, TradeInputField.usdcSize, 0)
        } else {
            test(
                {
                    perp.tradeInMarket("ETH-USD", 0)
                },
                """
            {
            }
                """.trimIndent(),
            )

            test(
                {
                    perp.trade("SELL", TradeInputField.side, 0)
                },
                """
            {
            }
                """.trimIndent(),
            )

            test(
                {
                    perp.trade(null, TradeInputField.usdcSize, 0)
                },
                """
            {
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("0.5", TradeInputField.usdcSize, 0)

            val size = perp.internalState.input.trade.size
            assertNotNull(size)
            assertEquals(size.usdcSize, 0.5)
            assertEquals(size.size, 0.0)

            val errors = perp.internalState.input.errors
            assertNotNull(errors)
            val error = errors[0]
            assertEquals(error.type, ErrorType.error)
            assertEquals(error.code, "ORDER_SIZE_BELOW_MIN_SIZE")
        } else {
            test(
                {
                    perp.trade("0.5", TradeInputField.usdcSize, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "size": {
                            "usdcSize": 0.5,
                            "size": 0.0
                        }
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "ORDER_SIZE_BELOW_MIN_SIZE"
                        }
                    ]
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade(null, TradeInputField.usdcSize, 0)
            val errors = perp.internalState.input.errors
            assertNotNull(errors)
            val error = errors[0]
            assertEquals(error.type, ErrorType.required)
            assertEquals(error.code, "REQUIRED_SIZE")
        } else {
            test(
                {
                    perp.trade(null, TradeInputField.usdcSize, 0)
                },
                """
            {
                "input": {
                    "errors": [
                        {
                            "type": "REQUIRED",
                            "code": "REQUIRED_SIZE"
                        }
                    ]
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("10", TradeInputField.usdcSize, 0)
            val size = perp.internalState.input.trade.size
            assertNotNull(size)
            assertEquals(size.usdcSize, 10.0)
            assertEquals(size.size, 0.006)
            assertEquals(size.balancePercent, 0.0000049629) // freeCollateral: 100000, 20x leverage
            assertEquals(size.input, "size.usdcSize")
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 0)
        } else {
            test(
                {
                    perp.trade("10", TradeInputField.usdcSize, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "size": {
                            "usdcSize": 10.0,
                            "size": 0.006,
                            "balancePercent": 0.000005,
                            "input": "size.usdcSize"
                        }
                    },
                    "errors": null
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("10", TradeInputField.size, 0)
            val size = perp.internalState.input.trade.size
            assertNotNull(size)
            assertEquals(size.usdcSize, 16543.0)
            assertEquals(size.size, 10.0)
            assertEquals(size.balancePercent, 0.0082715) // freeCollateral: 100000, 20x leverage
            assertEquals(size.input, "size.size")
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 0)
        } else {
            test(
                {
                    perp.trade("10", TradeInputField.size, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "size": {
                            "usdcSize": 16540.0,
                            "size": 10.0,
                            "balancePercent": 0.00827,
                            "input": "size.size"
                        }
                    },
                    "errors": null
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("0.5", TradeInputField.balancePercent, 0)
            val size = perp.internalState.input.trade.size
            assertNotNull(size)
            assertEquals(size.usdcSize, 1000000.1169)
            assertEquals(size.size, 605.7059999999999)
            assertEquals(size.balancePercent, 0.5) // freeCollateral: 100000, 20x leverage
            assertEquals(size.input, "size.balancePercent")
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 0)
        } else {
            test(
                {
                    perp.trade("0.5", TradeInputField.balancePercent, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "size": {
                            "usdcSize": 1000000.0,
                            "size": 593.6,
                            "balancePercent": 0.5,
                            "input": "size.balancePercent"
                        }
                    },
                    "errors": null
                }
            }
                """.trimIndent(),
            )
        }
    }

    private fun testIsolatedLimitTradeInputOnce() {
        if (perp.staticTyping) {
            perp.tradeInMarket("BTC-USD", 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.marketId, "BTC-USD")
        } else {
            test(
                {
                    perp.tradeInMarket("BTC-USD", 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "marketId": "BTC-USD"
                    }
                }
            }
                """.trimIndent(),
            )
        }
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

        if (perp.staticTyping) {
            perp.trade("1500", TradeInputField.limitPrice, 0)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            assertEquals(100000.0, subaccount.calculated[CalculationPeriod.current]?.equity)
            assertEquals(99980.61224492347, subaccount.calculated[CalculationPeriod.post]?.equity)

            val error = perp.internalState.input.errors?.first()
            assertEquals(ErrorType.error, error?.type)
            assertEquals("ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM", error?.code)
            assertEquals(iListOf("size.size"), error?.fields)
            assertEquals("APP.GENERAL.LEARN_MORE_ARROW", error?.linkText)
            assertEquals("https://help.dydx.trade/en/articles/171918-equity-tiers-and-rate-limits", error?.link)
            assertEquals("ERRORS.TRADE_BOX_TITLE.ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM", error?.resources?.title?.stringKey)
            assertEquals("ERRORS.TRADE_BOX.ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM", error?.resources?.text?.stringKey)
            val param = error?.resources?.text?.params?.first()!!
            assertEquals("20.0", param.value)
            assertEquals(ErrorFormat.UsdcPrice, param.format)
            assertEquals("MIN_VALUE", param.key)
            assertEquals("APP.TRADE.MODIFY_SIZE_FIELD", error?.resources?.action?.stringKey)
        } else {
            test(
                {
                    perp.trade("1500", TradeInputField.limitPrice, 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "freeCollateral": {
                                    "current": 100000.0,
                                    "postOrder": 99981.0
                                }
                            }
                        }
                    }
                },
                "input": {
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM",
                            "fields": [
                                "size.size"
                            ],
                            "linkText": "APP.GENERAL.LEARN_MORE_ARROW",
                            "link": "https://help.dydx.trade/en/articles/171918-equity-tiers-and-rate-limits",
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRADE_BOX_TITLE.ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRADE_BOX.ISOLATED_MARGIN_LIMIT_ORDER_BELOW_MINIMUM",
                                    "params": [
                                        {
                                            "value": 20.0,
                                            "format": "usdcPrice",
                                            "key": "MIN_VALUE"
                                        }
                                    ]
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_SIZE_FIELD"
                                }
                            }
                        }
                    ]
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("1", TradeInputField.size, 0)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            assertEquals(100000.0, subaccount.calculated[CalculationPeriod.current]?.equity)
            assertEquals(99923.4693877551, subaccount.calculated[CalculationPeriod.post]?.equity)
        } else {
            test(
                {
                    perp.trade("1", TradeInputField.size, 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 99923.4693877551
                                }
                            }
                        }
                    }
                },
                "input": {
                    "errors": null
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("0", TradeInputField.size, 0)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            assertEquals(100000.0, subaccount.calculated[CalculationPeriod.current]?.equity)
            assertEquals(null, subaccount.calculated[CalculationPeriod.post]?.equity)

            val error = perp.internalState.input.errors?.first()
            assertEquals(ErrorType.required, error?.type)
            assertEquals("REQUIRED_SIZE", error?.code)
            assertEquals(iListOf("size.size"), error?.fields)
            assertEquals("APP.TRADE.ENTER_AMOUNT", error?.resources?.action?.stringKey)
        } else {
            test(
                {
                    perp.trade("0", TradeInputField.size, 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": null
                                }
                            }
                        }
                    }
                },
                "input": {
                    "errors": [
                        {
                            "type": "REQUIRED",
                            "code": "REQUIRED_SIZE",
                            "fields": [
                                "size.size"
                            ],
                            "resources": {
                                "action": {
                                    "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                }
                            }
                        }
                    ]
                }
            }
                """.trimIndent(),
            )
        }
    }

    private fun testUpdates() {
        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_1, 0, null)
            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val calculated = subaccount?.calculated
            assertEquals(calculated?.get(CalculationPeriod.current)?.equity, 4185.625704)
            assertEquals(calculated?.get(CalculationPeriod.current)?.quoteBalance, 7250.506704)
            val position = subaccount?.openPositions?.get("ETH-USD")
            assertEquals(position?.calculated?.get(CalculationPeriod.current)?.size, -2.043254)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_1, 0, null)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4185.63
                                },
                                "quoteBalance": {
                                    "current": 7250.51
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -2.043254
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
                {
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_2, 0, null)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val calculated = subaccount?.calculated?.get(CalculationPeriod.current)
            assertEquals(calculated?.equity, 4272.436277000001)
            assertEquals(calculated?.quoteBalance, 8772.436277)
            val position = subaccount?.openPositions?.get("ETH-USD")
            assertEquals(position?.calculated?.get(CalculationPeriod.current)?.size, -3.0)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_2, 0, null)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
                {
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_3, 0, null)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val order = subaccount?.orders?.firstOrNull()
            assertEquals(order?.status, OrderStatus.PartiallyFilled)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_3, 0, null)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                },
                                "orders": {
                                    "2caebf6b-35d3-512c-a4d9-e438445d8dba": {
                                        "id": "2caebf6b-35d3-512c-a4d9-e438445d8dba",
                                        "side": "SELL",
                                        "type": "LIMIT",
                                        "status": "PARTIALLY_FILLED",
                                        "timeInForce": "GTT",
                                        "price": 1.0,
                                        "size": 1.0,
                                        "postOnly": false,
                                        "reduceOnly": false,
                                        "marketId": "ETH-USD",
                                        "resources": {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
                {
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_4, 0, null)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val order = subaccount?.orders?.firstOrNull()
            assertEquals(order?.status, OrderStatus.Filled)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_4, 0, null)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                },
                                "orders": {
                                    "2caebf6b-35d3-512c-a4d9-e438445d8dba": {
                                        "id": "2caebf6b-35d3-512c-a4d9-e438445d8dba",
                                        "side": "SELL",
                                        "type": "LIMIT",
                                        "status": "FILLED",
                                        "timeInForce": "GTT",
                                        "price": 1.0,
                                        "size": 1.0,
                                        "postOnly": false,
                                        "reduceOnly": false,
                                        "marketId": "ETH-USD",
                                        "resources": {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
                {
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_5, 0, null)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val order = subaccount?.orders?.firstOrNull()
            assertEquals(order?.status, OrderStatus.Filled)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_5, 0, null)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                },
                                "orders": {
                                    "2caebf6b-35d3-512c-a4d9-e438445d8dba": {
                                        "id": "2caebf6b-35d3-512c-a4d9-e438445d8dba",
                                        "side": "SELL",
                                        "type": "LIMIT",
                                        "status": "FILLED",
                                        "timeInForce": "GTT",
                                        "price": 1.0,
                                        "size": 1.0,
                                        "postOnly": false,
                                        "reduceOnly": false,
                                        "marketId": "ETH-USD",
                                        "resources": {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
                {
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_6, 0, null)

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val order = subaccount?.orders?.firstOrNull()
            assertEquals(order?.status, OrderStatus.Filled)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_6, 0, null)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                },
                                "orders": {
                                    "2caebf6b-35d3-512c-a4d9-e438445d8dba": {
                                        "id": "2caebf6b-35d3-512c-a4d9-e438445d8dba",
                                        "side": "SELL",
                                        "type": "LIMIT",
                                        "status": "FILLED",
                                        "timeInForce": "GTT",
                                        "price": 1.0,
                                        "size": 1.0,
                                        "postOnly": false,
                                        "reduceOnly": false,
                                        "marketId": "ETH-USD",
                                        "resources": {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
                {
                },
            )
        }
    }

    private fun testAdjustedMarginFraction() {
        if (perp.staticTyping) {
            perp.socket(
                url = mock.socketUrl,
                jsonString = mock.marketsChannel.v4_subscribed_for_adjusted_mf_calculation,
                subaccountNumber = 0,
                height = null,
            )

            val account = perp.internalState.wallet.account
            val subaccount = account.subaccounts[0]
            val ethPosition = subaccount?.openPositions?.get("ETH-USD")
            assertEquals(ethPosition?.calculated?.get(CalculationPeriod.current)?.adjustedImf, 0.05)
            assertEquals(ethPosition?.calculated?.get(CalculationPeriod.post)?.adjustedImf, 0.05)
            assertEquals(ethPosition?.calculated?.get(CalculationPeriod.current)?.adjustedMmf, 0.03)
            assertEquals(ethPosition?.calculated?.get(CalculationPeriod.post)?.adjustedMmf, 0.03)
            assertEquals(ethPosition?.calculated?.get(CalculationPeriod.current)?.liquidationPrice, 2838.976141423949)
            assertEquals(ethPosition?.calculated?.get(CalculationPeriod.post)?.liquidationPrice, 2829.267403559871)
            val btcPosition = subaccount?.openPositions?.get("BTC-USD")
            assertEquals(btcPosition?.calculated?.get(CalculationPeriod.post)?.liquidationPrice, 64878.02210679612)
        } else {
            test(
                {
                    perp.socket(
                        mock.socketUrl,
                        mock.marketsChannel.v4_subscribed_for_adjusted_mf_calculation,
                        0,
                        null,
                    )
                },
                """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "openPositions": {
                                        "ETH-USD": {
                                            "adjustedImf": {
                                                "current": 0.05,
                                                "postOrder": 0.05
                                            },
                                            "adjustedMmf": {
                                                "current": 0.03,
                                                "postOrder": 0.03
                                            },
                                            "liquidationPrice": {
                                                "current": 2838.98,
                                                "postOrder": 2829.27
                                            }
                                        },
                                        "BTC-USD": {
                                            "adjustedImf": {
                                                "current": 0.05,
                                                "postOrder": 0.05
                                            },
                                            "adjustedMmf": {
                                                "current": 0.03,
                                                "postOrder": 0.03
                                            },
                                            "liquidationPrice": {
                                                "postOrder": 64878.0
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }
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

        if (perp.staticTyping) {
            perp.trade("1000.0", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.price?.triggerPrice, 1000.0)
            assertEquals(trade.price?.limitPrice, 900.0)
        } else {
            test(
                {
                    perp.trade("1000.0", TradeInputField.triggerPrice, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "price": {
                            "limitPrice": 900.0,
                            "triggerPrice": 1000.0
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.tradeInMarket("ETH-USD", 0)
            perp.trade("0.1", TradeInputField.size, 0)
            perp.trade("1000", TradeInputField.triggerPrice, 0)
        } else {
            test(
                {
                    perp.tradeInMarket("ETH-USD", 0)
                },
                """
            {
            }
                """.trimMargin(),
            )

            test(
                {
                    perp.trade("0.1", TradeInputField.size, 0)
                },
                """
            {
            }
                """.trimMargin(),
            )

            test(
                {
                    perp.trade("1000", TradeInputField.triggerPrice, 0)
                },
                """
            {
            }
                """.trimMargin(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.summary?.payloadPrice, 949.5770392749245)
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "summary": {
                            "payloadPrice": 950.0
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("2000.0", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.price?.triggerPrice, 2000.0)
        } else {
            test(
                {
                    perp.trade("2000.0", TradeInputField.triggerPrice, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "price": {
                            "triggerPrice": 2000.0
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.summary?.payloadPrice, 1899.154078549849)
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "summary": {
                            "payloadPrice": 1900.0
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }
    }

    fun testReduceOnly() {
        if (perp.staticTyping) {
            perp.trade("MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Market)
            assertEquals(trade.options.needsReduceOnly, true)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.reduceOnlyTooltip, null)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("LIMIT", TradeInputField.type, 0)

        if (perp.staticTyping) {
            perp.trade("GTT", TradeInputField.timeInForceType, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Limit)
            assertEquals(trade.options.needsReduceOnly, false)
            assertEquals(trade.options.needsPostOnly, true)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("GTT", TradeInputField.timeInForceType, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": true,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("IOC", TradeInputField.timeInForceType, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Limit)
            assertEquals(trade.options.needsReduceOnly, true)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.reduceOnlyTooltip, null)
            assertEquals(trade.options.postOnlyTooltip?.bodyStringKey, "GENERAL.TRADE.POST_ONLY_TIMEINFORCE_GTT.BODY")
        } else {
            test(
                {
                    perp.trade("IOC", TradeInputField.timeInForceType, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": "GENERAL.TRADE.POST_ONLY_TIMEINFORCE_GTT"
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        if (perp.staticTyping) {
            perp.trade("DEFAULT", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            assertEquals(trade.options.needsReduceOnly, false)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("DEFAULT", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("IOC", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            assertEquals(trade.options.needsReduceOnly, true)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.reduceOnlyTooltip, null)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("IOC", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("POST_ONLY", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            assertEquals(trade.options.needsReduceOnly, false)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("POST_ONLY", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        if (perp.staticTyping) {
            perp.trade("DEFAULT", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            assertEquals(trade.options.needsReduceOnly, false)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("DEFAULT", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("IOC", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            assertEquals(trade.options.needsReduceOnly, true)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.reduceOnlyTooltip, null)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("IOC", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("POST_ONLY", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            assertEquals(trade.options.needsReduceOnly, false)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("POST_ONLY", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        if (perp.staticTyping) {
            perp.trade("IOC", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            assertEquals(trade.options.needsReduceOnly, true)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.reduceOnlyTooltip, null)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("IOC", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        if (perp.staticTyping) {
            perp.trade("IOC", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            assertEquals(trade.options.needsReduceOnly, true)
            assertEquals(trade.options.needsPostOnly, false)
            assertEquals(trade.options.reduceOnlyTooltip, null)
            assertEquals(trade.options.postOnlyTooltip, null)
        } else {
            test(
                {
                    perp.trade("IOC", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
                """.trimIndent(),
            )
        }
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

        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionStopLimitToTakeProfit() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            assertEquals(trade.execution, "DEFAULT")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "DEFAULT"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            assertEquals(trade.execution, "POST_ONLY")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "POST_ONLY"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionStopLimitToTakeProfitMarket() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                    // should change to IOC
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionStopMarket() {
        testExecutionStopMarketToStopLimit()
        testExecutionStopMarketToTakeProfit()
        testExecutionStopMarketToTakeProfitMarket()
    }

    private fun testExecutionStopMarketToStopLimit() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_LIMIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionStopMarketToTakeProfit() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionStopMarketToTakeProfitMarket() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionTakeProfit() {
        testExecutionTakeProfitToStopMarket()
        testExecutionTakeProfitToStopLimit()
        testExecutionTakeProfitToTakeProfitMarket()
    }

    private fun testExecutionTakeProfitToStopMarket() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                    // should change to IOC
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionTakeProfitToStopLimit() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            assertEquals(trade.execution, "DEFAULT")
        } else {
            test(
                {
                    perp.trade("STOP_LIMIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "DEFAULT"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_LIMIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            assertEquals(trade.execution, "POST_ONLY")
        } else {
            test(
                {
                    perp.trade("STOP_LIMIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "POST_ONLY"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionTakeProfitToTakeProfitMarket() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionTakeProfitMarket() {
        testExecutionTakeProfitMarketToStopLimit()
        testExecutionTakeProfitMarketToTakeProfit()
        testExecutionTakeProfitMarketToStopMarket()
    }

    private fun testExecutionTakeProfitMarketToStopLimit() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_LIMIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionTakeProfitMarketToTakeProfit() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testExecutionTakeProfitMarketToStopMarket() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            assertEquals(trade.execution, "IOC")
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
                """.trimIndent(),
            )
        }
    }
}
