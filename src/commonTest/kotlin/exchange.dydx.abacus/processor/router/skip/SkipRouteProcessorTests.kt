package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.tests.payloads.SkipRouteMock
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToMap(payload), decimals = 6.0)
        val expected = mapOf(
            "toAmountUSD" to 11.64,
            "toAmount" to 11.64,
            "bridgeFee" to .36,
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
        assertEquals(expected, result)
    }

    /**
     * Tests a CCTP withdrawal initiated from the cctpToNobleSkip method
     * This payload is used by the chain transaction method WithdrawToNobleIBC
     * This processes a Dydx -> Noble CCTP transaction
     */
    @Test
    fun testReceivedCCTPDydxToNobleWithdrawal() {
        val payload = skipRouteMock.payloadCCTPDydxToNoble
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToMap(payload), decimals = 6.0)
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
            "bridgeFee" to 0.0,
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
     * Tests a Non-CCTP withdrawal from Dydx to Ethereum
     */
    @Test
    fun testReceivedNonCCTPDydxToEthWithdrawal() {
        val payload = skipRouteMock.payloadDydxToEth
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToMap(payload), decimals = 18.0)
        val jsonEncoder = JsonEncoder()
        val expectedMsg = mapOf(
            "sourcePort" to "transfer",
            "sourceChannel" to "channel-0",
            "token" to mapOf(
                "denom" to "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                "amount" to "129996028",
            ),
            "sender" to "dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s",
            "receiver" to "noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf",
            "timeoutHeight" to mapOf<String, Any>(),
            "timeoutTimestamp" to 1718399715601228463,
            "memo" to "{\"forward\":{\"channel\":\"channel-1\",\"next\":{\"wasm\":{\"contract\":\"osmo1vkdakqqg5htq5c3wy2kj2geq536q665xdexrtjuwqckpads2c2nsvhhcyv\",\"msg\":{\"swap_and_action\":{\"affiliates\":[],\"min_asset\":{\"native\":{\"amount\":\"37656643372307734\",\"denom\":\"ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5\"}},\"post_swap_action\":{\"ibc_transfer\":{\"ibc_info\":{\"memo\":\"{\\\"destination_chain\\\":\\\"Ethereum\\\",\\\"destination_address\\\":\\\"0xD397883c12b71ea39e0d9f6755030205f31A1c96\\\",\\\"payload\\\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,15,120,51,119,123,252,158,247,45,139,118,174,149,77,56,73,221,113,248,41],\\\"type\\\":2,\\\"fee\\\":{\\\"amount\\\":\\\"7692677672185391\\\",\\\"recipient\\\":\\\"axelar1aythygn6z5thymj6tmzfwekzh05ewg3l7d6y89\\\"}}\",\"receiver\":\"axelar1dv4u5k73pzqrxlzujxg3qp8kvc3pje7jtdvu72npnt5zhq05ejcsn5qme5\",\"recover_address\":\"osmo1nhzuazjhyfu474er6v4ey8zn6wa5fy6gt044g4\",\"source_channel\":\"channel-208\"}}},\"timeout_timestamp\":1718399715601274600,\"user_swap\":{\"swap_exact_asset_in\":{\"operations\":[{\"denom_in\":\"ibc/498A0751C798A0D9A389AA3691123DADA57DAA4FE165D5C75894505B876BA6E4\",\"denom_out\":\"factory/osmo1z0qrq605sjgcqpylfl4aa6s90x738j7m58wyatt0tdzflg2ha26q67k743/wbtc\",\"pool\":\"1437\"},{\"denom_in\":\"factory/osmo1z0qrq605sjgcqpylfl4aa6s90x738j7m58wyatt0tdzflg2ha26q67k743/wbtc\",\"denom_out\":\"ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5\",\"pool\":\"1441\"}],\"swap_venue_name\":\"osmosis-poolmanager\"}}}}}},\"port\":\"transfer\",\"receiver\":\"osmo1vkdakqqg5htq5c3wy2kj2geq536q665xdexrtjuwqckpads2c2nsvhhcyv\",\"retries\":2,\"timeout\":1718399715601230847}}",
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
            "toAmountUSD" to 103.17,
            "toAmount" to 0.03034433583519616,
            "aggregatePriceImpact" to "0.2607",
            "bridgeFee" to 26.15,
            "slippage" to "1",
            "requestPayload" to mapOf(
                "fromChainId" to "dydx-mainnet-1",
                "fromAddress" to "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                "toChainId" to "1",
                "toAddress" to "ethereum-native",
                "data" to expectedData,
            ),
        )

        assertEquals(expected, result)
    }

    /**
     * Tests a CCTP withdrawal initiated from the cctpToNobleSkip method
     * This payload is used by the chain transaction method WithdrawToNobleIBC
     * This processes a Dydx -> Noble CCTP transaction
     */
    @Test
    fun testReceivedCCTPDydxToNoble() {
        val payload = skipRouteMock.payloadCCTPDydxToNoble
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToMap(payload), decimals = 6.0)
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
            "bridgeFee" to 0.0,
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
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToMap(payload), decimals = 6.0)
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
            "bridgeFee" to 0.0,
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
        val result = skipRouteProcessor.received(existing = mapOf(), payload = templateToMap(payload), decimals = 6.0)
        val expected = mapOf(
            "bridgeFee" to 0.0,
            "slippage" to "1",
            "errors" to "[{code=3, message=\"difference in usd value of route input and output is too large. input usd value: 100000.00 output usd value: 98811.81\", details=[{\"@type\":\"type.googleapis.com/google.rpc.ErrorInfo\",\"reason\":\"BAD_PRICE_ERROR\",\"domain\":\"skip.money\",\"metadata\":{}}]}]",
        )
        assertEquals(expected, result)
    }
}
