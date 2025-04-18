package exchange.dydx.abacus.calculator.tradeinput

import abs
import exchange.dydx.abacus.calculator.AccountTransformer
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalAccountState
import exchange.dydx.abacus.state.InternalConfigsState
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalMarketSummaryState
import exchange.dydx.abacus.state.InternalRewardsParamsState
import exchange.dydx.abacus.state.InternalSubaccountState
import exchange.dydx.abacus.state.InternalTradeInputState
import exchange.dydx.abacus.state.InternalUserState
import exchange.dydx.abacus.state.InternalWalletState
import exchange.dydx.abacus.state.machine.ClosePositionInputField
import exchange.dydx.abacus.state.safeCreate
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class TradeCalculation(val rawValue: String) {
    trade("TRADE"),
    closePosition("CLOSE_POSITION");

    companion object {
        operator fun invoke(rawValue: String) =
            TradeCalculation.entries.firstOrNull { it.rawValue == rawValue }
    }
}

internal object SlippageConstants {
    val MAJOR_MARKETS = listOf("ETH-USD", "BTC-USD", "SOL-USD")
    const val MARKET_ORDER_MAX_SLIPPAGE = 0.05
    const val STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET = 0.05
    const val TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET = 0.05
    const val STOP_MARKET_ORDER_SLIPPAGE_BUFFER = 0.1
    const val TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER = 0.1
    const val SLIPPAGE_STEP_SIZE = 0.00001
}

internal class TradeInputCalculator(
    private val parser: ParserProtocol,
    private val calculation: TradeCalculation,
    private val marginModeCalculator: TradeInputMarginModeCalculator = TradeInputMarginModeCalculator(),
    private val marketOrderCalculator: TradeInputMarketOrderCalculator = TradeInputMarketOrderCalculator(),
    private val nonMarketOrderCalculator: TradeInputNonMarketOrderCalculator = TradeInputNonMarketOrderCalculator(),
    private val optionsCalculator: TradeInputOptionsCalculator = TradeInputOptionsCalculator(parser),
    private val summaryCalculator: TradeInputSummaryCalculator = TradeInputSummaryCalculator(),
    private val accountTransformer: AccountTransformer = AccountTransformer(parser),
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
            period = CalculationPeriod.post,
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
