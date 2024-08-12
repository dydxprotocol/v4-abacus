package exchange.dydx.abacus.processor.input

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.TradeInputCalculator
import exchange.dydx.abacus.output.PerpetualMarketType
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.cannotModify
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.internalstate.InternalInputType
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputOptions
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradeInputField.bracketsExecution
import exchange.dydx.abacus.state.model.TradeInputField.bracketsGoodUntilDuration
import exchange.dydx.abacus.state.model.TradeInputField.bracketsGoodUntilUnit
import exchange.dydx.abacus.state.model.TradeInputField.bracketsStopLossPercent
import exchange.dydx.abacus.state.model.TradeInputField.bracketsStopLossPrice
import exchange.dydx.abacus.state.model.TradeInputField.bracketsStopLossReduceOnly
import exchange.dydx.abacus.state.model.TradeInputField.bracketsTakeProfitPercent
import exchange.dydx.abacus.state.model.TradeInputField.bracketsTakeProfitPrice
import exchange.dydx.abacus.state.model.TradeInputField.bracketsTakeProfitReduceOnly
import exchange.dydx.abacus.state.model.TradeInputField.execution
import exchange.dydx.abacus.state.model.TradeInputField.goodTilDuration
import exchange.dydx.abacus.state.model.TradeInputField.goodTilUnit
import exchange.dydx.abacus.state.model.TradeInputField.leverage
import exchange.dydx.abacus.state.model.TradeInputField.limitPrice
import exchange.dydx.abacus.state.model.TradeInputField.marginMode
import exchange.dydx.abacus.state.model.TradeInputField.postOnly
import exchange.dydx.abacus.state.model.TradeInputField.reduceOnly
import exchange.dydx.abacus.state.model.TradeInputField.side
import exchange.dydx.abacus.state.model.TradeInputField.size
import exchange.dydx.abacus.state.model.TradeInputField.targetLeverage
import exchange.dydx.abacus.state.model.TradeInputField.timeInForceType
import exchange.dydx.abacus.state.model.TradeInputField.trailingPercent
import exchange.dydx.abacus.state.model.TradeInputField.triggerPrice
import exchange.dydx.abacus.state.model.TradeInputField.type
import exchange.dydx.abacus.state.model.TradeInputField.usdcSize
import kollections.iListOf
import kotlin.math.abs

internal interface TradeInputProcessorProtocol {
    fun tradeInMarket(
        inputState: InternalInputState,
        marketState: InternalMarketState,
        accountState: InternalAccountState,
        marketId: String,
        subaccountNumber: Int,
    ): StateChanges
}

internal class TradeInputResult(
    val changes: StateChanges? = null,
    val error: ParsingError? = null,
)

internal class TradeInputProcessor(
    private val parser: ParserProtocol,
    private val calculator: TradeInputCalculator = TradeInputCalculator(parser, TradeCalculation.trade)
) : TradeInputProcessorProtocol {
    override fun tradeInMarket(
        inputState: InternalInputState,
        marketState: InternalMarketState,
        accountState: InternalAccountState,
        marketId: String,
        subaccountNumber: Int,
    ): StateChanges {
        if (inputState.trade.marketId == marketId) {
            if (inputState.currentType == InternalInputType.TRADE) {
                return StateChanges(iListOf()) // no change
            } else {
                inputState.currentType = InternalInputType.TRADE
                return StateChanges(
                    changes = iListOf(Changes.input),
                    markets = null,
                    subaccountNumbers = iListOf(subaccountNumber),
                )
            }
        }

        if (inputState.trade.marketId != null) {
            // existing trade
            inputState.trade.marketId = marketId
            inputState.trade.size = null
            inputState.trade.price = null
            inputState.trade.options = InternalTradeInputOptions()
        } else {
            // new trade
            inputState.trade = initialTradeInputState(
                marketId = marketId,
                subaccountNumber = subaccountNumber,
                accountState = accountState,
                marketState = marketState,
            )
        }

        initiateMarginModeLeverage(
            trade = inputState.trade,
            marketState = marketState,
            accountState = accountState,
            marketId = marketId,
            subaccountNumber = subaccountNumber,
        )

        inputState.currentType = InternalInputType.TRADE

        val subaccountNumbers =
            MarginCalculator.getChangedSubaccountNumbers(
                parser = parser,
                subaccounts = accountState.subaccounts,
                subaccountNumber = subaccountNumber,
                tradeInput = inputState.trade,
            )
        return StateChanges(
            changes = iListOf(Changes.subaccount, Changes.input),
            markets = null,
            subaccountNumbers = subaccountNumbers,
        )
    }

    fun trade(
        inputState: InternalInputState,
        accountState: InternalAccountState,
        inputData: String?,
        inputType: TradeInputField?,
        subaccountNumber: Int,
    ): TradeInputResult {
        inputState.currentType = InternalInputType.TRADE

        if (inputState.trade.marketId == null) {
            // new trade
            inputState.trade = initialTradeInputState(
                marketId = null,
                subaccountNumber = subaccountNumber,
                accountState = accountState,
                marketState = null,
            )
        }
        if (inputType == null) {
            return TradeInputResult(
                changes = StateChanges(
                    iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                    null,
                    iListOf(subaccountNumber),
                ),
            )
        }

        var error: ParsingError? = null
        var changes: StateChanges? = null

        var sizeChanged = false
        val trade = inputState.trade
        val validInput = inputType.validTradeInputAction?.invoke(trade) ?: true
        if (validInput) {
            val subaccountNumbers =
                MarginCalculator.getChangedSubaccountNumbers(
                    parser = parser,
                    subaccounts = accountState.subaccounts,
                    subaccountNumber = subaccountNumber,
                    tradeInput = trade,
                )
            when (inputType) {
                TradeInputField.type, TradeInputField.side -> {
                    if (inputData != null) {
                        if (trade.size?.input == "size.leverage") {
                            trade.size = TradeInputSize.safeCreate(trade.size).copy(input = "size.size")
                        }
                        inputType.updateValueAction?.invoke(trade, inputData, parser)
                        changes = StateChanges(
                            changes = iListOf(Changes.subaccount, Changes.input),
                            markets = null,
                            subaccountNumbers = subaccountNumbers,
                        )
                    } else {
                        error = ParsingError(
                            ParsingErrorType.MissingRequiredData,
                            "$inputData is not a valid string",
                        )
                    }
                }

                TradeInputField.size,
                TradeInputField.usdcSize,
                TradeInputField.leverage,
                TradeInputField.targetLeverage,
                -> {
                    sizeChanged =
                        (parser.asDouble(inputData) != parser.asDouble(inputType.valueAction?.invoke(trade)))
                    inputType.updateValueAction?.invoke(trade, inputData, parser)
                    changes = StateChanges(
                        changes = iListOf(Changes.subaccount, Changes.input),
                        markets = null,
                        subaccountNumbers = subaccountNumbers,
                    )
                }

                TradeInputField.lastInput,
                TradeInputField.limitPrice,
                TradeInputField.triggerPrice,
                TradeInputField.trailingPercent,
                TradeInputField.bracketsStopLossPrice,
                TradeInputField.bracketsStopLossPercent,
                TradeInputField.bracketsTakeProfitPrice,
                TradeInputField.bracketsTakeProfitPercent,
                TradeInputField.timeInForceType,
                TradeInputField.goodTilUnit,
                TradeInputField.bracketsGoodUntilUnit,
                TradeInputField.execution,
                TradeInputField.bracketsExecution,
                TradeInputField.goodTilDuration,
                TradeInputField.bracketsGoodUntilDuration,
                TradeInputField.reduceOnly,
                TradeInputField.postOnly,
                TradeInputField.bracketsStopLossReduceOnly,
                TradeInputField.bracketsTakeProfitReduceOnly,
                -> {
                    inputType.updateValueAction?.invoke(trade, inputData, parser)
                    changes = StateChanges(
                        changes = iListOf(Changes.subaccount, Changes.input),
                        markets = null,
                        subaccountNumbers = subaccountNumbers,
                    )
                }

                TradeInputField.marginMode
                -> {
                    inputType.updateValueAction?.invoke(trade, inputData, parser)
                    val changedSubaccountNumbers =
                        MarginCalculator.getChangedSubaccountNumbers(
                            parser = parser,
                            subaccounts = accountState.subaccounts,
                            subaccountNumber = subaccountNumber,
                            tradeInput = trade,
                        )
                    changes = StateChanges(
                        changes = iListOf(Changes.input, Changes.subaccount),
                        markets = null,
                        subaccountNumbers = changedSubaccountNumbers,
                    )
                }
            }
        } else {
            error = ParsingError.cannotModify(inputType.rawValue)
        }

        if (sizeChanged) {
            when (type) {
                TradeInputField.size,
                TradeInputField.usdcSize,
                TradeInputField.leverage,
                -> {
                    TradeInputSize.safeCreate(trade.size).copy(input = type.rawValue)
                }
                else -> {}
            }
        }

        return TradeInputResult(
            changes = changes,
            error = error,
        )
    }

    private fun initiateMarginModeLeverage(
        trade: InternalTradeInputState,
        marketState: InternalMarketState,
        accountState: InternalAccountState,
        marketId: String,
        subaccountNumber: Int,
    ) {
        val subaccount = accountState.subaccounts[subaccountNumber]
        val existingPosition = MarginCalculator.findExistingPosition(
            account = accountState,
            marketId = marketId,
            subaccountNumber = subaccountNumber,
        )
        val existingOrder = MarginCalculator.findExistingOrder(
            account = accountState,
            marketId = marketId,
            subaccountNumber = subaccountNumber,
        )
        if (existingPosition != null) {
            trade.marginMode =
                if (subaccount?.equity != null) MarginMode.Isolated else MarginMode.Cross
            val currentPositionLeverage =
                existingPosition.calculated[CalculationPeriod.current]?.leverage?.abs()
            val positionLeverage =
                if (currentPositionLeverage != null && currentPositionLeverage > 0) currentPositionLeverage else 1.0
            trade.targetLeverage = positionLeverage
        } else if (existingOrder != null) {
            trade.marginMode =
                if (existingOrder.subaccountNumber == subaccountNumber) MarginMode.Cross else MarginMode.Isolated
            trade.targetLeverage = 1.0
        } else {
            val marketType = marketState.perpetualMarket?.configs?.perpetualMarketType
            trade.marginMode = when (marketType) {
                PerpetualMarketType.CROSS -> MarginMode.Cross
                PerpetualMarketType.ISOLATED -> MarginMode.Isolated
                else -> null
            }
            trade.targetLeverage = 1.0
        }
    }

    private fun initialTradeInputState(
        marketId: String?,
        subaccountNumber: Int,
        accountState: InternalAccountState,
        marketState: InternalMarketState?,
    ): InternalTradeInputState {
//
//        val trade = exchange.dydx.abacus.utils.mutableMapOf<String, Any>()
//        trade["type"] = "LIMIT"
//        trade["side"] = "BUY"
//        trade["marketId"] = marketId ?: "ETH-USD"

//        val marginMode = MarginCalculator.findExistingMarginModeDeprecated(parser, account, marketId, subaccountNumber)
//            ?: MarginCalculator.findMarketMarginMode(parser, parser.asNativeMap(parser.value(marketsSummary, "markets.$marketId")))
//
//        trade.safeSet("marginMode", marginMode)
//
//        val calculator = TradeInputCalculator(parser, TradeCalculation.trade)
//        val params = exchange.dydx.abacus.utils.mutableMapOf<String, Any>()
//        params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
//        params.safeSet("account", account)
//        params.safeSet("user", user)
//        params.safeSet("trade", trade)
//        params.safeSet("rewardsParams", rewardsParams)
//        params.safeSet("configs", configs)
//
//        val modified = calculator.calculate(params, subaccountNumber, null)
//
//        return parser.asMap(modified["trade"])?.mutable() ?: trade

        val marginMode = MarginCalculator.findExistingMarginMode(
            account = accountState,
            marketId = marketId,
            subaccountNumber = subaccountNumber,
        ) ?: MarginCalculator.findMarketMarginMode(
            market = marketState?.perpetualMarket,
        )

        // TODO - implement TradeInputCalculatorV2
        // calculator.calculate()

        return InternalTradeInputState(
            marketId = marketId,
            size = null,
            price = null,
            type = OrderType.Limit,
            side = OrderSide.Buy,
            marginMode = marginMode,
        )
    }
}
