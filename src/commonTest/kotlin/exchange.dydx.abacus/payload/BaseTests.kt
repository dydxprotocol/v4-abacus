package exchange.dydx.abacus.payload

import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestFileSystem
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.app.manager.TestThreading
import exchange.dydx.abacus.app.manager.TestTimer
import exchange.dydx.abacus.app.manager.TestWebSocket
import exchange.dydx.abacus.output.Account
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.Configs
import exchange.dydx.abacus.output.FeeDiscount
import exchange.dydx.abacus.output.FeeTier
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
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountFundingPayment
import exchange.dydx.abacus.output.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.SubaccountTransfer
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.User
import exchange.dydx.abacus.output.Wallet
import exchange.dydx.abacus.output.input.ClosePositionInput
import exchange.dydx.abacus.output.input.ClosePositionInputSize
import exchange.dydx.abacus.output.input.Input
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
import exchange.dydx.abacus.state.app.AppVersion
import exchange.dydx.abacus.state.app.helper.DynamicLocalizer
import exchange.dydx.abacus.state.modal.PerpTradingStateMachine
import exchange.dydx.abacus.state.modal.TradingStateMachine
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.satisfies
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

open class BaseTests(private val maxSubaccountNumber: Int) {
    open val doAsserts = true
    internal val doesntMatchText = "doesn't match"
    internal val parser = Parser()
    internal val mock = AbacusMockData()
    internal var perp = createState()

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
            )
        }

        fun testLocalizer(ioImplementations: IOImplementations): LocalizerProtocol {
            return DynamicLocalizer(
                ioImplementations,
                "en",
                "/config",
                "https://dydx-v4-shared-resources.vercel.app/config",
            )
        }

        fun testUIImplementations(localizer: LocalizerProtocol?): UIImplementations {
            return UIImplementations(localizer, null)
        }
    }

    internal open fun createState(): PerpTradingStateMachine {
        val ioImplementations = testIOImplementations()
        return PerpTradingStateMachine(
            mock.v4Environment,
            testLocalizer(ioImplementations),
            null,
            AppVersion.v3,
            maxSubaccountNumber,
        )
    }

    internal open fun reset() {
        perp.state = null
        perp.input = null
        perp.marketsSummary = null
        perp.wallet = null
        perp.assets = null
        perp.configs = null
        setup()
    }

    internal open fun setup() {
    }

    internal fun test(perp: TradingStateMachine, expected: String) {
        val json = Json.parseToJsonElement(expected)
        val data = perp.data
        assertNotNull(data, "Missing data")
        data.satisfies(parser.asMap(json), parser)
    }

    internal fun test(perp: TradingStateMachine, json: JsonElement?) {
        val map = parser.asMap(json)
        assertNotNull(map)
        for ((key, value) in map) {
            val obj = obj(perp, key)
            assertNotNull(obj)
            val expectations = parser.asMap(value)
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

    private fun testItem(obj: Any, expectations: IMap<String, Any>) {
        for ((key, value) in expectations) {
            val numeric = parser.asDouble(value)
            if (numeric != null) {
                assertEquals(
                    numeric,
                    parser.asDouble(parser.value(obj, key)),
                    "$key $value not matching"
                )
            } else {
                assertEquals(
                    parser.asString(value),
                    parser.asString(parser.value(obj, key)),
                    "$key $value not matching"
                )
            }
        }
    }

    internal fun verifyState(state: PerpetualState?) {
        verifyConfigs(perp.configs, state?.configs, "configs")
        verifyWalletState(perp.wallet, state?.wallet, "wallet")
        verifyAccountState(perp.account, state?.account, "account")
        verifySubaccountFillsState(
            parser.asMap(perp.account?.get("subaccounts")),
            state?.fills,
            "fills"
        )
        verifySubaccountTransfersState(
            parser.asMap(perp.account?.get("subaccounts")),
            state?.transfers,
            "transfers"
        )
        verifySubaccountFundingPaymentsState(
            parser.asMap(perp.account?.get("subaccounts")),
            state?.fundingPayments,
            "fundingPayments"
        )
        verifySubaccountHistoricalPNLsState(
            parser.asMap(perp.account?.get("subaccounts")),
            state?.historicalPnl,
            ServerTime.now() - perp.historicalPnlDays.days,
            "historicalPnl"
        )
        verifyAssetsState(perp.assets, state?.assets, "assets")
        verifyMarketsState(
            perp.marketsSummary,
            perp.assets,
            state?.marketsSummary,
            "markets"
        )
        verifyMarketsHistoricalFundingsState(
            parser.asMap(perp.marketsSummary?.get("markets")),
            state?.historicalFundings,
            "historicalFundings"
        )
        verifyMarketsTradesState(
            parser.asMap(perp.marketsSummary?.get("markets")),
            state?.trades,
            "trades"
        )
        verifyMarketsCandlesState(
            parser.asMap(perp.marketsSummary?.get("markets")),
            state?.candles,
            "candles"
        )
        verifyMarketsOrderbookState(
            parser.asMap(perp.marketsSummary?.get("markets")),
            state?.orderbooks,
            "orderbooks"
        )
        verifyInputState(perp.input, state?.input, "input")
    }

    private fun verifyInputState(data: IMap<String, Any>?, obj: Input?, trace: String) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["current"]), obj.current?.rawValue, "$trace.current")
            when (obj.current?.rawValue) {
                "trade" -> {
                    verifyInputTradeState(parser.asMap(data["trade"]), obj.trade, "$trace.trade")
                }

                "closePosition" -> {
                    verifyInputClosePositionState(
                        parser.asMap(data["closePosition"]),
                        obj.closePosition,
                        "$trace.closePosition"
                    )
                }
            }

            verifyInputReceiptLinesState(
                parser.asList(data["receiptLines"]),
                obj.receiptLines,
                "$trace.receiptLines"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeState(data: IMap<String, Any>?, obj: TradeInput?, trace: String) {
        if (data != null) {
            assertNotNull(obj, "$trace should not be null")
            assertEquals(
                parser.asString(data["type"]),
                obj.type?.rawValue,
                "$trace.type $doesntMatchText"
            )
            assertEquals(
                parser.asString(data["side"]),
                obj.side?.rawValue,
                "$trace.side $doesntMatchText"
            )
            assertEquals(
                parser.asString(data["marketId"]),
                obj.marketId,
                "$trace.marketId $doesntMatchText"
            )
            assertEquals(
                parser.asString(data["execution"]),
                obj.execution,
                "$trace.execution $doesntMatchText"
            )
            assertEquals(
                parser.asString(data["timeInForce"]),
                obj.timeInForce,
                "$trace.timeInForce $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["postOnly"]) ?: false,
                obj.postOnly,
                "$trace.postOnly $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["reduceOnly"]) ?: false,
                obj.reduceOnly,
                "$trace.reduceOnly $doesntMatchText"
            )
            verifyInputTradeInputSizeState(parser.asMap(data["size"]), obj.size, "$trace.size")
            verifyInputTradeInputGoodUntilState(
                parser.asMap(data["goodUntil"]),
                obj.goodUntil,
                "$trace.goodUntil"
            )
            verifyInputTradeInputMarketOrderState(
                parser.asMap(data["marketOrder"]),
                obj.marketOrder,
                "$trace.marketOrder"
            )
            verifyInputTradeInputOptionsState(
                parser.asMap(data["options"]),
                obj.options,
                "$trace.options"
            )
            verifyInputTradeInputSummaryState(
                parser.asMap(data["summary"]),
                obj.summary,
                "$trace.summary"
            )
            verifyInputTradeInputBracketState(
                parser.asMap(data["bracket"]),
                obj.bracket,
                "$trace.bracket"
            )
        } else {
            assertNull(obj, "$trace should be null")
        }
    }

    private fun verifyInputClosePositionState(
        data: IMap<String, Any>?,
        obj: ClosePositionInput?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj, "$trace should not be null")
            assertEquals(
                parser.asString(data["marketId"]),
                obj.marketId,
                "$trace.marketId $doesntMatchText"
            )
            verifyInputClosePositionInputSizeState(
                parser.asMap(data["size"]),
                obj.size,
                "$trace.size"
            )
            verifyInputTradeInputMarketOrderState(
                parser.asMap(data["marketOrder"]),
                obj.marketOrder,
                "$trace.marketOrder"
            )
            verifyInputTradeInputSummaryState(
                parser.asMap(data["summary"]),
                obj.summary,
                "$trace.summary"
            )
        } else {
            assertNull(obj, "$trace should be null")
        }
    }

    private fun verifyInputClosePositionInputSizeState(
        data: IMap<String, Any>?,
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
                "$trace.usdcSize $doesntMatchText"
            )
            assertEquals(
                parser.asDouble(data["percent"]),
                obj.percent,
                "$trace.percent $doesntMatchText"
            )

        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputReceiptLinesState(
        data: IList<Any>?,
        obj: IList<ReceiptLine>?,
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
        data: IMap<String, Any>?,
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
                "$trace.usdcSize $doesntMatchText"
            )
            assertEquals(
                parser.asDouble(data["leverage"]),
                obj.leverage,
                "$trace.leverage $doesntMatchText"
            )

        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputPriceState(
        data: IMap<String, Any>?,
        obj: TradeInputPrice?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["limitPrice"]),
                obj.limitPrice,
                "$trace.limitPrice $doesntMatchText"
            )
            assertEquals(
                parser.asDouble(data["triggerPrice"]),
                obj.triggerPrice,
                "$trace.triggerPrice $doesntMatchText"
            )
            assertEquals(
                parser.asDouble(data["trailingPercent"]),
                obj.trailingPercent,
                "$trace.trailingPercent $doesntMatchText"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputGoodUntilState(
        data: IMap<String, Any>?,
        obj: TradeInputGoodUntil?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["unit"]), obj.unit, "$trace.unit $doesntMatchText")
            assertEquals(
                parser.asDouble(data["duration"]),
                obj.duration,
                "$trace.duration $doesntMatchText"
            )

        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputMarketOrderState(
        data: IMap<String, Any>?,
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
                "$trace.usdcSize $doesntMatchText"
            )
            assertEquals(
                parser.asDouble(data["worstPrice"]),
                obj.worstPrice,
                "$trace.worstPrice $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["filled"]) ?: false,
                obj.filled,
                "$trace.filled $doesntMatchText"
            )
            verifyInputTradeInputMarketOrderOrderbookUsageState(
                parser.asList(data["orderbook"]),
                obj.orderbook,
                "$trace.orderbook"
            )

        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputMarketOrderOrderbookUsageState(
        data: IList<Any>?,
        obj: IList<OrderbookUsage>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (i in obj.indices) {
                val itemData = parser.asMap(data[i])
                val item = obj[i]
                verifyInputTradeInputMarketOrderOrderbookUsageLineState(itemData, item, "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputMarketOrderOrderbookUsageLineState(
        data: IMap<String, Any>?,
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
        data: IMap<String, Any>?,
        obj: TradeInputOptions?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asBool(data["needsSize"]) ?: false,
                obj.needsSize,
                "$trace.needsSize $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["needsLeverage"]) ?: false,
                obj.needsLeverage,
                "$trace.needsLeverage $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["needsBrackets"]) ?: false,
                obj.needsBrackets,
                "$trace.needsBrackets $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["needsGoodUntil"]) ?: false,
                obj.needsGoodUntil,
                "$trace.needsGoodUntil $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["needsLimitPrice"]) ?: false,
                obj.needsLimitPrice,
                "$trace.needsLimitPrice $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["needsPostOnly"]) ?: false,
                obj.needsPostOnly,
                "$trace.needsPostOnly $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["needsReduceOnly"]) ?: false,
                obj.needsReduceOnly,
                "$trace.needsReduceOnly $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["needsTrailingPercent"]) ?: false,
                obj.needsTrailingPercent,
                "$trace.needsTrailingPercent $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["needsTriggerPrice"]) ?: false,
                obj.needsTriggerPrice,
                "$trace.needsTriggerPrice $doesntMatchText"
            )
            verifyInputTradeInputOptionsExecutionOptionsState(
                parser.asList(data["executionOptions"]),
                obj.executionOptions,
                "$trace.executionOptions"
            )
            verifyInputTradeInputOptionsExecutionOptionsState(
                parser.asList(data["timeInForceOptions"]),
                obj.timeInForceOptions,
                "$trace.timeInForceOptions"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputOptionsExecutionOptionsState(
        data: IList<Any>?,
        obj: IList<SelectionOption>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (i in obj.indices) {
                val itemData = parser.asMap(data[i])
                val item = obj[i]
                verifySelectionOptionState(itemData, item, "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }


    private fun verifySelectionOptionState(
        data: IMap<String, Any>?,
        obj: SelectionOption?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["type"]), obj.type, "$trace.type $doesntMatchText")
            assertEquals(
                parser.asString(data["stringKey"]),
                obj.stringKey,
                "$trace.stringKey $doesntMatchText"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputSummaryState(
        data: IMap<String, Any>?,
        obj: TradeInputSummary?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asBool(data["filled"]) ?: false,
                obj.filled,
                "$trace.filled $doesntMatchText"
            )
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size $doesntMatchText")
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price $doesntMatchText")
            assertEquals(parser.asDouble(data["fee"]), obj.fee, "$trace.fee $doesntMatchText")
            assertEquals(
                parser.asDouble(data["slippage"]),
                obj.slippage,
                "$trace.slippage $doesntMatchText"
            )
            assertEquals(
                parser.asDouble(data["usdcSize"]),
                obj.usdcSize,
                "$trace.usdcSize $doesntMatchText"
            )
            assertEquals(parser.asDouble(data["total"]), obj.total, "$trace.total $doesntMatchText")
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputBracketState(
        data: IMap<String, Any>?,
        obj: TradeInputBracket?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asString(data["execution"]),
                obj.execution,
                "$trace.execution $doesntMatchText"
            )
            verifyInputTradeInputGoodUntilState(
                parser.asMap(data["goodUntil"]),
                obj.goodUntil,
                "$trace.goodUntil"
            )
            verifyInputTradeInputBracketTriggerState(
                parser.asMap(data["stopLoss"]),
                obj.stopLoss,
                "$trace.stopLoss"
            )
            verifyInputTradeInputBracketTriggerState(
                parser.asMap(data["takeProfit"]),
                obj.takeProfit,
                "$trace.takeProfit"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyInputTradeInputBracketTriggerState(
        data: IMap<String, Any>?,
        obj: TradeInputBracketSide?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["triggerPrice"]),
                obj.triggerPrice,
                "$trace.triggerPrice $doesntMatchText"
            )
            assertEquals(
                parser.asDouble(data["percent"]),
                obj.percent,
                "$trace.percent $doesntMatchText"
            )
            assertEquals(
                parser.asBool(data["reduceOnly"]) ?: false,
                obj.reduceOnly,
                "$trace.reduceOnly $doesntMatchText"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyWalletState(data: IMap<String, Any>?, obj: Wallet?, trace: String) {
        /*
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asString(data["walletAddress"]),
                obj.walletAddress,
                "$trace.walletAddress"
            )
            verifyNumberState(parser.asMap(data["balance"]), obj.balance, "$trace.balance")
            verifyWalletUserState(parser.asMap(data["user"]), obj.user, "$trace.user")
        } else {
            assertNull(obj)
        }
         */
    }

    private fun verifyNumberState(
        data: IMap<String, Any>?,
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
                "$trace.postAllOrders"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyWalletUserState(data: IMap<String, Any>?, obj: User?, trace: String) {
        // Not needed for v4
    }

    private fun verifyAccountState(data: IMap<String, Any>?, obj: Account?, trace: String) {
        if (data != null) {
            assertNotNull(obj)
            verifyAccountSubaccountsState(
                parser.asMap(data["subaccounts"]),
                obj.subaccounts,
                "$trace.subaccounts"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountsState(
        data: IMap<String, Any>?,
        obj: IMap<String, Subaccount>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for ((key, itemData) in data) {
                verifyAccountSubaccountState(parser.asMap(itemData), obj[key], "$trace.$key")
            }
        } else {
            assertTrue {
                (obj == null || obj.size == 0)
            }
        }
    }

    private fun verifyAccountSubaccountState(
        data: IMap<String, Any>?,
        obj: Subaccount?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asInt(data["subaccountNumber"]) ?: 0,
                obj.subaccountNumber,
                "$trace.subaccountNumber"
            )
            assertEquals(
                parser.asBool(data["marginEnabled"]) ?: true,
                obj.marginEnabled,
                "$trace.marginEnabled"
            )
            assertEquals(parser.asDouble(data["pnl24h"]), obj.pnl24h, "$trace.pnl24h")
            assertEquals(
                parser.asDouble(data["pnl24hPercent"]),
                obj.pnl24hPercent,
                "$trace.pnl24hPercent"
            )
            assertEquals(parser.asDouble(data["pnlTotal"]), obj.pnlTotal, "$trace.pnlTotal")
            assertEquals(parser.asString(data["positionId"]), obj.positionId, "$trace.positionId")
            verifyDoubleValues(
                parser.asMap(data["adjustedImf"]),
                obj.adjustedImf,
                "$trace.adjustedImf"
            )
            verifyDoubleValues(parser.asMap(data["equity"]), obj.equity, "$trace.equity")
            verifyDoubleValues(
                parser.asMap(data["buyingPower"]),
                obj.buyingPower,
                "$trace.buyingPower"
            )
            verifyDoubleValues(parser.asMap(data["leverage"]), obj.leverage, "$trace.leverage")
            verifyDoubleValues(
                parser.asMap(data["freeCollateral"]),
                obj.freeCollateral,
                "$trace.freeCollateral"
            )
            verifyDoubleValues(
                parser.asMap(data["initialRiskTotal"]),
                obj.initialRiskTotal,
                "$trace.initialRiskTotal"
            )
            verifyDoubleValues(
                parser.asMap(data["marginUsage"]),
                obj.marginUsage,
                "$trace.marginUsage"
            )
            verifyDoubleValues(
                parser.asMap(data["valueTotal"]),
                obj.valueTotal,
                "$trace.valueTotal"
            )
            verifyDoubleValues(
                parser.asMap(data["notionalTotal"]),
                obj.notionalTotal,
                "$trace.notionalTotal"
            )
            verifyDoubleValues(
                parser.asMap(data["quoteBalance"]),
                obj.quoteBalance,
                "$trace.quoteBalance"
            )
            verifyAccountSubaccountOpenPositions(
                parser.asMap(data["openPositions"]),
                obj.openPositions,
                "$trace.openPositions"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyDoubleValues(
        data: IMap<String, Any>?,
        obj: TradeStatesWithDoubleValues?,
        trace: String,
    ) {
        assertEquals(parser.asDouble(data?.get("current")), obj?.current, "$trace.current")
        assertEquals(parser.asDouble(data?.get("postOrder")), obj?.postOrder, "$trace.postOrder")
        assertEquals(
            parser.asDouble(data?.get("postAllOrders")),
            obj?.postAllOrders,
            "$trace.postAllOrders"
        )
    }

    private fun verifyAccountSubaccountOpenPositions(
        data: IMap<String, Any>?,
        obj: IList<SubaccountPosition>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (position in obj) {
                val positionId = position.id
                verifyAccountSubaccountOpenPosition(
                    parser.asMap(data[positionId]),
                    position,
                    "$trace.$positionId"
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountOpenPosition(
        data: IMap<String, Any>?,
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
                "$trace.closedAt"
            )
            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt"
            )
            verifyDoubleValues(
                parser.asMap(data["adjustedImf"]),
                obj.adjustedImf,
                "$trace.adjustedImf"
            )
            verifyDoubleValues(
                parser.asMap(data["adjustedMmf"]),
                obj.adjustedMmf,
                "$trace.adjustedMmf"
            )
            verifyDoubleValues(parser.asMap(data["size"]), obj.size, "$trace.size")
            verifyDoubleValues(
                parser.asMap(data["entryPrice"]),
                obj.entryPrice,
                "$trace.entryPrice"
            )
            verifyDoubleValues(parser.asMap(data["leverage"]), obj.leverage, "$trace.leverage")
            verifyDoubleValues(
                parser.asMap(data["maxLeverage"]),
                obj.maxLeverage,
                "$trace.maxLeverage"
            )
            verifyDoubleValues(
                parser.asMap(data["buyingPower"]),
                obj.buyingPower,
                "$trace.buyingPower"
            )
            verifyDoubleValues(
                parser.asMap(data["initialRiskTotal"]),
                obj.initialRiskTotal,
                "$trace.initialRiskTotal"
            )
            verifyDoubleValues(
                parser.asMap(data["liquidationPrice"]),
                obj.liquidationPrice,
                "$trace.liquidationPrice"
            )
            verifyDoubleValues(
                parser.asMap(data["notionalTotal"]),
                obj.notionalTotal,
                "$trace.notionalTotal"
            )
            verifyDoubleValues(
                parser.asMap(data["realizedPnl"]),
                obj.realizedPnl,
                "$trace.realizedPnl"
            )
            verifyDoubleValues(
                parser.asMap(data["realizedPnlPercent"]),
                obj.realizedPnlPercent,
                "$trace.realizedPnlPercent"
            )
            verifyDoubleValues(
                parser.asMap(data["unrealizedPnl"]),
                obj.unrealizedPnl,
                "$trace.unrealizedPnl"
            )
            verifyDoubleValues(
                parser.asMap(data["unrealizedPnlPercent"]),
                obj.unrealizedPnlPercent,
                "$trace.unrealizedPnlPercent"
            )
            verifyDoubleValues(
                parser.asMap(data["valueTotal"]),
                obj.valueTotal,
                "$trace.valueTotal"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountOrders(
        data: IMap<String, Any>?,
        obj: IList<SubaccountOrder>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size $doesntMatchText")
            for (order in obj) {
                val orderId = order.id
                verifyAccountSubaccountOrder(parser.asMap(data[orderId]), order, "$trace.$orderId")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAccountSubaccountOrder(
        data: IMap<String, Any>?,
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
                "$trace.cancelReason"
            )
            assertEquals(parser.asDouble(data["price"]), obj.price, "$trace.price")
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size")
            assertEquals(
                parser.asDouble(data["triggerPrice"]),
                obj.triggerPrice,
                "$trace.triggerPrice"
            )
            assertEquals(parser.asBool(data["postOnly"]) ?: false, obj.postOnly, "$trace.postOnly")
            assertEquals(
                parser.asBool(data["reduceOnly"]) ?: false,
                obj.reduceOnly,
                "$trace.reduceOnly"
            )
            assertEquals(
                parser.asDouble(data["remainingSize"]),
                obj.remainingSize,
                "$trace.remainingSize"
            )
            assertEquals(
                parser.asDouble(data["totalFilled"]),
                obj.totalFilled,
                "$trace.totalFilled"
            )
            assertEquals(
                parser.asDouble(data["trailingPercent"]),
                obj.trailingPercent,
                "$trace.trailingPercent"
            )
            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt"
            )
            assertEquals(
                parser.asDatetime(data["expiresAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.expiresAtMilliseconds,
                "$trace.expiresAt"
            )
            assertEquals(
                parser.asDatetime(data["unfillableAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.unfillableAtMilliseconds,
                "$trace.unfillableAt"
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
        data: IMap<String, Any>?,
        obj: IMap<String, Asset>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifyAsset(parser.asMap(itemData), obj?.get(key), "$trace.$key")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyAsset(
        data: IMap<String, Any>?,
        obj: Asset?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asString(data.get("id")),
                obj.id,
                "$trace.id"
            )
            assertEquals(
                parser.asString(data.get("symbol")),
                obj.symbol,
                "$trace.symbol"
            )
            assertEquals(
                parser.asString(data.get("name")),
                obj.name,
                "$trace.name"
            )
            assertEquals(
                parser.asDouble(data.get("circulatingSupply")),
                obj.circulatingSupply,
                "$trace.circulatingSupply"
            )
            val tagsData = parser.asList(data["tags"])
            val tags = obj.tags
            assertEquals(
                tagsData?.size,
                tags?.size,
                "$trace.tags.size"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsState(
        data: IMap<String, Any>?,
        assets: IMap<String, Any>?,
        obj: PerpetualMarketSummary?,
        trace: String,
    ) {
        assertEquals(parser.asDouble(data?.get("trades24H")), obj?.trades24H, "$trace.trades24H")
        assertEquals(
            parser.asDouble(data?.get("volume24HUSDC")),
            obj?.volume24HUSDC,
            "$trace.volume24HUSDC"
        )
        assertEquals(
            parser.asDouble(data?.get("openInterestUSDC")),
            obj?.openInterestUSDC,
            "$trace.openInterestUSDC"
        )
        verifyMarkets(
            parser.asMap(data?.get("markets")),
            assets,
            obj?.markets,
            "$trace.markets"
        )
    }

    private fun verifyMarkets(
        data: IMap<String, Any>?,
        assets: IMap<String, Any>?,
        obj: IMap<String, PerpetualMarket>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, marketData) in data) {
                verifyMarket(parser.asMap(marketData), assets, obj?.get(key), "$trace.$key")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarket(
        data: IMap<String, Any>?,
        assets: IMap<String, Any>?,
        obj: PerpetualMarket?,
        trace: String
    ) {
        val assetId = parser.asString(data?.get("assetId"))
        val asset = if (assetId != null) parser.asMap(assets?.get(assetId)) else null
        val name = asset?.get("name")
        if (data != null &&
            data["id"] != null &&
            parser.asBool(parser.value(data, "status.canTrade")) == true &&
            asset != null && name != null
        ) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(parser.asString(data["assetId"]), obj.assetId, "$trace.assetId")
            assertEquals(parser.asString(data["market"]), obj.market, "$trace.market")
            assertEquals(parser.asDouble(data["marketCaps"]), obj.marketCaps, "$trace.marketCaps")
            assertEquals(parser.asDouble(data["indexPrice"]), obj.indexPrice, "$trace.indexPrice")
            assertEquals(
                parser.asDouble(data["oraclePrice"]),
                obj.oraclePrice,
                "$trace.oraclePrice"
            )
            assertEquals(
                parser.asDouble(data["priceChange24H"]),
                obj.priceChange24H,
                "$trace.priceChange24H"
            )
            assertEquals(
                parser.asDouble(data["priceChange24HPercent"]),
                obj.priceChange24HPercent,
                "$trace.priceChange24HPercent"
            )
            verifyMarketConfigs(parser.asMap(data["configs"]), obj.configs, "$trace.configs")
            verifyMarketPerpetual(
                parser.asMap(data["perpetual"]),
                obj.perpetual,
                "$trace.perpetual"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketConfigs(data: IMap<String, Any>?, obj: MarketConfigs?, trace: String) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["clobPairId"]), obj.clobPairId, "$trace.clobPairId")
            assertEquals(parser.asDouble(data["stepSize"]), obj.stepSize, "$trace.stepSize")
            assertEquals(parser.asDouble(data["tickSize"]), obj.tickSize, "$trace.tickSize")
            assertEquals(
                parser.asDouble(data["displayStepSize"] ?: data["stepSize"]),
                obj.displayStepSize,
                "$trace.displayStepSize"
            )
            assertEquals(
                parser.asDouble(data["displayTickSize"] ?: data["tickSize"]),
                obj.displayTickSize,
                "$trace.displayTickSize"
            )
            assertEquals(
                parser.asDouble(data["baselinePositionSize"]),
                obj.baselinePositionSize,
                "$trace.baselinePositionSize"
            )
            assertEquals(
                parser.asDouble(data["maxPositionSize"]),
                obj.maxPositionSize,
                "$trace.maxPositionSize"
            )
            assertEquals(
                parser.asDouble(data["minOrderSize"]),
                obj.minOrderSize,
                "$trace.minOrderSize"
            )
            assertEquals(
                parser.asDouble(data["incrementalInitialMarginFraction"]),
                obj.incrementalInitialMarginFraction,
                "$trace.incrementalInitialMarginFraction"
            )
            assertEquals(
                parser.asDouble(data["incrementalPositionSize"]),
                obj.incrementalPositionSize,
                "$trace.incrementalPositionSize"
            )
            assertEquals(
                parser.asDouble(data["initialMarginFraction"]),
                obj.initialMarginFraction,
                "$trace.initialMarginFraction"
            )
            assertEquals(
                parser.asDouble(data["maintenanceMarginFraction"]),
                obj.maintenanceMarginFraction,
                "$trace.maintenanceMarginFraction"
            )
            assertEquals(parser.asInt(data["largeSize"]), obj.largeSize, "$trace.largeSize")

        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketPerpetual(
        data: IMap<String, Any>?,
        obj: MarketPerpetual?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["openInterestUSDC"]),
                obj.openInterestUSDC,
                "$trace.openInterestUSDC"
            )
            assertEquals(
                parser.asDouble(data["volume24HUSDC"]),
                obj.volume24HUSDC,
                "$trace.volume24HUSDC"
            )
            assertEquals(parser.asDouble(data["trades24H"]), obj.trades24H, "$trace.trades24H")
            assertEquals(parser.asDouble(data["volume24H"]), obj.volume24H, "$trace.volume24H")
            assertEquals(
                parser.asDouble(data["nextFundingRate"]),
                obj.nextFundingRate,
                "$trace.nextFundingRate"
            )
            assertEquals(
                parser.asDouble(data["openInterest"]),
                obj.openInterest,
                "$trace.openInterest"
            )
            assertEquals(
                parser.asDatetime(data["nextFundingAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.nextFundingAtMilliseconds,
                "$trace.nextFundingAt"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsHistoricalFundingsState(
        data: IMap<String, Any>?,
        obj: IMap<String, IList<MarketHistoricalFunding>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifyMarketsHistoricalFundingsArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "historicalFunding"
                        )
                    ), obj?.get(key), "$trace.$key"
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsHistoricalFundingsArrayState(
        data: IList<Any>?,
        obj: IList<MarketHistoricalFunding>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifyMarketsHistoricalFundingState(parser.asMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsHistoricalFundingState(
        data: IMap<String, Any>?,
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
                "$trace.effectiveAt"
            )
        } else {
            assertNull(obj)
        }
    }


    private fun verifyMarketsCandlesState(
        data: IMap<String, Any>?,
        obj: IMap<String, MarketCandles>?,
        trace: String,
    ) {
        if (data != null) {
            for ((marketKey, itemData) in data) {
                val candleMap = parser.asMap(
                    parser.value(
                        itemData,
                        "candles"
                    )
                )
                val candles = obj?.get(marketKey)
                if (candleMap != null) {
                    for ((resolutionKey, resolutionData) in candleMap) {
                        val candlesData = parser.asList(resolutionData)
                        val candleArray = candles?.candles?.get(resolutionKey)

                        verifyMarketsCandlesArrayState(
                            candlesData, candleArray, "$trace.$marketKey.$resolutionKey"
                        )
                    }
                }
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsCandlesArrayState(
        data: IList<Any>?,
        obj: IList<MarketCandle>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifyMarketsCandleState(parser.asMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsCandleState(
        data: IMap<String, Any>?,
        obj: MarketCandle?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["baseTokenVolume"]),
                obj.baseTokenVolume,
                "$trace.baseTokenVolume"
            )
            assertEquals(parser.asDouble(data["high"]), obj.high, "$trace.high")
            assertEquals(parser.asDouble(data["low"]), obj.low, "$trace.low")
            assertEquals(parser.asDouble(data["close"]), obj.close, "$trace.close")
            assertEquals(parser.asDouble(data["open"]), obj.open, "$trace.open")
            assertEquals(parser.asDouble(data["usdVolume"]), obj.usdVolume, "$trace.usdVolume")

            assertEquals(
                parser.asDatetime(data["startedAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.startedAtMilliseconds,
                "$trace.startedAt"
            )
            assertEquals(
                parser.asDatetime(data["updatedAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.updatedAtMilliseconds,
                "$trace.updatedAt"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsTradesState(
        data: IMap<String, Any>?,
        obj: IMap<String, IList<MarketTrade>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifyMarketsTradesArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "trades"
                        )
                    ), obj?.get(key), "$trace.$key"
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsTradesArrayState(
        data: IList<Any>?,
        obj: IList<MarketTrade>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifyMarketsTradeState(parser.asMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsTradeState(
        data: IMap<String, Any>?,
        obj: MarketTrade?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(
                parser.asDouble(data["price"]),
                obj.price,
                "$trace.price"
            )
            assertEquals(parser.asDouble(data["size"]), obj.size, "$trace.size")
            assertEquals(
                parser.asBool(data["liquidation"]) ?: false,
                obj.liquidation,
                "$trace.liquidation"
            )
            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt"
            )
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketsOrderbookState(
        data: IMap<String, Any>?,
        obj: IMap<String, MarketOrderbook>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifyMarketOrderbook(
                    parser.asMap(
                        parser.value(
                            itemData,
                            "orderbook"
                        )
                    ), obj?.get(key), "$trace.$key"
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketOrderbook(
        data: IMap<String, Any>?,
        obj: MarketOrderbook?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asDouble(data["midPrice"]), obj.midPrice, "$trace.midPrice")
            assertEquals(
                parser.asDouble(data["spreadPercent"]),
                obj.spreadPercent,
                "$trace.spreadPercent"
            )
            verifyMarketOrderbookSide(parser.asList(data["asks"]), obj.asks, "$trace.asks")
            verifyMarketOrderbookSide(parser.asList(data["bids"]), obj.bids, "$trace.bids")
        } else {
            assertNull(obj)
        }
    }

    private fun verifyMarketOrderbookSide(
        data: IList<Any>?,
        obj: IList<OrderbookLine>?,
        trace: String,
    ) {
        val compacted = data?.mapNotNull { item ->
            val lineItem = parser.asMap(item)
            val size = parser.asDouble(lineItem?.get("size"))
            if (size != Numeric.double.ZERO) lineItem else null
        }

        assertEquals(compacted?.size, obj?.size, "$trace.size")
        if (obj != null) {
            for (i in obj.indices) {
                verifyMarketOrderbookLine(parser.asMap(compacted?.get(i)), obj[i], "$trace.$i")
            }
        }
    }

    private fun verifyMarketOrderbookLine(
        data: IMap<String, Any>?,
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
        data: IMap<String, Any>?,
        obj: Configs?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            verifyConfigsNetwork(parser.asMap(data["network"]), obj.network, "$trace.network")
            verifyConfigsFeeTiers(parser.asList(data["feeTiers"]), obj.feeTiers, "$trace.feeTiers")
            verifyConfigsFeeDiscounts(
                parser.asList(data["feeDiscounts"]),
                obj.feeDiscounts,
                "$trace.feeDiscounts"
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
//        verifyMarkets(parser.asMap(data?.get("markets")), obj?.markets, "$trace.markets")
    }

    private fun verifyConfigsNetwork(
        data: IMap<String, Any>?,
        obj: NetworkConfigs?,
        trace: String,
    ) {
    }

    private fun verifyConfigsFeeTiers(
        data: IList<Any>?,
        obj: IList<FeeTier>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertTrue { data.size >= obj.size }
            for (i in obj.indices) {
                verifyConfigsFeeTier(parser.asMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyConfigsFeeTier(
        data: IMap<String, Any>?,
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
        data: IList<Any>?,
        obj: IList<FeeDiscount>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size)
            for (i in obj.indices) {
                verifyConfigsFeeDiscount(parser.asMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifyConfigsFeeDiscount(
        data: IMap<String, Any>?,
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
        data: IMap<String, Any>?,
        obj: IMap<String, IList<SubaccountFill>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifySubaccountFillsArrayState(
                    parser.asList(parser.value(itemData, "fills")),
                    obj?.get(key),
                    "$trace.$key"
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountFillsArrayState(
        data: IList<Any>?,
        obj: IList<SubaccountFill>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifySubaccountFillState(parser.asMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountFillState(
        data: IMap<String, Any>?,
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
                "$trace.createdAt"
            )
        } else {
            assertNull(obj)
        }
    }


    private fun verifySubaccountTransfersState(
        data: IMap<String, Any>?,
        obj: IMap<String, IList<SubaccountTransfer>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifySubaccountTransfersArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "transfers"
                        )
                    ), obj?.get(key), "$trace.$key"
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountTransfersArrayState(
        data: IList<Any>?,
        obj: IList<SubaccountTransfer>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifySubaccountTransferState(parser.asMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountTransferState(
        data: IMap<String, Any>?,
        obj: SubaccountTransfer?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(parser.asString(data["id"]), obj.id, "$trace.id")
            assertEquals(
                parser.asString(data["fromAddress"]),
                obj.fromAddress,
                "$trace.fromAddress"
            )
            assertEquals(parser.asString(data["toAddress"]), obj.toAddress, "$trace.toAddress")
            assertEquals(
                parser.asString(data["asset"]),
                obj.asset,
                "$trace.asset"
            )
            assertEquals(
                parser.asDouble(data["amount"]),
                obj.amount,
                "$trace.amount"
            )
            assertEquals(
                parser.asInt(data["updatedAtBlock"]),
                obj.updatedAtBlock,
                "$trace.updatedAtBlock"
            )
            assertEquals(
                parser.asString(data["transactionHash"]),
                obj.transactionHash,
                "$trace.transactionHash"
            )
            assertEquals(
                (parser.asDatetime(data["confirmedAt"])
                    ?: parser.asDatetime(data["createdAt"]))?.toEpochMilliseconds()?.toDouble(),
                obj.updatedAtMilliseconds,
                "$trace.updatedAt"
            )
        } else {
            assertNull(obj)
        }
    }


    private fun verifySubaccountFundingPaymentsState(
        data: IMap<String, Any>?,
        obj: IMap<String, IList<SubaccountFundingPayment>>?,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifySubaccountFundingPaymentsArraysArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "fundingPayments"
                        )
                    ), obj?.get(key), "$trace.$key"
                )
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountFundingPaymentsArraysArrayState(
        data: IList<Any>?,
        obj: IList<SubaccountFundingPayment>?,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            assertEquals(data.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifySubaccountFundingPaymentState(parser.asMap(data[i]), obj[i], "$trace.$i")
            }
        } else {
            assertNull(obj)
        }
    }

    private fun verifySubaccountFundingPaymentState(
        data: IMap<String, Any>?,
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
                "$trace.positionSize"
            )
            assertEquals(
                parser.asDatetime(data["effectiveAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.effectiveAtMilliSeconds,
                "$trace.effectiveAt"
            )
        } else {
            assertNull(obj)
        }
    }


    private fun verifySubaccountHistoricalPNLsState(
        data: IMap<String, Any>?,
        obj: IMap<String, IList<SubaccountHistoricalPNL>>?,
        startTime: Instant,
        trace: String,
    ) {
        if (data != null) {
            for ((key, itemData) in data) {
                verifySubaccountHistoricalPNLsArraysArrayState(
                    parser.asList(
                        parser.value(
                            itemData,
                            "historicalPnl"
                        )
                    ), obj?.get(key), startTime, "$trace.$key"
                )
            }
        } else {
        }
    }

    private fun verifySubaccountHistoricalPNLsArraysArrayState(
        data: IList<Any>?,
        obj: IList<SubaccountHistoricalPNL>?,
        startTime: Instant,
        trace: String,
    ) {
        if (data != null) {
            assertNotNull(obj)
            val filteredData = data.mapNotNull { item ->
                val itemData = parser.asMap(item)!!
                val createdAt = parser.asDatetime(itemData["createdAt"])!!
                if (createdAt >= startTime) item else null
            }
            assertEquals(filteredData.size, obj.size, "$trace.size")
            for (i in obj.indices) {
                verifySubaccountHistoricalPNLState(
                    parser.asMap(filteredData[i]),
                    obj[i],
                    "$trace.$i"
                )
            }
        } else {
//            assertNull(obj)
        }
    }

    private fun verifySubaccountHistoricalPNLState(
        data: IMap<String, Any>?,
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
                "$trace.netTransfers"
            )

            assertEquals(
                parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble(),
                obj.createdAtMilliseconds,
                "$trace.createdAt"
            )
        } else {
            assertNull(obj)
        }
    }
}