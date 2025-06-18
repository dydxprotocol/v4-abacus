package exchange.dydx.abacus.tests.extensions

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.state.helper.NetworkParam
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.account
import exchange.dydx.abacus.state.machine.candles
import exchange.dydx.abacus.state.machine.historicalFundings
import exchange.dydx.abacus.state.machine.historicalPnl
import exchange.dydx.abacus.state.machine.onChainEquityTiers
import exchange.dydx.abacus.state.machine.onMegaVaultPnl
import exchange.dydx.abacus.state.machine.onVaultMarketPnls
import exchange.dydx.abacus.state.machine.onVaultMarketPositions
import exchange.dydx.abacus.state.machine.receivedBatchOrderbookChanges
import exchange.dydx.abacus.state.machine.receivedBatchSubaccountsChanges
import exchange.dydx.abacus.state.machine.receivedBatchedCandlesChanges
import exchange.dydx.abacus.state.machine.receivedBatchedMarketsChanges
import exchange.dydx.abacus.state.machine.receivedBatchedTradesChanges
import exchange.dydx.abacus.state.machine.receivedCandles
import exchange.dydx.abacus.state.machine.receivedCandlesChanges
import exchange.dydx.abacus.state.machine.receivedFills
import exchange.dydx.abacus.state.machine.receivedMarkets
import exchange.dydx.abacus.state.machine.receivedMarketsChanges
import exchange.dydx.abacus.state.machine.receivedOrderbook
import exchange.dydx.abacus.state.machine.receivedSubaccountSubscribed
import exchange.dydx.abacus.state.machine.receivedSubaccountsChanges
import exchange.dydx.abacus.state.machine.receivedTrades
import exchange.dydx.abacus.state.machine.receivedTradesChanges
import exchange.dydx.abacus.state.machine.receivedTransfers
import exchange.dydx.abacus.state.machine.sparklines
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.ServerTime
import indexer.codegen.IndexerSparklineTimePeriod
import kollections.iListOf
import kollections.iMutableListOf
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

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
        updateStateChanges(changes)
    }

    val errors = if (error != null) iListOf(error) else null
    return StateResponse(state, changes, errors)
}

fun TradingStateMachine.socket(
    url: AbUrl,
    jsonString: String,
    subaccountNumber: Int,
    height: BlockAndTime?,
): StateResponse {
    val errors = iMutableListOf<ParsingError>()
    val json =
        try {
            Json.parseToJsonElement(jsonString).jsonObject.toMap()
        } catch (e: SerializationException) {
            errors.add(
                ParsingError(
                    ParsingErrorType.ParsingError,
                    "$jsonString is not a valid JSON object",
                    e.stackTraceToString(),
                ),
            )
            null
        }
    if (json == null || errors.isNotEmpty()) {
        return StateResponse(state, null, errors)
    }
    return socket(url, json, subaccountNumber, height)
}

@Throws(Exception::class)
private fun TradingStateMachine.socket(
    url: AbUrl,
    payload: Map<String, Any>,
    subaccountNumber: Int,
    height: BlockAndTime?,
): StateResponse {
    var changes: StateChanges? = null
    val type = parser.asString(payload["type"])
    val channel = parser.asString(payload["channel"])
    val id = parser.asString(payload["id"])
    val childSubaccountNumber = parser.asInt(payload["subaccountNumber"])
    val info = SocketInfo(type, channel, id, childSubaccountNumber)
    try {
        when (type) {
            "subscribed" -> {
                val content = parser.asNativeMap(payload["contents"])
                    ?: throw ParsingException(
                        ParsingErrorType.MissingContent,
                        payload.toString(),
                    )
                when (channel) {
                    "v3_markets", "v4_markets" -> {
                        changes = receivedMarkets(content, subaccountNumber)
                    }

                    "v4_subaccounts", "v4_parent_subaccounts" -> {
                        changes = receivedSubaccountSubscribed(content, height)
                    }

                    "v3_orderbook", "v4_orderbook" -> {
                        val market = parser.asString(payload["id"])
                        changes = receivedOrderbook(market, content, subaccountNumber)
                    }

                    "v3_trades", "v4_trades" -> {
                        val market = parser.asString(payload["id"])
                        changes = receivedTrades(market, content)
                    }

                    "v4_candles" -> {
                        val channel = parser.asString(payload["id"])
                        val (market, resolution) = splitCandlesChannel(channel)
                        changes = receivedCandles(market, resolution, content)
                    }

                    else -> {
                        throw ParsingException(
                            ParsingErrorType.UnknownChannel,
                            "$channel subscribed is not known",
                        )
                    }
                }
            }

            "unsubscribed" -> {}

            "channel_data" -> {
                val content = parser.asNativeMap(payload["contents"])
                    ?: throw ParsingException(
                        ParsingErrorType.MissingContent,
                        payload.toString(),
                    )
                when (channel) {
                    "v3_markets", "v4_markets" -> {
                        changes = receivedMarketsChanges(content, subaccountNumber)
                    }

                    "v4_subaccounts", "v4_parent_subaccounts" -> {
                        changes = receivedSubaccountsChanges(content, info, height)
                    }

                    "v3_orderbook", "v4_orderbook" -> {
                        throw ParsingException(
                            ParsingErrorType.UnhandledEndpoint,
                            "channel_data for $channel is not implemented",
                        )
                        //                                    change = receivedOrderbookChanges(market, it)
                    }

                    "v3_trades", "v4_trades" -> {
                        val market = parser.asString(payload["id"])
                        changes = receivedTradesChanges(market, content)
                    }

                    "v4_candles" -> {
                        val channel = parser.asString(payload["id"])
                        val (market, resolution) = splitCandlesChannel(channel)
                        changes = receivedCandlesChanges(market, resolution, content)
                    }

                    else -> {
                        throw ParsingException(
                            ParsingErrorType.UnknownChannel,
                            "$channel channel data is not known",
                        )
                    }
                }
            }

            "channel_batch_data" -> {
                val content = parser.asList(payload["contents"])
                    ?: throw ParsingException(
                        ParsingErrorType.MissingContent,
                        payload.toString(),
                    )
                when (channel) {
                    "v3_markets", "v4_markets" -> {
                        changes = receivedBatchedMarketsChanges(content, subaccountNumber)
                    }

                    "v3_trades", "v4_trades" -> {
                        val market = parser.asString(payload["id"])
                        changes = receivedBatchedTradesChanges(market, content)
                    }

                    "v4_candles" -> {
                        val channel = parser.asString(payload["id"])
                        val (market, resolution) = splitCandlesChannel(channel)
                        changes = receivedBatchedCandlesChanges(market, resolution, content)
                    }

                    "v3_orderbook", "v4_orderbook" -> {
                        val market = parser.asString(payload["id"])
                        changes = receivedBatchOrderbookChanges(
                            market,
                            content,
                            subaccountNumber,
                        )
                    }

                    "v4_subaccounts", "v4_parent_subaccounts" -> {
                        changes = receivedBatchSubaccountsChanges(content, info, height)
                    }

                    else -> {
                        throw ParsingException(
                            ParsingErrorType.UnknownChannel,
                            "$channel channel batch data is not known",
                        )
                    }
                }
            }

            "connected" -> {}

            "error" -> {
                throw ParsingException(ParsingErrorType.BackendError, payload.toString())
            }

            else -> {
                throw ParsingException(
                    ParsingErrorType.Unhandled,
                    "Type [ $type # $channel ] is not handled",
                )
            }
        }
        var realChanges = changes
        changes?.let {
            realChanges = updateStateChanges(it)
        }
        return StateResponse(state, realChanges, null, info)
    } catch (e: ParsingException) {
        return StateResponse(state, null, iListOf(e.toParsingError()), info)
    }
}

private fun TradingStateMachine.splitCandlesChannel(channel: String?): Pair<String, String> {
    if (channel == null) {
        throw ParsingException(
            ParsingErrorType.UnknownChannel,
            "$channel is not known",
        )
    }
    val marketAndResolution = channel.split("/")
    if (marketAndResolution.size != 2) {
        throw ParsingException(
            ParsingErrorType.UnknownChannel,
            "$channel is not known",
        )
    }
    val market = marketAndResolution[0]
    val resolution = marketAndResolution[1]
    return Pair(market, resolution)
}

/**
 * function specifically for testing spoofed rest response processing
 */
fun TradingStateMachine.rest(
    url: AbUrl,
    payload: String,
    subaccountNumber: Int,
    height: Int?,
    deploymentUri: String? = null,
    period: String? = null,
): StateResponse {
    /*
    For backward compatibility only
     */
    var changes: StateChanges? = null
    var error: ParsingError? = null
    when (url.path) {
        "/v3/historical-pnl", "/v4/historical-pnl" -> {
            val subaccountNumber =
                parser.asInt(url.params?.firstOrNull { param -> param.key == "subaccountNumber" }?.value)
                    ?: 0
            changes = historicalPnl(payload, subaccountNumber)
        }

        "/v3/candles" -> {
            changes = candles(payload)
        }

        "/v4/sparklines" -> {
            changes = sparklines(payload, IndexerSparklineTimePeriod.ONE_DAY)
        }

        "/v4/fills" -> {
            val subaccountNumber =
                parser.asInt(url.params?.firstOrNull { param -> param.key == "subaccountNumber" }?.value)
                    ?: 0
            changes = fills(payload, subaccountNumber)
        }

        "/v4/transfers" -> {
            val subaccountNumber =
                parser.asInt(url.params?.firstOrNull { param -> param.key == "subaccountNumber" }?.value)
                    ?: 0
            changes = transfers(payload, subaccountNumber)
        }

        "/v4/vault/v1/megavault/historicalPnl" -> {
            changes = onMegaVaultPnl(arrayOf(payload))
        }

        "/v4/vault/v1/megavault/positions" -> {
            changes = onVaultMarketPositions(payload)
        }

        "/v4/vault/v1/vaults/historicalPnl" -> {
            changes = onVaultMarketPnls(payload)
        }

        "/configs/markets.json" -> {
            if (deploymentUri != null) {
                changes = configurations(
                    infoPayload = payload,
                    pricesPayload = "",
                    subaccountNumber = subaccountNumber,
                )
            }
        }

        else -> {
            if (url.path.contains("/v3/historical-funding/") || url.path.contains("/v4/historicalFunding/")) {
                changes = historicalFundings(payload)
            } else if (url.path.contains("/v3/candles/") || url.path.contains("/v4/candles/")) {
                changes = candles(payload)
            } else if (url.path.contains("/v4/addresses/")) {
                changes = account(payload)
            } else {
                error = ParsingError(
                    ParsingErrorType.UnhandledEndpoint,
                    "${url.path} parsing has not be implemented, or is an invalid endpoint",
                )
            }
        }
    }
    if (changes != null) {
        updateStateChanges(changes)
    }

    val errors = if (error != null) iListOf(error) else null
    return StateResponse(state, changes, errors)
}

private fun TradingStateMachine.transfers(payload: String, subaccountNumber: Int): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedTransfers(json, subaccountNumber)
    } else {
        StateChanges.noChange
    }
}

private fun TradingStateMachine.fills(payload: String, subaccountNumber: Int): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedFills(json, subaccountNumber)
    } else {
        StateChanges.noChange
    }
}
