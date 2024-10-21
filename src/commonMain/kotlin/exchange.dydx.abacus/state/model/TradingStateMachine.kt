package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.AccountCalculator
import exchange.dydx.abacus.calculator.AdjustIsolatedMarginInputCalculator
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarketCalculator
import exchange.dydx.abacus.calculator.ReceiptCalculator
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.TradeInputCalculator
import exchange.dydx.abacus.calculator.TransferInputCalculator
import exchange.dydx.abacus.calculator.TriggerOrdersInputCalculator
import exchange.dydx.abacus.calculator.v2.AccountCalculatorV2
import exchange.dydx.abacus.calculator.v2.AdjustIsolatedMarginInputCalculatorV2
import exchange.dydx.abacus.calculator.v2.TransferInputCalculatorV2
import exchange.dydx.abacus.calculator.v2.TriggerOrdersInputCalculatorV2
import exchange.dydx.abacus.calculator.v2.tradeinput.TradeInputCalculatorV2
import exchange.dydx.abacus.functional.vault.VaultAccountCalculator
import exchange.dydx.abacus.functional.vault.VaultCalculator
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.Configs
import exchange.dydx.abacus.output.LaunchIncentive
import exchange.dydx.abacus.output.LaunchIncentiveSeasons
import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.output.MarketCandles
import exchange.dydx.abacus.output.MarketHistoricalFunding
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.MarketTrade
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
import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.input.Input
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ReceiptLine
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
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalState
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
import indexer.models.configs.ConfigsMarketAsset
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
    val staticTyping: Boolean = false,
    private val trackingProtocol: TrackingProtocol?,
    val metadataService: Boolean = false,
) {
    internal var internalState: InternalState = InternalState()

    internal val parser: ParserProtocol = Parser()
    internal val marketsProcessor = MarketsSummaryProcessor(
        parser = parser,
        localizer = localizer,
        staticTyping = staticTyping,
    )
    internal val assetsProcessor = run {
        val processor = AssetsProcessor(
            parser = parser,
            localizer = localizer,
            metadataService = metadataService,
        )
        processor.environment = environment
        processor
    }
    internal val walletProcessor = WalletProcessor(parser, localizer)
    internal val vaultProcessor = VaultProcessor(parser, localizer)
    internal val configsProcessor = ConfigsProcessor(parser, localizer)
    internal val routerProcessor = SkipProcessor(
        parser = parser,
        internalState = internalState.input.transfer,
        staticTyping = staticTyping,
    )
    internal val rewardsProcessor = RewardsParamsProcessor(parser)
    internal val launchIncentiveProcessor = LaunchIncentiveProcessor(parser)
    internal val tradeInputProcessor = TradeInputProcessor(parser)
    internal val closePositionInputProcessor = ClosePositionInputProcessor(parser)

    internal val marketsCalculator = MarketCalculator(parser)
    internal val accountCalculator = AccountCalculator(parser, useParentSubaccount)
    internal val accountCalculatorV2 = AccountCalculatorV2(parser, useParentSubaccount)

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
        payload: String,
        subaccountNumber: Int?,
        deploymentUri: String
    ): StateChanges {
        val json = parser.decodeJsonObject(payload)
        if (staticTyping) {
            if (metadataService) {
                val parsedAssetPayload = parser.asTypedStringMap<ConfigsAssetMetadata>(json)
                if (parsedAssetPayload == null) {
                    Logger.e { "Error parsing asset payload" }
                    return StateChanges.noChange
                }
                return processMarketsConfigurationsWithMetadataService(
                    payload = parsedAssetPayload,
                    subaccountNumber = subaccountNumber,
                    deploymentUri = deploymentUri,
                )
            } else {
                val parsedAssetPayload = parser.asTypedStringMap<ConfigsMarketAsset>(json)
                if (parsedAssetPayload == null) {
                    Logger.e { "Error parsing asset payload" }
                    return StateChanges.noChange
                }

                return processMarketsConfigurations(
                    payload = parsedAssetPayload,
                    subaccountNumber = subaccountNumber,
                    deploymentUri = deploymentUri,
                )
            }
        } else {
            return if (json != null) {
                receivedMarketsConfigurationsDeprecated(json, subaccountNumber, deploymentUri)
            } else {
                StateChanges.noChange
            }
        }
    }

    internal fun updateStateChanges(changes: StateChanges): StateChanges {
        if (changes.changes.contains(Changes.input)) {
            val subaccountNumber = changes.subaccountNumbers?.firstOrNull()
            if (staticTyping) {
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
            } else {
                val subaccount = if (subaccountNumber != null) {
                    parser.asNativeMap(
                        parser.value(
                            this.account,
                            "subaccounts.$subaccountNumber",
                        ),
                    )
                } else {
                    null
                }
                this.input = inputValidator.validateDeprecated(
                    subaccountNumber = subaccountNumber,
                    wallet = this.wallet,
                    user = this.user,
                    subaccount = subaccount,
                    markets = parser.asNativeMap(this.marketsSummary?.get("markets")),
                    input = this.input,
                    configs = this.configs,
                    currentBlockAndHeight = this.currentBlockAndHeight,
                    environment = this.environment,
                )
            }

            if (subaccountNumber != null) {
                if (staticTyping) {
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
                } else {
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

                        "triggerOrders" -> {
                            calculateTriggerOrders(subaccountNumber)
                        }

                        "adjustIsolatedMargin" -> {
                            calculateAdjustIsolatedMargin(subaccountNumber)
                        }

                        else -> {}
                    }
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
        if (staticTyping) {
            val calculator = TradeInputCalculatorV2(parser, calculation)
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
        } else {
            val input = this.input?.mutable()
            val trade = parser.asNativeMap(input?.get(tag))
            val inputType = parser.asString(parser.value(trade, "size.input"))
            val calculator = TradeInputCalculator(parser, calculation)
            val params = mutableMapOf<String, Any>()
            params.safeSet("markets", parser.asNativeMap(marketsSummary?.get("markets")))
            params.safeSet("account", account)
            params.safeSet("user", user)
            params.safeSet("trade", trade)
            params.safeSet("rewardsParams", rewardsParams)
            params.safeSet("configs", configs)

            val modified = calculator.calculate(params, subaccountNumber, inputType)
            this.setMarkets(parser.asNativeMap(modified["markets"]))
            this.account = parser.asNativeMap(modified["account"])
            input?.safeSet(tag, parser.asNativeMap(modified["trade"]))

            this.input = input
        }
    }

    private fun calculateClosePosition(subaccountNumber: Int) {
        calculateTrade("closePosition", TradeCalculation.closePosition, subaccountNumber)
    }

    private fun calculateTransfer(subaccountNumber: Int?) {
        if (staticTyping) {
            val calculator = TransferInputCalculatorV2(parser = parser)
            calculator.calculate(
                transfer = internalState.input.transfer,
                wallet = internalState.wallet,
                subaccountNumber = subaccountNumber,
            )
        } else {
            val input = this.input?.mutable()
            val transfer = parser.asNativeMap(input?.get("transfer"))
            val calculator = TransferInputCalculator(parser)
            val params = mutableMapOf<String, Any>()
            params.safeSet("markets", parser.asNativeMap(marketsSummary?.get("markets")))
            params.safeSet("user", user)
            params.safeSet("transfer", transfer)
            params.safeSet("wallet", wallet)

            val modified = calculator.calculate(params, subaccountNumber)
            this.setMarkets(parser.asNativeMap(modified["markets"]))
            this.wallet = parser.asNativeMap(modified["wallet"])
            input?.safeSet("transfer", parser.asNativeMap(modified["transfer"]))

            this.input = input
        }
    }

    private fun calculateTriggerOrders(subaccountNumber: Int) {
        if (staticTyping) {
            val calculator = TriggerOrdersInputCalculatorV2()
            calculator.calculate(
                triggerOrders = internalState.input.triggerOrders,
                account = internalState.wallet.account,
                subaccountNumber = subaccountNumber,
            )
        } else {
            val input = this.input?.mutable()
            val triggerOrders = parser.asNativeMap(input?.get("triggerOrders"))
            val calculator = TriggerOrdersInputCalculator(parser)
            val params = mutableMapOf<String, Any>()
            params.safeSet("account", account)
            params.safeSet("user", user)
            params.safeSet("markets", parser.asNativeMap(marketsSummary?.get("markets")))
            params.safeSet("triggerOrders", triggerOrders)

            val modified = calculator.calculate(params, subaccountNumber)
            input?.safeSet("triggerOrders", parser.asNativeMap(modified["triggerOrders"]))

            this.input = input
        }
    }

    private fun calculateAdjustIsolatedMargin(subaccountNumber: Int?) {
        if (staticTyping) {
            val calculator = AdjustIsolatedMarginInputCalculatorV2(parser)
            internalState.input.adjustIsolatedMargin = calculator.calculate(
                adjustIsolatedMargin = internalState.input.adjustIsolatedMargin,
                walletState = internalState.wallet,
                markets = internalState.marketsSummary.markets,
                parentSubaccountNumber = subaccountNumber,
            )
        } else {
            val input = this.input?.mutable()
            val adjustIsolatedMargin = parser.asNativeMap(input?.get("adjustIsolatedMargin"))
            val calculator = AdjustIsolatedMarginInputCalculator(parser)
            val params = mutableMapOf<String, Any>()
            params.safeSet("wallet", wallet)
            params.safeSet("account", account)
            params.safeSet("user", user)
            params.safeSet("markets", parser.asNativeMap(marketsSummary?.get("markets")))
            params.safeSet("adjustIsolatedMargin", adjustIsolatedMargin)

            val modified = calculator.calculate(params, subaccountNumber)
            this.setMarkets(parser.asNativeMap(modified["markets"]))
            this.wallet = parser.asNativeMap(modified["wallet"])
            input?.safeSet(
                "adjustIsolatedMargin",
                parser.asNativeMap(modified["adjustIsolatedMargin"]),
            )

            this.input = input
        }
    }

    private fun subaccount(subaccountNumber: Int): Map<String, Any>? {
        return parser.asNativeMap(parser.value(account, "subaccounts.$subaccountNumber"))
    }

    private fun subaccountList(subaccountNumber: Int, name: String): IList<Any>? {
        return parser.asList(subaccount(subaccountNumber)?.get(name))
    }

    private fun groupedSubaccount(subaccountNumber: Int): Map<String, Any>? {
        return parser.asNativeMap(parser.value(account, "groupedSubaccounts.$subaccountNumber"))
    }

    private fun groupedSubaccountList(subaccountNumber: Int, name: String): IList<Any>? {
        return parser.asList(groupedSubaccount(subaccountNumber)?.get(name))
    }

    private fun subaccountHistoricalPnl(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "historicalPnl")
    }

    private fun subaccountFills(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "fills")
    }

    private fun subaccountTransfers(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "transfers")
    }

    private fun subaccountFundingPayments(subaccountNumber: Int): IList<Any>? {
        return subaccountList(subaccountNumber, "fundingPayments")
    }

    private fun groupedSubaccountHistoricalPnl(subaccountNumber: Int): IList<Any>? {
        return groupedSubaccountList(subaccountNumber, "historicalPnl")
    }

    private fun groupedSubaccountFills(subaccountNumber: Int): IList<Any>? {
        return groupedSubaccountList(subaccountNumber, "fills")
    }

    private fun groupedSubaccountTransfers(subaccountNumber: Int): IList<Any>? {
        return groupedSubaccountList(subaccountNumber, "transfers")
    }

    private fun groupedSubaccountFundingPayments(subaccountNumber: Int): IList<Any>? {
        return groupedSubaccountList(subaccountNumber, "fundingPayments")
    }

    private fun allSubaccountNumbers(): IList<Int> {
        if (staticTyping) {
            return internalState.wallet.account.subaccounts.keys.toIList()
        } else {
            val subaccountsData = parser.asNativeMap(account?.get("subaccounts"))
            return if (subaccountsData != null) {
                parser.asNativeMap(subaccountsData)?.keys?.mapNotNull { key ->
                    parser.asInt(key)
                }?.toIList() ?: iListOf<Int>()
            } else {
                iListOf<Int>()
            }
        }
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
            if (staticTyping) {
                val periods = if (internalState.input.currentType != null) {
                    setOf(
                        CalculationPeriod.current,
                        CalculationPeriod.post,
                        CalculationPeriod.settled,
                    )
                } else {
                    setOf(CalculationPeriod.current)
                }
                internalState.wallet.account = accountCalculatorV2.calculate(
                    account = internalState.wallet.account,
                    subaccountNumbers = subaccountNumbers,
                    marketsSummary = internalState.marketsSummary,
                    periods = periods,
                    price = null, // priceOverwrite(markets),
                    configs = null, // This is used to get the IMF.. with "null" the default value 0.05 will be used
                )
            } else {
                this.marketsSummary?.let { marketsSummary ->
                    val periods = if (this.input != null) {
                        setOf(
                            CalculationPeriod.current,
                            CalculationPeriod.post,
                            CalculationPeriod.settled,
                        )
                    } else {
                        setOf(CalculationPeriod.current)
                    }

                    parser.asNativeMap(marketsSummary["markets"])?.let { markets ->
                        val modifiedAccount = accountCalculator.calculate(
                            account = account,
                            subaccountNumbers = subaccountNumbers,
                            configs = null,
                            markets = markets,
                            price = priceOverwrite(markets),
                            periods = periods,
                        )
                        this.account = modifiedAccount
                    }
                }
            }
        }

        if (staticTyping) {
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
        } else {
            if (parser.value(account, "groupedSubaccounts") != null) {
                if (changes.changes.contains(Changes.fills)) {
                    this.account = mergeFillsDeprecated(this.account, subaccountNumbers)
                }
                if (changes.changes.contains(Changes.transfers)) {
                    this.account = mergeTransfersDeprecated(this.account, subaccountNumbers)
                }
            }
        }

        if (changes.changes.contains(Changes.input)) {
            if (staticTyping) {
                // finalize the trade input leverage
                if (internalState.input.currentType == InputType.TRADE) {
                    val trade = internalState.input.trade
                    val account = internalState.wallet.account
                    val sizeInput = TradeInputField.invoke(trade.size?.input)
                    if (sizeInput == TradeInputField.size || sizeInput == TradeInputField.usdcSize || sizeInput == TradeInputField.balancePercent) {
                        val subaccountNumber = changes.subaccountNumbers?.firstOrNull()
                        val marketId = trade.marketId
                        if (subaccountNumber != null && marketId != null) {
                            val position = account.subaccounts[subaccountNumber]?.openPositions?.get(marketId)
                            val postOrderLeverage = position?.calculated?.get(CalculationPeriod.post)?.leverage
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
            } else {
                val modified = this.input?.mutable() ?: return
                when (parser.asString(modified["current"])) {
                    "trade" -> {
                        when (parser.asString(parser.value(modified, "trade.size.input"))) {
                            "size.size", "size.usdcSize", "size.balancePercent" -> {
                                val subaccountNumber = changes.subaccountNumbers?.firstOrNull()
                                val marketId =
                                    parser.asString(parser.value(modified, "trade.marketId"))
                                if (subaccountNumber != null && marketId != null) {
                                    val leverage =
                                        parser.asDouble(
                                            parser.value(
                                                this.account,
                                                "subaccounts.$subaccountNumber.openPositions.$marketId.leverage.postOrder",
                                            ),
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

                    "triggerOrders" -> {
                        // TODO: update price diffs based on price.input
                    }

                    "closePosition", "transfer" -> {
                    }
                }
                modified.safeSet("receiptLines", calculateReceipt(modified))
                this.input = modified
            }
        }
    }

    private fun calculateReceipt(input: Map<String, Any>): List<String>? {
        return when (parser.asString(input["current"])) {
            "trade" -> {
                val trade = parser.asNativeMap(input["trade"]) ?: return null
                val type = parser.asString(trade["type"]) ?: return null
                return when (type) {
                    "MARKET", "STOP_MARKET", "TAKE_PROFIT_MARKET", "TRAILING_STOP" -> {
                        listOf(
                            ReceiptLine.ExpectedPrice.rawValue,
                            ReceiptLine.LiquidationPrice.rawValue,
                            ReceiptLine.PositionMargin.rawValue,
                            ReceiptLine.PositionLeverage.rawValue,
                            ReceiptLine.Fee.rawValue,
                            ReceiptLine.Reward.rawValue,
                        )
                    }

                    else -> {
                        listOf(
                            ReceiptLine.LiquidationPrice.rawValue,
                            ReceiptLine.PositionMargin.rawValue,
                            ReceiptLine.PositionLeverage.rawValue,
                            ReceiptLine.Fee.rawValue,
                            ReceiptLine.Reward.rawValue,
                        )
                    }
                }
            }

            "closePosition" -> {
                listOf(
                    ReceiptLine.BuyingPower.rawValue,
                    ReceiptLine.MarginUsage.rawValue,
                    ReceiptLine.ExpectedPrice.rawValue,
                    ReceiptLine.Fee.rawValue,
                    ReceiptLine.Reward.rawValue,
                )
            }

            "transfer" -> {
                val transfer = parser.asNativeMap(input["transfer"]) ?: return null
                val type = parser.asString(transfer["type"]) ?: return null
                return when (type) {
                    "DEPOSIT", "WITHDRAWAL" -> {
                        listOf(
                            ReceiptLine.Equity.rawValue,
                            ReceiptLine.BuyingPower.rawValue,
                            ReceiptLine.BridgeFee.rawValue,
                            // add these back when supported by Skip
//                            ReceiptLine.ExchangeRate.rawValue,
//                            ReceiptLine.ExchangeReceived.rawValue,
//                            ReceiptLine.Fee.rawValue,
                            ReceiptLine.Slippage.rawValue,
                            ReceiptLine.TransferRouteEstimatedDuration.rawValue,
                        )
                    }

                    "TRANSFER_OUT" -> {
                        listOf(
                            ReceiptLine.Equity.rawValue,
                            ReceiptLine.MarginUsage.rawValue,
                            ReceiptLine.Fee.rawValue,
                        )
                    }

                    else -> {
                        listOf()
                    }
                }
            }

            "adjustIsolatedMargin" -> {
                listOf(
                    ReceiptLine.CrossFreeCollateral.rawValue,
                    ReceiptLine.CrossMarginUsage.rawValue,
                    ReceiptLine.PositionLeverage.rawValue,
                    ReceiptLine.PositionMargin.rawValue,
                    ReceiptLine.LiquidationPrice.rawValue,
                )
            }

            else -> null
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
            if (staticTyping) {
                marketsSummary =
                    PerpetualMarketSummary.apply(
                        existing = marketsSummary,
                        parser = parser,
                        data = emptyMap(),
                        assets = null,
                        staticTyping = staticTyping,
                        marketSummaryState = internalState.marketsSummary,
                        changes = changes,
                    )
            } else {
                parser.asNativeMap(data?.get("markets"))?.let {
                    marketsSummary =
                        PerpetualMarketSummary.apply(
                            existing = marketsSummary,
                            parser = parser,
                            data = it,
                            assets = this.assets,
                            staticTyping = staticTyping,
                            marketSummaryState = internalState.marketsSummary,
                            changes = changes,
                        )
                } ?: run {
                    marketsSummary = null
                }
            }
        }
        if (changes.changes.contains(Changes.orderbook)) {
            val markets = changes.markets
            orderbooks = if (markets != null) {
                val modified = orderbooks?.toIMutableMap() ?: iMutableMapOf()
                for (marketId in markets) {
                    val orderbook = if (staticTyping) {
                        internalState.marketsSummary.markets[marketId]?.groupedOrderbook
                    } else {
                        val data =
                            parser.asNativeMap(
                                parser.value(
                                    data,
                                    "markets.markets.$marketId.orderbook",
                                ),
                            )
                        val existing = orderbooks?.get(marketId)
                        MarketOrderbook.create(existing, parser, data)
                    }
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
                    if (staticTyping) {
                        val trades = internalState.marketsSummary.markets[marketId]?.trades
                        modified.typedSafeSet(marketId, trades?.toIList())
                    } else {
                        val data = parser.asList(
                            parser.value(
                                data,
                                "markets.markets.$marketId.trades",
                            ),
                        ) as? IList<Map<String, Any>>
                        val existing = trades?.get(marketId)
                        val trades = MarketTrade.create(existing, parser, data, localizer)
                        modified.typedSafeSet(marketId, trades)
                    }
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
                    val data = parser.asList(
                        parser.value(
                            data,
                            "markets.markets.$marketId.historicalFunding",
                        ),
                    ) as? IList<Map<String, Any>>
                    val existing = historicalFundings?.get(marketId)
                    val historicalFunding = MarketHistoricalFunding.create(existing, parser, data)
                    modified.typedSafeSet(marketId, historicalFunding)
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
                    if (staticTyping) {
                        val candles = internalState.marketsSummary.markets[marketId]?.candles
                        val marketCandles: MutableMap<String, IList<MarketCandle>> = mutableMapOf()
                        for ((key, value) in candles ?: emptyMap()) {
                            marketCandles[key] = value.toIList()
                        }
                        modified.typedSafeSet(marketId, MarketCandles(candles = marketCandles.toIMap()))
                    } else {
                        val data =
                            parser.asNativeMap(
                                parser.value(
                                    data,
                                    "markets.markets.$marketId.candles",
                                ),
                            )
                        val existing = candles?.get(marketId)
                        val candles = MarketCandles.create(existing, parser, data)
                        modified.typedSafeSet(marketId, candles)
                    }
                }
                candles = modified
            } else {
                candles = null
            }
        }
        if (changes.changes.contains(Changes.assets)) {
            if (staticTyping) {
                assets = internalState.assets.toIMutableMap()
                if (assets.isEmpty()) {
                    assets = null
                }
            } else {
                this.assets?.let {
                    assets = assets ?: mutableMapOf<String, Asset>()
                    for ((key, data) in it) {
                        parser.asNativeMap(data)?.let {
                            Asset.create(assets?.get(key), parser, it, localizer)?.let {
                                assets!![key] = it
                            }
                        }
                    }
                } ?: run {
                    assets = null
                }
            }
        }
        if (changes.changes.contains(Changes.configs)) {
            if (staticTyping) {
                configs = Configs(
                    network = null,
                    feeTiers = internalState.configs.feeTiers?.toIList(),
                    feeDiscounts = null,
                    equityTiers = internalState.configs.equityTiers,
                    withdrawalGating = internalState.configs.withdrawalGating,
                    withdrawalCapacity = WithdrawalCapacity(
                        capacity = internalState.configs.withdrawalCapacity?.capacity,
                    ),
                )
            } else {
                this.configs?.let {
                    configs = Configs.create(configs, parser, it, localizer)
                } ?: run {
                    configs = null
                }
            }
        }
        if (changes.changes.contains(Changes.wallet)) {
            if (staticTyping) {
                wallet = Wallet.create(internalState.wallet)
            } else {
                this.wallet?.let {
                    wallet = Wallet.createDeprecated(
                        existing = wallet,
                        parser = parser,
                        data = it,
                    )
                } ?: run {
                    wallet = null
                }
            }
        }
        val subaccountNumbers = changes.subaccountNumbers ?: allSubaccountNumbers()
        val accountData = this.account
        if (accountData != null || staticTyping) {
            if (changes.changes.contains(Changes.subaccount)) {
                account = if (account == null) {
                    Account.create(
                        existing = null,
                        parser = parser,
                        data = accountData ?: emptyMap(),
                        tokensInfo = tokensInfo,
                        localizer = localizer,
                        staticTyping = staticTyping,
                        internalState = internalState.wallet.account,
                    )
                } else {
                    val subaccounts = account.subaccounts?.toIMutableMap() ?: mutableMapOf()
                    for (subaccountNumber in subaccountNumbers) {
                        val subaccount = Subaccount.create(
                            existing = account.subaccounts?.get("$subaccountNumber"),
                            parser = parser,
                            data = subaccount(subaccountNumber),
                            localizer = localizer,
                            staticTyping = staticTyping,
                            internalState = internalState.wallet.account.subaccounts[subaccountNumber],
                        )
                        subaccounts.typedSafeSet("$subaccountNumber", subaccount)
                    }
                    val groupedSubaccounts = account.groupedSubaccounts?.toIMutableMap() ?: mutableMapOf()
                    for (subaccountNumber in subaccountNumbers) {
                        if (subaccountNumber < NUM_PARENT_SUBACCOUNTS) {
                            val subaccount = Subaccount.create(
                                existing = account.groupedSubaccounts?.get("$subaccountNumber"),
                                parser = parser,
                                data = groupedSubaccount(subaccountNumber),
                                localizer = localizer,
                                staticTyping = staticTyping,
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
                    data = accountData ?: emptyMap(),
                    tokensInfo = tokensInfo,
                    localizer = localizer,
                    staticTyping = staticTyping,
                    internalState = internalState.wallet.account,
                )
            }
        } else {
            account = null
            fills = null
            historicalPnl = null
            transfers = null
            fundingPayments = null
        }
        for (subaccountNumber in subaccountNumbers) {
            val subaccountText = "$subaccountNumber"
            val subaccount =
                parser.asNativeMap(parser.value(this.account, "groupedSubaccounts.$subaccountNumber")) ?: parser.asNativeMap(parser.value(this.account, "subaccounts.$subaccountNumber"))

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

                if (staticTyping) {
                    subaccountHistoricalPnl =
                        internalState.wallet.account.subaccounts[subaccountNumber]?.historicalPNLs?.toIList()?.filter {
                            it.createdAtMilliseconds >= start.toEpochMilliseconds()
                        }
                } else {
                    val subaccountHistoricalPnlData =
                        (subaccountHistoricalPnl(subaccountNumber) as? IList<Map<String, Any>>)?.mutable()
                            ?: mutableListOf()

                    subaccountHistoricalPnl = SubaccountHistoricalPNL.create(
                        existing = subaccountHistoricalPnl,
                        parser = parser,
                        data = subaccountHistoricalPnlData,
                        startTime = start,
                    )
                }
                modifiedHistoricalPnl.typedSafeSet(subaccountText, subaccountHistoricalPnl)
                historicalPnl = modifiedHistoricalPnl
            }
            if (changes.changes.contains(Changes.fills)) {
                val modifiedFills = fills?.toIMutableMap() ?: mutableMapOf()
                var subaccountFills = fills?.get(subaccountText)
                if (staticTyping) {
                    val newFills = internalState.wallet.account.subaccounts[subaccountNumber]?.fills?.toIList()
                    subaccountFills = SubaccountFill.merge(
                        existing = subaccountFills,
                        new = newFills,
                    )
                } else {
                    subaccountFills = SubaccountFill.create(
                        subaccountFills,
                        parser,
                        subaccountFills(subaccountNumber) as? IList<Map<String, Any>>,
                        localizer,
                    )
                }
                modifiedFills.typedSafeSet(subaccountText, subaccountFills)
                fills = modifiedFills
            }
            if (changes.changes.contains(Changes.transfers)) {
                val modifiedTransfers = transfers?.toIMutableMap() ?: mutableMapOf()
                var subaccountTransfers = transfers?.get(subaccountText)
                if (staticTyping) {
                    subaccountTransfers = internalState.wallet.account.subaccounts[subaccountNumber]?.transfers?.toIList()
                } else {
                    subaccountTransfers = SubaccountTransfer.create(
                        subaccountTransfers,
                        parser,
                        subaccountTransfers(subaccountNumber) as? IList<Map<String, Any>>,
                    )
                }
                modifiedTransfers.typedSafeSet(subaccountText, subaccountTransfers)
                transfers = modifiedTransfers
            }
            if (changes.changes.contains(Changes.fundingPayments)) {
                val modifiedFundingPayments = fundingPayments?.toIMutableMap() ?: mutableMapOf()
                var subaccountFundingPayments = fundingPayments?.get(subaccountText)
                subaccountFundingPayments = SubaccountFundingPayment.create(
                    subaccountFundingPayments,
                    parser,
                    subaccountFundingPayments(subaccountNumber) as? IList<Map<String, Any>>,
                )
                modifiedFundingPayments.typedSafeSet(subaccountText, subaccountFundingPayments)
                fundingPayments = modifiedFundingPayments
            }

            if (changes.changes.contains(Changes.input)) {
                if (staticTyping) {
                    inputValidator.validate(
                        internalState = internalState,
                        subaccountNumber = subaccountNumber,
                        currentBlockAndHeight = currentBlockAndHeight,
                        environment = environment,
                    )
                } else {
                    this.input = inputValidator.validateDeprecated(
                        subaccountNumber = subaccountNumber,
                        wallet = this.wallet,
                        user = this.user,
                        subaccount = subaccount,
                        markets = parser.asNativeMap(this.marketsSummary?.get("markets")),
                        input = this.input,
                        configs = this.configs,
                        currentBlockAndHeight = this.currentBlockAndHeight,
                        environment = this.environment,
                    )
                }

                input = Input.create(
                    existing = input,
                    parser = parser,
                    data = this.input,
                    environment = environment,
                    internalState = internalState,
                    staticTyping = staticTyping,
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
            if (staticTyping) {
                launchIncentive = LaunchIncentive(
                    seasons = LaunchIncentiveSeasons(
                        seasons = internalState.launchIncentive.seasons?.toIList() ?: iListOf(),
                    ),
                )
            } else {
                this.launchIncentive?.let {
                    launchIncentive = LaunchIncentive.create(launchIncentive, parser, it)
                } ?: run {
                    launchIncentive = null
                }
            }
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
                    VaultAccountCalculator.calculateUserVaultInfo(vaultInfo = accountInfo, vaultTransfers = transfers)
                } else {
                    null
                }
                vault = Vault(details = internalState.vault?.details, positions = positions, account = account)
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

    private fun priceOverwrite(markets: Map<String, Any>): Map<String, Any>? {
        // TODO(@aforaleka): Uncomment when protocol can match collateralization check at limit price
        // if (parser.asString(input?.get("current")) == "trade") {
        //     val trade = parser.asNativeMap(input?.get("trade"))
        //     when (parser.asString(trade?.get("type"))) {
        //         "LIMIT", "STOP_LIMIT", "TAKE_PROFIT", "TRAILING_STOP", "STOP_MARKET", "TAKE_PROFIT_MARKET" -> {
        //             val price = parser.asDouble(parser.value(trade, "summary.price"))
        //             val marketId = parser.asString(trade?.get("marketId"))
        //             if (marketId != null && price != null) {
        //                 val market = parser.asNativeMap(markets[marketId])
        //                 val oraclePrice =
        //                     parser.asDouble(market?.get("oraclePrice"))
        //                 if (oraclePrice != null) {
        //                     val side = parser.asString(trade?.get("side"))
        //                     if ((side == "BUY" && price < oraclePrice) || (side == "SELL" && price > oraclePrice)) {
        //                         return iMapOf(marketId to price)
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }
        return null
    }

    private fun setMarkets(markets: Map<String, Any>?) {
    }

    fun setHistoricalPnlDays(days: Int, subaccountNumber: Int): StateResponse {
        return if (historicalPnlDays != days) {
            historicalPnlDays = days
            val now = ServerTime.now()
            val startTime = now - days.days
            val historicalPnls = state?.historicalPnl?.get("$subaccountNumber") ?: return noChange()
            val first = historicalPnls.firstOrNull() ?: return noChange()
            val changes = StateChanges(iListOf(Changes.historicalPnl))
            state = updateState(state, changes, tokensInfo, localizer)
            StateResponse(state, changes)
        } else {
            noChange()
        }
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
                    iListOf(subaccountNumber),
                )
                state = updateState(state, changes, tokensInfo, localizer)
                StateResponse(state, changes)
            } else {
                noChange()
            }
        } else {
            noChange()
        }
    }

    private fun clearTradeInput(input: Map<String, Any>): Map<String, Any> {
        val trade = parser.asNativeMap(input["trade"])?.toMutableMap()
        trade?.safeSet("size", null)
        trade?.safeSet("price", null)
        val modifiedInput = input.toMutableMap()
        modifiedInput.safeSet("trade", trade)
        return modifiedInput
    }

    private fun clearTransferInput(input: Map<String, Any>): Map<String, Any> {
        val trade = parser.asNativeMap(input["trade"])?.toMutableMap()
        trade?.safeSet("size", null)
        trade?.safeSet("price", null)
        val modifiedInput = input.toMutableMap()
        modifiedInput.safeSet("trade", trade)
        return modifiedInput
    }

    fun received(subaccountNumber: Int, height: BlockAndTime?): StateResponse {
        val wallet = wallet
        if (wallet != null) {
            val (modifiedWallet, updated) = walletProcessor.received(
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
