package exchange.dydx.abacus.payload

import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestFileSystem
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.app.manager.TestThreading
import exchange.dydx.abacus.app.manager.TestTimer
import exchange.dydx.abacus.app.manager.TestWebSocket
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.Configs
import exchange.dydx.abacus.output.FeeDiscount
import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.LaunchIncentivePoints
import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.output.MarketCandles
import exchange.dydx.abacus.output.MarketConfigs
import exchange.dydx.abacus.output.MarketHistoricalFunding
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.MarketPerpetual
import exchange.dydx.abacus.output.MarketTrade
import exchange.dydx.abacus.output.NetworkConfigs
import exchange.dydx.abacus.output.OrderbookLine
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.PerpetualMarketType
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.User
import exchange.dydx.abacus.output.Wallet
import exchange.dydx.abacus.output.account.Account
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.SubaccountPendingPosition
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.input.ClosePositionInput
import exchange.dydx.abacus.output.input.ClosePositionInputSize
import exchange.dydx.abacus.output.input.Input
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderbookUsage
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.output.input.TradeInputBracket
import exchange.dydx.abacus.output.input.TradeInputBracketSide
import exchange.dydx.abacus.output.input.TradeInputGoodUntil
import exchange.dydx.abacus.output.input.TradeInputMarketOrder
import exchange.dydx.abacus.output.input.TradeInputOptions
import exchange.dydx.abacus.output.input.TradeInputPrice
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.output.input.TradeInputSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.helper.DynamicLocalizer
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.model.PerpTradingStateMachine
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.satisfies
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import kollections.toIMap
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

internal typealias LoadingFunction = () -> StateResponse
internal typealias VerificationFunction = (response: StateResponse) -> Unit

open class BaseTests(
    private val maxSubaccountNumber: Int,
    private val useParentSubaccount: Boolean,
    private val staticTyping: Boolean = true, // turn on static typing for testing
) {
    open val doAsserts = true
    internal val deploymentUri = "https://api.examples.com"
    internal val doesntMatchText = "doesn't match"
    internal val parser = Parser()
    internal val mock = AbacusMockData()
    internal var perp = createState(
        useParentSubaccount = useParentSubaccount,
        staticTyping = staticTyping,
    )

    companion object {
        fun testIOImplementations(): IOImplementations {
            return IOImplementations(
                rest = TestRest(),
                webSocket = TestWebSocket(),
                chain = TestChain(),
                tracking = null,
                threading = TestThreading(),
                timer = TestTimer(),
                fileSystem = TestFileSystem(),
                logging = null,
            )
        }

        fun testLocalizer(ioImplementations: IOImplementations): LocalizerProtocol {
            return DynamicLocalizer(
                ioImplementations = ioImplementations,
                systemLanguage = "en",
                path = "/config",
                endpoint = "https://dydx-v4-shared-resources.vercel.app/config",
            )
        }

        fun testUIImplementations(localizer: LocalizerProtocol?): UIImplementations {
            return UIImplementations(localizer, null)
        }
    }

    internal open fun createState(
        useParentSubaccount: Boolean,
        staticTyping: Boolean
    ): PerpTradingStateMachine {
        val ioImplementations = testIOImplementations()
        return PerpTradingStateMachine(
            environment = mock.v4Environment,
            localizer = testLocalizer(ioImplementations),
            formatter = null,
            maxSubaccountNumber = maxSubaccountNumber,
            useParentSubaccount = useParentSubaccount,
            staticTyping = staticTyping,
        )
    }

    internal open fun reset() {
        perp.state = null
        perp.input = null
        perp.marketsSummary = null
        perp.wallet = null
        perp.assets = null
        perp.configs = null
        perp.internalState = InternalState()
        setup()
    }

    internal fun payload(text: String): Map<String, Any>? {
        return parser.asNativeMap(Json.parseToJsonElement(text))
    }

    internal open fun setup() {
    }

    internal fun test(perp: TradingStateMachine, expected: String) {
        val json = parser.asNativeMap(Json.parseToJsonElement(expected))
        assertNotNull(json, "Missing expectations")
        val data = perp.data
        assertNotNull(data, "Missing data")
        try {
            data.satisfies(json, parser)
        } catch (e: Throwable) {
            println("Internal state match failed...")
            println("  Actual State: ${data.toJsonPrettyPrint()}")
            println("  Expected State: ${json.toJsonPrettyPrint()}")
            throw e
        }
    }

    internal fun test(perp: TradingStateMachine, json: JsonElement?) {
        val map = parser.asNativeMap(json)
        assertNotNull(map)
        for ((key, value) in map) {
            val obj = obj(perp, key)
            assertNotNull(obj)
            val expectations = parser.asNativeMap(value)
            assertNotNull(expectations)
            testItem(obj, expectations)
        }
    }

    private fun obj(perp: TradingStateMachine, key: String): Any? {
        return when (key) {
            "input" -> perp.input
            "markets" -> perp.marketsSummary
            "assets" -> perp.assets
            "wallet" -> perp.wallet
            "configs" -> perp.configs
            else -> null
        }
    }

    private fun testItem(obj: Any, expectations: Map<String, Any>) {
        for ((key, value) in expectations) {
            val numeric = parser.asDouble(value)
            if (numeric != null) {
                assertEquals(
                    numeric,
                    parser.asDouble(parser.value(obj, key)),
                    "$key $value not matching",
                )
            } else {
                assertEquals(
                    parser.asString(value),
                    parser.asString(parser.value(obj, key)),
                    "$key $value not matching",
                )
            }
        }
    }

    internal open fun verifyState(state: PerpetualState?) {
        verifyConfigs(perp.configs, state?.configs, "configs")
        verifyWalletState(perp.wallet, state?.wallet, "wallet")
        verifyAccountState(
            data = perp.account,
            state = perp.internalState.wallet.account,
            staticTyping = perp.staticTyping,
            obj = state?.account,
            trace = "account",
        )
        if (staticTyping) {
            for ((key, value) in perp.internalState.wallet.account.subaccounts) {
                assertEquals(value.fills ?: emptyList(), state?.fills?.get("$key") ?: emptyList())
            }
        } else {
            verifySubaccountFillsState(
                parser.asNativeMap(perp.account?.get("subaccounts")),
                state?.fills,
                "fills",
            )
        }

        if (staticTyping) {
            for ((key, value) in perp.internalState.wallet.account.subaccounts) {
                assertEquals(value.transfers ?: emptyList(), state?.transfers?.get("$key") ?: emptyList())
            }
        } else {
            verifySubaccountTransfersState(
                parser.asNativeMap(perp.account?.get("subaccounts")),
                state?.transfers,
                "transfers",
            )
        }

        verifySubaccountFundingPaymentsState(
            parser.asNativeMap(perp.account?.get("subaccounts")),
            state?.fundingPayments,
            "fundingPayments",
        )
        verifySubaccountHistoricalPNLsState(
            parser.asNativeMap(perp.account?.get("subaccounts")),
            state?.historicalPnl,
            ServerTime.now() - perp.historicalPnlDays.days,
            "historicalPnl",
        )
        if (staticTyping) {
            assertEquals(perp.internalState.assets.toIMap(), state?.assets ?: emptyMap())
        } else {
            verifyAssetsState(perp.assets, state?.assets, "assets")
        }
        verifyMarketsState(
            perp.marketsSummary,
            perp.assets,
            state?.marketsSummary,
            "markets",
        )
        verifyMarketsHistoricalFundingsState(
            parser.asNativeMap(perp.marketsSummary?.get("markets")),
            state?.historicalFundings,
            "historicalFundings",
        )
        verifyMarketsTradesState(
            parser.asNativeMap(perp.marketsSummary?.get("markets")),
            state?.trades,
            "trades",
        )
        verifyMarketsCandlesState(
            parser.asNativeMap(perp.marketsSummary?.get("markets")),
            state?.candles,
            "candles",
        )
        verifyMarketsOrderbookState(
            parser.asNativeMap(perp.marketsSummary?.get("markets")),
            state?.orderbooks,
            "orderbooks",
        )
        verifyInputState(perp.input, state?.input, "input")
    }

    private fun verifyInputState(data: Map<String, Any>?, obj: Input?, trace: String) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["current"]), obj.current?.rawValue, "$trace.current")
            when (obj.current?.rawValue) {
                "trade" -> {
                    verifyInputTradeState(
                        parser.asNativeMap(data["trade"]),
                        obj.trade,
                        "$trace.trade",
                    )
                }

                "closePosition" -> {
                    verifyInputClosePositionState(
                        parser.asNativeMap(data["closePosition"]),
                        obj.closePosition,
                        "$trace.closePosition",
                    )
                }
            }

            verifyInputReceiptLinesState(
                parser.asList(data["receiptLines"]),
                obj.receiptLines,
                "$trace.receiptLines",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeState(data: Map<String, Any>?, obj: TradeInput?, trace: String) {
        if (data != null) {
            assertNotNull(obj, "$trace should not be null")
            assertEquals(
                parser.asString(data["type"]),
                obj.type?.rawValue,
                "$trace.type $doesntMatchText",
            )
            assertEquals(
                parser.asString(data["side"]),
                obj.side?.rawValue,
                "$trace.side $doesntMatchText",
            )
            assertEquals(
                parser.asString(data["marketId"]),
                obj.marketId,
                "$trace.marketId $doesntMatchText",
            )
            assertEquals(
                parser.asString(data["execution"]),
                obj.execution,
                "$trace.execution $doesntMatchText",
            )
            assertEquals(
                parser.asString(data["timeInForce"]),
                obj.timeInForce,
                "$trace.timeInForce $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["postOnly"]) ?: false,
                obj.postOnly,
                "$trace.postOnly $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["reduceOnly"]) ?: false,
                obj.reduceOnly,
                "$trace.reduceOnly $doesntMatchText",
            )
            assertEquals(
                MarginMode.invoke(parser.asString(data["marginMode"])),
                obj.marginMode,
                "$trace.marginMode $doesntMatchText",
            )
            verifyInputTradeInputSizeState(
                parser.asNativeMap(data["size"]),
                obj.size,
                "$trace.size",
            )
            verifyInputTradeInputGoodUntilState(
                parser.asNativeMap(data["goodTil"]),
                obj.goodTil,
                "$trace.goodTil",
            )
            verifyInputTradeInputMarketOrderState(
                parser.asNativeMap(data["marketOrder"]),
                obj.marketOrder,
                "$trace.marketOrder",
            )
            verifyInputTradeInputOptionsState(
                parser.asNativeMap(data["options"]),
                obj.options,
                "$trace.options",
            )
            verifyInputTradeInputSummaryState(
                parser.asNativeMap(data["summary"]),
                obj.summary,
                "$trace.summary",
            )
            verifyInputTradeInputBracketState(
                parser.asNativeMap(data["bracket"]),
                obj.bracket,
                "$trace.bracket",
            )
        } else {
            assertNull(obj, "$trace should be null")
        }
    }

    private fun verifyInputClosePositionState(
        data: Map<String, Any>?,
        obj: ClosePositionInput?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj, "$trace should not be null")
            assertEquals(
                parser.asString(data["marketId"]),
                obj.marketId,
                "$trace.marketId $doesntMatchText",
            )
            verifyInputClosePositionInputSizeState(
                parser.asNativeMap(data["size"]),
                obj.size,
                "$trace.size",
            )
            verifyInputTradeInputMarketOrderState(
                parser.asNativeMap(data["marketOrder"]),
                obj.marketOrder,
                "$trace.marketOrder",
            )
            verifyInputTradeInputSummaryState(
                parser.asNativeMap(data["summary"]),
                obj.summary,
                "$trace.summary",
            )
        } else {
            assertNull(obj, "$trace should be null")
        }
    }

    private fun verifyInputClosePositionInputSizeState(
        data: Map<String, Any>?,
        obj: ClosePositionInputSize?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["input"]), obj.input, "$trace.input $doesntMatchText")
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size $doesntMatchText")
            assertEquals(
                parser.asDouble(data["usdcSize"]),
                obj.usdcSize,
                "$trace.usdcSize $doesntMatchText",
            )
            assertEquals(
                parser.asDouble(data["percent"]),
                obj.percent,
                "$trace.percent $doesntMatchText",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputReceiptLinesState(
        data: List<Any>?,
        obj: List<ReceiptLine>?,
        trace: String,
    ) {
        assertEquals(data?.size, obj?.size)
        val size = data?.size
        if (size != null) {
            for (i in 0 until size) {
                val item = parser.asString(data[i])
                val line = obj?.get(i)
                assertEquals(item, line?.rawValue)
            }
        }
    }

    private fun verifyInputTradeInputSizeState(
        data: Map<String, Any>?,
        obj: TradeInputSize?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["input"]), obj.input, "$trace.input $doesntMatchText")
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size $doesntMatchText")
            assertEquals(
                parser.asDouble(data["usdcSize"]),
                obj.usdcSize,
                "$trace.usdcSize $doesntMatchText",
            )
            assertEquals(
                parser.asDouble(data["leverage"]),
                obj.leverage,
                "$trace.leverage $doesntMatchText",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputPriceState(
        data: Map<String, Any>?,
        obj: TradeInputPrice?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["limitPrice"]),
                obj.limitPrice,
                "$trace.limitPrice $doesntMatchText",
            )
            assertEquals(
                parser.asDouble(data["triggerPrice"]),
                obj.triggerPrice,
                "$trace.triggerPrice $doesntMatchText",
            )
            assertEquals(
                parser.asDouble(data["trailingPercent"]),
                obj.trailingPercent,
                "$trace.trailingPercent $doesntMatchText",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputGoodUntilState(
        data: Map<String, Any>?,
        obj: TradeInputGoodUntil?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["unit"]), obj.unit, "$trace.unit $doesntMatchText")
            assertEquals(
                parser.asDouble(data["duration"]),
                obj.duration,
                "$trace.duration $doesntMatchText",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputMarketOrderState(
        data: Map<String, Any>?,
        obj: TradeInputMarketOrder?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size $doesntMatchText")
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price $doesntMatchText")
            assertEquals(
                parser.asDouble(data["usdcSize"]),
                obj.usdcSize,
                "$trace.usdcSize $doesntMatchText",
            )
            assertEquals(
                parser.asDouble(data["worstPrice"]),
                obj.worstPrice,
                "$trace.worstPrice $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["filled"]) ?: false,
                obj.filled,
                "$trace.filled $doesntMatchText",
            )
            verifyInputTradeInputMarketOrderOrderbookUsageState(
                parser.asList(data["orderbook"]),
                obj.orderbook,
                "$trace.orderbook",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputMarketOrderOrderbookUsageState(
        data: List<Any>?,
        obj: List<OrderbookUsage>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (i in obj.indices) {
                val itemData = parser.asNativeMap(data[i])
                val item = obj[i]
                verifyInputTradeInputMarketOrderOrderbookUsageLineState(itemData, item, "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputMarketOrderOrderbookUsageLineState(
        data: Map<String, Any>?,
        obj: OrderbookUsage?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size $doesntMatchText")
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price $doesntMatchText")
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputOptionsState(
        data: Map<String, Any>?,
        obj: TradeInputOptions?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asBool(data["needsMarginMode"]) ?: false,
                obj.needsMarginMode,
                "$trace.needsMarginMode $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsSize"]) ?: false,
                obj.needsSize,
                "$trace.needsSize $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsLeverage"]) ?: false,
                obj.needsLeverage,
                "$trace.needsLeverage $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsBrackets"]) ?: false,
                obj.needsBrackets,
                "$trace.needsBrackets $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsGoodUntil"]) ?: false,
                obj.needsGoodUntil,
                "$trace.needsGoodUntil $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsLimitPrice"]) ?: false,
                obj.needsLimitPrice,
                "$trace.needsLimitPrice $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsPostOnly"]) ?: false,
                obj.needsPostOnly,
                "$trace.needsPostOnly $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsReduceOnly"]) ?: false,
                obj.needsReduceOnly,
                "$trace.needsReduceOnly $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsTrailingPercent"]) ?: false,
                obj.needsTrailingPercent,
                "$trace.needsTrailingPercent $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["needsTriggerPrice"]) ?: false,
                obj.needsTriggerPrice,
                "$trace.needsTriggerPrice $doesntMatchText",
            )
            assertEquals(
                parser.asString(data["reduceOnlyPromptStringKey"])?.let {
                    "$it.TITLE"
                },
                obj.reduceOnlyTooltip?.titleStringKey,
                "$trace.reduceOnlyPromptStringKey title $doesntMatchText",
            )
            assertEquals(
                parser.asString(data["reduceOnlyPromptStringKey"])?.let {
                    "$it.BODY"
                },
                obj.reduceOnlyTooltip?.bodyStringKey,
                "$trace.reduceOnlyPromptStringKey body $doesntMatchText",
            )
            assertEquals(
                parser.asString(data["postOnlyPromptStringKey"])?.let {
                    "$it.TITLE"
                },
                obj.postOnlyTooltip?.titleStringKey,
                "$trace.postOnlyPromptStringKey title $doesntMatchText",
            )
            assertEquals(
                parser.asString(data["postOnlyPromptStringKey"])?.let {
                    "$it.BODY"
                },
                obj.postOnlyTooltip?.bodyStringKey,
                "$trace.postOnlyPromptStringKey body $doesntMatchText",
            )
            verifyInputTradeInputOptionsExecutionOptionsState(
                parser.asList(data["executionOptions"]),
                obj.executionOptions,
                "$trace.executionOptions",
            )
            verifyInputTradeInputOptionsExecutionOptionsState(
                parser.asList(data["timeInForceOptions"]),
                obj.timeInForceOptions,
                "$trace.timeInForceOptions",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputOptionsExecutionOptionsState(
        data: List<Any>?,
        obj: List<SelectionOption>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (i in obj.indices) {
                val itemData = parser.asNativeMap(data[i])
                val item = obj[i]
                verifySelectionOptionState(itemData, item, "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySelectionOptionState(
        data: Map<String, Any>?,
        obj: SelectionOption?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["type"]), obj.type, "$trace.type $doesntMatchText")
            assertEquals(
                parser.asString(data["stringKey"]),
                obj.stringKey,
                "$trace.stringKey $doesntMatchText",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputSummaryState(
        data: Map<String, Any>?,
        obj: TradeInputSummary?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asBool(data["filled"]) ?: false,
                obj.filled,
                "$trace.filled $doesntMatchText",
            )
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size $doesntMatchText")
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price $doesntMatchText")
            assertEquals(parser.asDouble(data["fee"]), obj.fee, "$trace.fee $doesntMatchText")
            assertEquals(
                parser.asDouble(data["slippage"]),
                obj.slippage,
                "$trace.slippage $doesntMatchText",
            )
            assertEquals(
                parser.asDouble(data["usdcSize"]),
                obj.usdcSize,
                "$trace.usdcSize $doesntMatchText",
            )
            assertEquals(parser.asDouble(data["total"]), obj.total, "$trace.total $doesntMatchText")
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputBracketState(
        data: Map<String, Any>?,
        obj: TradeInputBracket?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asString(data["execution"]),
                obj.execution,
                "$trace.execution $doesntMatchText",
            )
            verifyInputTradeInputGoodUntilState(
                parser.asNativeMap(data["goodTil"]),
                obj.goodTil,
                "$trace.goodTil",
            )
            verifyInputTradeInputBracketTriggerState(
                parser.asNativeMap(data["stopLoss"]),
                obj.stopLoss,
                "$trace.stopLoss",
            )
            verifyInputTradeInputBracketTriggerState(
                parser.asNativeMap(data["takeProfit"]),
                obj.takeProfit,
                "$trace.takeProfit",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputBracketTriggerState(
        data: Map<String, Any>?,
        obj: TradeInputBracketSide?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["triggerPrice"]),
                obj.triggerPrice,
                "$trace.triggerPrice $doesntMatchText",
            )
            assertEquals(
                parser.asDouble(data["percent"]),
                obj.percent,
                "$trace.percent $doesntMatchText",
            )
            assertEquals(
                parser.asBool(data["reduceOnly"]) ?: false,
                obj.reduceOnly,
                "$trace.reduceOnly $doesntMatchText",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyWalletState(data: Map<String, Any>?, obj: Wallet?, trace: String) {
        /*
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asString(data["walletAddress"]),
                obj.walletAddress,
                "$trace.walletAddress"
            )
            verifyNumberState(parser.asNativeMap(data["balance"]), obj.balance, "$trace.balance")
            verifyWalletUserState(parser.asNativeMap(data["user"]), obj.user, "$trace.user")
        } else {
            assertNull(obj)
        }
         */
    }

    private fun verifyNumberState(
        data: Map<String, Any>?,
        obj: TradeStatesWithStringValues?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["current"]), obj.current, "$trace.current")
            assertEquals(parser.asString(data["postOrder"]), obj.postOrder, "$trace.postOrder")
            assertEquals(
                parser.asString(data["postAllOrders"]),
                obj.postAllOrders,
                "$trace.postAllOrders",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyWalletUserState(data: Map<String, Any>?, obj: User?, trace: String) {
        // Not needed for v4
    }

    private fun verifyLaunchIncentivePointsState(
        data: Map<String, Any>?,
        obj: LaunchIncentivePoints?,
        trace: String
    ) {
        if (data != null) {
            assertNotNull(obj)
        } else {
            assertNull(obj)
        }
    }

    internal open fun verifyAccountState(
        data: Map<String, Any>?,
        state: InternalAccountState?,
        staticTyping: Boolean,
        obj: Account?,
        trace: String
    ) {
        if (staticTyping) {
            verifyAccountSubaccountsState(
                internalState = state?.subaccounts,
                obj = obj?.subaccounts,
                trace = "$trace.subaccounts",
            )
            // TODO: Grouped subaccounts
        } else {
            if (data != null) {
                assertNotNull(obj)
                verifyAccountSubaccountsStateDeprecated(
                    parser.asNativeMap(data["subaccounts"]),
                    obj.subaccounts,
                    "$trace.subaccounts",
                )
                verifyAccountSubaccountsStateDeprecated(
                    parser.asNativeMap(data["groupedSubaccounts"]),
                    obj.groupedSubaccounts,
                    "$trace.groupedSubaccounts",
                )
                verifyLaunchIncentivePointsState(
                    parser.asNativeMap(data["launchIncentivePoints"]),
                    obj.launchIncentivePoints,
                    "$trace.launchIncentivePoints",
                )
            } else {
                assertNull(obj)
            }
        }
    }

    private fun verifyAccountSubaccountsState(
        internalState: Map<Int, InternalSubaccountState>?,
        obj: Map<String, Subaccount>?,
        trace: String,
    ) {
        if (internalState?.isNotEmpty() == true) {
            assertNotNull(obj)
            assertEquals(internalState.size, obj.size, "$trace.size $doesntMatchText")
            for ((key, itemData) in internalState) {
                verifyAccountSubaccountState(
                    internalState = itemData,
                    obj = obj[key.toString()],
                    trace = "$trace.$key",
                )
            }
        } else {
            assertTrue {
                (obj == null || obj.size == 0)
            }
        }
    }

    private fun verifyAccountSubaccountsStateDeprecated(
        data: Map<String, Any>?,
        obj: Map<String, Subaccount>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for ((key, itemData) in data) {
                verifyAccountSubaccountStateDeprecated(parser.asNativeMap(itemData), obj[key], "$trace.$key")
            }
        } else {
            assertTrue {
                (obj == null || obj.size == 0)
            }
        }
    }

    private fun verifyAccountSubaccountState(
        internalState: InternalSubaccountState?,
        obj: Subaccount?,
        trace: String,
    ) {
        if (internalState != null) {
            assertNotNull(obj)
            assertEquals(internalState.subaccountNumber, obj.subaccountNumber, "$trace.subaccountNumber")
            assertEquals(internalState.marginEnabled, obj.marginEnabled, "$trace.marginEnabled")
            // TODO: Calculated fields
//            assertEquals(parser.asDouble(data["pnl24h"]), obj.pnl24h, "$trace.pnl24h")
//            assertEquals(
//                parser.asDouble(data["pnl24hPercent"]),
//                obj.pnl24hPercent,
//                "$trace.pnl24hPercent",
//            )
//            assertEquals(parser.asDouble(data["pnlTotal"]), obj.pnlTotal, "$trace.pnlTotal")
//            assertEquals(parser.asString(data["positionId"]), obj.positionId, "$trace.positionId
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountStateDeprecated(
        data: Map<String, Any>?,
        obj: Subaccount?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asInt(data["subaccountNumber"]) ?: 0,
                obj.subaccountNumber,
                "$trace.subaccountNumber",
            )
            assertEquals(
                parser.asBool(data["marginEnabled"]) ?: true,
                obj.marginEnabled,
                "$trace.marginEnabled",
            )
            assertEquals(parser.asDouble(data["pnl24h"]), obj.pnl24h, "$trace.pnl24h")
            assertEquals(
                parser.asDouble(data["pnl24hPercent"]),
                obj.pnl24hPercent,
                "$trace.pnl24hPercent",
            )
            assertEquals(parser.asDouble(data["pnlTotal"]), obj.pnlTotal, "$trace.pnlTotal")
            assertEquals(parser.asString(data["positionId"]), obj.positionId, "$trace.positionId")
            verifyDoubleValues(
                parser.asNativeMap(data["adjustedImf"]),
                obj.adjustedImf,
                "$trace.adjustedImf",
            )
            verifyDoubleValues(parser.asNativeMap(data["equity"]), obj.equity, "$trace.equity")
            verifyDoubleValues(
                parser.asNativeMap(data["buyingPower"]),
                obj.buyingPower,
                "$trace.buyingPower",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["leverage"]),
                obj.leverage,
                "$trace.leverage",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["freeCollateral"]),
                obj.freeCollateral,
                "$trace.freeCollateral",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["initialRiskTotal"]),
                obj.initialRiskTotal,
                "$trace.initialRiskTotal",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["marginUsage"]),
                obj.marginUsage,
                "$trace.marginUsage",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["valueTotal"]),
                obj.valueTotal,
                "$trace.valueTotal",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["notionalTotal"]),
                obj.notionalTotal,
                "$trace.notionalTotal",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["quoteBalance"]),
                obj.quoteBalance,
                "$trace.quoteBalance",
            )
            verifyAccountSubaccountOpenPositions(
                parser.asNativeMap(data["openPositions"]),
                obj.openPositions,
                "$trace.openPositions",
            )
            verifyAccountSubaccountPendingPositions(
                parser.asNativeList(data["pendingPositions"]),
                obj.pendingPositions,
                "$trace.pendingPositions",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyDoubleValues(
        data: Map<String, Any>?,
        obj: TradeStatesWithDoubleValues?,
        trace: String,
    ) {
        assertEquals(parser.asDouble(data?.get("current")), obj?.current, "$trace.current")
        assertEquals(parser.asDouble(data?.get("postOrder")), obj?.postOrder, "$trace.postOrder")
        assertEquals(
            parser.asDouble(data?.get("postAllOrders")),
            obj?.postAllOrders,
            "$trace.postAllOrders",
        )
    }

    private fun verifyAccountSubaccountOpenPositions(
        data: Map<String, Any>?,
        obj: List<SubaccountPosition>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (position in obj) {
                val positionId = position.id
                verifyAccountSubaccountOpenPosition(
                    parser.asNativeMap(data[positionId]),
                    position,
                    "$trace.$positionId",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountOpenPosition(
        data: Map<String, Any>?,
        obj: SubaccountPosition?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(parser.asString(data["assetId"]), obj.assetId, "$trace.assetId")
            assertEquals(parser.asDouble(data["exitPrice"]), obj.exitPrice, "$trace.exitPrice")
            assertEquals(parser.asDouble(data["netFunding"]), obj.netFunding, "$trace.netFunding")
            assertEquals(
                parser.asDatetime(data["closedAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.closedAtMilliseconds,
                "$trace.closedAt",
            )
            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["adjustedImf"]),
                obj.adjustedImf,
                "$trace.adjustedImf",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["adjustedMmf"]),
                obj.adjustedMmf,
                "$trace.adjustedMmf",
            )
            verifyDoubleValues(parser.asNativeMap(data["size"]), obj.size, "$trace.size")
            verifyDoubleValues(
                parser.asNativeMap(data["entryPrice"]),
                obj.entryPrice,
                "$trace.entryPrice",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["leverage"]),
                obj.leverage,
                "$trace.leverage",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["maxLeverage"]),
                obj.maxLeverage,
                "$trace.maxLeverage",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["buyingPower"]),
                obj.buyingPower,
                "$trace.buyingPower",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["initialRiskTotal"]),
                obj.initialRiskTotal,
                "$trace.initialRiskTotal",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["liquidationPrice"]),
                obj.liquidationPrice,
                "$trace.liquidationPrice",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["notionalTotal"]),
                obj.notionalTotal,
                "$trace.notionalTotal",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["realizedPnl"]),
                obj.realizedPnl,
                "$trace.realizedPnl",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["realizedPnlPercent"]),
                obj.realizedPnlPercent,
                "$trace.realizedPnlPercent",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["unrealizedPnl"]),
                obj.unrealizedPnl,
                "$trace.unrealizedPnl",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["unrealizedPnlPercent"]),
                obj.unrealizedPnlPercent,
                "$trace.unrealizedPnlPercent",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["valueTotal"]),
                obj.valueTotal,
                "$trace.valueTotal",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["quoteBalance"]),
                obj.quoteBalance,
                "$trace.quoteBalance",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["equity"]),
                obj.equity,
                "$trace.equity",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountPendingPositions(
        data: List<Any>?,
        obj: IList<SubaccountPendingPosition>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (i in 0 until obj.size) {
                val position = obj[i]
                val positionId = position.assetId
                val itemData = parser.asNativeMap(data[i])
                assertNotNull(itemData)
                verifyAccountSubaccountPendingPosition(
                    itemData,
                    position,
                    "$trace.$positionId",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountPendingPosition(
        data: Map<String, Any>?,
        obj: SubaccountPendingPosition?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["assetId"]), obj.assetId, "$trace.assetId")
            assertEquals(parser.asInt(data["orderCount"]), obj.orderCount, "$trace.orderCount")
            verifyDoubleValues(
                parser.asNativeMap(data["quoteBalance"]),
                obj.quoteBalance,
                "$trace.quoteBalance",
            )
            verifyDoubleValues(
                parser.asNativeMap(data["equity"]),
                obj.equity,
                "$trace.equity",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountOrders(
        data: Map<String, Any>?,
        obj: List<SubaccountOrder>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (order in obj) {
                val orderId = order.id
                verifyAccountSubaccountOrder(
                    parser.asNativeMap(data[orderId]),
                    order,
                    "$trace.$orderId",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountOrder(
        data: Map<String, Any>?,
        obj: SubaccountOrder?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(parser.asString(data["marketId"]), obj.marketId, "$trace.marketId")
            assertEquals(
                parser.asString(data["cancelReason"]),
                obj.cancelReason,
                "$trace.cancelReason",
            )
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price")
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size")
            assertEquals(
                parser.asDouble(data["triggerPrice"]),
                obj.triggerPrice,
                "$trace.triggerPrice",
            )
            assertEquals(parser.asBool(data["postOnly"]) ?: false, obj.postOnly, "$trace.postOnly")
            assertEquals(
                parser.asBool(data["reduceOnly"]) ?: false,
                obj.reduceOnly,
                "$trace.reduceOnly",
            )
            assertEquals(
                parser.asDouble(data["remainingSize"]),
                obj.remainingSize,
                "$trace.remainingSize",
            )
            assertEquals(
                parser.asDouble(data["totalFilled"]),
                obj.totalFilled,
                "$trace.totalFilled",
            )
            assertEquals(
                parser.asDouble(data["trailingPercent"]),
                obj.trailingPercent,
                "$trace.trailingPercent",
            )
            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt",
            )
            assertEquals(
                parser.asDatetime(data["expiresAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.expiresAtMilliseconds,
                "$trace.expiresAt",
            )
            assertEquals(
                parser.asDatetime(data["unfillableAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.unfillableAtMilliseconds,
                "$trace.unfillableAt",
            )
        } else {
            assertNull(obj)
        }
    }

    internal fun test(
        load: LoadingFunction,
        expected: String?,
        moreVerification: VerificationFunction? = null,
    ): StateResponse {
        val response = load()
        if (doAsserts) {
            if (expected != null) {
                test(perp, expected)
            }
            moreVerification?.invoke(response)
            verifyState(response.state)
        }
        return response
    }

    private fun verifyAssetsState(
        data: Map<String, Any>?,
        obj: Map<String, Asset>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifyAsset(parser.asNativeMap(itemData), obj?.get(key), "$trace.$key")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAsset(
        data: Map<String, Any>?,
        obj: Asset?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asString(data.get("id")),
                obj.id,
                "$trace.id",
            )
            assertEquals(
                parser.asString(data.get("name")),
                obj.name,
                "$trace.name",
            )
            val tagsData = parser.asList(data["tags"])
            val tags = obj.tags
            assertEquals(
                tagsData?.size,
                tags?.size,
                "$trace.tags.size",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsState(
        data: Map<String, Any>?,
        assets: Map<String, Any>?,
        obj: PerpetualMarketSummary?,
        trace: String,
    ) {
        assertEquals(parser.asDouble(data?.get("trades24H")), obj?.trades24H, "$trace.trades24H")
        assertEquals(
            parser.asDouble(data?.get("volume24HUSDC")),
            obj?.volume24HUSDC,
            "$trace.volume24HUSDC",
        )
        assertEquals(
            parser.asDouble(data?.get("openInterestUSDC")),
            obj?.openInterestUSDC,
            "$trace.openInterestUSDC",
        )
        verifyMarkets(
            parser.asNativeMap(data?.get("markets")),
            assets,
            obj?.markets,
            "$trace.markets",
        )
    }

    private fun verifyMarkets(
        data: Map<String, Any>?,
        assets: Map<String, Any>?,
        obj: Map<String, PerpetualMarket>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, marketData) in data) {
                verifyMarket(parser.asNativeMap(marketData), assets, obj?.get(key), "$trace.$key")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarket(
        data: Map<String, Any>?,
        assets: Map<String, Any>?,
        obj: PerpetualMarket?,
        trace: String
    ) {
        val assetId = parser.asString(data?.get("assetId"))
        val asset = if (assetId != null) parser.asNativeMap(assets?.get(assetId)) else null
        val name = asset?.get("name")
        if (data != null &&
            data["id"] != null &&
            (
                parser.asBool(parser.value(data, "status.canTrade")) == true ||
                    parser.asBool(parser.value(data, "status.canReduce")) == true
                ) &&
            (parser.asInt(parser.value(data, "configs.stepSize"))) != null &&
            (parser.asInt(parser.value(data, "configs.tickSize"))) != null &&
            (parser.asDouble(parser.value(data, "configs.initialMarginFraction"))) != null &&
            (parser.asDouble(parser.value(data, "configs.maintenanceMarginFraction"))) != null &&
            (parser.asInt(parser.value(data, "configs.v4.clobPairId"))) != null &&
            (parser.asInt(parser.value(data, "configs.v4.atomicResolution"))) != null &&
            (parser.asInt(parser.value(data, "configs.v4.stepBaseQuantums"))) != null &&
            (parser.asInt(parser.value(data, "configs.v4.quantumConversionExponent"))) != null &&
            (parser.asInt(parser.value(data, "configs.v4.subticksPerTick"))) != null
        ) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(parser.asString(data["assetId"]), obj.assetId, "$trace.assetId")
            assertEquals(parser.asString(data["market"]), obj.market, "$trace.market")
            assertEquals(parser.asDouble(data["marketCaps"]), obj.marketCaps, "$trace.marketCaps")
            assertEquals(
                parser.asDouble(data["oraclePrice"]),
                obj.oraclePrice,
                "$trace.oraclePrice",
            )
            assertEquals(
                parser.asDouble(data["priceChange24H"]),
                obj.priceChange24H,
                "$trace.priceChange24H",
            )
            assertEquals(
                parser.asDouble(data["priceChange24HPercent"]),
                obj.priceChange24HPercent,
                "$trace.priceChange24HPercent",
            )
            verifyMarketConfigs(parser.asNativeMap(data["configs"]), obj.configs, "$trace.configs")
            verifyMarketPerpetual(
                parser.asNativeMap(data["perpetual"]),
                obj.perpetual,
                "$trace.perpetual",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketConfigs(data: Map<String, Any>?, obj: MarketConfigs?, trace: String) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["clobPairId"]), obj.clobPairId, "$trace.clobPairId")
            assertEquals(parser.asDouble(data["stepSize"]), obj.stepSize, "$trace.stepSize")
            assertEquals(parser.asDouble(data["tickSize"]), obj.tickSize, "$trace.tickSize")
            assertEquals(
                parser.asDouble(data["displayStepSize"] ?: data["stepSize"]),
                obj.displayStepSize,
                "$trace.displayStepSize",
            )
            assertEquals(
                parser.asDouble(data["displayTickSize"] ?: data["tickSize"]),
                obj.displayTickSize,
                "$trace.displayTickSize",
            )
            assertEquals(
                parser.asDouble(data["baselinePositionSize"]),
                obj.baselinePositionSize,
                "$trace.baselinePositionSize",
            )
            assertEquals(
                parser.asDouble(data["maxPositionSize"]),
                obj.maxPositionSize,
                "$trace.maxPositionSize",
            )
            assertEquals(
                parser.asDouble(data["minOrderSize"]),
                obj.minOrderSize,
                "$trace.minOrderSize",
            )
            assertEquals(
                parser.asDouble(data["incrementalInitialMarginFraction"]),
                obj.incrementalInitialMarginFraction,
                "$trace.incrementalInitialMarginFraction",
            )
            assertEquals(
                parser.asDouble(data["incrementalPositionSize"]),
                obj.incrementalPositionSize,
                "$trace.incrementalPositionSize",
            )
            assertEquals(
                parser.asDouble(data["initialMarginFraction"]),
                obj.initialMarginFraction,
                "$trace.initialMarginFraction",
            )
            assertEquals(
                parser.asDouble(data["maintenanceMarginFraction"]),
                obj.maintenanceMarginFraction,
                "$trace.maintenanceMarginFraction",
            )
            assertEquals(
                PerpetualMarketType.invoke(parser.asString(data["perpetualMarketType"])),
                obj.perpetualMarketType,
                "$trace.perpetualMarketType",
            )
            assertEquals(parser.asInt(data["largeSize"]), obj.largeSize, "$trace.largeSize")
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketPerpetual(
        data: Map<String, Any>?,
        obj: MarketPerpetual?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["openInterestUSDC"]),
                obj.openInterestUSDC,
                "$trace.openInterestUSDC",
            )
            assertEquals(
                parser.asDouble(data["volume24HUSDC"]),
                obj.volume24HUSDC,
                "$trace.volume24HUSDC",
            )
            assertEquals(parser.asDouble(data["trades24H"]), obj.trades24H, "$trace.trades24H")
            assertEquals(parser.asDouble(data["volume24H"]), obj.volume24H, "$trace.volume24H")
            assertEquals(
                parser.asDouble(data["nextFundingRate"]),
                obj.nextFundingRate,
                "$trace.nextFundingRate",
            )
            assertEquals(
                parser.asDouble(data["openInterest"]),
                obj.openInterest,
                "$trace.openInterest",
            )
            assertEquals(
                parser.asDatetime(data["nextFundingAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.nextFundingAtMilliseconds,
                "$trace.nextFundingAt",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsHistoricalFundingsState(
        data: Map<String, Any>?,
        obj: Map<String, List<MarketHistoricalFunding>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifyMarketsHistoricalFundingsArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "historicalFunding",
                        ),
                    ),
                    obj?.get(key),
                    "$trace.$key",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsHistoricalFundingsArrayState(
        data: List<Any>?,
        obj: List<MarketHistoricalFunding>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifyMarketsHistoricalFundingState(
                    parser.asNativeMap(data[i]),
                    obj[i],
                    "$trace.$i",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsHistoricalFundingState(
        data: Map<String, Any>?,
        obj: MarketHistoricalFunding?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asDouble(data["rate"]), obj.rate, "$trace.rate")
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price")

            assertEquals(
                parser.asDatetime(data["effectiveAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.effectiveAtMilliseconds,
                "$trace.effectiveAt",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsCandlesState(
        data: Map<String, Any>?,
        obj: Map<String, MarketCandles>?,
        trace: String,
    ) {
        if (data != null) {
            for ((marketKey, itemData) in data) {
                val candleMap = parser.asNativeMap(
                    parser.value(
                        itemData,
                        "candles",
                    ),
                )
                val candles = obj?.get(marketKey)
                if (candleMap != null) {
                    for ((resolutionKey, resolutionData) in candleMap) {
                        val candlesData = parser.asList(resolutionData)
                        val candleArray = candles?.candles?.get(resolutionKey)

                        verifyMarketsCandlesArrayState(
                            candlesData,
                            candleArray,
                            "$trace.$marketKey.$resolutionKey",
                        )
                    }
                }
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsCandlesArrayState(
        data: List<Any>?,
        obj: List<MarketCandle>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifyMarketsCandleState(parser.asNativeMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsCandleState(
        data: Map<String, Any>?,
        obj: MarketCandle?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["baseTokenVolume"]),
                obj.baseTokenVolume,
                "$trace.baseTokenVolume",
            )
            assertEquals(parser.asDouble(data["high"]), obj.high, "$trace.high")
            assertEquals(parser.asDouble(data["low"]), obj.low, "$trace.low")
            assertEquals(parser.asDouble(data["close"]), obj.close, "$trace.close")
            assertEquals(parser.asDouble(data["open"]), obj.open, "$trace.open")
            assertEquals(parser.asDouble(data["usdVolume"]), obj.usdVolume, "$trace.usdVolume")

            assertEquals(
                parser.asDatetime(data["startedAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.startedAtMilliseconds,
                "$trace.startedAt",
            )
            assertEquals(
                parser.asDatetime(data["updatedAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.updatedAtMilliseconds,
                "$trace.updatedAt",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsTradesState(
        data: Map<String, Any>?,
        obj: Map<String, List<MarketTrade>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifyMarketsTradesArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "trades",
                        ),
                    ),
                    obj?.get(key),
                    "$trace.$key",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsTradesArrayState(
        data: List<Any>?,
        obj: List<MarketTrade>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifyMarketsTradeState(parser.asNativeMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsTradeState(
        data: Map<String, Any>?,
        obj: MarketTrade?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["price"]),
                obj.price,
                "$trace.price",
            )
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size")
            assertEquals(
                parser.asString(data["type"]),
                obj.type?.rawValue,
                "$trace.liquidation",
            )
            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsOrderbookState(
        data: Map<String, Any>?,
        obj: Map<String, MarketOrderbook>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifyMarketOrderbook(
                    parser.asNativeMap(
                        parser.value(
                            itemData,
                            "orderbook",
                        ),
                    ),
                    obj?.get(key),
                    "$trace.$key",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketOrderbook(
        data: Map<String, Any>?,
        obj: MarketOrderbook?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asDouble(data["midPrice"]), obj.midPrice, "$trace.midPrice")
            assertEquals(
                parser.asDouble(data["spreadPercent"]),
                obj.spreadPercent,
                "$trace.spreadPercent",
            )
            verifyMarketOrderbookSide(parser.asList(data["asks"]), obj.asks, "$trace.asks")
            verifyMarketOrderbookSide(parser.asList(data["bids"]), obj.bids, "$trace.bids")
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketOrderbookSide(
        data: List<Any>?,
        obj: List<OrderbookLine>?,
        trace: String,
    ) {
        val compacted = data?.mapNotNull { item ->
            val lineItem = parser.asNativeMap(item)
            val size = parser.asDouble(lineItem?.get("size"))
            if (size != Numeric.double.ZERO) lineItem else null
        }

        assertEquals(compacted?.size, obj?.size, "$trace.size")
        if (obj != null) {
            for (i in obj.indices) {
                verifyMarketOrderbookLine(
                    parser.asNativeMap(compacted?.get(i)),
                    obj[i],
                    "$trace.$i",
                )
            }
        }
    }

    private fun verifyMarketOrderbookLine(
        data: Map<String, Any>?,
        obj: OrderbookLine?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size")
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price")
            assertEquals(parser.asDouble(data["depth"]), obj.depth, "$trace.depth")
            assertEquals(parser.asInt(data["offset"]) ?: 0, obj.offset, "$trace.offset")
        } else {
            assertNull(obj)
        }
    }

    private fun verifyConfigs(
        data: Map<String, Any>?,
        obj: Configs?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            verifyConfigsNetwork(parser.asNativeMap(data["network"]), obj.network, "$trace.network")
            verifyConfigsFeeTiers(parser.asList(data["feeTiers"]), obj.feeTiers, "$trace.feeTiers")
            verifyConfigsFeeDiscounts(
                parser.asList(data["feeDiscounts"]),
                obj.feeDiscounts,
                "$trace.feeDiscounts",
            )
        } else {
            assertNull(obj)
        }
//        assertEquals(
//            parser.asDouble(data?.get("volume24HUSDC")),
//            obj?.volume24HUSDC,
//            "$trace.volume24HUSDC"
//        )
//        assertEquals(
//            parser.asDouble(data?.get("openInterestUSDC")),
//            obj?.openInterestUSDC,
//            "$trace.openInterestUSDC"
//        )
//        verifyMarkets(parser.asNativeMap(data?.get("markets")), obj?.markets, "$trace.markets")
    }

    private fun verifyConfigsNetwork(
        data: Map<String, Any>?,
        obj: NetworkConfigs?,
        trace: String,
    ) {
    }

    private fun verifyConfigsFeeTiers(
        data: List<Any>?,
        obj: List<FeeTier>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertTrue { data.size >= obj.size }
            for (i in obj.indices) {
                verifyConfigsFeeTier(parser.asNativeMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyConfigsFeeTier(
        data: Map<String, Any>?,
        obj: FeeTier?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asDouble(data["maker"]), obj.maker, "$trace.maker")
            assertEquals(parser.asDouble(data["taker"]), obj.taker, "$trace.taker")
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(parser.asString(data["tier"]), obj.tier, "$trace.tier")
            assertEquals(parser.asString(data["symbol"]), obj.symbol, "$trace.symbol")
            assertEquals(parser.asInt(data["volume"]), obj.volume, "$trace.volume")
            assertEquals(parser.asDouble(data["totalShare"]), obj.totalShare, "$trace.totalShare")
            assertEquals(parser.asDouble(data["makerShare"]), obj.makerShare, "$trace.makerShare")
        } else {
            assertNull(obj)
        }
    }

    private fun verifyConfigsFeeDiscounts(
        data: List<Any>?,
        obj: List<FeeDiscount>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size)
            for (i in obj.indices) {
                verifyConfigsFeeDiscount(parser.asNativeMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyConfigsFeeDiscount(
        data: Map<String, Any>?,
        obj: FeeDiscount?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(parser.asString(data["tier"]), obj.tier, "$trace.tier")
            assertEquals(parser.asString(data["symbol"]), obj.symbol, "$trace.symbol")
            assertEquals(parser.asInt(data["balance"]), obj.balance, "$trace.balance")
            assertEquals(parser.asDouble(data["discount"]), obj.discount, "$trace.discount")
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountFillsState(
        data: Map<String, Any>?,
        obj: Map<String, List<SubaccountFill>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifySubaccountFillsArrayState(
                    parser.asList(parser.value(itemData, "fills")),
                    obj?.get(key),
                    "$trace.$key",
                )
            }
        } else {
            assertTrue { obj.isNullOrEmpty() }
        }
    }

    private fun verifySubaccountFillsArrayState(
        data: List<Any>?,
        obj: List<SubaccountFill>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifySubaccountFillState(parser.asNativeMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountFillState(
        data: Map<String, Any>?,
        obj: SubaccountFill?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(parser.asString(data["marketId"]), obj.marketId, "$trace.marketId")
            assertEquals(parser.asString(data["orderId"]), obj.orderId, "$trace.orderId")
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size")
            assertEquals(parser.asDouble(data["fee"]), obj.fee, "$trace.fee")
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price")
            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountTransfersState(
        data: Map<String, Any>?,
        obj: Map<String, List<SubaccountTransfer>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifySubaccountTransfersArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "transfers",
                        ),
                    ),
                    obj?.get(key),
                    "$trace.$key",
                )
            }
        } else {
            assertTrue { obj.isNullOrEmpty() }
        }
    }

    private fun verifySubaccountTransfersArrayState(
        data: List<Any>?,
        obj: List<SubaccountTransfer>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifySubaccountTransferState(parser.asNativeMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountTransferState(
        data: Map<String, Any>?,
        obj: SubaccountTransfer?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(
                parser.asString(data["fromAddress"]),
                obj.fromAddress,
                "$trace.fromAddress",
            )
            assertEquals(parser.asString(data["toAddress"]), obj.toAddress, "$trace.toAddress")
            assertEquals(
                parser.asString(data["asset"]),
                obj.asset,
                "$trace.asset",
            )
            assertEquals(
                parser.asDouble(data["amount"]),
                obj.amount,
                "$trace.amount",
            )
            assertEquals(
                parser.asInt(data["updatedAtBlock"]),
                obj.updatedAtBlock,
                "$trace.updatedAtBlock",
            )
            assertEquals(
                parser.asString(data["transactionHash"]),
                obj.transactionHash,
                "$trace.transactionHash",
            )
            assertEquals(
                (
                    parser.asDatetime(data["confirmedAt"])
                        ?: parser.asDatetime(data["createdAt"])
                    )?.toEpochMilliseconds()?.toDouble(),
                obj.updatedAtMilliseconds,
                "$trace.updatedAt",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountFundingPaymentsState(
        data: Map<String, Any>?,
        obj: Map<String, List<SubaccountFundingPayment>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifySubaccountFundingPaymentsArraysArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "fundingPayments",
                        ),
                    ),
                    obj?.get(key),
                    "$trace.$key",
                )
            }
        } else {
            assertTrue { obj.isNullOrEmpty() }
        }
    }

    private fun verifySubaccountFundingPaymentsArraysArrayState(
        data: List<Any>?,
        obj: List<SubaccountFundingPayment>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifySubaccountFundingPaymentState(
                    parser.asNativeMap(data[i]),
                    obj[i],
                    "$trace.$i",
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountFundingPaymentState(
        data: Map<String, Any>?,
        obj: SubaccountFundingPayment?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["marketId"]), obj.marketId, "$trace.marketId")
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price")
            assertEquals(parser.asDouble(data["payment"]), obj.payment, "$trace.payment")
            assertEquals(parser.asDouble(data["rate"]), obj.rate, "$trace.rate")
            assertEquals(
                parser.asDouble(data["positionSize"]),
                obj.positionSize,
                "$trace.positionSize",
            )
            assertEquals(
                parser.asDatetime(data["effectiveAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.effectiveAtMilliSeconds,
                "$trace.effectiveAt",
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountHistoricalPNLsState(
        data: Map<String, Any>?,
        obj: Map<String, List<SubaccountHistoricalPNL>>?,
        startTime: Instant,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifySubaccountHistoricalPNLsArraysArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "historicalPnl",
                        ),
                    ),
                    obj?.get(key),
                    startTime,
                    "$trace.$key",
                )
            }
        } else {
        }
    }

    private fun verifySubaccountHistoricalPNLsArraysArrayState(
        data: List<Any>?,
        obj: List<SubaccountHistoricalPNL>?,
        startTime: Instant,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            val filteredData = data.mapNotNull { item ->
                val itemData = parser.asNativeMap(item)!!
                val createdAt = parser.asDatetime(itemData["createdAt"])!!
                if (createdAt >= startTime) item else null
            }
            assertEquals(filteredData.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifySubaccountHistoricalPNLState(
                    parser.asNativeMap(filteredData[i]),
                    obj[i],
                    "$trace.$i",
                )
            }
        } else {
//            assertNull(obj)
        }
    }

    private fun verifySubaccountHistoricalPNLState(
        data: Map<String, Any>?,
        obj: SubaccountHistoricalPNL?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asDouble(data["equity"]), obj.equity, "$trace.equity")
            assertEquals(parser.asDouble(data["totalPnl"]), obj.totalPnl, "$trace.totalPnl")
            assertEquals(
                parser.asDouble(data["netTransfers"]),
                obj.netTransfers,
                "$trace.netTransfers",
            )

            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt",
            )
        } else {
            assertNull(obj)
        }
    }
}
