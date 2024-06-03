package exchange.dydx.abacus.processor.router.skip
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputTokenResource
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.tests.payloads.SkipChainsMock
import exchange.dydx.abacus.tests.payloads.SkipTokensMock
import exchange.dydx.abacus.utils.Parser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal fun templateToJson(template: String): Map<String, Any> {
    return Json.parseToJsonElement(template.trimIndent()).jsonObject.toMap()
}

class SkipProcessorTests {

    internal val internalState = InternalTransferInputState()
    internal val parser = Parser()
    internal val skipProcessor = SkipProcessor(parser = parser, internalState = internalState)
    internal val skipChainsMock = SkipChainsMock()
    internal val skipTokensMock = SkipTokensMock()
    internal val selectedChainId = "osmosis-1"
    internal val selectedTokenAddress = "selectedTokenDenom"
    internal val selectedTokenSymbol = "selectedTokenSymbol"
    internal val selectedTokenDecimals = "15"
    internal val selectedChainAssets = listOf(
        mapOf(
            "denom" to selectedTokenAddress,
            "symbol" to selectedTokenSymbol,
            "decimals" to selectedTokenDecimals,
            "name" to "some-name",
            "logo_uri" to "some-logo-uri",
        ),
        mapOf(
            "denom" to "testTokenKeyValue2",
            "symbol" to "ARB",
            "decimals" to 8,
            "name" to "some-name-2",
            "logo_uri" to "some-logo-uri-2",
        ),
        mapOf(
            "denom" to "testTokenKeyValue3",
            "symbol" to "ETH",
            "decimals" to 5,
            "name" to "some-name-3",
            "logo_uri" to "some-logo-uri-3",
        ),
    )

    /**
     * Adds tokens to the skipProcessor instance
     * This is a reduced scope mock that is to be used for UNIT TESTS ONLY.
     * Integration tests should use the skipChainsMock or skipTokensMock structures.
     * The test tokens fixture looks like this:
     * {
     *      "osmosis-1": {
     *          "assets": [
     *              {
     *                  "denom": "selectedTokenDenom",
     *                  "symbol": "selectedTokenSymbol",
     *                  "decimals": 15
     *              },
     *              {
     *                  "denom": "testTokenKeyValue2",
     *                  "symbol": "ARB",
     *                  "decimals": 8
     *              },
     *              {
     *                  "denom": "testTokenKeyValue3"
     *                  "symbol": "ETH",
     *                  "decimals": 5
     *              }
     *          ]
     *      },
     *      "dont-select": {
     *          "assets": [
     *              {"denom": "shouldNotBeSelectedValue1"},
     *              {"denom": "shouldNotBeSelectedValue2"},
     *              {"denom": "shouldNotBeSelectedValue3"}
     *          ]
     *      }
     * }
     *
     * This makes it easy to know what the filteredTokens output should be
     * which in turn helps us know the results of the funs that depend on it.
     */
    internal fun addTokens() {
        skipProcessor.skipTokens = mapOf(
            selectedChainId to mapOf("assets" to selectedChainAssets),
            "dont-select" to mapOf(
                "assets" to listOf(
                    mapOf("shouldNotBeSelected1" to "shouldNotBeSelectedValue1"),
                    mapOf("shouldNotBeSelected2" to "shouldNotBeSelectedValue2"),
                    mapOf("shouldNotBeSelected3" to "shouldNotBeSelectedValue3"),
                ),
            ),
        )
    }

    @BeforeTest
    internal fun setUp() {
        addTokens()
    }

// //////////////////    UNIT TESTS     //////////////////////
    @Test
    fun testFilteredTokens() {
        val result = skipProcessor.filteredTokens(chainId = selectedChainId)
        val expected = selectedChainAssets
        assertEquals(expected, result)
    }

    @Test
    fun testSelectedTokenSymbol() {
        val result = skipProcessor.selectedTokenSymbol(tokenAddress = selectedTokenAddress, selectedChainId = selectedChainId)
        val expected = selectedTokenSymbol
        assertEquals(expected, result)
    }

    @Test
    fun testSelectedTokenDecimals() {
        val result = skipProcessor.selectedTokenDecimals(tokenAddress = selectedTokenAddress, selectedChainId = selectedChainId)
        val expected = selectedTokenDecimals
        assertEquals(expected, result)
    }

    @Test
    fun testDefaultTokenAddress() {
        val result = skipProcessor.defaultTokenAddress(selectedChainId)
        val expected = selectedTokenAddress
        assertEquals(expected, result)
    }

    @Test
    fun testTokenResources() {
        val result = skipProcessor.tokenResources(selectedChainId)
        val expected = mapOf(
            selectedTokenAddress to TransferInputTokenResource(
                address = selectedTokenAddress,
                symbol = selectedTokenSymbol,
                decimals = parser.asInt(selectedTokenDecimals),
                name = "some-name",
                iconUrl = "some-logo-uri",
            ),
            "testTokenKeyValue2" to TransferInputTokenResource(
                address = "testTokenKeyValue2",
                symbol = "ARB",
                decimals = 8,
                name = "some-name-2",
                iconUrl = "some-logo-uri-2",
            ),
            "testTokenKeyValue3" to TransferInputTokenResource(
                address = "testTokenKeyValue3",
                symbol = "ETH",
                decimals = 5,
                name = "some-name-3",
                iconUrl = "some-logo-uri-3",
            ),
        )
        assertEquals(expected, result)
    }

    @Test
    fun testTokenOptions() {
        val result = skipProcessor.tokenOptions(selectedChainId)
        val expected = listOf(
            SelectionOption(
                stringKey = "some-name",
                string = "some-name",
                type = selectedTokenAddress,
                iconUrl = "some-logo-uri",
            ),
            SelectionOption(
                stringKey = "some-name-2",
                string = "some-name-2",
                type = "testTokenKeyValue2",
                iconUrl = "some-logo-uri-2",
            ),
            SelectionOption(
                stringKey = "some-name-3",
                string = "some-name-3",
                type = "testTokenKeyValue3",
                iconUrl = "some-logo-uri-3",
            ),
        )
        assertEquals(expected, result)
    }

    // /////////////// INTEGRATION TESTS ////////////////////

    @Test
    fun testReceivedChains() {
        val payload = templateToJson(
            skipChainsMock.payload,
        )
        val modified = skipProcessor.receivedChains(
            existing = mapOf(),
            payload = payload,
        )
        val expectedChains = listOf(
            SelectionOption(stringKey = "Ethereum", string = "Ethereum", type = "1", iconUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png"),
            SelectionOption(stringKey = "aura", string = "aura", type = "xstaxy-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png"),
            SelectionOption(stringKey = "cheqd", string = "cheqd", type = "cheqd-mainnet-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/cheqd-mainnet/chain.png"),
            SelectionOption(stringKey = "kujira", string = "kujira", type = "kaiyo-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/kaiyo/chain.png"),
            SelectionOption(stringKey = "osmosis", string = "osmosis", type = "osmosis-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/osmosis/chain.png"),
            SelectionOption(stringKey = "stride", string = "stride", type = "stride-1", iconUrl = "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/stride/chain.png"),
        )
        val expectedChainResources = mapOf(
            "1" to TransferInputChainResource(
                chainName = "Ethereum",
                chainId = 1,
                iconUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png",
            ),
        )
        val expectedModified = mapOf(
            "transfer" to mapOf(
                "chain" to "1",
            ),
        )

        assertEquals(expectedChains, internalState.chains)
        assertEquals(payload["chains"], skipProcessor.chains)
        assertEquals(expectedChainResources, internalState.chainResources)
        assertEquals(expectedModified, modified)
    }

    @Test
    fun testReceivedTokens() {
        val payload = templateToJson(skipTokensMock.payload)
        skipProcessor.skipTokens = null
        skipProcessor.chains = listOf(
            mapOf(
                "chain_name" to "aura",
                "chain_id" to "1",
                "pfm_enabled" to false,
                "supports_memo" to true,
                "logo_uri" to "https ://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png",
                "bech32_prefix" to "aura",
                "chain_type" to "cosmos",
                "is_testnet" to false,
            ),
        )
        val modified = skipProcessor.receivedTokens(
            existing = mapOf(),
            payload = payload,
        )
        val expectedModified = mapOf(
            "transfer" to mapOf(
                "token" to "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E",
            ),
        )
        val expectedTokens = listOf(
            SelectionOption(
                stringKey = "Euro Coin",
                string = "Euro Coin",
                type = "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c",
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/euroc.svg",
            ),
            SelectionOption(
                stringKey = "Real Yield USD",
                string = "Real Yield USD",
                type = "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E",
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/yieldusd.svg",
            ),
            SelectionOption(
                stringKey = "Umee native token",
                string = "Umee native token",
                type = "0x923e030f951A2401426a3407a9bcc7EB715d9a0b",
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/umee.svg",
            ),
        )
        val expectedTokenResources = mapOf(
            "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E" to TransferInputTokenResource(
                name = "Real Yield USD",
                address = "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E",
                symbol = "YieldUSD",
                decimals = 18,
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/yieldusd.svg",
            ),
            "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c" to TransferInputTokenResource(
                name = "Euro Coin",
                address = "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c",
                symbol = "EUROC",
                decimals = 6,
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/euroc.svg",
            ),
            "0x923e030f951A2401426a3407a9bcc7EB715d9a0b" to TransferInputTokenResource(
                name = "Umee native token",
                address = "0x923e030f951A2401426a3407a9bcc7EB715d9a0b",
                symbol = "UMEE",
                decimals = 6,
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/umee.svg",
            ),
        )

        assertEquals(expectedModified, modified)
        assertEquals(payload["chain_to_assets_map"], skipProcessor.skipTokens)
        assertEquals(expectedTokens, internalState.tokens)
        assertEquals(expectedTokenResources, internalState.tokenResources)
    }
}
