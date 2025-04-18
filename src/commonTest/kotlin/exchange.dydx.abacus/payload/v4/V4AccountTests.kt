package exchange.dydx.abacus.payload.v4

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.EquityTier
import exchange.dydx.abacus.output.account.FillLiquidity
import exchange.dydx.abacus.output.account.TransferRecordType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderTimeInForce
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.InternalAccountBalanceState
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.state.machine.historicalTradingRewards
import exchange.dydx.abacus.state.machine.onChainAccountBalances
import exchange.dydx.abacus.state.machine.onChainDelegations
import exchange.dydx.abacus.state.machine.updateHeight
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.notification.NotificationsProvider
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountSubscribed
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountWithOrdersAndFillsChanged
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ServerTime
import indexer.codegen.IndexerPerpetualPositionStatus
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class V4AccountTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testAccountsOnce()
    }

    private fun testAccountsOnce() {
        var time = ServerTime.now()
        testSubaccountsReceived()
        time = perp.log("Accounts Received", time)

        testSubaccountFillsReceived()
        time = perp.log("Fills Received", time)

        testSubaccountSubscribed()
        time = perp.log("Accounts Subscribed", time)

        testSubaccountTransfersReceived()
        time = perp.log("Transfers Received", time)

        testSubaccountFillsChannelData()

        testSubaccountChanged()
        time = perp.log("Accounts Changed", time)

        testBatchedSubaccountChanged()

        testPartiallyFilledAndCanceledOrders()

        testEquityTiers()

        testFeeTiers()

        testUserFeeTier()

        testUserStats()

        testAccountHistoricalTradingRewards()
    }

    private fun testSubaccountsReceived() {
        perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")

        val account = perp.internalState.wallet.account
        assertEquals(2800.8, account.tradingRewards.total)

        val subaccount = account.subaccounts[0]
        val calculated = subaccount?.calculated?.get(CalculationPeriod.current)!!
        assertEquals(108116.7318528828, calculated.equity)
        assertEquals(106640.3767269893, calculated.freeCollateral)
        assertEquals(99872.368956, calculated.quoteBalance)
    }

    private fun testSubaccountSubscribed() {
        perp.loadv4SubaccountSubscribed(mock, testWsUrl)

        val account = perp.internalState.wallet.account
        assertEquals(2800.8, account.tradingRewards.total)

        val subaccount = account.subaccounts[0]
        val calculated = subaccount?.calculated?.get(CalculationPeriod.current)!!
        assertEquals(122034.20090508368, calculated.equity)
        assertEquals(100728.9107212275, calculated.freeCollateral)
        assertEquals(68257.215192, calculated.quoteBalance)

        val orders = subaccount.orders
        assertEquals(2, orders?.size)

        val openPositions = subaccount.openPositions
        assertEquals(2, openPositions?.size)
    }

    private fun testSubaccountFillsReceived() {
        perp.rest(
            url = AbUrl.fromString("$testRestUrl/v4/fills?subaccountNumber=0"),
            payload = mock.fillsChannel.v4_rest,
            subaccountNumber = 0,
            height = null,
        )

        var fills = perp.internalState.wallet.account.subaccounts[0]?.fills
        assertEquals(100, fills?.size)

        perp.socket(testWsUrl, mock.fillsChannel.v4_subscribed, 0, null)

        fills = perp.internalState.wallet.account.subaccounts[0]?.fills
        assertEquals(100, fills?.size)
        val fill = fills?.first()!!
        assertEquals("dad7abeb-4c04-58d3-8dda-fd0bc0528deb", fill.id)
        assertEquals(OrderSide.Buy, fill.side)
        assertEquals(FillLiquidity.taker, fill.liquidity)
        assertEquals(OrderType.Limit, fill.type)
        assertEquals("BTC-USD", fill.marketId)
        assertEquals("4f2a6f7d-a897-5c4e-986f-d48f5760102a", fill.orderId)
        assertEquals(18275.31, fill.price)
        assertEquals(4.41E-6, fill.size)
        assertEquals(0.0, fill.fee)
    }

    private fun testSubaccountTransfersReceived() {
        perp.rest(
            url = AbUrl.fromString("$testRestUrl/v4/transfers?subaccountNumber=0"),
            payload = mock.transfersMock.transfer_data,
            subaccountNumber = 0,
            height = null,
        )

        var transfers = perp.internalState.wallet.account.subaccounts[0]?.transfers
        var transfer = transfers?.first()!!
        assertEquals("89586775-0646-582e-9b36-4f131715644d", transfer.id)
        assertEquals(TransferRecordType.TRANSFER_OUT, transfer.type)
        assertEquals("USDC", transfer.asset)
        assertEquals(
            parser.asDatetime("2023-08-21T21:37:53.373Z")?.toEpochMilliseconds()?.toDouble(),
            transfer.updatedAtMilliseconds,
        )
        assertEquals(404014, transfer.updatedAtBlock)
        assertEquals(419.98472, transfer.amount)
        assertEquals("dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg", transfer.fromAddress)
        assertEquals("dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8", transfer.toAddress)
        assertEquals("MOCKHASH1", transfer.transactionHash)

        perp.socket(testWsUrl, mock.transfersMock.channel_data, 0, null)

        transfers = perp.internalState.wallet.account.subaccounts[0]?.transfers
        transfer = transfers?.first()!!
        assertEquals(
            "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B",
            transfer.id,
        )
        assertEquals(
            "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B",
            transfer.transactionHash,
        )
    }

    private fun testSubaccountFillsChannelData() {
        perp.socket(testWsUrl, mock.fillsChannel.v4_channel_data, 0, null)

        val account = perp.internalState.wallet.account
        val blockReward = account.tradingRewards.blockRewards[0]
        assertEquals("0.02", blockReward.tradingReward)
        assertEquals("2422", blockReward.createdAtHeight)
        val blockReward2 = account.tradingRewards.blockRewards[1]
        assertEquals("0.01", blockReward2.tradingReward)
        assertEquals("2501", blockReward2.createdAtHeight)

        val subaccount = account.subaccounts[0]
        val calculated = subaccount?.calculated?.get(CalculationPeriod.current)!!
        assertEquals(122034.20090508368, calculated.equity)
        assertEquals(100728.9107212275, calculated.freeCollateral)
        assertEquals(68257.215192, calculated.quoteBalance)

        val fills = subaccount.fills
        assertEquals(101, fills?.size)
        val fill = fills?.firstOrNull { it.id == "0cf41e16-036e-534d-bbaf-cf318b44b840" }
        assertEquals("0cf41e16-036e-534d-bbaf-cf318b44b840", fill?.id)
        assertEquals(OrderSide.Sell, fill?.side)
        assertEquals(FillLiquidity.taker, fill?.liquidity)
        assertEquals(OrderType.Limit, fill?.type)
        assertEquals("f5d440b9-6e93-535a-a5d6-fbb74852c6d8", fill?.orderId)
        assertEquals(1570.19, fill?.price)
        assertEquals(0.003, fill?.size)

        val orders = subaccount.orders
        assertEquals(3, orders?.size)
        val order = orders?.firstOrNull { it.id == "b812bea8-29d3-5841-9549-caa072f6f8a8" }
        assertEquals("b812bea8-29d3-5841-9549-caa072f6f8a8", order?.id)
        assertEquals(OrderSide.Sell, order?.side)
        assertEquals(OrderType.Limit, order?.type)
        assertEquals(OrderTimeInForce.GTT, order?.timeInForce)
        assertEquals(1255.927, order?.price)
        assertEquals(1.653451, order?.size)
        assertEquals(false, order?.postOnly)
        assertEquals(false, order?.reduceOnly)
        assertEquals(0.970818, order?.remainingSize)
        assertEquals(0.682633, order?.totalFilled)
    }

    private fun testSubaccountChanged() {
        perp.loadv4SubaccountWithOrdersAndFillsChanged(mock, testWsUrl)

        val subaccount = perp.internalState.wallet.account.subaccounts[0]!!

        val calculated = subaccount.calculated[CalculationPeriod.current]!!
        assertEquals(-161020.048352526628, calculated.equity)
        assertEquals(-172483.91152975295, calculated.freeCollateral)
        assertEquals(68257.215192, calculated.quoteBalance)

        var order = subaccount.orders?.firstOrNull { it.id == "b812bea8-29d3-5841-9549-caa072f6f8a8" }
        assertEquals("b812bea8-29d3-5841-9549-caa072f6f8a8", order?.id)
        assertEquals(OrderSide.Sell, order?.side)
        assertEquals(OrderType.Limit, order?.type)
        assertEquals(OrderTimeInForce.GTT, order?.timeInForce)
        assertEquals(1255.927, order?.price)
        assertEquals(1.653451, order?.size)
        assertEquals(false, order?.postOnly)
        assertEquals(false, order?.reduceOnly)

        val transfers = subaccount.transfers
        assertTrue {
            transfers?.any { it.id == "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B" } == true
        }
        val transfer = transfers?.first { it.id == "89586775-0646-582e-9b36-4f131715644d" }!!
        assertEquals("89586775-0646-582e-9b36-4f131715644d", transfer.id)
        assertEquals(TransferRecordType.TRANSFER_OUT, transfer.type)
        assertEquals("USDC", transfer.asset)
        assertEquals(419.98472, transfer.amount)
        assertEquals("dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg", transfer.fromAddress)
        assertEquals("dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8", transfer.toAddress)
        assertEquals("MOCKHASH1", transfer.transactionHash)

        val fills = subaccount.fills
        assertEquals(102, fills?.size)

        val ethPosition = subaccount.openPositions?.get("ETH-USD")!!
        assertEquals("ETH-USD", ethPosition.market)
        assertEquals(IndexerPerpetualPositionStatus.OPEN, ethPosition.status)
        assertEquals(106.180627, ethPosition.maxSize)
        assertEquals(0.0, ethPosition.netFunding)
        assertEquals(-102.716895, ethPosition.realizedPnl)
        assertEquals(-51730.736277242424, ethPosition.calculated[CalculationPeriod.current]?.unrealizedPnl)
        assertEquals(parser.asDatetime("2022-12-11T17:29:39.792Z"), ethPosition.createdAt)
        assertEquals(1266.094016, ethPosition.entryPrice)
        assertEquals(-106.17985, ethPosition.size)

        perp.socket(
            url = testWsUrl,
            jsonString = mock.accountsChannel.v4_best_effort_cancelled,
            subaccountNumber = 0,
            height = BlockAndTime(16940, Clock.System.now()),
        )

        order = perp.internalState.wallet.account.subaccounts[0]?.orders?.firstOrNull {
            it.id == "80133551-6d61-573b-9788-c1488e11027a"
        }
        assertEquals(OrderStatus.Pending, order?.status)

        perp.socket(
            url = testWsUrl,
            jsonString = mock.accountsChannel.v4_best_effort_cancelled,
            subaccountNumber = 0,
            height = BlockAndTime(16960, Clock.System.now()),
        )

        order = perp.internalState.wallet.account.subaccounts[0]?.orders?.firstOrNull {
            it.id == "80133551-6d61-573b-9788-c1488e11027a"
        }
        assertEquals(OrderStatus.Canceled, order?.status)

        perp.updateHeight(BlockAndTime(16960, Clock.System.now()))

        order = perp.internalState.wallet.account.subaccounts[0]?.orders?.firstOrNull {
            it.id == "80133551-6d61-573b-9788-c1488e11027a"
        }
        assertEquals(OrderStatus.Canceled, order?.status)
    }

    private fun testBatchedSubaccountChanged() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.accountsChannel.v4_batched,
            subaccountNumber = 0,
            height = BlockAndTime(16960, Clock.System.now()),
        )

        var subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        var calculated = subaccount.calculated[CalculationPeriod.current]!!
        assertEquals(-41124.184464506594, calculated.equity)

        var order =
            subaccount.orders?.firstOrNull { it.id == "1118c548-1715-5a72-9c41-f4388518c6e2" }
        assertEquals(OrderStatus.PartiallyFilled, order?.status)

        val fills = subaccount.fills
        assertEquals(112, fills?.size)

        perp.socket(
            url = testWsUrl,
            jsonString = mock.accountsChannel.v4_position_closed,
            subaccountNumber = 0,
            height = BlockAndTime(16961, Clock.System.now()),
        )

        subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        calculated = subaccount.calculated[CalculationPeriod.current]!!
        assertEquals(-41281.9808525066, calculated.equity)
        val btcPosition = subaccount.openPositions?.get("BTC-USD")!!
        val positionCalculated = btcPosition.calculated[CalculationPeriod.current]!!
        assertEquals(-1.792239322, btcPosition.size)

        val ioImplementations = testIOImplementations()
        val localizer = testLocalizer(ioImplementations)
        val uiImplementations = testUIImplementations(localizer)
        val notificationsProvider =
            NotificationsProvider(
                stateMachine = perp,
                uiImplementations = uiImplementations,
                environment = mock.v4Environment,
                parser = Parser(),
                jsonEncoder = JsonEncoder(),
            )
        val notifications = notificationsProvider.buildNotifications(0)
        assertEquals(
            6,
            notifications.size,
        )
        val notification = notifications["order:1118c548-1715-5a72-9c41-f4388518c6e2"]
        assertNotNull(notification)
        assertEquals(
            "NOTIFICATIONS.ORDER_PARTIAL_FILL.TITLE",
            notification.title,
        )
        val position = notifications["position:ETH-USD"]
        assertNotNull(position)
        assertEquals(
            "NOTIFICATIONS.POSITION_CLOSED.TITLE",
            position.title,
        )
    }

    private fun testPartiallyFilledAndCanceledOrders() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.accountsChannel.v4_parent_subaccounts_partially_filled_and_canceled_orders,
            subaccountNumber = 0,
            height = BlockAndTime(14689438, Clock.System.now()),
        )

        val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
        val orders = subaccount.orders
        val order1 = orders?.firstOrNull { it.id == "3a8c6f8f-d8dd-54b5-a3a1-d318f586a80c" }
        assertEquals(OrderStatus.PartiallyCanceled, order1?.status)
        val order2 = orders?.firstOrNull { it.id == "a4586c75-c3f5-5bf5-877a-b3f2c8ff32a7" }
        assertEquals(OrderStatus.PartiallyFilled, order2?.status)

        val ioImplementations = testIOImplementations()
        val localizer = testLocalizer(ioImplementations)
        val uiImplementations = testUIImplementations(localizer)
        val notificationsProvider =
            NotificationsProvider(
                stateMachine = perp,
                uiImplementations = uiImplementations,
                environment = mock.v4Environment,
                parser = Parser(),
                jsonEncoder = JsonEncoder(),
            )
        val notifications = notificationsProvider.buildNotifications(0)
        assertEquals(
            8,
            notifications.size,
        )
        val order = notifications["order:3a8c6f8f-d8dd-54b5-a3a1-d318f586a80c"]
        assertNotNull(order)
        assertEquals(
            "NOTIFICATIONS.ORDER_PARTIAL_FILL.TITLE",
            order.title,
        )
    }

    private fun testEquityTiers() {
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        assertEquals(
            perp.internalState.configs.equityTiers?.shortTermOrderEquityTiers?.size,
            6,
        )
        assertEquals(
            perp.internalState.configs.equityTiers?.statefulOrderEquityTiers?.size,
            6,
        )
        assertEquals(
            perp.internalState.configs.equityTiers?.shortTermOrderEquityTiers?.get(0),
            EquityTier(
                requiredTotalNetCollateralUSD = 0.0,
                nextLevelRequiredTotalNetCollateralUSD = 20.0,
                maxOrders = 0,
            ),
        )
        assertEquals(
            perp.internalState.configs.equityTiers?.shortTermOrderEquityTiers?.get(1),
            EquityTier(
                requiredTotalNetCollateralUSD = 20.0,
                nextLevelRequiredTotalNetCollateralUSD = 100.0,
                maxOrders = 1,
            ),
        )
        assertEquals(
            perp.internalState.configs.equityTiers?.statefulOrderEquityTiers?.get(0),
            EquityTier(
                requiredTotalNetCollateralUSD = 0.0,
                nextLevelRequiredTotalNetCollateralUSD = 20.0,
                maxOrders = 0,
            ),
        )
        assertEquals(
            perp.internalState.configs.equityTiers?.statefulOrderEquityTiers?.get(1),
            EquityTier(
                requiredTotalNetCollateralUSD = 20.0,
                nextLevelRequiredTotalNetCollateralUSD = 100.0,
                maxOrders = 1,
            ),
        )
    }

    private fun testFeeTiers() {
        perp.parseOnChainFeeTiers(mock.v4OnChainMock.fee_tiers)
        assertEquals(perp.internalState.configs.feeTiers?.size, 9)
        assertEquals(perp.internalState.configs.feeTiers?.get(0)?.tier, "1")
    }

    private fun testUserFeeTier() {
        perp.parseOnChainUserFeeTier(mock.v4OnChainMock.user_fee_tier)
        assertEquals(perp.internalState.wallet.user?.feeTierId, "1")
        assertEquals(perp.internalState.wallet.user?.makerFeeRate, 0.0)
        assertEquals(perp.internalState.wallet.user?.takerFeeRate, 0.0)
    }

    private fun testUserStats() {
        perp.parseOnChainUserStats(mock.v4OnChainMock.user_stats)
        assertEquals(perp.internalState.wallet.user?.makerVolume30D, 1.0)
        assertEquals(perp.internalState.wallet.user?.takerVolume30D, 1.0)
    }

    @Test
    fun testAccountBalances() {
        val changes = perp.onChainAccountBalances(mock.v4OnChainMock.account_balances)
        perp.updateStateChanges(changes)
        assertEquals(perp.internalState.wallet.account.balances?.size, 2)
        assertEquals(
            perp.internalState.wallet.account.balances?.get("ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5"),
            InternalAccountBalanceState(
                "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                110.0.toBigDecimal(),
            ),
        )
        assertEquals(
            perp.internalState.wallet.account.balances?.get("dv4tnt"),
            InternalAccountBalanceState(
                "dv4tnt",
                1220.0.toBigDecimal(),
            ),
        )
    }

    @Test
    fun testAccountStakingBalances() {
        val changes = perp.onChainDelegations(mock.v4OnChainMock.account_delegations)
        assertEquals(perp.internalState.wallet.account.stakingBalances?.size, 1)
        assertEquals(
            perp.internalState.wallet.account.stakingBalances?.get("dv4tnt"),
            InternalAccountBalanceState(
                "dv4tnt",
                2001000.0.toBigDecimal(),
            ),
        )
        assertTrue { changes.changes.contains(Changes.accountBalances) }
    }

    @Test
    fun testAccountHistoricalTradingRewards() {
        reset()
        setup()

        var changes = perp.historicalTradingRewards(
            payload = mock.historicalTradingRewards.weeklyCall,
            period = HistoricalTradingRewardsPeriod.WEEKLY,
        )
        perp.updateStateChanges(changes)
        assertEquals(perp.internalState.wallet.account.tradingRewards.historical.size, 1)
        assertEquals(
            perp.internalState.wallet.account.tradingRewards.historical[HistoricalTradingRewardsPeriod.WEEKLY]?.size,
            2,
        )

        changes = perp.historicalTradingRewards(
            payload = mock.historicalTradingRewards.dailyCall,
            period = HistoricalTradingRewardsPeriod.DAILY,
        )
        perp.updateStateChanges(changes)
        assertEquals(perp.internalState.wallet.account.tradingRewards.historical.size, 2)
        assertEquals(
            perp.internalState.wallet.account.tradingRewards.historical[HistoricalTradingRewardsPeriod.DAILY]?.size,
            2,
        )

        changes = perp.historicalTradingRewards(
            payload = mock.historicalTradingRewards.monthlyCall,
            period = HistoricalTradingRewardsPeriod.MONTHLY,
        )
        perp.updateStateChanges(changes)
        assertEquals(perp.internalState.wallet.account.tradingRewards.historical.size, 3)
        assertEquals(
            perp.internalState.wallet.account.tradingRewards.historical[HistoricalTradingRewardsPeriod.MONTHLY]?.size,
            2,
        )

        changes = perp.historicalTradingRewards(
            payload = mock.historicalTradingRewards.monthlySecondCall,
            period = HistoricalTradingRewardsPeriod.MONTHLY,
        )
        perp.updateStateChanges(changes)
        assertEquals(perp.internalState.wallet.account.tradingRewards.historical.size, 3)
        assertEquals(
            perp.internalState.wallet.account.tradingRewards.historical[HistoricalTradingRewardsPeriod.MONTHLY]?.size,
            3,
        )
    }
}
