package exchange.dydx.abacus.tests.extensions

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.app.adaptors.NetworkParam
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.onChainEquityTiers
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.ServerTime
import kollections.iListOf
import kotlinx.datetime.Instant

fun TradingStateMachine.loadMarkets(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.marketsChannel.subscribed, 0, null)
}

fun TradingStateMachine.loadMarketsChanged(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.marketsChannel.channel_data, 0, null)
}

fun TradingStateMachine.loadMarketsConfigurations(mock: AbacusMockData, deploymentUri: String): StateResponse {
    return rest(
        AbUrl(
            host = "dydx-v4-shared-resources.vercel.app",
            path = "/configs/markets.json",
            scheme = "https://",
        ),
        mock.marketsConfigurations.configurations,
        0,
        null,
        deploymentUri,
    )
}

fun TradingStateMachine.loadAccounts(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.accountsChannel.subscribed, 0, null)
}

fun TradingStateMachine.loadAccountsChanged(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.accountsChannel.channel_data, 0, null)
}

fun TradingStateMachine.loadv4Accounts(mock: AbacusMockData, endpoint: String): StateResponse {
    return rest(AbUrl.fromString(endpoint), mock.accountsChannel.v4accountsReceived, 0, null)
}

fun TradingStateMachine.loadv4SubaccountsWithPositions(
    mock: AbacusMockData,
    endpoint: String,
): StateResponse {
    return rest(
        url = AbUrl.fromString(urlString = endpoint),
        payload = mock.accountsChannel.v4accountsReceivedWithPositions,
        subaccountNumber = 0,
        height = null,
    )
}

fun TradingStateMachine.loadv4SubaccountSubscribed(
    mock: AbacusMockData,
    endpoint: AbUrl,
): StateResponse {
    return socket(endpoint, mock.accountsChannel.v4_subscribed, 0, null)
}

fun TradingStateMachine.loadv4SubaccountChanged(
    mock: AbacusMockData,
    endpoint: AbUrl,
): StateResponse {
    return socket(endpoint, mock.accountsChannel.v4_channel_data, 0, null)
}

fun TradingStateMachine.loadv4SubaccountWithOrdersAndFillsChanged(
    mock: AbacusMockData,
    endpoint: AbUrl,
): StateResponse {
    return socket(endpoint, mock.accountsChannel.v4_channel_data_with_orders, 0, null)
}

fun TradingStateMachine.loadv4MarketsSubscribed(
    mock: AbacusMockData,
    endpoint: AbUrl,
): StateResponse {
    return socket(endpoint, mock.marketsChannel.v4_subscribed, 0, null)
}

fun TradingStateMachine.loadv4MarketsChanged(mock: AbacusMockData, endpoint: AbUrl): StateResponse {
    return socket(endpoint, mock.marketsChannel.v4_channel_data, 0, null)
}

fun TradingStateMachine.loadv4MarketsBatchChanged(
    mock: AbacusMockData,
    endpoint: AbUrl,
): StateResponse {
    return socket(endpoint, mock.marketsChannel.v4_channel_batch_data, 0, null)
}

fun TradingStateMachine.loadv4TradesSubscribed(
    mock: AbacusMockData,
    endpoint: AbUrl,
): StateResponse {
    return socket(endpoint, mock.tradesChannel.v4_subscribed, 0, null)
}

fun TradingStateMachine.loadv4TradesChanged(mock: AbacusMockData, endpoint: AbUrl): StateResponse {
    return socket(endpoint, mock.tradesChannel.v4_channel_data, 0, null)
}

fun TradingStateMachine.loadv4TradesBatchChanged(
    mock: AbacusMockData,
    endpoint: AbUrl,
): StateResponse {
    return socket(endpoint, mock.tradesChannel.v4_channel_batch_data, 0, null)
}

fun TradingStateMachine.loadv4FillsReceived(mock: AbacusMockData, endpoint: String): StateResponse {
    return rest(AbUrl.fromString(endpoint), mock.fillsChannel.v4_rest, 0, null)
}

fun TradingStateMachine.loadSimpleAccounts(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.accountsChannel.simpleSubscribed, 0, null)
}

fun TradingStateMachine.loadUser(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "dydx-v4-shared-resources.vercel.app",
            path = "/v3/users",
            scheme = "https://",
        ),
        mock.user.call,
        0,
        null,
    )
}

fun TradingStateMachine.loadTrades(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.tradesChannel.subscribed, 0, null)
}

fun TradingStateMachine.loadTradesChanged(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.tradesChannel.channel_data, 0, null)
}

fun TradingStateMachine.loadOrderbook(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.orderbookChannel.subscribed, 0, null)
}

fun TradingStateMachine.loadOrderbookChanged(mock: AbacusMockData): StateResponse {
    return socket(mock.socketUrl, mock.orderbookChannel.channel_batch_data, 0, null)
}

fun TradingStateMachine.loadHistoricalPnlsFirst(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "api.stage.dydx.exchange",
            path = "/v3/historical-pnl",
            scheme = "https://",
        ),
        mock.historicalPNL.firstCall,
        0,
        null,
    )
}

fun TradingStateMachine.loadHistoricalPnlsSecond(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "api.stage.dydx.exchange",
            path = "/v3/historical-pnl",
            scheme = "https://",
        ),
        mock.historicalPNL.secondCall,
        0,
        null,
    )
}

fun TradingStateMachine.loadCandlesAllMarkets(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "api.stage.dydx.exchange",
            port = null,
            path = "/v3/candles",
            scheme = "https://",
            NetworkParam.parse("resolution=1HOUR&limit=25"),
        ),
        mock.candles.summaryCall,
        0,
        null,
    )
}

fun TradingStateMachine.loadCandlesFirst(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "api.stage.dydx.exchange",
            port = null,
            path = "/v3/candles",
            scheme = "https://",
            NetworkParam.parse("market=ETH-USD&resolution=15MIN"),
        ),
        mock.candles.firstCall,
        0,
        null,
    )
}

fun TradingStateMachine.loadCandlesSecond(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "api.stage.dydx.exchange",
            port = null,
            path = "/v3/candles",
            scheme = "https://",
            NetworkParam.parse("market=ETH-USD&resolution=15MIN"),
        ),
        mock.candles.secondCall,
        0,
        null,
    )
}

fun TradingStateMachine.loadFeeTiers(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "dydx-v4-shared-resources.vercel.app",
            port = null,
            path = "/config/staging/fee_tiers.json",
            scheme = "https://",
        ),
        mock.feeTiers.call,
        0,
        null,
    )
}

fun TradingStateMachine.loadFeeDiscounts(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "dydx-v4-shared-resources.vercel.app",
            port = null,
            path = "/config/staging/fee_discounts.json",
            scheme = "https://",
        ),
        mock.feeDiscounts.call,
        0,
        null,
    )
}

fun TradingStateMachine.loadHistoricalFundings(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "api.stage.dydx.exchange",
            port = null,
            path = "/v3/historical-funding/ETH-USD",
            scheme = "https://",
        ),
        mock.historicalFundingsMock.call,
        0,
        null,
    )
}

fun TradingStateMachine.loadFirstCalculation(mock: AbacusMockData): StateResponse {
    return rest(
        AbUrl(
            host = "api.stage.dydx.exchange",
            port = null,
            path = "/v3/candles",
            scheme = "https:/",
        ),
        mock.candles.secondCall,
        0,
        null,
    )
}

fun TradingStateMachine.log(text: String, from: Instant): Instant {
    val now = ServerTime.now()
    val timeLapse = now.minus(from).inWholeMilliseconds
    print(text)
    print("\n")
    print(timeLapse)
    print("\n\n")
    return now
}

fun TradingStateMachine.parseOnChainEquityTiers(payload: String): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    try {
        changes = onChainEquityTiers(payload)
    } catch (e: ParsingException) {
        error = e.toParsingError()
    }
    if (changes != null) {
        update(changes)
    }

    val errors = if (error != null) iListOf(error) else null
    return StateResponse(state, changes, errors)
}
