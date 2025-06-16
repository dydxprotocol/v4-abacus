package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.calculator.AccountCalculator
import exchange.dydx.abacus.calculator.AdjustIsolatedMarginInputCalculator
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarketCalculator
import exchange.dydx.abacus.calculator.ReceiptCalculator
import exchange.dydx.abacus.calculator.TransferInputCalculator
import exchange.dydx.abacus.calculator.TriggerOrdersInputCalculator
import exchange.dydx.abacus.calculator.tradeinput.TradeCalculation
import exchange.dydx.abacus.calculator.tradeinput.TradeInputCalculator
import exchange.dydx.abacus.functional.vault.VaultAccountCalculator
import exchange.dydx.abacus.functional.vault.VaultCalculator
import exchange.dydx.abacus.output.Configs
import exchange.dydx.abacus.output.LaunchIncentive
import exchange.dydx.abacus.output.LaunchIncentiveSeasons
import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.output.MarketCandles
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.TransferStatus
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.output.Wallet
import exchange.dydx.abacus.output.WithdrawalCapacity
import exchange.dydx.abacus.output.account.Account
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.input.Input
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.processor.assets.AssetsProcessor
import exchange.dydx.abacus.processor.configs.ConfigsProcessor
import exchange.dydx.abacus.processor.configs.RewardsParamsProcessor
import exchange.dydx.abacus.processor.input.ClosePositionInputProcessor
import exchange.dydx.abacus.processor.input.TradeInputProcessor
import exchange.dydx.abacus.processor.launchIncentive.LaunchIncentiveProcessor
import exchange.dydx.abacus.processor.markets.MarketsSummaryProcessor
import exchange.dydx.abacus.processor.router.skip.SkipProcessor
import exchange.dydx.abacus.processor.vault.VaultProcessor
import exchange.dydx.abacus.processor.wallet.WalletProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.abacus.protocols.asTypedStringMap
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.InternalAccountState
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.state.StateChanges.Companion.noChange
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.EnvironmentFeatureFlags
import exchange.dydx.abacus.state.manager.TokenInfo
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.TradeValidationTracker
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.typedSafeSet
import exchange.dydx.abacus.validator.InputValidator
import indexer.models.configs.ConfigsAssetMetadata
import indexer.models.configs.ConfigsAssetMetadataPrice
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIList
import kollections.toIMap
import kollections.toIMutableMap
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.days

@Suppress("UNCHECKED_CAST")
@JsExport
open class TradingStateMachine(
    private val environment: V4Environment?,
    private val localizer: LocalizerProtocol?,
    private val formatter: Formatter?,
    private val maxSubaccountNumber: Int,
    private val useParentSubaccount: Boolean,
    val skipGoFast: Boolean = false,
    private val trackingProtocol: TrackingProtocol?,
) {
    internal var internalState: InternalState = InternalState()

    internal val parser: ParserProtocol = Parser()
    internal val marketsProcessor = MarketsSummaryProcessor(
        parser = parser,
        localizer = localizer,
    )
    internal val assetsProcessor = run {
        val processor = AssetsProcessor(
            parser = parser,
            localizer = localizer,
        )
        processor.environment = environment
        processor
    }
    internal val walletProcessor = WalletProcessor(parser, localizer)
    internal val vaultProcessor = VaultProcessor(parser, environment, localizer)
    internal val configsProcessor = ConfigsProcessor(parser, localizer)
    internal val routerProcessor = SkipProcessor(
        parser = parser,
        internalState = internalState.input.transfer,
    )
    internal val rewardsProcessor = RewardsParamsProcessor(parser)
    internal val launchIncentiveProcessor = LaunchIncentiveProcessor(parser)
    internal val tradeInputProcessor = TradeInputProcessor(parser)
    internal val closePositionInputProcessor = ClosePositionInputProcessor(parser)

    internal val marketsCalculator = MarketCalculator(parser)
    internal val accountCalculator = AccountCalculator(parser, useParentSubaccount)

    private val receiptCalculator = ReceiptCalculator()

    private val tradeValidationTracker = TradeValidationTracker(trackingProtocol)

    internal val inputValidator = InputValidator(localizer, formatter, parser, tradeValidationTracker)

    internal var data: Map<String, Any>? = null

    private var dummySubaccountPNLs = mutableMapOf<String, SubaccountHistoricalPNL>();

    internal val tokensInfo: Map<String, TokenInfo>
        get() = environment?.tokens!!

    internal val featureFlags: EnvironmentFeatureFlags
        get() = environment?.featureFlags!!

    internal var currentBlockAndHeight: BlockAndTime? = null

    internal var groupingMultiplier: Int
        get() = marketsProcessor.groupingMultiplier
        set(value) {
            marketsProcessor.groupingMultiplier = value
        }

    internal var marketsSummary: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("markets"))
        }
        set(value) {
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("markets", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var historicalPnlDays: Int = 1

    internal var assets: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("assets"))
        }
        set(value) {
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("assets", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var wallet: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("wallet"))
        }
        set(value) {
            val oldAddress = parser.asString(parser.value(wallet, "walletAddress"))
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("wallet", value)
            this.data = if (modified.isEmpty()) null else modified
            val address = parser.asString(parser.value(wallet, "walletAddress"))
            if (address != oldAddress) {
                dummySubaccountPNLs = mutableMapOf()
            }
        }

    internal var account: Map<String, Any>?
        get() {
            return parser.asNativeMap(wallet?.get("account"))
        }
        set(value) {
            val modified = wallet?.mutable() ?: mutableMapOf()
            modified.safeSet("account", value)
            this.wallet = if (modified.size != 0) modified else null
        }

    internal var user: Map<String, Any>?
        get() {
            return parser.asNativeMap(wallet?.get("user"))
        }
        set(value) {
            val modified = wallet?.mutable() ?: mutableMapOf()
            modified.safeSet("user", value)
            this.wallet = if (modified.size != 0) modified else null
        }

    internal var configs: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("configs"))
        }
        set(value) {
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("configs", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var input: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("input"))
        }
        set(value) {
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("input", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var transferStatuses: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("transferStatuses"))
        }
        set(value) {
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("transferStatuses", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var trackStatuses: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("trackStatuses"))
        }
        set(value) {
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("trackStatuses", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var rewardsParams: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("rewardsParams"))
        }
        set(value) {
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("rewardsParams", value)
            this.data = if (modified.size != 0) modified else null
        }

    internal var launchIncentive: Map<String, Any>?
        get() {
            return parser.asNativeMap(data?.get("launchIncentive"))
        }
        set(value) {
            val modified = data?.mutable() ?: mutableMapOf()
            modified.safeSet("launchIncentive", value)
            this.data = if (modified.size != 0) modified else null
        }

    var state: PerpetualState? = null

    private fun noChange(): StateResponse {
        return StateResponse(state, null)
    }

    internal fun resetWallet(accountAddress: String?): StateResponse {
        val wallet = if (accountAddress != null) iMapOf("walletAddress" to accountAddress) else null
        this.wallet = wallet
        if (accountAddress != internalState.wallet.walletAddress) {
            internalState.wallet.walletAddress = accountAddress
            internalState.wallet.account = InternalAccountState()
            internalState.wallet.user = null
        }
        if (accountAddress == null) {
            this.account = null
        }
        val changes = StateChanges(
            iListOf(
                Changes.wallet,
                Changes.subaccount,
                Changes.tradingRewards,
                Changes.historicalPnl,
                Changes.fills,
                Changes.transfers,
                Changes.fundingPayments,
            ),
        )
        updateStateChanges(changes)
        walletProcessor.accountAddress = accountAddress
        return StateResponse(state, changes, null)
    }

    internal fun configurations(
        infoPayload: String,
        pricesPayload: String,
        subaccountNumber: Int?,
    ): StateChanges {
        val json = parser.decodeJsonObject(infoPayload)
        val infoPayload = parser.asTypedStringMap<ConfigsAssetMetadata>(json)
        val pricesJson = parser.decodeJsonObject(pricesPayload)
        val pricesPayload = parser.asTypedStringMap<ConfigsAssetMetadataPrice>(pricesJson)
        if (infoPayload == null) {
            Logger.e { "Error parsing asset payload" }
            return noChange
        }
        return processMarketsConfigurationsWithMetadataService(
            infoPayload = infoPayload,
            pricesPayload = pricesPayload,
            subaccountNumber = subaccountNumber,
        )
    }

    internal fun updateStateChanges(changes: StateChanges): StateChanges {
        if (changes.changes.contains(Changes.input)) {
            val subaccountNumber = changes.subaccountNumbers?.firstOrNull()
            val subaccount = internalState.wallet.account.subaccounts[subaccountNumber]
            // Only run validation if the subaccount is null since updateState will run validation for each subaccount
            if (subaccount == null) {
                inputValidator.validate(
                    internalState = internalState,
                    subaccountNumber = subaccountNumber,
                    currentBlockAndHeight = currentBlockAndHeight,
                    environment = environment,
                )
            }

            if (subaccountNumber != null) {
                when (internalState.input.currentType) {
                    InputType.TRADE -> {
                        calculateTrade(subaccountNumber)
                    }

                    InputType.TRANSFER -> {
                        calculateTransfer(subaccountNumber)
                    }

                    InputType.TRIGGER_ORDERS -> {
                        calculateTriggerOrders(subaccountNumber)
                    }

                    InputType.ADJUST_ISOLATED_MARGIN -> {
                        calculateAdjustIsolatedMargin(subaccountNumber)
                    }

                    InputType.CLOSE_POSITION -> {
                        calculateClosePosition(subaccountNumber)
                    }

                    else -> {}
                }
            }
        }
        recalculateStates(changes)

        val wallet = state?.wallet
        val input = state?.input

        state = updateState(state, changes, tokensInfo, localizer)

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
                Changes.tradingRewards,
                Changes.historicalPnl,
                Changes.fills,
                Changes.transfers,
                Changes.fundingPayments,
                Changes.trades,
                Changes.configs,
                Changes.transferStatuses,
                Changes.trackStatuses,
                Changes.orderbook,
                Changes.launchIncentive,
                Changes.vault
                -> true

                Changes.wallet -> state?.wallet != wallet
                Changes.input -> state?.input != input

                // Restriction is handled separately and shouldn't have gone through here
                Changes.restriction -> {
                    Logger.d { "Restriction is handled separately and shouldn't have gone through here" }
                    false
                }

                Changes.compliance -> {
                    Logger.d { "Compliance is handled separately and shouldn't have gone through here" }
                    false
                }
            }
            if (didChange) {
                realChanges.add(change)
            }
        }
        return StateChanges(realChanges, changes.markets, changes.subaccountNumbers)
    }

    private fun calculateTrade(subaccountNumber: Int) {
        calculateTrade("trade", TradeCalculation.trade, subaccountNumber)
    }

    private fun calculateTrade(tag: String, calculation: TradeCalculation, subaccountNumber: Int) {
        val calculator = TradeInputCalculator(parser, calculation)
        calculator.calculate(
            trade = when (calculation) {
                TradeCalculation.closePosition -> internalState.input.closePosition
                TradeCalculation.trade -> internalState.input.trade
            },
            wallet = internalState.wallet,
            marketSummary = internalState.marketsSummary,
            rewardsParams = internalState.rewardsParams,
            configs = internalState.configs,
            subaccountNumber = subaccountNumber,
            input = when (calculation) {
                TradeCalculation.closePosition -> internalState.input.closePosition.size?.input
                TradeCalculation.trade -> internalState.input.trade.size?.input
            },
        )
    }

    private fun calculateClosePosition(subaccountNumber: Int) {
        calculateTrade("closePosition", TradeCalculation.closePosition, subaccountNumber)
    }

    private fun calculateTransfer(subaccountNumber: Int?) {
        val calculator = TransferInputCalculator(parser = parser)
        calculator.calculate(
            transfer = internalState.input.transfer,
            wallet = internalState.wallet,
            subaccountNumber = subaccountNumber,
        )
    }

    private fun calculateTriggerOrders(subaccountNumber: Int) {
        val calculator = TriggerOrdersInputCalculator()
        calculator.calculate(
            triggerOrders = internalState.input.triggerOrders,
            account = internalState.wallet.account,
            subaccountNumber = subaccountNumber,
        )
    }

    private fun calculateAdjustIsolatedMargin(subaccountNumber: Int?) {
        val calculator = AdjustIsolatedMarginInputCalculator(parser)
        internalState.input.adjustIsolatedMargin = calculator.calculate(
            adjustIsolatedMargin = internalState.input.adjustIsolatedMargin,
            walletState = internalState.wallet,
            markets = internalState.marketsSummary.markets,
            parentSubaccountNumber = subaccountNumber,
        )
    }

    private fun subaccount(subaccountNumber: Int): Map<String, Any>? {
        return parser.asNativeMap(parser.value(account, "subaccounts.$subaccountNumber"))
    }

    private fun subaccountList(subaccountNumber: Int, name: String): IList<Any>? {
        return parser.asList(subaccount(subaccountNumber)?.get(name))
    }

    private fun subaccountFundingPayments(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "fundingPayments")
    }

    private fun allSubaccountNumbers(): IList<Int> {
        return internalState.wallet.account.subaccounts.keys.toIList()
    }

    private fun maxSubaccountNumber(): Int? {
        var maxSubaccountNumber: Int? = null
        val subaccountsData = parser.asNativeMap(account?.get("subaccounts"))
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
        } else {
            iListOf(0)
        }
    }

    private fun recalculateStates(changes: StateChanges) {
        val subaccountNumbers = changes.subaccountNumbers ?: allSubaccountNumbers()
        if (changes.changes.contains(Changes.subaccount)) {
            val periods = if (internalState.input.currentType != null) {
                setOf(
                    CalculationPeriod.current,
                    CalculationPeriod.post,
                    CalculationPeriod.settled,
                )
            } else {
                setOf(CalculationPeriod.current)
            }
            internalState.wallet.account = accountCalculator.calculate(
                account = internalState.wallet.account,
                subaccountNumbers = subaccountNumbers,
                marketsSummary = internalState.marketsSummary,
                periods = periods,
                price = null, // priceOverwrite(markets),
                configs = null, // This is used to get the IMF.. with "null" the default value 0.02 will be used
            )
        }

        if (internalState.wallet.account.groupedSubaccounts.isNotEmpty()) {
            if (changes.changes.contains(Changes.fills)) {
                internalState.wallet.account = mergeFills(
                    account = internalState.wallet.account,
                    subaccountNumbers = subaccountNumbers,
                )
            }
            if (changes.changes.contains(Changes.transfers) || changes.changes.contains(Changes.fundingPayments)) {
                internalState.wallet.account = mergeTransfers(
                    account = internalState.wallet.account,
                    subaccountNumbers = subaccountNumbers,
                )
            }
        }

        if (changes.changes.contains(Changes.input)) {
            // finalize the trade input leverage
            if (internalState.input.currentType == InputType.TRADE) {
                val trade = internalState.input.trade
                val account = internalState.wallet.account
                val sizeInput = TradeInputField.invoke(trade.size?.input)
                if (sizeInput == TradeInputField.size || sizeInput == TradeInputField.usdcSize || sizeInput == TradeInputField.balancePercent) {
                    val subaccountNumber = changes.subaccountNumbers?.firstOrNull()
                    val marketId = trade.marketId
                    if (subaccountNumber != null && marketId != null) {
                        val position =
                            account.subaccounts[subaccountNumber]?.openPositions?.get(marketId)
                        val postOrderLeverage =
                            position?.calculated?.get(CalculationPeriod.post)?.leverage
                        trade.size = trade.size?.copy(leverage = postOrderLeverage)
                    } else {
                        trade.size = trade.size?.copy(leverage = null)
                    }
                }
            }
            // calculate the receipt lines
            receiptCalculator.calculate(
                input = internalState.input,
            )
        }
    }

    private fun updateState(
        state: PerpetualState?,
        changes: StateChanges,
        tokensInfo: Map<String, TokenInfo>,
        localizer: LocalizerProtocol?,
    ): PerpetualState {
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
        var trackStatuses = state?.trackStatuses?.toIMutableMap()
        val restriction = state?.restriction
        var launchIncentive = state?.launchIncentive
        val geo = state?.compliance
        var vault = state?.vault

        if (changes.changes.contains(Changes.markets)) {
            marketsSummary =
                PerpetualMarketSummary.apply(
                    internalState = internalState,
                )
        }
        if (changes.changes.contains(Changes.orderbook)) {
            val markets = changes.markets
            orderbooks = if (markets != null) {
                val modified = orderbooks?.toIMutableMap() ?: iMutableMapOf()
                for (marketId in markets) {
                    val orderbook =
                        internalState.marketsSummary.markets[marketId]?.groupedOrderbook

                    modified.typedSafeSet(marketId, orderbook)
                }
                modified
            } else {
                null
            }
        }
        if (changes.changes.contains(Changes.trades)) {
            val markets = changes.markets
            if (markets != null) {
                val modified = trades?.toIMutableMap() ?: mutableMapOf()
                for (marketId in markets) {
                    val trades = internalState.marketsSummary.markets[marketId]?.trades
                    modified.typedSafeSet(marketId, trades?.toIList())
                }
                trades = modified
            } else {
                trades = null
            }
        }
        if (changes.changes.contains(Changes.historicalFundings)) {
            val markets = changes.markets
            if (markets != null) {
                val modified = historicalFundings?.toIMutableMap() ?: mutableMapOf()
                for (marketId in markets) {
                    val historicalFundings =
                        internalState.marketsSummary.markets[marketId]?.historicalFundings
                    modified.typedSafeSet(marketId, historicalFundings?.toIList())
                }
                historicalFundings = modified
            } else {
                historicalFundings = null
            }
        }
        if (changes.changes.contains(Changes.candles)) {
            val markets = changes.markets
            if (markets != null) {
                val modified = candles?.toIMutableMap() ?: mutableMapOf()
                for (marketId in markets) {
                    val candles = internalState.marketsSummary.markets[marketId]?.candles
                    val marketCandles: MutableMap<String, IList<MarketCandle>> = mutableMapOf()
                    for ((key, value) in candles ?: emptyMap()) {
                        marketCandles[key] = value.toIList()
                    }
                    modified.typedSafeSet(marketId, MarketCandles(candles = marketCandles.toIMap()))
                }
                candles = modified
            } else {
                candles = null
            }
        }
        if (changes.changes.contains(Changes.assets)) {
            assets = internalState.assets.toIMutableMap()
            if (assets.isEmpty()) {
                assets = null
            }
        }
        if (changes.changes.contains(Changes.configs)) {
            configs = Configs(
                network = null,
                feeTiers = internalState.configs.feeTiers?.toIList(),
                feeDiscounts = null,
                equityTiers = internalState.configs.equityTiers,
                withdrawalGating = internalState.configs.withdrawalGating,
                withdrawalCapacity = WithdrawalCapacity(
                    capacity = internalState.configs.withdrawalCapacity?.capacity,
                ),
                rpcMap = internalState.configs.rpcMap,
            )
        }
        if (changes.changes.contains(Changes.wallet)) {
            wallet = Wallet.create(internalState.wallet)
        }
        val subaccountNumbers = changes.subaccountNumbers ?: allSubaccountNumbers()

        if (changes.changes.contains(Changes.subaccount)) {
            account = if (account == null) {
                Account.create(
                    existing = null,
                    parser = parser,
                    tokensInfo = tokensInfo,
                    internalState = internalState.wallet.account,
                )
            } else {
                val subaccounts = account.subaccounts?.toIMutableMap() ?: mutableMapOf()
                for (subaccountNumber in subaccountNumbers) {
                    val subaccount = Subaccount.create(
                        existing = account.subaccounts?.get("$subaccountNumber"),
                        internalState = internalState.wallet.account.subaccounts[subaccountNumber],
                    )
                    subaccounts.typedSafeSet("$subaccountNumber", subaccount)
                }
                val groupedSubaccounts =
                    account.groupedSubaccounts?.toIMutableMap() ?: mutableMapOf()
                for (subaccountNumber in subaccountNumbers) {
                    if (subaccountNumber < NUM_PARENT_SUBACCOUNTS) {
                        val subaccount = Subaccount.create(
                            existing = account.groupedSubaccounts?.get("$subaccountNumber"),
                            internalState = internalState.wallet.account.groupedSubaccounts[subaccountNumber],
                        )
                        groupedSubaccounts.typedSafeSet("$subaccountNumber", subaccount)
                    }
                }
                Account(
                    balances = account.balances,
                    stakingBalances = account.stakingBalances,
                    stakingDelegations = account.stakingDelegations,
                    unbondingDelegation = account.unbondingDelegation,
                    stakingRewards = account.stakingRewards,
                    subaccounts = subaccounts,
                    groupedSubaccounts = groupedSubaccounts,
                    tradingRewards = account.tradingRewards,
                    launchIncentivePoints = account.launchIncentivePoints,
                )
            }
        }
        if (changes.changes.contains(Changes.accountBalances) || changes.changes.contains(
                Changes.tradingRewards,
            )
        ) {
            account = Account.create(
                existing = account,
                parser = parser,
                tokensInfo = tokensInfo,
                internalState = internalState.wallet.account,
            )
        }

        if (internalState.wallet.account.subaccounts.isEmpty()) {
            fills = null
            historicalPnl = null
            transfers = null
            fundingPayments = null
        }

        for (subaccountNumber in subaccountNumbers) {
            val subaccountText = "$subaccountNumber"

            if (changes.changes.contains(Changes.historicalPnl)) {
                val now = ServerTime.now()
                val start = now - historicalPnlDays.days
                val modifiedHistoricalPnl = historicalPnl?.toIMutableMap() ?: mutableMapOf()
                var subaccountHistoricalPnl = historicalPnl?.get(subaccountText)
                if (subaccountHistoricalPnl?.size == 1) {
                    // Check if the PNL was generated from equity
                    val first = subaccountHistoricalPnl.firstOrNull()
                    if (first === dummySubaccountPNLs[subaccountText]) {
                        subaccountHistoricalPnl = null
                    }
                }

                subaccountHistoricalPnl =
                    internalState.wallet.account.subaccounts[subaccountNumber]?.historicalPNLs?.toIList()
                        ?.filter {
                            it.createdAtMilliseconds >= start.toEpochMilliseconds()
                        }

                modifiedHistoricalPnl.typedSafeSet(subaccountText, subaccountHistoricalPnl)
                historicalPnl = modifiedHistoricalPnl
            }
            if (changes.changes.contains(Changes.fills)) {
                val modifiedFills = fills?.toIMutableMap() ?: mutableMapOf()
                var subaccountFills = fills?.get(subaccountText)
                val newFills =
                    internalState.wallet.account.subaccounts[subaccountNumber]?.fills?.toIList()
                subaccountFills = SubaccountFill.merge(
                    existing = subaccountFills,
                    new = newFills,
                )
                modifiedFills.typedSafeSet(subaccountText, subaccountFills)
                fills = modifiedFills
            }
            if (changes.changes.contains(Changes.transfers)) {
                val modifiedTransfers = transfers?.toIMutableMap() ?: mutableMapOf()
                var subaccountTransfers =
                    internalState.wallet.account.subaccounts[subaccountNumber]?.transfers?.toIList()
                modifiedTransfers.typedSafeSet(subaccountText, subaccountTransfers)
                transfers = modifiedTransfers
            }
            if (changes.changes.contains(Changes.fundingPayments)) {
                val modifiedFundingPayments = fundingPayments?.toIMutableMap() ?: mutableMapOf()
                var subaccountFundingPayments = fundingPayments?.get(subaccountText)
                val newPayments =
                    internalState.wallet.account.subaccounts[subaccountNumber]?.fundingPayments?.toIList()
                subaccountFundingPayments = SubaccountFundingPayment.merge(
                    existing = subaccountFundingPayments,
                    new = newPayments,
                )
                modifiedFundingPayments.typedSafeSet(subaccountText, subaccountFundingPayments)
                fundingPayments = modifiedFundingPayments
            }

            if (changes.changes.contains(Changes.input)) {
                inputValidator.validate(
                    internalState = internalState,
                    subaccountNumber = subaccountNumber,
                    currentBlockAndHeight = currentBlockAndHeight,
                    environment = environment,
                )

                input = Input.create(
                    existing = input,
                    parser = parser,
                    internalState = internalState,
                )
            }
        }
        if (changes.changes.contains(Changes.transferStatuses)) {
            this.transferStatuses?.let {
                transferStatuses = transferStatuses ?: mutableMapOf<String, TransferStatus>()
                for ((key, data) in it) {
                    parser.asNativeMap(data)?.let {
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
        if (changes.changes.contains(Changes.trackStatuses)) {
            this.trackStatuses?.let {
                trackStatuses = trackStatuses ?: mutableMapOf<String, Boolean>()
                for ((key, data) in it) {
                    val isTracked = parser.asBool(data)
                    if (isTracked != null) {
                        trackStatuses!![key] = isTracked
                    } else {
                        trackStatuses!!.remove(key)
                    }
                }
            }
        }
        if (changes.changes.contains(Changes.launchIncentive)) {
            launchIncentive = LaunchIncentive(
                seasons = LaunchIncentiveSeasons(
                    seasons = internalState.launchIncentive.seasons?.toIList() ?: iListOf(),
                ),
            )
        }
        if (changes.changes.contains(Changes.vault) || changes.changes.contains(Changes.markets)) {
            if (internalState.vault != null) {
                val positions = VaultCalculator.calculateVaultPositionsInternal(
                    vault = internalState.vault,
                    markets = marketsSummary?.markets,
                )
                val accountInfo = internalState.vault?.account
                val transfers = internalState.vault?.transfers
                val account = if (accountInfo != null && transfers != null) {
                    VaultAccountCalculator.calculateUserVaultInfo(
                        vaultInfo = accountInfo,
                        vaultTransfers = transfers,
                    )
                } else {
                    null
                }
                vault = Vault(
                    details = internalState.vault?.details,
                    positions = positions,
                    account = account,
                )
            } else {
                vault = null
            }
        }
        return PerpetualState(
            assets = assets,
            marketsSummary = marketsSummary,
            orderbooks = orderbooks,
            candles = candles,
            trades = trades,
            historicalFundings = historicalFundings,
            wallet = wallet,
            account = account,
            historicalPnl = historicalPnl,
            fills = fills,
            transfers = transfers,
            fundingPayments = fundingPayments,
            configs = configs,
            input = input,
            availableSubaccountNumbers = subaccountNumbersWithPlaceholders(maxSubaccountNumber()),
            transferStatuses = transferStatuses,
            trackStatuses = trackStatuses,
            restriction = restriction,
            launchIncentive = launchIncentive,
            compliance = geo,
            vault = vault,
        )
    }

    fun setHistoricalPnlDays(days: Int, subaccountNumber: Int): StateResponse {
        return if (historicalPnlDays != days) {
            historicalPnlDays = days
            val historicalPnls = state?.historicalPnl?.get("$subaccountNumber") ?: return noChange()
            val first = historicalPnls.firstOrNull() ?: return noChange()
            val changes = StateChanges(iListOf(Changes.historicalPnl))
            state = updateState(state, changes, tokensInfo, localizer)
            StateResponse(state, changes)
        } else {
            noChange()
        }
    }

    fun received(subaccountNumber: Int, height: BlockAndTime?): StateResponse {
        val wallet = wallet
        if (wallet != null) {
            val (_, updated) = walletProcessor.received(
                wallet,
                subaccountNumber,
                height,
            )
            if (updated) {
                this.wallet = wallet

                val changes = StateChanges(iListOf(Changes.subaccount))
                state = updateState(state, changes, tokensInfo, localizer)
                return StateResponse(state, changes)
            }
        }
        return noChange()
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
            updateStateChanges(changes)
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
            updateStateChanges(changes)
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
            updateStateChanges(changes)
        }

        val errors = if (error != null) iListOf(error) else null
        return StateResponse(state, changes, errors)
    }

    fun updateResponse(changes: StateChanges?): StateResponse {
        if (changes != null) {
            updateStateChanges(changes)
        }

        return StateResponse(state, changes, null)
    }
}
