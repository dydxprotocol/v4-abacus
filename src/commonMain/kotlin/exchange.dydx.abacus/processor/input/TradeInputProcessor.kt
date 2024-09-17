package exchange.dydx.abacus.processor.input

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.v2.tradeinput.TradeInputCalculatorV2
import exchange.dydx.abacus.output.PerpetualMarketType
import exchange.dydx.abacus.output.input.InputType
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
import exchange.dydx.abacus.state.internalstate.InternalConfigsState
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.internalstate.InternalRewardsParamsState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputOptions
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.utils.Numeric
import kollections.iListOf

internal interface TradeInputProcessorProtocol {
    fun tradeInMarket(
        inputState: InternalInputState,
        marketSummaryState: InternalMarketSummaryState,
        walletState: InternalWalletState,
        configs: InternalConfigsState,
        rewardsParams: InternalRewardsParamsState?,
        marketId: String,
        subaccountNumber: Int,
    ): StateChanges

    fun trade(
        inputState: InternalInputState,
        walletState: InternalWalletState,
        marketSummaryState: InternalMarketSummaryState,
        configs: InternalConfigsState,
        rewardsParams: InternalRewardsParamsState?,
        inputData: String?,
        inputType: TradeInputField?,
        subaccountNumber: Int,
    ): InputProcessorResult
}

internal class TradeInputProcessor(
    private val parser: ParserProtocol,
    private val calculator: TradeInputCalculatorV2 = TradeInputCalculatorV2(parser, TradeCalculation.trade)
) : TradeInputProcessorProtocol {
    override fun tradeInMarket(
        inputState: InternalInputState,
        marketSummaryState: InternalMarketSummaryState,
        walletState: InternalWalletState,
        configs: InternalConfigsState,
        rewardsParams: InternalRewardsParamsState?,
        marketId: String,
        subaccountNumber: Int,
    ): StateChanges {
        if (inputState.trade.marketId == marketId) {
            if (inputState.currentType == InputType.TRADE) {
                return StateChanges(iListOf()) // no change
            } else {
                inputState.currentType = InputType.TRADE
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
                walletState = walletState,
                marketSummaryState = marketSummaryState,
                configs = configs,
                rewardsParams = rewardsParams,
            )
        }

        val market = marketSummaryState.markets[marketId]
        initiateMarginModeLeverage(
            trade = inputState.trade,
            marketState = market,
            marketSummaryState = marketSummaryState,
            accountState = walletState.account,
            marketId = marketId,
            subaccountNumber = subaccountNumber,
        )

        inputState.currentType = InputType.TRADE

        val subaccountNumbers =
            MarginCalculator.getChangedSubaccountNumbers(
                parser = parser,
                subaccounts = walletState.account.subaccounts,
                subaccountNumber = subaccountNumber,
                tradeInput = inputState.trade,
            )
        return StateChanges(
            changes = iListOf(Changes.subaccount, Changes.input),
            markets = null,
            subaccountNumbers = subaccountNumbers,
        )
    }

    override fun trade(
        inputState: InternalInputState,
        walletState: InternalWalletState,
        marketSummaryState: InternalMarketSummaryState,
        configs: InternalConfigsState,
        rewardsParams: InternalRewardsParamsState?,
        inputData: String?,
        inputType: TradeInputField?,
        subaccountNumber: Int,
    ): InputProcessorResult {
        inputState.currentType = InputType.TRADE

        if (inputState.trade.marketId == null) {
            // new trade
            inputState.trade = initialTradeInputState(
                marketId = null,
                subaccountNumber = subaccountNumber,
                walletState = walletState,
                marketSummaryState = marketSummaryState,
                configs = configs,
                rewardsParams = rewardsParams,
            )
        }
        if (inputType == null) {
            return InputProcessorResult(
                changes = StateChanges(
                    changes = iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                    markets = null,
                    subaccountNumbers = iListOf(subaccountNumber),
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
                    subaccounts = walletState.account.subaccounts,
                    subaccountNumber = subaccountNumber,
                    tradeInput = trade,
                )
            when (inputType) {
                TradeInputField.type, TradeInputField.side -> {
                    if (inputData != null) {
                        val sizeInput = TradeInputField.invoke(trade.size?.input)
                        if (sizeInput == TradeInputField.leverage || sizeInput == TradeInputField.balancePercent) {
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
                            type = ParsingErrorType.MissingRequiredData,
                            message = "$inputData is not a valid string",
                        )
                    }
                }

                TradeInputField.size,
                TradeInputField.usdcSize,
                TradeInputField.leverage,
                TradeInputField.balancePercent,
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

                TradeInputField.targetLeverage -> {
                    sizeChanged =
                        (parser.asDouble(inputData) != parser.asDouble(inputType.valueAction?.invoke(trade)))
                    inputType.updateValueAction?.invoke(trade, inputData, parser)
                    val changedSubaccountNumbers =
                        MarginCalculator.getChangedSubaccountNumbers(
                            parser = parser,
                            subaccounts = walletState.account.subaccounts,
                            subaccountNumber = subaccountNumber,
                            tradeInput = trade,
                        )
                    changes = StateChanges(
                        changes = iListOf(Changes.subaccount, Changes.input),
                        markets = null,
                        subaccountNumbers = changedSubaccountNumbers,
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
                            subaccounts = walletState.account.subaccounts,
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
            when (inputType) {
                TradeInputField.size,
                TradeInputField.usdcSize,
                TradeInputField.leverage,
                TradeInputField.balancePercent,
                -> {
                    trade.size = TradeInputSize.safeCreate(trade.size).copy(input = inputType.rawValue)
                }
                else -> {}
            }
        }

        return InputProcessorResult(
            changes = changes,
            error = error,
        )
    }

    private fun initiateMarginModeLeverage(
        trade: InternalTradeInputState,
        marketState: InternalMarketState?,
        marketSummaryState: InternalMarketSummaryState,
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
        val market = marketSummaryState.markets[marketId]
        val maxMarketLeverage = market?.perpetualMarket?.configs?.maxMarketLeverage ?: Numeric.double.ONE

        if (existingPosition != null) {
            trade.marginMode =
                if (subaccount?.equity != null) MarginMode.Isolated else MarginMode.Cross
            val currentPositionLeverage =
                existingPosition.calculated[CalculationPeriod.current]?.leverage?.abs()
            val positionLeverage =
                if (currentPositionLeverage != null && currentPositionLeverage > Numeric.double.ZERO) currentPositionLeverage else Numeric.double.ONE
            trade.targetLeverage = positionLeverage
        } else if (existingOrder != null) {
            trade.marginMode =
                if (existingOrder.subaccountNumber == subaccountNumber) MarginMode.Cross else MarginMode.Isolated
            trade.targetLeverage = maxMarketLeverage
        } else {
            val marketType = marketState?.perpetualMarket?.configs?.perpetualMarketType
            trade.marginMode = when (marketType) {
                PerpetualMarketType.CROSS -> MarginMode.Cross
                PerpetualMarketType.ISOLATED -> MarginMode.Isolated
                else -> null
            }
            trade.targetLeverage = maxMarketLeverage
        }
    }

    private fun initialTradeInputState(
        marketId: String?,
        subaccountNumber: Int,
        walletState: InternalWalletState,
        marketSummaryState: InternalMarketSummaryState,
        configs: InternalConfigsState,
        rewardsParams: InternalRewardsParamsState?,
    ): InternalTradeInputState {
        val market = marketSummaryState.markets[marketId]
        val marginMode = MarginCalculator.findExistingMarginMode(
            account = walletState.account,
            marketId = marketId,
            subaccountNumber = subaccountNumber,
        ) ?: MarginCalculator.findMarketMarginMode(
            market = market?.perpetualMarket,
        )

        return calculator.calculate(
            trade = InternalTradeInputState(
                marketId = marketId ?: "ETH-USD",
                size = null,
                price = null,
                type = OrderType.Limit,
                side = OrderSide.Buy,
                marginMode = marginMode,
            ),
            wallet = walletState,
            marketSummary = marketSummaryState,
            rewardsParams = rewardsParams,
            configs = configs,
            subaccountNumber = subaccountNumber,
            input = null,
        )
    }
}
