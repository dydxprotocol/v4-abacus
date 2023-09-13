package exchange.dydx.abacus.app

import exchange.dydx.abacus.state.app.ApiStatus
import exchange.dydx.abacus.state.app.AppStateMachine
import exchange.dydx.abacus.state.app.AppStateResponse
import exchange.dydx.abacus.state.app.NetworkStatus
import exchange.dydx.abacus.state.app.V4AppStateMachineProtocol
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.app.adaptors.HttpVerb
import exchange.dydx.abacus.state.app.adaptors.V4ApiAdaptor
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.Parser
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class V4AppStateMachineTransactionTests {
    val mock = AbacusMockData()
    private val chainUrl = "https://validator.v4staging.dydx.exchange"
    private val testRestUrl = "https://indexer.v4staging.dydx.exchange"
    private val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")
    private var appStateMachine = kotlin.run {
        val appStateMachine = AppStateMachine()
        appStateMachine.setChainId("dydxprotocol-testnet")
        return@run appStateMachine
    }
    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
    // mockedClient and mockedTransactionRequestID values come from the TransactionMock data
    private val mockedTransactionRequestId = 106799053660
    private val mockedClientId = 1067413651

    private fun setStateMachineConnectedWithMarketsAndFundedSubaccounts(): AppStateResponse {
        appStateMachine.setChainId("dydxprotocol-testnet")
        appStateMachine.setReadyToConnect(true)
        appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )
        appStateMachine.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        appStateMachine.processSocketResponse(
            testWsUrl, mock.marketsChannel.v4_subscribed_r1
        )
        appStateMachine.processSocketResponse(
            testWsUrl,
            """
                {
                  "type": "subscribed",
                  "connection_id": "3936dbcc-fe3f-4598-ba07-fa8656c455b1",
                  "message_id": 9,
                  "channel": "v4_orderbook",
                  "id": "ETH-USD",
                  "contents": {
                    "asks": [
                      {
                        "size": "31.231",
                        "price": "1500.1"
                      }
                      ],
                      "bids": [
                      {
                        "size": "31.231",
                        "price": "1499.9"
                      }
                      ]
                    }
                }
            """.trimIndent()
        )
        appStateMachine.setMarket("ETH-USD")
        val address = testCosmoAddress
        val addressState =
            (appStateMachine as V4AppStateMachineProtocol).setWalletCosmoAddress(address)
        val subaccountsRequest = addressState.networkRequests!!.restRequests?.firstOrNull()
        (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            subaccountsRequest!!.url,
            mock.accountsChannel.v4accountsReceived
        )

         appStateMachine.processSocketResponse(
            testWsUrl, mock.accountsChannel.subscribed
        )

        val subaccountText = """
            {
            	"subaccounts": [{
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 0,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 1,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 2,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 3,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 4,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 5,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 6,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 7,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 8,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}, {
            		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            		"subaccountNumber": 9,
            		"equity": "100000.000000",
            		"freeCollateral": "100000.000000",
            		"openPerpetualPositions": {},
            		"quoteBalance": "100000.000000",
            		"marginEnabled": true
            	}]
            }
        """.trimIndent()
        val url = AbUrl.fromString("$testRestUrl/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm")
        return appStateMachine.processHttpResponse(url, subaccountText)
    }
    private fun setHeight() {
        var url = AbUrl.fromString("$testRestUrl/v4/height")
        var state = appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        assertEquals(ApiStatus.NORMAL, state.apiState?.status)
        assertNull(state.apiState?.haltedBlock)
        assertNull(state.apiState?.trailingBlocks)
        var v4Adapter = (appStateMachine.adaptor as? V4ApiAdaptor)
        assertNotNull(v4Adapter)
        assertEquals(NetworkStatus.NORMAL, v4Adapter.indexerState.status)
        assertEquals(16750, v4Adapter.indexerState.block)
    }
    @BeforeTest
    fun resetAppStateMachine() {
        appStateMachine = kotlin.run {
            val appStateMachine = AppStateMachine()
            appStateMachine.setChainId("dydxprotocol-testnet")
            return@run appStateMachine
        }
        (appStateMachine as V4AppStateMachineProtocol).setSubaccountNumber(0)
        setStateMachineConnectedWithMarketsAndFundedSubaccounts()
        setHeight()
    }

    @Test
    fun testPlaceOrder() {
        appStateMachine.setSocketConnected(testWsUrl, true)
        appStateMachine.trade("LIMIT", TradeInputField.type)
        appStateMachine.trade("IOC", TradeInputField.timeInForceType)
        appStateMachine.trade("100.0", TradeInputField.limitPrice)
        val response = appStateMachine.trade("1.0", TradeInputField.size)
        assertNull(response.lastOrder)
        var placeOrder = appStateMachine.placeOrderPayload()
        assertNotNull(placeOrder)
        val clientId = placeOrder.clientId
        assertEquals(clientId, (appStateMachine.adaptor as? V4ApiAdaptor)?.lastOrderClientId)
        assertEquals(null, (appStateMachine.adaptor as? V4ApiAdaptor)?.lastTransactionRequestId)
        assertEquals(null, (appStateMachine.adaptor as? V4ApiAdaptor)?.lastTransmittedOrderClientId)
        assertEquals(0, placeOrder.clientMetadata)

        appStateMachine.trade("0.01", TradeInputField.size)
        appStateMachine.trade("MARKET", TradeInputField.type)
        placeOrder = appStateMachine.placeOrderPayload()
        assertEquals(1, placeOrder?.clientMetadata)
    }

    @Test
    fun testCancelOrder() {
        val parser = Parser()

        // Older API
        appStateMachine.processSocketResponse(testWsUrl, mock.accountsChannel.v4_subaccounts_update_3)
        var cancelOrder = appStateMachine.cancelOrderPayload("2caebf6b-35d3-512c-a4d9-e438445d8dba")
        assertNotNull(cancelOrder)
        assertEquals(cancelOrder.orderFlags, "SHORT_TERM")
        assertEquals(cancelOrder.goodUntilBlock, 1110)
        var clientId = cancelOrder.clientId
        assertEquals(clientId, mockedClientId)

        cancelOrder = appStateMachine.cancelOrderPayload("0ef4a74e-6916-5a1e-9744-baa9c37d5ce3")
        assertNotNull(cancelOrder)
        assertEquals(cancelOrder.orderFlags, "LONG_TERM")
        assertEquals(cancelOrder.goodUntilTime, parser.asDatetime("2023-06-01T21:12:07.000Z")?.epochSeconds?.toDouble())
        clientId = cancelOrder.clientId
        assertEquals(clientId, 2130017314)
        assertEquals(cancelOrder.clobPairId, 1)
    }

    @Test
    fun testPlaceOrder2() {
        appStateMachine.setSocketConnected(testWsUrl, true)
        appStateMachine.trade("LIMIT", TradeInputField.type)
        appStateMachine.trade("IOC", TradeInputField.timeInForceType)
        appStateMachine.trade("100.0", TradeInputField.limitPrice)
        val response = appStateMachine.trade("1.0", TradeInputField.size)
        assertNull(response.lastOrder)
        var placeOrder = appStateMachine.placeOrderPayload2()
        assertNotNull(placeOrder)
        val clientId = placeOrder.clientId
        assertEquals(clientId, (appStateMachine.adaptor as? V4ApiAdaptor)?.lastOrderClientId)
        assertEquals(null, (appStateMachine.adaptor as? V4ApiAdaptor)?.lastTransactionRequestId)
        assertEquals(null, (appStateMachine.adaptor as? V4ApiAdaptor)?.lastTransmittedOrderClientId)
        assertEquals(0, placeOrder.clientMetadata)

        appStateMachine.trade("0.01", TradeInputField.size)
        appStateMachine.trade("MARKET", TradeInputField.type)
        placeOrder = appStateMachine.placeOrderPayload2()
        assertEquals(1, placeOrder?.clientMetadata)
    }

    @Test
    fun testCancelOrder2() {
        val parser = Parser()

        appStateMachine.processSocketResponse(testWsUrl, mock.accountsChannel.v4_subaccounts_update_3)
        var cancelOrder = appStateMachine.cancelOrderPayload2("2caebf6b-35d3-512c-a4d9-e438445d8dba")
        assertNotNull(cancelOrder)
        assertEquals(cancelOrder.orderFlags, 0)
        assertEquals(cancelOrder.goodTilBlock, 1110)
        var clientId = cancelOrder.clientId
        assertEquals(clientId, mockedClientId)
        assertEquals(cancelOrder.clobPairId, 1)

        cancelOrder = appStateMachine.cancelOrderPayload2("0ef4a74e-6916-5a1e-9744-baa9c37d5ce3")
        assertNotNull(cancelOrder)
        assertEquals(cancelOrder.orderFlags, 64)
        assertEquals(cancelOrder.goodTilBlockTime, parser.asDatetime("2023-06-01T21:12:07.000Z")?.epochSeconds?.toDouble())
        clientId = cancelOrder.clientId
        assertEquals(clientId, 2130017314)
        assertEquals(cancelOrder.clobPairId, 1)
    }

    @Test
    fun testSubmitTransaction() {
        val tx = "Cm0KawogL2R5ZHhwcm90b2NvbC5jbG9iLk1zZ1BsYWNlT3JkZXISRwpFCjEKLwotY29zbW9zMXl1Z2ZkOWt1MGRhemFkOWsyMnJmYzQwdTZ0bjBwZzJ6bm12dDVoEAEYASCAhK9fKIDeoMsFMPEGElIKTgpGCh8vY29zbW9zLmNyeXB0by5zZWNwMjU2azEuUHViS2V5EiMKIQP8sqt6pOhWE/0xDsNPETOZcUTfCfWgjE1LMbODPnfdmhIECgIIARIAGkBr7/vtJJGWGp/XXm0pAlgf6oDuC1PbvoZIdv147S3dFU+r+n0S/t+kZC5FVfc9H99X7fVauyIskw6KvUXmaNrO"

        val state = (appStateMachine as? V4AppStateMachineProtocol)?.transaction(tx)
        val post = state?.networkRequests?.restRequests?.firstOrNull()
        assertNotNull(post)
        val url = post.url.urlString
        assertEquals("https://validator.v4staging.dydx.exchange/", url)
        val verb = post.verb
        assertEquals(HttpVerb.post, verb)
        val body = post.body
        assertNotNull(body)

        val parser = Parser()
        val payload = parser.asMap(Json.parseToJsonElement(body))
        assertNotNull(payload)
        assertEquals("2.0", parser.asString(payload["jsonrpc"]))
        assertEquals("broadcast_tx_sync", parser.asString(payload["method"]))
        val params = parser.asMap(payload["params"])
        assertNotNull(params)

        assertEquals(tx, parser.asString(params["tx"]))
        (appStateMachine.adaptor as? V4ApiAdaptor)?.lastTransactionRequestId = mockedTransactionRequestId
    }

    @Test
    fun testTransactionResponse() {
        (appStateMachine.adaptor as? V4ApiAdaptor)?.lastOrderClientId = mockedClientId
        (appStateMachine.adaptor as? V4ApiAdaptor)?.lastTransactionRequestId = mockedTransactionRequestId

        var url1 = AbUrl.fromString("$chainUrl/")
        var state = appStateMachine.processHttpResponse(url1, mock.transactionsMock.place_order_transaction)
        assertNull(state.lastOrder)
        assertNull(state.state?.input?.trade?.size)
        assertNull(state.state?.input?.trade?.price)

        state = appStateMachine.processSocketResponse(testWsUrl, mock.accountsChannel.v4_subaccounts_update_3)
        assertNotNull(state.lastOrder)
        assertEquals(mockedClientId, state.lastOrder?.clientId)
        val lastOrder = state.lastOrder

        state = appStateMachine.processHttpResponse(url1, mock.transactionsMock.place_order_fok_failed_transaction)
        assertNotNull(state.errors)
        val error = state.errors?.firstOrNull()
        assertNotNull(error)
        assertEquals("FillOrKill order could not be fully filled", error.message)
        assertEquals("TRANSACTIONERROR.ORDER.2000_CANNOT_FULLY_FILL_FOK", error.stringKey)

        state = appStateMachine.processSocketResponse(testWsUrl, mock.accountsChannel.v4_subaccounts_update_7)
        assertNotNull(state.lastOrder)
        assertEquals(mockedClientId, state.lastOrder?.clientId)
        val lastOrder3 = state.lastOrder

        assertTrue { lastOrder3 === lastOrder }
    }

}