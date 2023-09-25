package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.placeOrder
import exchange.dydx.abacus.state.modal.quantum
import exchange.dydx.abacus.state.modal.trade
import exchange.dydx.abacus.state.modal.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadOrderbook
import exchange.dydx.abacus.tests.extensions.loadv4Accounts
import kotlin.test.Test
import kotlin.test.assertEquals

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
    }

    override fun setup() {
        super.setup()
        loadOrderbook()
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

    internal fun loadOrderbook(): StateResponse {
        return test({
            perp.loadOrderbook(mock)
        }, null)
    }

    private fun testOnce() {
        testMarketTradeInputOnce()
        testLimitTradeInputOnce()
        testUpdates()
    }

    private fun testLimitTradeInputOnce() {
        test(
            {
                perp.tradeInMarket("ETH-USD", 0)
            }, """
            {
                "input": {
                    "trade": {
                        "marketId": "ETH-USD"
                    }
                }
            }
        """.trimIndent()
        )

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

        test(
            {
                perp.trade("1", TradeInputField.limitPrice, 0)
            }, """
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
                                            "postOrder": 0.2
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        )

        test(
            {
                perp.trade("10000", TradeInputField.limitPrice, 0)
            }, """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 98300.0
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
        """.trimIndent()
        )

        test(
            {
                perp.trade("SELL", TradeInputField.side, 0)
            }, """
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
                                            "postOrder": -2000.0
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
                        "BUYING_POWER",
                        "MARGIN_USAGE",
                        "FEE"
                    ]
                }
            }
        """.trimIndent()
        )

        test(
            {
                perp.trade("1", TradeInputField.limitPrice, 0)
            }, """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 99700.2
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
        """.trimIndent()
        )

//        test({
//            perp.trade(null, TradeInputField.limitPrice)
//        }, null)

        test({
            perp.trade("1500", TradeInputField.limitPrice, 0)
        }, null, {
            val placeOrder = perp.placeOrder(20)
            assertEquals(1, placeOrder?.clobPairId)
            assertEquals("SELL", placeOrder?.side)
            assertEquals(200000000.0, placeOrder?.quantums)
            assertEquals(1500000000.0, placeOrder?.subticks)
            assertEquals(null, placeOrder?.goodUntilBlock)
            assertEquals(false, placeOrder?.reduceOnly)
            assertEquals("TIME_IN_FORCE_UNSPECIFIED", placeOrder?.timeInForce)
            assertEquals("LONG_TERM", placeOrder?.orderFlags)

            val quatum = perp.quantum(parser.asDecimal(1.089)!!, -10, parser.asDecimal(10)!!)
            assertEquals(10890000000, quatum)
        })



        test(
            {
                perp.tradeInMarket("BTC-USD", 0)
            }, """
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
        """.trimIndent()
        )

        test({
            perp.trade("10000", TradeInputField.limitPrice, 0)
        }, null)

        test({
            perp.trade("0.1", TradeInputField.size, 0)
        }, null, {
            val placeOrder = perp.placeOrder(20)
            assertEquals(placeOrder?.clobPairId, 0)
        })

        test(
            {
                perp.trade("190", TradeInputField.goodUntilDuration, 0)
            }, """
            {
                "input": {
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "INVALID_GOOD_TIL",
                            "fields": [
                                "goodUntil"
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
        """.trimIndent()
        )
    }

    private fun testMarketTradeInputOnce() {
        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        test(
            {
                perp.tradeInMarket("LTC-USD", 0)
            }, """
            {
            }
        """.trimIndent()
        )

        test(
            {
                perp.trade("1", TradeInputField.size, 0)
            }, """
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
        """.trimIndent()
        )

        test(
            {
                perp.tradeInMarket("ETH-USD", 0)
            }, """
            {
            }
        """.trimIndent()
        )

        test(
            {
                perp.trade("SELL", TradeInputField.side, 0)
            }, """
            {
            }
        """.trimIndent()
        )

        test(
            {
                perp.trade(null, TradeInputField.usdcSize, 0)
            }, """
            {
            }
        """.trimIndent()
        )

        test(
            {
                perp.trade("1", TradeInputField.usdcSize, 0)
            }, """
            {
                "input": {
                    "trade": {
                        "size": {
                            "usdcSize": 1.0,
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
        """.trimIndent()
        )

        test(
            {
                perp.trade(null, TradeInputField.usdcSize, 0)
            }, """
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
        """.trimIndent()
        )
    }

    private fun testUpdates() {
        test({
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_1, 0, null)
        }, """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4185.625704
                                },
                                "quoteBalance": {
                                    "current": 7250.506704
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
        """.trimIndent(), {
        })

        test({
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_2, 0, null)
        }, """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.436277
                                },
                                "quoteBalance": {
                                    "current": 8772.436277
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
        """.trimIndent(), {
        })

        test({
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_3, 0, null)
        }, """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.436277
                                },
                                "quoteBalance": {
                                    "current": 8772.436277
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
        """.trimIndent(), {
        })

        test({
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_4, 0, null)
        }, """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.436277
                                },
                                "quoteBalance": {
                                    "current": 8772.436277
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
        """.trimIndent(), {
        })

        test({
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_5, 0, null)
        }, """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.436277
                                },
                                "quoteBalance": {
                                    "current": 8772.436277
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
        """.trimIndent(), {
        })

        test({
            perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_6, 0, null)
        }, """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.436277
                                },
                                "quoteBalance": {
                                    "current": 8772.436277
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
        """.trimIndent(), {
        })
    }

    private fun testAdjustedMarginFraction() {
        test(
            {
                perp.socket(
                    mock.socketUrl,
                    mock.marketsChannel.v4_subscribed_for_adjusted_mf_calculation,
                    0,
                    null
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
                                                "current": 0.2738612787525831,
                                                "postOrder": 0.2738612787525831
                                            },
                                            "adjustedMmf": {
                                                "current": 0.16431676725154984,
                                                "postOrder": 0.16431676725154984
                                            },
                                            "liquidationPrice": {
                                                "current": 2511.468964386139,
                                                "postOrder": 2484.3090217561517
                                            }
                                        },
                                        "BTC-USD": {
                                            "adjustedImf": {
                                                "current": 0.05,
                                                "postOrder": 0.158113883008419
                                            },
                                            "adjustedMmf": {
                                                "current": 0.03,
                                                "postOrder": 0.09486832980505139
                                            },
                                            "liquidationPrice": {
                                                "postOrder": 57353.80049182219
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    fun testConditional() {
        test({
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("12", TradeInputField.goodUntilDuration, 0)
        }, null)

        test({
            perp.trade("D", TradeInputField.goodUntilUnit, 0)
        }, null)

        test({
            perp.trade("900.0", TradeInputField.limitPrice, 0)
        }, null)

        test({
            perp.trade("1000.0", TradeInputField.triggerPrice, 0)
        }, """
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
        """.trimIndent(), {
            val placeOrder = perp.placeOrder(20)
            assertEquals(0, placeOrder?.clobPairId)
            assertEquals("SELL", placeOrder?.side)
            assertEquals(1000000000.0, placeOrder?.quantums)
            assertEquals(9000000.0, placeOrder?.subticks)
            assertEquals(23, placeOrder?.goodUntilBlock)
            assertEquals(false, placeOrder?.reduceOnly)
            assertEquals("TIME_IN_FORCE_UNSPECIFIED", placeOrder?.timeInForce)
            assertEquals("CONDITIONAL", placeOrder?.orderFlags)
            assertEquals("CONDITION_TYPE_STOP_LOSS", placeOrder?.conditionType)
            assertEquals(10000000.0, placeOrder?.conditionalOrderTriggerSubticks)
        })

        test(
            {
                perp.tradeInMarket("ETH-USD", 0)
            }, """
            {
            }
        """.trimMargin()
        )

        test(
            {
                perp.trade("0.1", TradeInputField.size, 0)
            }, """
            {
            }
        """.trimMargin()
        )

        test(
            {
                perp.trade("1000", TradeInputField.triggerPrice, 0)
            }, """
            {
            }
        """.trimMargin()
        )

        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
            }, """
            {
                "input": {
                    "trade": {
                        "summary": {
                            "payloadPrice": 950.0
                        }
                    }
                }
            }
        """.trimIndent()
        )

        test(
            {
                perp.trade("2000.0", TradeInputField.triggerPrice, 0)
            }, """
            {
                "input": {
                    "trade": {
                        "price": {
                            "triggerPrice": 2000.0
                        }
                    }
                }
            }
        """.trimIndent()
        )


        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
            }, """
            {
                "input": {
                    "trade": {
                        "summary": {
                            "payloadPrice": 1800.0
                        }
                    }
                }
            }
        """.trimIndent()
        )

    }
}
