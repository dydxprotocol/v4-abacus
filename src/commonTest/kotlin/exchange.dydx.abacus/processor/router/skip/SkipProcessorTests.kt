package exchange.dydx.abacus.processor.router.skip
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

    internal val skipProcessor = SkipProcessor(parser=Parser())
    internal val skipChainsMock = SkipChainsMock()

    @Test
    fun testReceivedChains() {
        val modified = skipProcessor.receivedChains(
            existing = mapOf(),
            payload = templateToJson(
                skipChainsMock.payload,
            ),
        )

        val expected = mapOf(
            "transfer" to mapOf(
                "depositOptions" to mapOf(
                    "chains" to listOf(
                        mapOf("stringKey" to "aura", "type" to "xstaxy-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png"),
                        mapOf("stringKey" to "cheqd", "type" to "cheqd-mainnet-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/cheqd-mainnet/chain.png"),
                        mapOf("stringKey" to "kujira", "type" to "kaiyo-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/kaiyo/chain.png"),
                        mapOf("stringKey" to "osmosis", "type" to "osmosis-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/osmosis/chain.png"),
                        mapOf("stringKey" to "stride", "type" to "stride-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/stride/chain.png"),
                    ),
                ),
                "withdrawalOptions" to mapOf(
                    "chains" to listOf(
                        mapOf("stringKey" to "aura", "type" to "xstaxy-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png"),
                        mapOf("stringKey" to "cheqd", "type" to "cheqd-mainnet-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/cheqd-mainnet/chain.png"),
                        mapOf("stringKey" to "kujira", "type" to "kaiyo-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/kaiyo/chain.png"),
                        mapOf("stringKey" to "osmosis", "type" to "osmosis-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/osmosis/chain.png"),
                        mapOf("stringKey" to "stride", "type" to "stride-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/stride/chain.png"),
                    ),
                ),
            ),
        )
        assertEquals(modified, expected)
    }
}
