package exchange.dydx.abacus.calculator.v2.TradeInput

import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.input.OrderType
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

internal class TradeInputCalculatorV2(
    private val parser: ParserProtocol,
    private val calculation: TradeCalculation,
    private val marginModeCalculator: TradeInputMarginModeCalculator = TradeInputMarginModeCalculator(),
    private val marketOrderCalculator: TradeInputMarketOrderCalculator = TradeInputMarketOrderCalculator(calculation),
    private val nonMarketOrderCalculator: TradeInputNonMarketOrderCalculator = TradeInputNonMarketOrderCalculator(),
    private val optionsCalculator: TradeInputOptionsCalculator = TradeInputOptionsCalculator(parser),
    private val summaryCalculator: TradeInputSummaryCalculator = TradeInputSummaryCalculator(),
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
        val subaccount =
            account.groupedSubaccounts[subaccountNumber] ?: account.subaccounts[subaccountNumber]
        val user = wallet.user
        val markets = marketSummary.markets

        marginModeCalculator.updateTradeInputMarginMode(
            tradeInput = trade,
            markets = markets,
            account = account,
            subaccountNumber = subaccountNumber,
        )

        if (input != null) {
            when (trade.type) {
                OrderType.Market,
                OrderType.StopMarket,
                OrderType.TakeProfitMarket ->
                    marketOrderCalculator.calculate(
                        trade = trade,
                        market = markets[trade.marketId],
                        subaccount = subaccount,
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
        val type = trade.type
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
