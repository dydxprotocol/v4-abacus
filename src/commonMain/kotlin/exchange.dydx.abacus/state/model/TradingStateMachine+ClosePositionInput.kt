package exchange.dydx.abacus.state.model

import abs
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.TradeInputCalculator
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.responses.cannotModify
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.StatsigConfig
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class ClosePositionInputField(val rawValue: String) {
    market("market"),
    size("size.size"),
    percent("size.percent"),

    useLimit("useLimit"),
    limitPrice("price.limitPrice");

    companion object {
        operator fun invoke(rawValue: String?) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.closePosition(
    data: String?,
    type: ClosePositionInputField,
    subaccountNumber: Int
): StateResponse {
    if (staticTyping) {
        val result = closePositionInputProcessor.closePosition(
            inputState = internalState.input,
            walletState = internalState.wallet,
            marketSummaryState = internalState.marketsSummary,
            configs = internalState.configs,
            rewardsParams = internalState.rewardsParams,
            data = data,
            type = type,
            subaccountNumber = subaccountNumber,
        )
        result.changes?.let {
            updateStateChanges(it)
        }
        return StateResponse(
            state,
            result.changes,
            if (result.error != null) iListOf(result.error) else null,
        )
    } else {
        var changes: StateChanges? = null
        var error: ParsingError? = null
        val typeText = type.rawValue

        val input = this.input?.mutable() ?: mutableMapOf()
        input["current"] = "closePosition"
        val trade =
            parser.asMap(input["closePosition"])?.mutable() ?: initiateClosePosition(
                null,
                subaccountNumber,
            )

        val childSubaccountNumber =
            MarginCalculator.getChildSubaccountNumberForIsolatedMarginClosePositionDeprecated(
                parser,
                account,
                subaccountNumber,
                trade,
            )
        val subaccountNumberChanges = if (subaccountNumber == childSubaccountNumber) {
            iListOf(subaccountNumber)
        } else {
            iListOf(subaccountNumber, childSubaccountNumber)
        }

        var sizeChanged = false
        when (typeText) {
            ClosePositionInputField.market.rawValue -> {
                val position = if (data != null) getPosition(data, subaccountNumber) else null
                if (position != null) {
                    if (data != null) {
                        if (parser.asString(trade["marketId"]) != data) {
                            trade.safeSet("marketId", data)
                            trade.safeSet("size", null)
                        }
                    }
                    trade["type"] = "MARKET"

                    val positionSize =
                        parser.asDouble(parser.value(position, "size.current")) ?: Numeric.double.ZERO
                    trade["side"] = if (positionSize > Numeric.double.ZERO) "SELL" else "BUY"

                    trade["timeInForce"] = "IOC"
                    trade["reduceOnly"] = true

                    val market = parser.asNativeMap(parser.value(marketsSummary, "markets.${trade["marketId"]}"))
                    val imf = parser.asDouble(parser.value(market, "configs.initialMarginFraction")) ?: Numeric.double.ZERO
                    val effectiveImf = parser.asDouble(parser.value(market, "configs.effectiveInitialMarginFraction")) ?: Numeric.double.ZERO
                    val maxMarketLeverage = if (effectiveImf > Numeric.double.ZERO) {
                        Numeric.double.ONE / effectiveImf
                    } else if (imf > Numeric.double.ZERO) {
                        Numeric.double.ONE / imf
                    } else {
                        Numeric.double.ONE
                    }
                    val currentPositionLeverage = parser.asDouble(parser.value(position, "leverage.current"))?.abs()
                    trade["targetLeverage"] = if (currentPositionLeverage != null && currentPositionLeverage > 0) currentPositionLeverage else maxMarketLeverage

                    // default full close
                    trade.safeSet("size.percent", 1.0)
                    trade.safeSet("size.input", "size.percent")

                    changes = StateChanges(
                        iListOf(Changes.subaccount, Changes.input),
                        null,
                        subaccountNumberChanges,
                    )
                } else {
                    error = ParsingError.cannotModify(typeText)
                }
            }
            ClosePositionInputField.size.rawValue, ClosePositionInputField.percent.rawValue -> {
                sizeChanged = (parser.asDouble(data) != parser.asDouble(trade[typeText]))
                trade.safeSet(typeText, data)
                changes = StateChanges(
                    iListOf(Changes.subaccount, Changes.input),
                    null,
                    subaccountNumberChanges,
                )
            }
            ClosePositionInputField.useLimit.rawValue -> {
                val useLimitClose = (parser.asBool(data) ?: false) && StatsigConfig.ff_enable_limit_close
                trade.safeSet(typeText, useLimitClose)

                if (useLimitClose) {
                    trade["type"] = "LIMIT"
                    trade["timeInForce"] = "GTT"
                    parser.asString(trade["marketId"])?.let {
                        trade.safeSet("price.limitPrice", getMidMarketPrice(it))
                    }
                } else {
                    trade["type"] = "MARKET"
                    trade["timeInForce"] = "IOC"
                }

                changes = StateChanges(
                    iListOf(Changes.subaccount, Changes.input),
                    null,
                    subaccountNumberChanges,
                )
            }
            ClosePositionInputField.limitPrice.rawValue -> {
                trade.safeSet(typeText, parser.asDouble(data))
                changes = StateChanges(
                    iListOf(Changes.subaccount, Changes.input),
                    null,
                    subaccountNumberChanges,
                )
            }
            else -> {}
        }
        if (sizeChanged) {
            when (typeText) {
                ClosePositionInputField.size.rawValue,
                ClosePositionInputField.percent.rawValue -> {
                    trade.safeSet("size.input", typeText)
                }

                else -> {}
            }
        }
        input["closePosition"] = trade
        this.input = input

        changes?.let {
            updateStateChanges(it)
        }
        return StateResponse(state, changes, if (error != null) iListOf(error) else null)
    }
}

private fun TradingStateMachine.getPosition(
    marketId: String,
    subaccountNumber: Int,
): Map<String, Any>? {
    val groupedSubaccounts = parser.asMap(parser.value(wallet, "account.groupedSubaccounts"))
    val path = if (groupedSubaccounts != null) {
        "account.groupedSubaccounts.$subaccountNumber.openPositions.$marketId"
    } else {
        "account.subaccounts.$subaccountNumber.openPositions.$marketId"
    }
    val position = parser.asMap(
        parser.value(
            wallet,
            path,
        ),
    )

    return if (position != null && (
            parser.asDouble(parser.value(position, "size.current"))
                ?: Numeric.double.ZERO
            ) != Numeric.double.ZERO
    ) {
        position
    } else {
        null
    }
}

private fun TradingStateMachine.initiateClosePosition(
    marketId: String?,
    subaccountNumber: Int,
): MutableMap<String, Any> {
    val trade = mutableMapOf<String, Any>()
    trade["type"] = "MARKET"
    trade["side"] = "BUY"
    trade["marketId"] = marketId ?: "ETH-USD"
    // default full close
    trade.safeSet("size.percent", 1.0)
    trade.safeSet("size.input", "size.percent")

    val calculator = TradeInputCalculator(parser, TradeCalculation.closePosition)
    val params = mutableMapOf<String, Any>()
    params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
    params.safeSet("account", account)
    params.safeSet("user", user)
    params.safeSet("trade", trade)
    params.safeSet("rewardsParams", rewardsParams)
    params.safeSet("configs", configs)

    val modified = calculator.calculate(params, subaccountNumber, "size.percent")

    return parser.asMap(modified["trade"])?.mutable() ?: trade
}

private fun TradingStateMachine.getMidMarketPrice(
    marketId: String
): Double? {
    val markets = parser.asNativeMap(marketsSummary?.get("markets"))
    return parser.asNativeMap(parser.asNativeMap(markets?.get(marketId))?.get("orderbook_consolidated"))?.let { orderbook ->
        parser.asDouble(parser.value(orderbook, "asks.0.price"))?.let { firstAskPrice ->
            parser.asDouble(parser.value(orderbook, "bids.0.price"))?.let { firstBidPrice ->
                (firstAskPrice + firstBidPrice) / 2.0
            }
        }
    }
}
