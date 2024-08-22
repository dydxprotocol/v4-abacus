package exchange.dydx.abacus.processor.input

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.v2.tradeinput.TradeInputCalculatorV2
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.cannotModify
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalConfigsState
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalRewardsParamsState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.utils.Numeric
import kollections.iListOf

internal interface ClosePositionInputProcessorProtocol

internal class ClosePositionInputProcessor(
    private val parser: ParserProtocol,
) : ClosePositionInputProcessorProtocol {

    fun closePosition(
        inputState: InternalInputState,
        walletState: InternalWalletState,
        marketSummaryState: InternalMarketSummaryState,
        configs: InternalConfigsState,
        rewardsParams: InternalRewardsParamsState?,
        data: String?,
        type: ClosePositionInputField,
        subaccountNumber: Int,
    ): TradeInputResult {
        var changes: StateChanges? = null
        var error: ParsingError? = null

        if (inputState.currentType != InputType.CLOSE_POSITION) {
            inputState.currentType = InputType.CLOSE_POSITION
            inputState.closePosition = initiateClosePosition(
                marketId = null,
                subaccountNumber = subaccountNumber,
                walletState = walletState,
                marketSummaryState = marketSummaryState,
                configs = configs,
                rewardsParams = rewardsParams,
            )
        }
        inputState.currentType = InputType.CLOSE_POSITION

        val childSubaccountNumber =
            MarginCalculator.getChildSubaccountNumberForIsolatedMarginClosePosition(
                account = walletState.account,
                subaccountNumber = subaccountNumber,
                tradeInput = inputState.closePosition,
            )
        val subaccountNumberChanges = if (subaccountNumber == childSubaccountNumber) {
            iListOf(subaccountNumber)
        } else {
            iListOf(subaccountNumber, childSubaccountNumber)
        }

        var sizeChanged = false
        val trade = inputState.closePosition
        when (type) {
            ClosePositionInputField.market -> {
                val position = if (data != null) getPosition(data, subaccountNumber, walletState) else null
                if (position != null) {
                    if (data != null) {
                        if (trade.marketId != data) {
                            trade.marketId = data
                            trade.size = null
                        }
                    }
                    trade.type = OrderType.Market

                    val positionSize = position.calculated[CalculationPeriod.current]?.size ?: Numeric.double.ZERO
                    trade.side = if (positionSize > Numeric.double.ZERO) OrderSide.Sell else OrderSide.Buy

                    trade.timeInForce = "IOC"
                    trade.reduceOnly = true

                    val currentPositionLeverage = position.calculated[CalculationPeriod.current]?.leverage?.abs()
                    trade.targetLeverage = if (currentPositionLeverage != null && currentPositionLeverage > 0) currentPositionLeverage else 1.0

                    // default full close
                    trade.sizePercent = 1.0
                    trade.size = TradeInputSize(
                        size = null,
                        usdcSize = null,
                        leverage = null,
                        input = "size.percent",
                    )

                    changes = StateChanges(
                        changes = iListOf(Changes.subaccount, Changes.input),
                        markets = null,
                        subaccountNumbers = subaccountNumberChanges,
                    )
                } else {
                    error = ParsingError.cannotModify(type.rawValue)
                }
            }
            ClosePositionInputField.size -> {
                val newSize = parser.asDouble(data)
                sizeChanged = (newSize != trade.size?.size)
                trade.size = trade.size?.copy(size = newSize)
                changes = StateChanges(
                    changes = iListOf(Changes.subaccount, Changes.input),
                    markets = null,
                    subaccountNumbers = subaccountNumberChanges,
                )
            }
            ClosePositionInputField.percent -> {
                val newPercent = parser.asDouble(data)
                sizeChanged = (newPercent != trade.sizePercent)
                trade.sizePercent = newPercent
                changes = StateChanges(
                    changes = iListOf(Changes.subaccount, Changes.input),
                    markets = null,
                    subaccountNumbers = subaccountNumberChanges,
                )
            }
        }
        if (sizeChanged) {
            when (type) {
                ClosePositionInputField.size,
                ClosePositionInputField.percent -> {
                    trade.size = trade.size?.copy(input = type.rawValue)
                }
                else -> { }
            }
        }

        return TradeInputResult(
            changes = changes,
            error = error,
        )
    }

    private fun getPosition(
        marketId: String,
        subaccountNumber: Int,
        wallet: InternalWalletState,
    ): InternalPerpetualPosition? {
        val position = wallet.account.groupedSubaccounts[subaccountNumber]?.openPositions?.get(marketId)
            ?: wallet.account.subaccounts[subaccountNumber]?.openPositions?.get(marketId)

        val size = position?.calculated?.get(CalculationPeriod.current)?.size
        return if (size != null && size != Numeric.double.ZERO) {
            position
        } else {
            null
        }
    }

    private fun initiateClosePosition(
        marketId: String?,
        subaccountNumber: Int,
        walletState: InternalWalletState,
        marketSummaryState: InternalMarketSummaryState,
        configs: InternalConfigsState,
        rewardsParams: InternalRewardsParamsState?
    ): InternalTradeInputState {
        val closePosition = InternalTradeInputState()
        closePosition.type = OrderType.Market
        closePosition.side = OrderSide.Buy
        closePosition.marketId = marketId ?: "ETH-USD"
        // default full close
        closePosition.sizePercent = 1.0
        closePosition.size = TradeInputSize(
            size = null,
            usdcSize = null,
            leverage = null,
            input = "size.percent",
        )

        val calculator = TradeInputCalculatorV2(parser, TradeCalculation.closePosition)
        return calculator.calculate(
            trade = closePosition,
            wallet = walletState,
            marketSummary = marketSummaryState,
            rewardsParams = rewardsParams,
            configs = configs,
            subaccountNumber = subaccountNumber,
            input = "size.percent",
        )
    }
}
