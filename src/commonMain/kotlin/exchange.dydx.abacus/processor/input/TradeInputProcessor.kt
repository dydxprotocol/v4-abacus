package exchange.dydx.abacus.processor.input

import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.internalstate.InternalInputType
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import kollections.iListOf

internal class TradeInputProcessor(
    private val parser: ParserProtocol,
) {
    fun tradeInMarket(
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
        } else {
            if (inputState.trade.marketId != null) {
                // existing trade
                inputState.trade.marketId = marketId
                inputState.trade.size = null
                inputState.trade.price = null
            } else {
                // new trade
                inputState.trade = initialTradeInputState(
                    marketId = marketId,
                    subaccountNumber = subaccountNumber,
                    accountState = accountState,
                    marketState = marketState,
                )
            }
        }

        return StateChanges(
            changes = iListOf(Changes.input),
            markets = null,
            subaccountNumbers = iListOf(subaccountNumber),
        )
    }

    private fun initialTradeInputState(
        marketId: String?,
        subaccountNumber: Int,
        accountState: InternalAccountState,
        marketState: InternalMarketState,
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
            market = marketState.perpetualMarket,
        )

        return InternalTradeInputState(
            marketId = marketId,
            size = null,
            price = null,
            type = "LIMIT",
            side = "BUY",
            marginMode = marginMode, // TODO
        )
    }
}
