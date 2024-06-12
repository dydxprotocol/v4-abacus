package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.tests.payloads.SkipRouteMock
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SkipRouteProcessorTests {
    val parser = Parser()
    internal val skipRouteMock = SkipRouteMock()
    internal val skipRouteProcessor = SkipRouteProcessor(parser = parser)

    /**
     * Tests an EVM CCTP deposit.
     * This processes an EVM -> Noble USDC transaction (we only support deposits from EVM chains)
     */
    @Test
    fun testReceivedCCTPDeposit() {
        val payload = skipRouteMock.payload
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToJson(payload), decimals = 6.0)
        val expected = mapOf(
            "toAmountUSD" to 11.64,
            "toAmount" to 11.64,
            "bridgeFees" to .36,
            "slippage" to "1",
            "requestPayload" to mapOf(
                "targetAddress" to "0xBC8552339dA68EB65C8b88B414B5854E0E366cFc",
                "data" to "0xd77d6ec00000000000000000000000000000000000000000000000000000000000b19cc000000000000000000000000000000000000000000000000000000000000000040000000000000000000000009dc5ce8a5722795f5723d32b921c53d3bb449348000000000000000000000000a0b86991c6218b36c1d19d4a2e9eb0ce3606eb480000000000000000000000000000000000000000000000000000000000057e40000000000000000000000000691cf4641d5608f085b2c1921172120bb603d074",
                "value" to "0",
                "fromChainId" to "1",
                "fromAddress" to "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                "toChainId" to "noble-1",
                "toAddress" to "uusdc",
            ),
        )
        assertTrue(expected == result)
    }

    /**
     * Tests a CCTP withdrawal initiated from the cctpToNobleSkip method
     * This payload is used by the chain transaction method WithdrawToNobleIBC
     * This processes a Dydx -> Noble CCTP transaction
     */
    @Test
    fun testReceivedCCTPDydxToNoble() {
        val payload = skipRouteMock.payloadCCTPDydxToNoble
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToJson(payload), decimals = 6.0)
        val jsonEncoder = JsonEncoder()
        val expectedMsg = mapOf(
            "sourcePort" to "transfer",
            "sourceChannel" to "channel-0",
            "token" to mapOf(
                "denom" to "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                "amount" to "10996029",
            ),
            "sender" to "dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s",
            "receiver" to "noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf",
            "timeoutHeight" to mapOf<String, Any>(),
            "timeoutTimestamp" to 1718308711061386287,
        )
        val expectedData = jsonEncoder.encode(
            mapOf(
                "msg" to expectedMsg,
                "value" to expectedMsg,
                "msgTypeUrl" to "/ibc.applications.transfer.v1.MsgTransfer",
                "typeUrl" to "/ibc.applications.transfer.v1.MsgTransfer",
            ),
        )
        val expected = mapOf(
            "toAmountUSD" to 11.01,
            "toAmount" to 10.996029,
            "slippage" to "1",
            "requestPayload" to mapOf(
                "fromChainId" to "dydx-mainnet-1",
                "fromAddress" to "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                "toChainId" to "noble-1",
                "toAddress" to "uusdc",
                "data" to expectedData,
            ),
        )
        assertEquals(expected, result)
    }

    /**
     * Tests a CCTP autosweep from the transferNobleBalance method
     * This payload is used by the chain transaction method sendNobleIBC
     * This processes a Noble -> Dydx CCTP transaction
     */
    @Test
    fun testReceivedCCTPNobleToDydx() {
        val payload = skipRouteMock.payloadCCTPNobleToDydx
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToJson(payload), decimals = 6.0)
        val jsonEncoder = JsonEncoder()
        val expectedMsg = mapOf(
            "sourcePort" to "transfer",
            "sourceChannel" to "channel-33",
            "token" to mapOf(
                "denom" to "uusdc",
                "amount" to "5884",
            ),
            "sender" to "noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf",
            "receiver" to "dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s",
            "timeoutHeight" to mapOf<String, Any>(),
            "timeoutTimestamp" to 1718318348813666048,
        )
        val expectedData = jsonEncoder.encode(
            mapOf(
                "msg" to expectedMsg,
                "value" to expectedMsg,
                "msgTypeUrl" to "/ibc.applications.transfer.v1.MsgTransfer",
                "typeUrl" to "/ibc.applications.transfer.v1.MsgTransfer",
            ),
        )
        val expected = mapOf(
            "toAmountUSD" to 0.01,
            "toAmount" to 0.005884,
            "slippage" to "1",
            "requestPayload" to mapOf(
                "fromChainId" to "noble-1",
                "fromAddress" to "uusdc",
                "toChainId" to "dydx-mainnet-1",
                "toAddress" to "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                "data" to expectedData,
            ),
        )
        assertEquals(expected, result)
    }

    @Test
    fun testReceivedError() {
        val payload = skipRouteMock.payloadError
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToJson(payload), decimals = 6.0)
        val expected = mapOf(
            "slippage" to "1",
            "requestPayload" to emptyMap<String, Any>(),
            "errors" to "[{code=3, message=\"difference in usd value of route input and output is too large. input usd value: 100000.00 output usd value: 98811.81\", details=[{\"@type\":\"type.googleapis.com/google.rpc.ErrorInfo\",\"reason\":\"BAD_PRICE_ERROR\",\"domain\":\"skip.money\",\"metadata\":{}}]}]",
        )
        assertEquals(expected.toString(), result.toString())
    }
}
