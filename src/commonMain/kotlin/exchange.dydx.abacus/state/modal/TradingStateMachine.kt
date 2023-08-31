package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.calculator.*
import exchange.dydx.abacus.output.*
import exchange.dydx.abacus.output.input.Input
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.processor.assets.AssetsProcessor
import exchange.dydx.abacus.processor.configs.ConfigsProcessor
import exchange.dydx.abacus.processor.markets.MarketsSummaryProcessor
import exchange.dydx.abacus.processor.squid.SquidProcessor
import exchange.dydx.abacus.processor.wallet.WalletProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.*
import exchange.dydx.abacus.state.app.AppVersion
import exchange.dydx.abacus.state.app.V4Environment
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.*
import exchange.dydx.abacus.validator.InputValidator
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.iSetOf
import kollections.toIList
import kollections.toIMap
import kollections.toIMutableMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.days


@Suppress("UNCHECKED_CAST")
@JsExport
//@Serializable
open class TradingStateMachine(
    private val environment: V4Environment?,
    private val localizer: LocalizerProtocol?,
    private val formatter: Formatter?,
    private val version: AppVersion,
    private val maxSubaccountNumber: Int,
) {
    internal val parser: ParserProtocol = Parser()
    internal val marketsProcessor = MarketsSummaryProcessor(parser, version == AppVersion.v3)
    internal val assetsProcessor = run {
        val processor = AssetsProcessor(parser)
        processor.environment = environment
        processor
    }
    internal val walletProcessor = WalletProcessor(parser)
    internal val configsProcessor = ConfigsProcessor(parser)
    internal val squidProcessor = SquidProcessor(parser)

    internal val marketsCalculator = MarketCalculator(parser)
    internal val accountCalculator = AccountCalculator(parser)
    internal val inputValidator = InputValidator(localizer, formatter, parser)


    internal var data: IMap<String, Any>? = null

    private var dummySubaccountPNLs = iMutableMapOf<String, SubaccountHistoricalPNL>();

    internal var groupingMultiplier: Int
        get() = marketsProcessor.groupingMultiplier
        set(value) {
            marketsProcessor.groupingMultiplier = value
        }

    internal var marketsSummary: IMap<String, Any>?
        get() {
            return parser.asMap(data?.get("markets"))
        }
        set(value) {
            val modified = data?.mutable() ?: iMutableMapOf()
            modified.safeSet("markets", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var historicalPnlDays: Int = 1

    internal var assets: IMap<String, Any>?
        get() {
            return parser.asMap(data?.get("assets"))
        }
        set(value) {
            val modified = data?.mutable() ?: iMutableMapOf()
            modified.safeSet("assets", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var wallet: IMap<String, Any>?
        get() {
            return parser.asMap(data?.get("wallet"))
        }
        set(value) {
            val oldAddress = parser.asString(parser.value(wallet, "walletAddress"))
            val modified = data?.mutable() ?: iMutableMapOf()
            modified.safeSet("wallet", value)
            this.data = if (modified.isEmpty()) null else modified
            val address = parser.asString(parser.value(wallet, "walletAddress"))
            if (address != oldAddress) {
                dummySubaccountPNLs = iMutableMapOf()
            }
        }

    internal var account: IMap<String, Any>?
        get() {
            return parser.asMap(wallet?.get("account"))
        }
        set(value) {
            val modified = wallet?.mutable() ?: iMutableMapOf()
            modified.safeSet("account", value)
            this.wallet = if (modified.size != 0) modified else null
        }

    internal var user: IMap<String, Any>?
        get() {
            return parser.asMap(wallet?.get("user"))
        }
        set(value) {
            val modified = wallet?.mutable() ?: iMutableMapOf()
            modified.safeSet("user", value)
            this.wallet = if (modified.size != 0) modified else null
        }


    internal var configs: IMap<String, Any>?
        get() {
            return parser.asMap(data?.get("configs"))
        }
        set(value) {
            val modified = data?.mutable() ?: iMutableMapOf()
            modified.safeSet("configs", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var input: IMap<String, Any>?
        get() {
            return parser.asMap(data?.get("input"))
        }
        set(value) {
            val modified = data?.mutable() ?: iMutableMapOf()
            modified.safeSet("input", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var transferStatuses: IMap<String, Any>?
        get() {
            return parser.asMap(data?.get("transferStatuses"))
        }
        set(value) {
            val modified = data?.mutable() ?: iMutableMapOf()
            modified.safeSet("transferStatuses", value)
            this.data = if (modified.size != 0) modified else null
        }

    var state: PerpetualState? = null

    private fun noChange(): StateResponse {
        return StateResponse(state, null)
    }

    fun socket(
        url: AbUrl,
        jsonString: String,
        subaccountNumber: Int,
        height: Int?,
    ): StateResponse {
        val errors = iMutableListOf<ParsingError>()
        val json =
            try {
                Json.parseToJsonElement(jsonString).jsonObject.toIMap()
            } catch (e: Exception) {
                errors.add(
                    ParsingError(
                        ParsingErrorType.ParsingError,
                        "$jsonString is not a valid JSON object",
                        e.stackTraceToString()
                    )
                )
                null
            }
        if (json == null || errors.isNotEmpty()) {
            return StateResponse(state, null, errors)
        }
        return socket(url, json, subaccountNumber, height)
    }

    @Throws(Exception::class)
    private fun socket(
        url: AbUrl,
        payload: IMap<String, Any>,
        subaccountNumber: Int,
        height: Int?,
    ): StateResponse {
        var changes: StateChanges? = null
        val type = parser.asString(payload["type"])
        val channel = parser.asString(payload["channel"])
        val id = parser.asString(payload["id"])
        val info = SocketInfo(type, channel, id)

        try {
            when (type) {
                "subscribed" -> {
                    val content = parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString()
                        )
                    when (channel) {
                        "v3_markets", "v4_markets" -> {
                            changes = receivedMarkets(content, subaccountNumber)
                        }

                        "v3_accounts", "v4_subaccounts" -> {
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

                        else -> {
                            throw ParsingException(
                                ParsingErrorType.UnknownChannel,
                                "$channel is not known"
                            )
                        }
                    }
                }

                "channel_data" -> {
                    val content = parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString()
                        )
                    when (channel) {
                        "v3_markets", "v4_markets" -> {
                            changes = receivedMarketsChanges(content, subaccountNumber)
                        }

                        "v3_accounts", "v4_subaccounts" -> {
                            changes = receivedAccountsChanges(content, info, height)
                        }

                        "v3_orderbook", "v4_orderbook" -> {
                            throw ParsingException(
                                ParsingErrorType.UnhandledEndpoint,
                                "channel_data for ${channel} is not implemented"
                            )
                            //                                    change = receivedOrderbookChanges(market, it)
                        }

                        "v3_trades", "v4_trades" -> {
                            val market = parser.asString(payload["id"])
                            changes = receivedTradesChanges(market, content)
                        }

                        else -> {
                            throw ParsingException(
                                ParsingErrorType.UnknownChannel,
                                "$channel is not known"
                            )
                        }
                    }
                }

                "channel_batch_data" -> {
                    val content = parser.asList(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString()
                        )
                    when (channel) {
                        "v3_markets", "v4_markets" -> {
                            changes = receivedBatchedMarketsChanges(content, subaccountNumber)
                        }

                        "v3_trades", "v4_trades" -> {
                            val market = parser.asString(payload["id"])
                            changes = receivedBatchedTradesChanges(market, content)
                        }

                        "v3_orderbook", "v4_orderbook" -> {
                            val market = parser.asString(payload["id"])
                            changes = receivedBatchOrderbookChanges(
                                market,
                                content,
                                subaccountNumber
                            )
                        }

                        "v3_accounts", "v4_subaccounts" -> {
                            changes = receivedBatchAccountsChanges(content, info, height)
                        }

                        else -> {
                            throw ParsingException(
                                ParsingErrorType.UnknownChannel,
                                "$channel is not known"
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
                        "Type [ $type # $channel ] is not handled"
                    )
                }
            }
            var realChanges = changes
            changes?.let {
                realChanges = update(it)
            }
            return StateResponse(state, realChanges, null, info)
        } catch (e: ParsingException) {
            return StateResponse(state, null, iListOf(e.toParsingError()), info)
        }
    }

    fun rest(url: AbUrl, payload: String, subaccountNumber: Int, height: Int?): StateResponse {
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

            "/v3/users" -> {
                changes = user(payload)
            }

            "/v3/candles" -> {
                changes = candles(payload)
            }

            "/v4/sparklines" -> {
                changes = sparklines(payload)
            }

            "/v3/fills", "/v4/fills" -> {
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

            "/config/markets.json", "/v4/markets.json" -> {
                changes = configurations(payload, subaccountNumber)
            }

            "/config/staging/fee_tiers.json", "/config/fee_tiers.json" -> {
                changes = feeTiers(payload)
            }

            "/config/staging/fee_discounts.json", "/config/fee_discounts.json" -> {
                changes = feeDiscounts(payload)
            }

            else -> {
                if (url.path.contains("/v3/historical-funding/") || url.path.contains("/v4/historicalFunding/"))
                    changes = historicalFundings(payload)
                else if (url.path.contains("/v3/candles/") || url.path.contains("/v4/candles/"))
                    changes = candles(payload)
                else if (url.path.contains("/v4/addresses/"))
                    changes = subaccounts(payload)
                else
                    error = ParsingError(
                        ParsingErrorType.UnhandledEndpoint,
                        "${url.path} parsing has not be implemented, or is an invalid endpoint"
                    )
            }
        }
        if (changes != null) {
            update(changes)
        }

        val errors = if (error != null) iListOf(error) else null
        return StateResponse(state, changes, errors)
    }

//    internal fun process(host: String, path: String, payload: String): StateResponse {
//        val url = URL.parse("$host$path")
//            ?: throw ParsingException(ParsingErrorType.InvalidUrl, "Couldn't parse $host$path")
//        return rest(url, payload)
//    }

    internal fun resetWallet(accountAddress: String?): StateResponse {
        val modified = data?.mutable() ?: iMutableMapOf()
        val wallet = if (accountAddress != null) iMapOf("walletAddress" to accountAddress) else null
        this.wallet = wallet
        val changes = StateChanges(
            iListOf(
                Changes.wallet,
                Changes.subaccount,
                Changes.historicalPnl,
                Changes.fills,
                Changes.transfers,
                Changes.fundingPayments
            )
        )
        update(changes)
        walletProcessor.accountAddress = accountAddress
        return StateResponse(state, changes, null)
    }

    internal fun configurations(payload: String, subaccountNumber: Int?): StateChanges {
        val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
        return receivedMarketsConfigurations(json, subaccountNumber)
    }

    internal fun update(changes: StateChanges): StateChanges {
        if (changes.changes.contains(Changes.input)) {
            val subaccountNumber = changes.subaccountNumbers?.firstOrNull()

            val subaccount = if (subaccountNumber != null)
                parser.asMap(parser.value(this.account, "subaccounts.$subaccountNumber")) else null
            this.input = inputValidator.validate(
                this.wallet,
                this.user,
                subaccount,
                parser.asMap(this.marketsSummary?.get("markets")),
                this.input,
                this.configs
            )

            when (this.input?.get("current")) {
                "trade" -> {
                    calculateTrade(subaccountNumber)
                }

                "closePosition" -> {
                    calculateClosePosition(subaccountNumber)
                }

                "transfer" -> {
                    calculateTransfer(subaccountNumber)
                }

                else -> {}
            }
        }
        recalculateStates(changes)

        val wallet = state?.wallet
        val input = state?.input

        state = update(state, changes)

        val realChanges = iMutableListOf<Changes>()
        for (change in changes.changes) {
            val didChange = when (change) {
                Changes.assets,
                Changes.markets,
                Changes.candles,
                Changes.sparklines,
                Changes.historicalFundings,
                Changes.accountBalances,
                Changes.subaccount,
                Changes.historicalPnl,
                Changes.fills,
                Changes.transfers,
                Changes.fundingPayments,
                Changes.trades,
                Changes.configs,
                Changes.transferStatuses,
                Changes.orderbook,
                -> true

                Changes.wallet -> state?.wallet != wallet
                Changes.input -> state?.input != input
            }
            if (didChange) {
                realChanges.add(change)
            }
        }
        return StateChanges(realChanges, changes.markets, changes.subaccountNumbers)
    }

    private fun calculateTrade(subaccountNumber: Int?) {
        calculateTrade("trade", TradeCalculation.trade, subaccountNumber)
    }

    private fun calculateTrade(tag: String, calculation: TradeCalculation, subaccountNumber: Int?) {
        val input = this.input?.mutable()
        val trade = parser.asMap(input?.get(tag))
        val inputType = parser.asString(parser.value(trade, "size.input"))
        val calculator = TradeInputCalculator(parser, calculation)
        val params = iMutableMapOf<String, Any>()
        params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
        params.safeSet("account", account)
        params.safeSet("user", user)
        params.safeSet("trade", trade)

        val modified = calculator.calculate(params, subaccountNumber, inputType)
        this.setMarkets(parser.asMap(modified["markets"]))
        this.account = parser.asMap(modified["account"])
        input?.safeSet(tag, parser.asMap(modified["trade"]))

        this.input = input
    }

    private fun calculateClosePosition(subaccountNumber: Int?) {
        calculateTrade("closePosition", TradeCalculation.closePosition, subaccountNumber)
    }

    private fun calculateTransfer(subaccountNumber: Int?) {
        val input = this.input?.mutable()
        val transfer = parser.asMap(input?.get("transfer"))
        val calculator = TransferInputCalculator(parser)
        val params = iMutableMapOf<String, Any>()
        params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
        params.safeSet("user", user)
        params.safeSet("transfer", transfer)
        params.safeSet("wallet", wallet)

        val modified = calculator.calculate(params, subaccountNumber)
        this.setMarkets(parser.asMap(modified["markets"]))
        this.wallet = parser.asMap(modified["wallet"])
        input?.safeSet("transfer", parser.asMap(modified["transfer"]))

        this.input = input
    }

    private fun subaccount(subaccountNumber: Int): IMap<String, Any>? {
        return parser.asMap(parser.value(account, "subaccounts.$subaccountNumber"))
    }

    private fun setSubaccount(subaccount: IMap<String, Any>?, subaccountNumber: Int) {
        val modifiedAccount = account?.mutable() ?: iMutableMapOf()
        modifiedAccount.safeSet("subaccounts.$subaccountNumber", subaccount)
        this.account = modifiedAccount
    }

    private fun subaccountList(subaccountNumber: Int, name: String): IList<Any>? {
        return parser.asList(subaccount(subaccountNumber)?.get(name))
    }

    private fun setSubaccountList(list: IList<Any>?, subaccountNumber: Int, name: String) {
        val modifiedSubaccount = subaccount(subaccountNumber)?.mutable() ?: iMutableMapOf()
        modifiedSubaccount.safeSet(name, list)
        setSubaccount(modifiedSubaccount, subaccountNumber)
    }


    private fun subaccountHistoricalPnl(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "historicalPnl")
    }

    private fun setSubaccountHistoricalPnl(historicalPnl: IList<Any>?, subaccountNumber: Int) {
        setSubaccountList(historicalPnl, subaccountNumber, "historicalPnl")
    }

    private fun subaccountFills(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "fills")
    }

    private fun setSubaccountFills(fills: IList<Any>?, subaccountNumber: Int) {
        setSubaccountList(fills, subaccountNumber, "fills")
    }

    private fun subaccountTransfers(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "transfers")
    }

    private fun setSubaccountTransfers(transfers: IList<Any>?, subaccountNumber: Int) {
        setSubaccountList(transfers, subaccountNumber, "transfers")
    }

    private fun subaccountFundingPayments(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "fundingPayments")
    }

    private fun setSubaccountFundingPayments(fundingPayments: IList<Any>?, subaccountNumber: Int) {
        setSubaccountList(fundingPayments, subaccountNumber, "fundingPayments")
    }

    private fun allSubaccountNumbers(): IList<Int> {
        val subaccountsData = parser.asMap(account?.get("subaccounts"))
        return if (subaccountsData != null) {
            parser.asMap(subaccountsData)?.keys?.mapNotNull { key ->
                parser.asInt(key)
            }?.toIList() ?: iListOf<Int>()
        } else iListOf<Int>()
    }

    private fun maxSubaccountNumber(): Int? {
        var maxSubaccountNumber: Int? = null
        val subaccountsData = parser.asMap(account?.get("subaccounts"))
        if (subaccountsData != null) {
            for ((key, value) in subaccountsData) {
                val subaccountNumber = parser.asInt(key)
                if (subaccountNumber != null) {
                    if (maxSubaccountNumber != null) {
                        maxSubaccountNumber = max(maxSubaccountNumber, subaccountNumber)
                    } else {
                        maxSubaccountNumber = subaccountNumber
                    }
                }
            }
        }
        return maxSubaccountNumber
    }

    private fun subaccountNumbersWithPlaceholders(maxSubaccountNumber: Int?): IList<Int> {
        return if (maxSubaccountNumber != null) {
            val subaccountNumbers = iMutableListOf<Int>()
            for (i in 0 until min(maxSubaccountNumber, this.maxSubaccountNumber) + 1) {
                subaccountNumbers.add(i)
            }
            subaccountNumbers
        } else iListOf(0)
    }

    private fun recalculateStates(changes: StateChanges) {
        val subaccountNumbers = changes.subaccountNumbers ?: allSubaccountNumbers()
        if (changes.changes.contains(Changes.subaccount)) {
            val periods = if (this.input != null) iSetOf(
                CalculationPeriod.current,
                CalculationPeriod.post,
                CalculationPeriod.settled
            ) else iSetOf(CalculationPeriod.current)

            this.marketsSummary?.let { marketsSummary ->
                parser.asMap(marketsSummary["markets"])?.let { markets ->
                    val modifiedAccount = accountCalculator.calculate(
                        account,
                        subaccountNumbers,
                        null,
                        markets,
                        priceOverwrite(markets),
                        periods,
                        version
                    )
                    this.account = modifiedAccount
                }
            }
        }
        if (changes.changes.contains(Changes.input)) {
            val modified = this.input?.mutable() ?: return
            when (parser.asString(modified["current"])) {
                "trade" -> {
                    when (parser.asString(parser.value(modified, "trade.size.input"))) {
                        "size.size", "size.usdcSize" -> {
                            val subaccountNumber = changes.subaccountNumbers?.firstOrNull()
                            val marketId = parser.asString(parser.value(modified, "trade.marketId"))
                            if (subaccountNumber != null && marketId != null) {
                                val leverage =
                                    parser.asDouble(
                                        parser.value(
                                            this.account,
                                            "subaccounts.$subaccountNumber.openPositions.$marketId.leverage.postOrder"
                                        )
                                    )
                                modified.safeSet("trade.size.leverage", leverage)
                            } else {
                                modified.safeSet("trade.size.leverage", null)
                            }
                        }

                        else -> {
                        }
                    }
                }

                "closePosition", "transfer" -> {

                }
            }
            modified.safeSet("receiptLines", calculateReceipt(modified))
            this.input = modified
        }
    }

    private fun calculateReceipt(input: IMap<String, Any>): IList<String>? {
        return when (parser.asString(input["current"])) {
            "trade" -> {
                val trade = parser.asMap(input["trade"]) ?: return null
                val type = parser.asString(trade["type"]) ?: return null
                return when (type) {
                    "MARKET", "STOP_MARKET", "TAKE_PROFIT_MARKET", "TRAILING_STOP" -> {
                        iListOf(
                            ReceiptLine.buyingPower.rawValue,
                            ReceiptLine.marginUsage.rawValue,
                            ReceiptLine.expectedPrice.rawValue,
                            ReceiptLine.fee.rawValue
                        )
                    }

                    else -> {
                        iListOf(
                            ReceiptLine.buyingPower.rawValue,
                            ReceiptLine.marginUsage.rawValue,
                            ReceiptLine.fee.rawValue
                        )
                    }
                }
            }

            "closePosition" -> {
                iListOf(
                    ReceiptLine.buyingPower.rawValue,
                    ReceiptLine.marginUsage.rawValue,
                    ReceiptLine.expectedPrice.rawValue,
                    ReceiptLine.fee.rawValue
                )
            }

            "transfer" -> {
                val transfer = parser.asMap(input["transfer"]) ?: return null
                val type = parser.asString(transfer["type"]) ?: return null
                return when (type) {
                    "DEPOSIT", "WITHDRAWAL" -> {
                        iListOf(
                            ReceiptLine.equity.rawValue,
                            ReceiptLine.buyingPower.rawValue,
                            ReceiptLine.exchangeRate.rawValue,
                            ReceiptLine.exchangeReceived.rawValue,
                            ReceiptLine.bridgeFee.rawValue,
                            ReceiptLine.fee.rawValue,
                            ReceiptLine.slippage.rawValue
                        )
                    }
                    "TRANSFER_OUT" -> {
                        iListOf(
                            ReceiptLine.equity.rawValue,
                            ReceiptLine.marginUsage.rawValue,
                            ReceiptLine.fee.rawValue
                        )
                    }
                    else -> {
                        iListOf()
                    }
                }
            }

            else -> null
        }
    }

    private fun update(state: PerpetualState?, changes: StateChanges): PerpetualState {
        var marketsSummary = state?.marketsSummary
        var orderbooks = state?.orderbooks
        var trades = state?.trades
        var candles = state?.candles
        var historicalFundings = state?.historicalFundings
        var assets = state?.assets?.toIMutableMap()
        var wallet = state?.wallet
        var account = state?.account
        var historicalPnl = state?.historicalPnl
        var fills = state?.fills
        var transfers = state?.transfers
        var fundingPayments = state?.fundingPayments
        var configs = state?.configs
        var input = state?.input
        var transferStatuses = state?.transferStatuses?.toIMutableMap()

        if (changes.changes.contains(Changes.markets)) {
            parser.asMap(data?.get("markets"))?.let {
                marketsSummary = PerpetualMarketSummary.apply(marketsSummary, parser, it, changes)
            }
        }
        if (changes.changes.contains(Changes.orderbook)) {
            val markets = changes.markets
            if (markets != null) {
                val modified = orderbooks?.toIMutableMap() ?: iMutableMapOf()
                for (marketId in markets) {
                    val data =
                        parser.asMap(parser.value(data, "markets.markets.$marketId.orderbook"))
                    val existing = orderbooks?.get(marketId)
                    val orderbook = MarketOrderbook.create(existing, parser, data)
                    modified.typedSafeSet(marketId, orderbook)
                }
                orderbooks = modified
            }
        }
        if (changes.changes.contains(Changes.trades)) {
            val markets = changes.markets
            if (markets != null) {
                val modified = trades?.toIMutableMap() ?: iMutableMapOf()
                for (marketId in markets) {
                    val data = parser.asList(
                        parser.value(
                            data,
                            "markets.markets.$marketId.trades"
                        )
                    ) as? IList<IMap<String, Any>>
                    val existing = trades?.get(marketId)
                    val trades = MarketTrade.create(existing, parser, data)
                    modified.typedSafeSet(marketId, trades)
                }
                trades = modified
            }
        }
        if (changes.changes.contains(Changes.historicalFundings)) {
            val markets = changes.markets
            if (markets != null) {
                val modified = historicalFundings?.toIMutableMap() ?: iMutableMapOf()
                for (marketId in markets) {
                    val data = parser.asList(
                        parser.value(
                            data,
                            "markets.markets.$marketId.historicalFunding"
                        )
                    ) as? IList<IMap<String, Any>>
                    val existing = historicalFundings?.get(marketId)
                    val historicalFunding = MarketHistoricalFunding.create(existing, parser, data)
                    modified.typedSafeSet(marketId, historicalFunding)
                }
                historicalFundings = modified
            }
        }
        if (changes.changes.contains(Changes.candles)) {
            val markets = changes.markets
            if (markets != null) {
                val modified = candles?.toIMutableMap() ?: iMutableMapOf()
                for (marketId in markets) {
                    val data = parser.asMap(parser.value(data, "markets.markets.$marketId.candles"))
                    val existing = candles?.get(marketId)
                    val candles = MarketCandles.create(existing, parser, data)
                    modified.typedSafeSet(marketId, candles)
                }
                candles = modified
            }
        }
        if (changes.changes.contains(Changes.assets)) {
            this.assets?.let {
                assets = assets ?: iMutableMapOf<String, Asset>()
                for ((key, data) in it) {
                    parser.asMap(data)?.let {
                        Asset.create(assets?.get(key), parser, it)?.let {
                            assets!![key] = it
                        }
                    }
                }
            }
        }
        if (changes.changes.contains(Changes.configs)) {
            this.configs?.let {
                configs = Configs.create(configs, parser, it)
            }
        }
        if (changes.changes.contains(Changes.wallet)) {
            this.wallet?.let {
                wallet = Wallet.create(wallet, parser, it)
            }
        }
        val subaccountNumbers = changes.subaccountNumbers ?: allSubaccountNumbers()
        val accountData = this.account
        if (accountData != null) {
            if (changes.changes.contains(Changes.subaccount)) {
                account = if (account == null) {
                    Account.create(null, parser, accountData)
                } else {
                    val subaccounts = account.subaccounts?.toIMutableMap() ?: iMutableMapOf()
                    for (subaccountNumber in subaccountNumbers) {
                        val subaccount = Subaccount.create(
                            account.subaccounts?.get("$subaccountNumber"),
                            parser,
                            subaccount(subaccountNumber)
                        )
                        subaccounts.typedSafeSet("$subaccountNumber", subaccount)
                    }
                    Account(account.balances, subaccounts)
                }
            }
            if (changes.changes.contains(Changes.accountBalances)) {
                account = Account.create(account, parser, accountData)
            }
        } else {
            account = null
            fills = null
            historicalPnl = null
            transfers = null
            fundingPayments = null
        }
        if (subaccountNumbers.size == 1) {
            val subaccountNumber = subaccountNumbers.first()
            val subaccountText = "$subaccountNumber"
            val subaccount =
                parser.asMap(parser.value(this.account, "subaccounts.$subaccountNumber"))

            if (changes.changes.contains(Changes.historicalPnl)) {
                val now = ServerTime.now()
                val start = now - historicalPnlDays.days
                val modifiedHistoricalPnl = historicalPnl?.toIMutableMap() ?: iMutableMapOf()
                var subaccountHistoricalPnl = historicalPnl?.get(subaccountText)
                val subaccountHistoricalPnlData =
                    (subaccountHistoricalPnl(subaccountNumber) as? IList<IMap<String, Any>>)?.mutable()
                        ?: iMutableListOf()
                val equity = parser.asDouble(parser.value(subaccount, "equity.current"))
                if (subaccountHistoricalPnl?.size == 1) {
                    // Check if the PNL was generated from equity
                    val first = subaccountHistoricalPnl.firstOrNull()
                    if (first === dummySubaccountPNLs[subaccountText]) {
                        subaccountHistoricalPnl = null
                    }
                }
                subaccountHistoricalPnl = SubaccountHistoricalPNL.create(
                    subaccountHistoricalPnl,
                    parser,
                    subaccountHistoricalPnlData,
                    start
                )
                modifiedHistoricalPnl.typedSafeSet(subaccountText, subaccountHistoricalPnl)
                historicalPnl = modifiedHistoricalPnl
            }
            if (changes.changes.contains(Changes.fills)) {
                val modifiedFills = fills?.toIMutableMap() ?: iMutableMapOf()
                var subaccountFills = fills?.get(subaccountText)
                subaccountFills = SubaccountFill.create(
                    subaccountFills,
                    parser,
                    subaccountFills(subaccountNumber) as? IList<IMap<String, Any>>
                )
                modifiedFills.typedSafeSet(subaccountText, subaccountFills)
                fills = modifiedFills
            }
            if (changes.changes.contains(Changes.transfers)) {
                val modifiedTransfers = transfers?.toIMutableMap() ?: iMutableMapOf()
                var subaccountTransfers = transfers?.get(subaccountText)
                subaccountTransfers = SubaccountTransfer.create(
                    subaccountTransfers,
                    parser,
                    subaccountTransfers(subaccountNumber) as? IList<IMap<String, Any>>
                )
                modifiedTransfers.typedSafeSet(subaccountText, subaccountTransfers)
                transfers = modifiedTransfers
            }
            if (changes.changes.contains(Changes.fundingPayments)) {
                val modifiedFundingPayments = fundingPayments?.toIMutableMap() ?: iMutableMapOf()
                var subaccountFundingPayments = fundingPayments?.get(subaccountText)
                subaccountFundingPayments = SubaccountFundingPayment.create(
                    subaccountFundingPayments,
                    parser,
                    subaccountFundingPayments(subaccountNumber) as? IList<IMap<String, Any>>
                )
                modifiedFundingPayments.typedSafeSet(subaccountText, subaccountFundingPayments)
                fundingPayments = modifiedFundingPayments
            }

            if (changes.changes.contains(Changes.input)) {
                this.input = inputValidator.validate(
                    this.wallet,
                    this.user,
                    subaccount,
                    parser.asMap(this.marketsSummary?.get("markets")),
                    this.input,
                    this.configs
                )
                this.input?.let {
                    input = Input.create(input, parser, it, version)
                }
            }
        }
        if (changes.changes.contains(Changes.transferStatuses)) {
            this.transferStatuses?.let {
                transferStatuses = transferStatuses ?: iMutableMapOf<String, TransferStatus>()
                for ((key, data) in it) {
                    parser.asMap(data)?.let {
                        val status = TransferStatus.create(transferStatuses?.get(key), parser, it)
                        if (status != null) {
                            transferStatuses!![key] = status
                        } else {
                            transferStatuses!!.remove(key)
                        }
                    }
                }
            }
        }
        return PerpetualState(
            assets,
            marketsSummary,
            orderbooks,
            candles,
            trades,
            historicalFundings,
            wallet,
            account,
            historicalPnl,
            fills,
            transfers,
            fundingPayments,
            configs,
            input,
            subaccountNumbersWithPlaceholders(maxSubaccountNumber()),
            transferStatuses,
        )
    }

    private fun calculateAccount(subaccountNumbers: IList<Int>, period: CalculationPeriod) {
        this.account?.let {
            this.marketsSummary?.let { marketsSummary ->
                parser.asMap(marketsSummary["markets"])?.let { markets ->
                    this.account = accountCalculator.calculate(
                        it,
                        subaccountNumbers,
                        null,
                        markets,
                        priceOverwrite(markets),
                        iSetOf(period),
                        version
                    )
                }
            }
        }
    }

    private fun priceOverwrite(markets: IMap<String, Any>): IMap<String, Any>? {
        if (parser.asString(input?.get("current")) == "trade") {
            val trade = parser.asMap(input?.get("trade"))
            when (parser.asString(trade?.get("type"))) {
                "LIMIT", "STOP_LIMIT", "TAKE_PROFIT", "TRAILING_STOP", "STOP_MARKET", "TAKE_PROFIT_MARKET" -> {
                    val price = parser.asDouble(parser.value(trade, "summary.price"))
                    val marketId = parser.asString(trade?.get("marketId"))
                    if (marketId != null && price != null) {
                        val market = parser.asMap(markets[marketId])
                        val oraclePrice =
                            parser.asDouble(market?.get("oraclePrice"))
                        if (oraclePrice != null) {
                            val side = parser.asString(trade?.get("side"))
                            if ((side == "BUY" && price < oraclePrice) || (side == "SELL" && price > oraclePrice)) {
                                return iMapOf(marketId to price)
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun setMarkets(markets: IMap<String, Any>?) {

    }

    fun setHistoricalPnlDays(days: Int, subaccountNumber: Int): StateResponse {
        return if (historicalPnlDays != days) {
            historicalPnlDays = days
            val now = ServerTime.now()
            val startTime = now - days.days
            val historicalPnls = state?.historicalPnl?.get("$subaccountNumber") ?: return noChange()
            val first = historicalPnls.firstOrNull() ?: return noChange()
            val changes = StateChanges(iListOf(Changes.historicalPnl))
            state = update(state, changes)
            StateResponse(state, changes)
        } else noChange()
    }

    fun clearInput(subaccountNumber: Int): StateResponse {
        val input = input
        return if (input != null) {
            val current = parser.asString(input["current"])
            val modified = when (current) {
                "trade", "closePosition" -> clearTradeInput(input)
                "transfer" -> clearTransferInput(input)
                else -> null
            }
            if (modified != null) {
                this.input = modified

                val changes = StateChanges(
                    iListOf(Changes.input, Changes.subaccount),
                    null,
                    iListOf(subaccountNumber)
                )
                state = update(state, changes)
                StateResponse(state, changes)
            } else noChange()
        } else noChange()
    }

    fun clearTradeInput(input: IMap<String, Any>): IMap<String, Any> {
        val trade = parser.asMap(input["trade"])?.toIMutableMap()
        trade?.safeSet("size", null)
        trade?.safeSet("price", null)
        val modifiedInput = input.toIMutableMap()
        modifiedInput.safeSet("trade", trade)
        return modifiedInput
    }

    fun clearTransferInput(input: IMap<String, Any>): IMap<String, Any> {
        val trade = parser.asMap(input["trade"])?.toIMutableMap()
        trade?.safeSet("size", null)
        trade?.safeSet("price", null)
        val modifiedInput = input.toIMutableMap()
        modifiedInput.safeSet("trade", trade)
        return modifiedInput
    }

    fun received(subaccountNumber: Int, height: Int?): StateResponse {
        val wallet = wallet
        if (wallet != null) {
            val (modifiedWallet, updated) = walletProcessor.received(
                wallet,
                subaccountNumber,
                height
            )
            if (updated) {
                this.wallet = wallet

                val changes = StateChanges(iListOf(Changes.subaccount))
                state = update(state, changes)
                return StateResponse(state, changes)
            }
        }
        return noChange()
    }

    fun parseOnChainEquityTiers(payload: String): StateResponse {
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

    fun parseOnChainFeeTiers(payload: String): StateResponse {
        var changes: StateChanges? = null
        var error: ParsingError? = null
        try {
            changes = onChainFeeTiers(payload)
        } catch (e: ParsingException) {
            error = e.toParsingError()
        }
        if (changes != null) {
            update(changes)
        }

        val errors = if (error != null) iListOf(error) else null
        return StateResponse(state, changes, errors)
    }

    fun parseOnChainUserFeeTier(payload: String): StateResponse {
        var changes: StateChanges? = null
        var error: ParsingError? = null
        try {
            changes = onChainUserFeeTier(payload)
        } catch (e: ParsingException) {
            error = e.toParsingError()
        }
        if (changes != null) {
            update(changes)
        }

        val errors = if (error != null) iListOf(error) else null
        return StateResponse(state, changes, errors)
    }

    fun parseOnChainUserStats(payload: String): StateResponse {
        var changes: StateChanges? = null
        var error: ParsingError? = null
        try {
            changes = onChainUserStats(payload)
        } catch (e: ParsingException) {
            error = e.toParsingError()
        }
        if (changes != null) {
            update(changes)
        }

        val errors = if (error != null) iListOf(error) else null
        return StateResponse(state, changes, errors)
    }
}
