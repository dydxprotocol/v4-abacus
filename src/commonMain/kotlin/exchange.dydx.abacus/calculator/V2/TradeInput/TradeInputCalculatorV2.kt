package exchange.dydx.abacus.calculator.v2.tradeinput

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.v2.AccountTransformerV2
import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalConfigsState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.internalstate.InternalRewardsParamsState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.InternalUserState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder

internal class TradeInputCalculatorV2(
    private val parser: ParserProtocol,
    private val calculation: TradeCalculation,
    private val marginModeCalculator: TradeInputMarginModeCalculator = TradeInputMarginModeCalculator(),
    private val marketOrderCalculator: TradeInputMarketOrderCalculator = TradeInputMarketOrderCalculator(),
    private val nonMarketOrderCalculator: TradeInputNonMarketOrderCalculator = TradeInputNonMarketOrderCalculator(),
    private val optionsCalculator: TradeInputOptionsCalculator = TradeInputOptionsCalculator(parser),
    private val summaryCalculator: TradeInputSummaryCalculator = TradeInputSummaryCalculator(),
    private val accountTransformer: AccountTransformerV2 = AccountTransformerV2(parser),
) {
    fun calculate(
        trade: InternalTradeInputState,
        wallet: InternalWalletState,
        marketSummary: InternalMarketSummaryState,
        rewardsParams: InternalRewardsParamsState?,
        configs: InternalConfigsState,
        subaccountNumber: Int,
        input: String?,
    ): InternalTradeInputState {
        val account = wallet.account

        val crossMarginSubaccount = account.subaccounts[subaccountNumber]
        val subaccount =
            account.groupedSubaccounts[subaccountNumber] ?: crossMarginSubaccount
        val user = wallet.user
        val markets = marketSummary.markets

        marginModeCalculator.updateTradeInputMarginMode(
            tradeInput = trade,
            markets = markets,
            account = account,
            subaccountNumber = subaccountNumber,
        )

        if (input != null) {
            if (calculation == TradeCalculation.closePosition) {
                calculateClosePositionSize(trade, markets[trade.marketId], subaccount)
            }
            when (trade.type) {
                OrderType.Market,
                OrderType.StopMarket,
                OrderType.TakeProfitMarket ->
                    marketOrderCalculator.calculate(
                        trade = trade,
                        market = markets[trade.marketId],
                        subaccount = if (trade.marginMode == MarginMode.Isolated) subaccount else crossMarginSubaccount,
                        user = user,
                        input = input,
                    )

                OrderType.Limit,
                OrderType.StopLimit,
                OrderType.TakeProfitLimit,
                OrderType.TrailingStop,
                OrderType.Liquidated,
                OrderType.Liquidation,
                OrderType.Offsetting,
                OrderType.Deleveraged,
                OrderType.FinalSettlement,
                null ->
                    nonMarketOrderCalculator.calculate(
                        trade = trade,
                        market = markets[trade.marketId],
                        input = input,
                    )
            }
        }

        finalize(
            trade = trade,
            account = account,
            subaccount = subaccount,
            user = user,
            market = markets[trade.marketId],
            rewardsParams = rewardsParams,
            feeTiers = configs.feeTiers,
        )

        accountTransformer.applyTradeToAccount(
            account = account,
            subaccountNumber = subaccountNumber,
            trade = trade,
            market = markets[trade.marketId],
            CalculationPeriod.post,
        )

        return trade
    }

    private fun calculateClosePositionSize(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
    ): InternalTradeInputState {
        val inputType = ClosePositionInputField.invoke(trade.size?.input)
        val marketId = trade.marketId ?: return trade
        val position = subaccount?.openPositions?.get(marketId) ?: return trade
        val positionSize = position.calculated[CalculationPeriod.current]?.size ?: return trade
        val positionSizeAbs = positionSize.abs()
        trade.side = if (positionSize > Numeric.double.ZERO) OrderSide.Sell else OrderSide.Buy
        when (inputType) {
            ClosePositionInputField.percent -> {
                val percent = trade.sizePercent ?: return trade
                val size =
                    if (percent > Numeric.double.ONE) positionSizeAbs else positionSizeAbs * percent
                val stepSize = market?.perpetualMarket?.configs?.stepSize ?: return trade
                trade.size =
                    TradeInputSize.safeCreate(trade.size).copy(size = Rounder.round(size, stepSize))
                return trade
            }

            ClosePositionInputField.size -> {
                trade.sizePercent = null
                val size = trade.size?.size ?: return trade
                if (size > positionSizeAbs) {
                    trade.size = TradeInputSize.safeCreate(trade.size).copy(size = positionSizeAbs)
                }
            }

            else -> {}
        }
        return trade
    }

    private fun finalize(
        trade: InternalTradeInputState,
        account: InternalAccountState,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        market: InternalMarketState?,
        rewardsParams: InternalRewardsParamsState?,
        feeTiers: List<FeeTier>?,
    ): InternalTradeInputState {
        val marketId = market?.perpetualMarket?.id
        val position = if (marketId != null) {
            subaccount?.openPositions?.get(marketId)
        } else {
            null
        }

        optionsCalculator.calculate(
            trade = trade,
            position = position,
            account = account,
            subaccount = subaccount,
            market = market,
        )

        summaryCalculator.calculate(
            trade = trade,
            subaccount = subaccount,
            user = user,
            market = market,
            rewardsParams = rewardsParams,
            feeTiers = feeTiers,
        )

        return trade
    }
}
