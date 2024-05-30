package exchange.dydx.abacus.processor.router.skip
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.tests.payloads.SkipChainsMock
import exchange.dydx.abacus.utils.Parser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

internal fun templateToJson(template: String): Map<String, Any> {
    return Json.parseToJsonElement(template.trimIndent()).jsonObject.toMap()
}

class SkipProcessorTests {

    internal val internalState = InternalTransferInputState()
    internal val skipProcessor = SkipProcessor(parser = Parser(), internalState = internalState)
    internal val skipChainsMock = SkipChainsMock()

    @Test
    fun testReceivedChains() {
        val modified = skipProcessor.receivedChains(
            existing = mapOf(),
            payload = templateToJson(
                skipChainsMock.payload,
            ),
        )

        val expectedChains = listOf(
            SelectionOption(stringKey = "Ethereum", string = "Ethereum", type = "1", iconUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png"),
            SelectionOption(stringKey = "aura", string = "aura", type = "xstaxy-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png"),
            SelectionOption(stringKey = "cheqd", string = "cheqd", type = "cheqd-mainnet-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/cheqd-mainnet/chain.png"),
            SelectionOption(stringKey = "kujira", string = "kujira", type = "kaiyo-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/kaiyo/chain.png"),
            SelectionOption(stringKey = "osmosis", string = "osmosis", type = "osmosis-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/osmosis/chain.png"),
            SelectionOption(stringKey = "stride", string = "stride", type = "stride-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/stride/chain.png"),
        )
        assertEquals(expectedChains, internalState.chains)

        val expectedChainResources = mapOf(
            "1" to TransferInputChainResource(
                chainName = "Ethereum",
                chainId = 1,
                iconUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png",
            )
        )
        assertEquals(expectedChainResources, internalState.chainResources)

        val expectedModified = mapOf(
            "transfer" to mapOf(
                "chain" to "1",
            ),
        )
        assertEquals(expectedModified, modified)
    }
}
